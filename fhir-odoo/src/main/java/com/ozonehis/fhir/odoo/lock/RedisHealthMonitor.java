/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.lock;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Slf4j
public class RedisHealthMonitor {

    private final RedisConnectionFactory connectionFactory;

    private final long maxStartupWaitMs;

    private final long probeIntervalMs;

    private final long periodicWarnIntervalMs;

    private final AtomicBoolean redisReachable = new AtomicBoolean(false);

    /** Timestamp (nanos) of the last periodic warn emission. Starts at 0 to force first-emit. */
    private final AtomicLong lastPeriodicWarnNanos = new AtomicLong(0L);

    private final ScheduledExecutorService probeExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "redis-health-probe");
        thread.setDaemon(true);
        return thread;
    });

    public RedisHealthMonitor(RedisConnectionFactory connectionFactory, RedisLockProperties properties) {
        this.connectionFactory = connectionFactory;
        this.maxStartupWaitMs = properties.getStartup().getMaxWaitMs();
        this.probeIntervalMs = properties.getHealth().getProbeIntervalMs();
        this.periodicWarnIntervalMs = properties.getHealth().getPeriodicWarnIntervalMs();
    }

    @PostConstruct
    void initialize() {
        waitForRedisAtStartup();
        startBackgroundProbe();
    }

    @PreDestroy
    void shutdown() {
        log.debug("Shutting down Redis health probe executor.");
        probeExecutor.shutdownNow();
        try {
            if (!probeExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                log.warn("Redis health probe executor did not terminate within 2 seconds.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Asserts that Redis is currently reachable. Throws {@link RedisUnavailableException} if the background probe has marked
     * Redis as down, so the caller's lock attempt is rejected immediately rather than hanging until a timeout.
     *
     * @param redisKey the Redis key of the lock being acquired (used in the exception message)
     * @throws RedisUnavailableException if Redis is currently unreachable
     */
    public void assertReachable(String redisKey) {
        if (!redisReachable.get()) {
            throw new RedisUnavailableException("Redis is currently unreachable. Lock attempt rejected for key: "
                    + redisKey + ". Retry once Redis recovers at " + redisAddress() + ".");
        }
    }

    /**
     * Blocks the calling thread, retrying {@code PING} at {@code probeIntervalMs} intervals until either Redis responds or
     * {@code maxStartupWaitMs} elapses.
     *
     * @throws IllegalStateException if Redis does not become available within the configured timeout
     */
    void waitForRedisAtStartup() {
        log.info(
                "Waiting for Redis to become available at {} (max wait: {}ms, probe interval: {}ms)...",
                redisAddress(),
                maxStartupWaitMs,
                probeIntervalMs);

        long deadlineNanos = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(maxStartupWaitMs);
        int attempts = 0;

        do {
            attempts++;
            if (pingRedis()) {
                redisReachable.set(true);
                log.info("Redis is available at {} (took {} attempt(s)).", redisAddress(), attempts);
                return;
            }
            log.debug(
                    "Redis not yet available at {} (attempt {}); retrying in {}ms.",
                    redisAddress(),
                    attempts,
                    probeIntervalMs);
            sleepQuietly(probeIntervalMs);
        } while (System.nanoTime() <= deadlineNanos);

        throw new IllegalStateException("Redis is unavailable after "
                + maxStartupWaitMs
                + "ms ("
                + attempts
                + " attempts). The application cannot start. "
                + "Ensure Redis is reachable at "
                + redisAddress()
                + ".");
    }

    private void startBackgroundProbe() {
        probeExecutor.scheduleAtFixedRate(
                this::runHealthProbe, probeIntervalMs, probeIntervalMs, TimeUnit.MILLISECONDS);
        log.debug("Redis background health probe started (interval: {}ms).", probeIntervalMs);
    }

    /**
     * Executes a single health probe cycle. Exposed as package-private for direct invocation in unit tests without relying
     * on the scheduler.
     */
    void runHealthProbe() {
        boolean currentlyReachable = pingRedis();
        boolean wasReachable = redisReachable.getAndSet(currentlyReachable);

        if (wasReachable && !currentlyReachable) {
            // Transition: AVAILABLE → DOWN
            lastPeriodicWarnNanos.set(System.nanoTime()); // reset so periodic warn fires next interval
            log.warn(
                    "Redis is now UNREACHABLE at {}. All lock attempts will be rejected until Redis recovers.",
                    redisAddress());
        } else if (!wasReachable && currentlyReachable) {
            // Transition: DOWN → RECOVERED
            log.info("Redis has RECOVERED at {}. Lock operations will resume normally.", redisAddress());
        } else if (!currentlyReachable) {
            // Continuously down — emit periodic warning
            emitPeriodicWarnIfDue();
        } else {
            log.debug("Redis health probe successful at {}.", redisAddress());
        }
    }

    private void emitPeriodicWarnIfDue() {
        long nowNanos = System.nanoTime();
        long warnIntervalNanos = TimeUnit.MILLISECONDS.toNanos(periodicWarnIntervalMs);
        long last = lastPeriodicWarnNanos.get();

        if ((nowNanos - last) >= warnIntervalNanos) {
            // CAS guarantees only one concurrent thread emits the log even if probes overlap
            if (lastPeriodicWarnNanos.compareAndSet(last, nowNanos)) {
                log.warn(
                        "Redis is still UNREACHABLE at {}. Lock attempts are being rejected. "
                                + "Check Redis connectivity.",
                        redisAddress());
            }
        }
    }

    /**
     * Sends a direct {@code PING} to Redis via a fresh connection obtained from the {@link RedisConnectionFactory},
     * bypassing any {@code RedisTemplate} connection caching.
     *
     * @return {@code true} if Redis responded with {@code PONG}; {@code false} on any error
     */
    boolean pingRedis() {
        try (RedisConnection connection = connectionFactory.getConnection()) {
            String pong = connection.ping();
            return "PONG".equalsIgnoreCase(pong);
        } catch (Exception e) {
            log.debug("Redis ping failed: {}", e.getMessage());
            return false;
        }
    }

    private void sleepQuietly(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for Redis to become available.", e);
        }
    }

    private String redisAddress() {
        if (connectionFactory instanceof LettuceConnectionFactory lf) {
            return lf.getHostName() + ":" + lf.getPort();
        }
        return "unknown";
    }

    /** Returns the current Redis availability state. Exposed for testing. */
    boolean isRedisReachable() {
        return redisReachable.get();
    }
}
