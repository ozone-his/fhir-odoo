/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Unit tests for {@link RedisHealthMonitor} — startup wait, live gating, state-transition logging,
 * and periodic warnings. The {@link RedisConnectionFactory} and {@link RedisConnection} are mocked
 * so no real Redis instance is required.
 *
 * <p>Tests exercise package-private methods ({@link RedisHealthMonitor#runHealthProbe()},
 * {@link RedisHealthMonitor#waitForRedisAtStartup()}) directly to avoid scheduler timing
 * non-determinism.
 */
class RedisHealthMonitorTest {

    private RedisConnectionFactory connectionFactory;

    private RedisConnection connection;

    private RedisHealthMonitor monitor;

    @BeforeEach
    void setUp() {
        connectionFactory = mock(RedisConnectionFactory.class);
        connection = mock(RedisConnection.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
    }

    @AfterEach
    void tearDown() {
        if (monitor != null) {
            monitor.shutdown();
        }
    }

    @Test
    @DisplayName("waitForRedisAtStartup() succeeds immediately when Redis responds with PONG")
    void waitForRedisAtStartup_succeedsImmediately_whenRedisPongsOnFirstAttempt() {
        when(connection.ping()).thenReturn("PONG");
        monitor = monitorWithShortTimeouts();

        monitor.waitForRedisAtStartup(); // must not throw

        assertThat(monitor.isRedisReachable()).isTrue();
    }

    @Test
    @DisplayName("waitForRedisAtStartup() retries and succeeds when Redis becomes available after failures")
    void waitForRedisAtStartup_retriesAndSucceeds_whenRedisBecomesAvailableAfterInitialFailures() {
        when(connection.ping())
                .thenThrow(new RuntimeException("connection refused"))
                .thenThrow(new RuntimeException("connection refused"))
                .thenReturn("PONG");
        monitor = monitorWithShortTimeouts();

        monitor.waitForRedisAtStartup();

        assertThat(monitor.isRedisReachable()).isTrue();
        verify(connection, atLeastOnce()).ping();
    }

    @Test
    @DisplayName("waitForRedisAtStartup() throws IllegalStateException after maxWaitMs with explicit message")
    void waitForRedisAtStartup_throwsIllegalStateException_afterMaxWaitMsExceeded() {
        when(connection.ping()).thenThrow(new RuntimeException("connection refused"));
        monitor = monitorWithShortTimeouts(); // maxWaitMs=200, probeInterval=50

        assertThatThrownBy(() -> monitor.waitForRedisAtStartup())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unavailable")
                .hasMessageContaining("200ms");

        assertThat(monitor.isRedisReachable()).isFalse();
    }

    @Test
    @DisplayName("assertReachable() does not throw when Redis is reachable")
    void assertReachable_doesNotThrow_whenRedisIsReachable() {
        when(connection.ping()).thenReturn("PONG");
        monitor = monitorWithShortTimeouts();
        monitor.waitForRedisAtStartup();

        monitor.assertReachable("test:key"); // must not throw
    }

    @Test
    @DisplayName("assertReachable() throws RedisUnavailableException when Redis is unreachable")
    void assertReachable_throwsRedisUnavailableException_whenRedisIsUnreachable() {
        // Do NOT call waitForRedisAtStartup() — redisReachable stays false
        monitor = monitorWithoutStartup();

        assertThatThrownBy(() -> monitor.assertReachable("test:key"))
                .isInstanceOf(RedisUnavailableException.class)
                .hasMessageContaining("test:key");
    }

    @Test
    @DisplayName("runHealthProbe() sets reachable=false on AVAILABLE→DOWN transition")
    void runHealthProbe_setsReachableFalse_onAvailableToDownTransition() {
        // Start reachable, then simulate Redis going down
        when(connection.ping()).thenReturn("PONG");
        monitor = monitorWithShortTimeouts();
        monitor.waitForRedisAtStartup(); // sets reachable=true
        assertThat(monitor.isRedisReachable()).isTrue();

        when(connection.ping()).thenThrow(new RuntimeException("connection lost"));
        monitor.runHealthProbe();

        assertThat(monitor.isRedisReachable()).isFalse();
    }

    @Test
    @DisplayName("runHealthProbe() sets reachable=true on DOWN→RECOVERED transition")
    void runHealthProbe_setsReachableTrue_onDownToRecoveredTransition() {
        // Start unreachable
        monitor = monitorWithoutStartup();
        assertThat(monitor.isRedisReachable()).isFalse();

        when(connection.ping()).thenReturn("PONG");
        monitor.runHealthProbe();

        assertThat(monitor.isRedisReachable()).isTrue();
    }

    @Test
    @DisplayName("runHealthProbe() keeps reachable=false when Redis remains down")
    void runHealthProbe_keepsReachableFalse_whenRedisRemainsDown() {
        monitor = monitorWithoutStartup();
        when(connection.ping()).thenThrow(new RuntimeException("still down"));

        monitor.runHealthProbe();
        monitor.runHealthProbe();
        monitor.runHealthProbe();

        assertThat(monitor.isRedisReachable()).isFalse();
    }

    @Test
    @DisplayName("runHealthProbe() keeps reachable=true when Redis remains available")
    void runHealthProbe_keepsReachableTrue_whenRedisRemainsAvailable() {
        when(connection.ping()).thenReturn("PONG");
        monitor = monitorWithShortTimeouts();
        monitor.waitForRedisAtStartup();

        monitor.runHealthProbe();
        monitor.runHealthProbe();

        assertThat(monitor.isRedisReachable()).isTrue();
    }

    @Test
    @DisplayName("pingRedis() returns true when Redis responds with PONG")
    void pingRedis_returnsTrue_whenRedisRespondsPong() {
        when(connection.ping()).thenReturn("PONG");
        monitor = monitorWithoutStartup();

        assertThat(monitor.pingRedis()).isTrue();
    }

    @Test
    @DisplayName("pingRedis() returns false when getConnection() throws")
    void pingRedis_returnsFalse_whenGetConnectionThrows() {
        when(connectionFactory.getConnection()).thenThrow(new RuntimeException("refused"));
        monitor = monitorWithoutStartup();

        assertThat(monitor.pingRedis()).isFalse();
    }

    @Test
    @DisplayName("pingRedis() returns false when ping() returns unexpected value")
    void pingRedis_returnsFalse_whenPingReturnsUnexpectedValue() {
        when(connection.ping()).thenReturn("ERR");
        monitor = monitorWithoutStartup();

        assertThat(monitor.pingRedis()).isFalse();
    }

    /** Creates a monitor with short timeouts suitable for timeout-testing startup wait. */
    private RedisHealthMonitor monitorWithShortTimeouts() {
        RedisLockProperties props = new RedisLockProperties();
        props.getStartup().setMaxWaitMs(200L);
        props.getHealth().setProbeIntervalMs(50L);
        props.getHealth().setPeriodicWarnIntervalMs(100L);
        return new RedisHealthMonitor(connectionFactory, props);
    }

    /**
     * Creates a monitor without running startup wait — {@code redisReachable} starts as {@code false}.
     * Use to test probe and gating behaviour independently of startup logic.
     */
    private RedisHealthMonitor monitorWithoutStartup() {
        RedisLockProperties props = new RedisLockProperties();
        props.getStartup().setMaxWaitMs(200L);
        props.getHealth().setProbeIntervalMs(50L);
        props.getHealth().setPeriodicWarnIntervalMs(100L);
        return new RedisHealthMonitor(connectionFactory, props);
    }
}
