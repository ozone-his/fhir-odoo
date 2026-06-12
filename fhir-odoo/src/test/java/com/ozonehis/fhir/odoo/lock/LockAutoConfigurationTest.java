/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

class LockAutoConfigurationTest {

    /**
     * Registers both the outer configuration and the nested {@code RedisLockConfiguration} as
     * independent candidates, mirroring what component scanning does in the real application
     * (nested {@code @Configuration} classes are scanned as top-level candidates too). This is
     * the arrangement that previously produced two {@code DistributedLockManager} beans.
     */
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(
                    MockRedisInfrastructure.class,
                    LockAutoConfiguration.class,
                    LockAutoConfiguration.RedisLockConfiguration.class);

    @Test
    @DisplayName("Should register exactly one Redis-backed DistributedLockManager when Redis locking is enabled")
    void shouldRegisterOnlyRedisLockManagerWhenEnabled() {
        contextRunner.withPropertyValues("fhir.odoo.lock.redis.enabled=true").run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBeansOfType(DistributedLockManager.class)).hasSize(1);
            assertThat(context.getBean(DistributedLockManager.class)).isInstanceOf(RedisDistributedLockManager.class);
        });
    }

    @Test
    @DisplayName("Should register exactly one no-op DistributedLockManager when Redis locking is disabled")
    void shouldRegisterOnlyNoOpLockManagerWhenDisabled() {
        contextRunner.withPropertyValues("fhir.odoo.lock.redis.enabled=false").run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBeansOfType(DistributedLockManager.class)).hasSize(1);
            assertThat(context.getBean(DistributedLockManager.class)).isInstanceOf(NoOpDistributedLockManager.class);
        });
    }

    @Test
    @DisplayName("Should default to the no-op DistributedLockManager when the enabled property is absent")
    void shouldDefaultToNoOpLockManagerWhenPropertyAbsent() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBeansOfType(DistributedLockManager.class)).hasSize(1);
            assertThat(context.getBean(DistributedLockManager.class)).isInstanceOf(NoOpDistributedLockManager.class);
        });
    }

    /** Provides the Redis infrastructure beans the nested configuration depends on, without a live Redis. */
    @Configuration
    static class MockRedisInfrastructure {

        @Bean
        RedisConnectionFactory redisConnectionFactory() {
            RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
            RedisConnection connection = mock(RedisConnection.class);
            when(factory.getConnection()).thenReturn(connection);
            when(connection.ping()).thenReturn("PONG");
            return factory;
        }

        @Bean
        StringRedisTemplate stringRedisTemplate() {
            return mock(StringRedisTemplate.class);
        }
    }
}
