/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.lock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Top-level lock configuration — always active.
 *
 * <p>Property binding is enabled here so {@link RedisLockProperties} is available regardless of
 * whether Redis locking is enabled. The Redis-specific beans live in the nested
 * {@link RedisLockConfiguration} class which is only activated when
 * {@code fhir.odoo.lock.redis.enabled=true}. When that condition is false the
 * {@link NoOpDistributedLockManager} fallback is registered instead, ensuring the application
 * context always contains exactly one {@link DistributedLockManager} bean.
 */
@Configuration
@EnableConfigurationProperties(RedisLockProperties.class)
public class LockAutoConfiguration {

    /**
     * Fallback {@link DistributedLockManager} registered when Redis locking is disabled
     * ({@code fhir.odoo.lock.redis.enabled=false}, the default).
     *
     * <p>Executes actions directly — no lock is acquired, no Redis connection is required.
     * This allows the application to start without Redis. A WARN is logged at startup to
     * make it explicit that mutual exclusion is not enforced.
     *
     * <p>This bean is skipped automatically when {@link RedisLockConfiguration} registers a
     * Redis-backed {@link DistributedLockManager} first.
     */
    @Bean
    @ConditionalOnMissingBean(DistributedLockManager.class)
    public DistributedLockManager noOpDistributedLockManager() {
        return new NoOpDistributedLockManager();
    }

    /**
     * Nested configuration activated only when {@code fhir.odoo.lock.redis.enabled=true}.
     *
     * <p>Declares the {@link RedisHealthMonitor} (which blocks at {@link jakarta.annotation.PostConstruct} until
     * Redis is reachable or the startup timeout elapses) and the Redis-backed
     * {@link DistributedLockManager}. Because these beans are registered before the outer-class
     * {@link #noOpDistributedLockManager()} is evaluated, the {@code @ConditionalOnMissingBean}
     * fallback is correctly suppressed.
     */
    @Configuration
    @ConditionalOnProperty(name = "fhir.odoo.lock.redis.enabled", havingValue = "true")
    static class RedisLockConfiguration {

        /**
         * Creates the {@link RedisHealthMonitor}, which performs the Redis startup wait and runs
         * the ongoing background probe. Declared before the lock manager to ensure Redis is
         * reachable before any lock operation is ever attempted.
         */
        @Bean
        public RedisHealthMonitor redisHealthMonitor(
                RedisConnectionFactory connectionFactory, RedisLockProperties properties) {
            return new RedisHealthMonitor(connectionFactory, properties);
        }

        /**
         * Creates the {@link DistributedLockManager} backed by Redis. Depends on
         * {@link RedisHealthMonitor} being fully initialized.
         */
        @Bean
        public DistributedLockManager distributedLockManager(
                StringRedisTemplate stringRedisTemplate,
                RedisLockProperties properties,
                RedisHealthMonitor redisHealthMonitor) {
            return new RedisDistributedLockManager(stringRedisTemplate, properties, redisHealthMonitor);
        }
    }
}
