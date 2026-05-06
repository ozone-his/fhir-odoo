/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.lock;

import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.util.StringUtils;

/**
 * Redis-backed {@link DistributedLockManager} implementation using the "SET key value NX PX" pattern for lock acquisition
 * and Lua scripts for atomic release and renewal.
 * <p>Key features:
 * <ul>
 *   <li>Configurable lease duration, wait timeout, and retry interval per lock purpose.</li>
 *   <li>Automatic lock renewal at a fraction of the lease duration to prevent accidental expiry during long-running actions.</li>
 *   <li>Robust handling of Redis unavailability via {@link RedisHealthMonitor} integration, throwing {@link RedisUnavailableException} when the backend is unreachable.</li>
 *   <li>Non-propagating release failures to avoid suppressing original exceptions from the guarded action; locks will auto-expire if release fails.</li>
 * </ul>
 */
@Slf4j
public class RedisDistributedLockManager implements DistributedLockManager {

    /**
     * Atomically releases a lock only if the stored value still matches the caller's token, preventing accidental release of
     * a lock re-acquired by another owner after lease expiry.
     */
    private static final DefaultRedisScript<Long> RELEASE_LOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    /**
     * Atomically renews the lock TTL only if the stored value still matches the caller's token, preventing renewal of a lock
     * that has already been acquired by a different owner.
     */
    private static final DefaultRedisScript<Long> RENEW_LOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('pexpire', KEYS[1], ARGV[2]) else return 0 end",
            Long.class);

    private final StringRedisTemplate stringRedisTemplate;

    private final RedisLockProperties properties;

    private final RedisHealthMonitor healthMonitor;

    private final ScheduledExecutorService renewalExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "redis-distributed-lock-renewal");
        thread.setDaemon(true);
        return thread;
    });

    public RedisDistributedLockManager(
            StringRedisTemplate stringRedisTemplate, RedisLockProperties properties, RedisHealthMonitor healthMonitor) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.properties = properties;
        this.healthMonitor = healthMonitor;
    }

    @Override
    public <T> T executeWithLock(LockPurpose purpose, String lockKey, Supplier<T> action) {
        Objects.requireNonNull(action, "action must not be null");
        RedisLockProperties.PurposeSettings settings = properties.resolve(purpose);
        String redisKey = buildRedisKey(settings, lockKey);
        String lockValue = UUID.randomUUID().toString();

        acquireLock(redisKey, lockValue, settings);
        log.debug("Acquired Redis lock [purpose={}, key={}]", purpose, redisKey);
        ScheduledFuture<?> renewalTask = scheduleLockRenewal(redisKey, lockValue, settings);
        try {
            return action.get();
        } finally {
            renewalTask.cancel(true);
            releaseLock(redisKey, lockValue, settings);
            log.debug("Released Redis lock [purpose={}, key={}]", purpose, redisKey);
        }
    }

    @Override
    public <T> Optional<T> tryWithLock(LockPurpose purpose, String lockKey, Supplier<T> action) {
        Objects.requireNonNull(action, "action must not be null");
        RedisLockProperties.PurposeSettings settings = properties.resolve(purpose);
        String redisKey = buildRedisKey(settings, lockKey);
        String lockValue = UUID.randomUUID().toString();

        healthMonitor.assertReachable(redisKey);

        Boolean acquired = stringRedisTemplate
                .opsForValue()
                .setIfAbsent(redisKey, lockValue, settings.getLeaseDurationMs(), TimeUnit.MILLISECONDS);
        if (!Boolean.TRUE.equals(acquired)) {
            log.debug(
                    "Redis lock [purpose={}, key={}] is already held by another owner; skipping action.",
                    purpose,
                    redisKey);
            return Optional.empty();
        }

        log.debug("Acquired Redis lock (try) [purpose={}, key={}]", purpose, redisKey);
        ScheduledFuture<?> renewalTask = scheduleLockRenewal(redisKey, lockValue, settings);
        try {
            return Optional.ofNullable(action.get());
        } finally {
            renewalTask.cancel(true);
            releaseLock(redisKey, lockValue, settings);
            log.debug("Released Redis lock (try) [purpose={}, key={}]", purpose, redisKey);
        }
    }

    @PreDestroy
    void shutdownRenewalExecutor() {
        log.debug("Shutting down Redis lock renewal executor.");
        renewalExecutor.shutdownNow();
        try {
            if (!renewalExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                log.warn("Redis lock renewal executor did not terminate within 2 seconds.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void acquireLock(String redisKey, String lockValue, RedisLockProperties.PurposeSettings settings) {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(settings.getWaitTimeoutMs());

        do {
            healthMonitor.assertReachable(redisKey);
            Boolean acquired = stringRedisTemplate
                    .opsForValue()
                    .setIfAbsent(redisKey, lockValue, settings.getLeaseDurationMs(), TimeUnit.MILLISECONDS);
            if (Boolean.TRUE.equals(acquired)) {
                return;
            }

            log.debug(
                    "Redis lock [key={}] is held by another owner; retrying in {}ms.",
                    redisKey,
                    settings.getRetryIntervalMs());
            waitForRetry(redisKey, settings.getRetryIntervalMs());
        } while (System.nanoTime() <= deadline);
        throw new LockAcquisitionException(
                "Timed out acquiring Redis lock for key " + redisKey + " after " + settings.getWaitTimeoutMs() + "ms.");
    }

    private ScheduledFuture<?> scheduleLockRenewal(
            String redisKey, String lockValue, RedisLockProperties.PurposeSettings settings) {
        long renewalIntervalMs = Math.max(1_000L, settings.getLeaseDurationMs() / 3L);
        return renewalExecutor.scheduleAtFixedRate(
                () -> renewLock(redisKey, lockValue, settings.getLeaseDurationMs()),
                renewalIntervalMs,
                renewalIntervalMs,
                TimeUnit.MILLISECONDS);
    }

    private void renewLock(String redisKey, String lockValue, long leaseDurationMs) {
        try {
            Long renewed = stringRedisTemplate.execute(
                    RENEW_LOCK_SCRIPT, List.of(redisKey), lockValue, String.valueOf(leaseDurationMs));
            if (!Long.valueOf(1L).equals(renewed)) {
                log.warn(
                        "Redis lock [key={}] could not be renewed — ownership was lost. "
                                + "The action may continue but the lock is no longer held.",
                        redisKey);
            } else {
                log.debug("Renewed Redis lock [key={}] for {}ms.", redisKey, leaseDurationMs);
            }
        } catch (RuntimeException e) {
            // Non-fatal: log and allow the action to complete; the lease will expire naturally.
            log.warn("Failed to renew Redis lock [key={}]: {}", redisKey, e.getMessage());
        }
    }

    /**
     * Releases the lock atomically. Release failures are <em>deliberately non-propagating</em>: this method is always called
     * from a {@code finally} block, and throwing here would suppress the original exception from the guarded action. The
     * lock will auto-expire after the configured lease duration.
     */
    private void releaseLock(String redisKey, String lockValue, RedisLockProperties.PurposeSettings settings) {
        try {
            Long released = stringRedisTemplate.execute(RELEASE_LOCK_SCRIPT, List.of(redisKey), lockValue);
            if (!Long.valueOf(1L).equals(released)) {
                log.warn(
                        "Redis lock [key={}] was not released because it no longer belongs to this owner. "
                                + "It will expire in at most {}ms.",
                        redisKey,
                        settings.getLeaseDurationMs());
            }
        } catch (RuntimeException e) {
            // Log as error but never propagate from a finally block.
            log.error(
                    "Failed to release Redis lock [key={}]; lock will expire in at most {}ms.",
                    redisKey,
                    settings.getLeaseDurationMs(),
                    e);
        }
    }

    private void waitForRetry(String redisKey, long retryIntervalMs) {
        try {
            TimeUnit.MILLISECONDS.sleep(retryIntervalMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockAcquisitionException(
                    "Interrupted while waiting to acquire Redis lock for key " + redisKey + ".", e);
        }
    }

    private String buildRedisKey(RedisLockProperties.PurposeSettings settings, String lockKey) {
        if (!StringUtils.hasText(lockKey)) {
            throw new IllegalArgumentException(
                    "lockKey must not be blank. Provide a non-empty business identifier (e.g. a requisition ID).");
        }
        return settings.getKeyPrefix() + ":" + lockKey;
    }
}
