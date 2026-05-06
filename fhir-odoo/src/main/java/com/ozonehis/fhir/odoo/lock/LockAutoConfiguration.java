/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.lock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Spring configuration that wires the Redis-backed distributed lock subsystem.
 * <p>All lock beans are created here rather than via {@code @Component} annotations so that
 * the dependency graph is explicit, testable, and disabled as a unit when {@code fhir.odoo.lock.redis.enabled=false}.
 * <p>The entire subsystem is skipped when {@code fhir.odoo.lock.redis.enabled=false}, allowing
 * tests or environments without Redis to opt out cleanly.
 */
@Configuration
@EnableConfigurationProperties(RedisLockProperties.class)
@ConditionalOnProperty(name = "fhir.odoo.lock.redis.enabled", havingValue = "true", matchIfMissing = true)
public class LockAutoConfiguration {

    /**
     * Creates the {@link RedisHealthMonitor}, which performs the Redis startup wait via
     * {@link jakarta.annotation.PostConstruct} and runs the ongoing background probe.
     * <p>This bean is declared first so that it is fully initialized (and startup wait has
     * completed) before the {@link DistributedLockManager} bean that depends on it is created.
     */
    @Bean
    public RedisHealthMonitor redisHealthMonitor(
            RedisConnectionFactory connectionFactory, RedisLockProperties properties) {
        return new RedisHealthMonitor(connectionFactory, properties);
    }

    /**
     * Creates the {@link DistributedLockManager} backed by Redis.
     * <p>Depends on {@link RedisHealthMonitor} being fully initialized, which guarantees Redis is
     * reachable before any lock operation is ever attempted.
     */
    @Bean
    public DistributedLockManager distributedLockManager(
            StringRedisTemplate stringRedisTemplate,
            RedisLockProperties properties,
            RedisHealthMonitor redisHealthMonitor) {
        return new RedisDistributedLockManager(stringRedisTemplate, properties, redisHealthMonitor);
    }
}
