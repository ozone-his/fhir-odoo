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
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class RedisDistributedLockManager implements DistributedLockManager {

    private static final DefaultRedisScript<Long> RELEASE_LOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private static final DefaultRedisScript<Long> RENEW_LOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('pexpire', KEYS[1], ARGV[2]) else return 0 end",
            Long.class);

    private final StringRedisTemplate stringRedisTemplate;

    private final ScheduledExecutorService renewalExecutor;

    private final String keyPrefix;

    private final long waitTimeoutMs;

    private final long leaseDurationMs;

    private final long retryIntervalMs;

    public RedisDistributedLockManager(
            StringRedisTemplate stringRedisTemplate,
            @Value("${fhir.odoo.lock.redis.key-prefix:fhir-odoo:lock}") String keyPrefix,
            @Value("${fhir.odoo.lock.redis.wait-timeout-ms:5000}") long waitTimeoutMs,
            @Value("${fhir.odoo.lock.redis.lease-duration-ms:30000}") long leaseDurationMs,
            @Value("${fhir.odoo.lock.redis.retry-interval-ms:100}") long retryIntervalMs) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.keyPrefix = keyPrefix;
        this.waitTimeoutMs = waitTimeoutMs;
        this.leaseDurationMs = leaseDurationMs;
        this.retryIntervalMs = retryIntervalMs;
        this.renewalExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "redis-distributed-lock-renewal");
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public <T> T executeWithLock(String lockKey, Supplier<T> action) {
        Objects.requireNonNull(action, "action must not be null");
        String redisKey = toRedisKey(lockKey);
        String lockValue = UUID.randomUUID().toString();

        acquireLock(redisKey, lockValue);
        ScheduledFuture<?> renewalTask = scheduleLockRenewal(redisKey, lockValue);
        try {
            return action.get();
        } finally {
            renewalTask.cancel(true);
            releaseLock(redisKey, lockValue);
        }
    }

    @PreDestroy
    void shutdownExecutor() {
        renewalExecutor.shutdownNow();
    }

    private void acquireLock(String redisKey, String lockValue) {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(waitTimeoutMs);

        do {
            Boolean acquired = stringRedisTemplate
                    .opsForValue()
                    .setIfAbsent(redisKey, lockValue, leaseDurationMs, TimeUnit.MILLISECONDS);
            if (Boolean.TRUE.equals(acquired)) {
                return;
            }

            waitForRetry(redisKey);
        } while (System.nanoTime() <= deadline);

        throw new IllegalStateException("Timed out acquiring Redis lock for key " + redisKey);
    }

    private ScheduledFuture<?> scheduleLockRenewal(String redisKey, String lockValue) {
        long renewalIntervalMs = Math.max(1000L, leaseDurationMs / 3L);
        return renewalExecutor.scheduleAtFixedRate(
                () -> renewLock(redisKey, lockValue), renewalIntervalMs, renewalIntervalMs, TimeUnit.MILLISECONDS);
    }

    private void renewLock(String redisKey, String lockValue) {
        try {
            Long renewed = stringRedisTemplate.execute(
                    RENEW_LOCK_SCRIPT, List.of(redisKey), lockValue, String.valueOf(leaseDurationMs));
            if (!Long.valueOf(1L).equals(renewed)) {
                log.warn("Redis lock {} could not be renewed because ownership was lost", redisKey);
            }
        } catch (RuntimeException exception) {
            log.warn("Failed to renew Redis lock {}", redisKey, exception);
        }
    }

    private void releaseLock(String redisKey, String lockValue) {
        try {
            Long released = stringRedisTemplate.execute(RELEASE_LOCK_SCRIPT, List.of(redisKey), lockValue);
            if (!Long.valueOf(1L).equals(released)) {
                log.warn("Redis lock {} was not released because it no longer belonged to this owner", redisKey);
            }
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Failed to release Redis lock for key " + redisKey, exception);
        }
    }

    private void waitForRetry(String redisKey) {
        try {
            TimeUnit.MILLISECONDS.sleep(retryIntervalMs);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Interrupted while waiting to acquire Redis lock for key " + redisKey, exception);
        }
    }

    private String toRedisKey(String lockKey) {
        if (!StringUtils.hasText(lockKey)) {
            throw new IllegalArgumentException("lockKey must not be blank");
        }

        return keyPrefix + ":" + lockKey;
    }
}
