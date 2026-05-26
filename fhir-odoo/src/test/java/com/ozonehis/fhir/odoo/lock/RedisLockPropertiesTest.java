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

import com.ozonehis.fhir.odoo.lock.RedisLockProperties.PurposeSettings;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RedisLockProperties} — purpose resolution, default fallback, and merge
 * semantics. Validates directly without a Spring context to keep tests fast.
 */
class RedisLockPropertiesTest {

    private RedisLockProperties properties;

    @BeforeEach
    void setUp() {
        properties = new RedisLockProperties();
        // Built-in defaults are set by field initialisation; no further wiring needed.
    }

    @Test
    @DisplayName("resolve() returns built-in defaults when no purpose-specific override is configured")
    void resolve_returnsBuiltInDefaults_whenNoPurposeOverrideIsConfigured() {
        PurposeSettings resolved = properties.resolve(LockPurpose.SERVICE_REQUEST_REQUISITION);

        assertThat(resolved.getKeyPrefix()).isEqualTo("fhir-odoo:lock");
        assertThat(resolved.getWaitTimeoutMs()).isEqualTo(5_000L);
        assertThat(resolved.getLeaseDurationMs()).isEqualTo(30_000L);
        assertThat(resolved.getRetryIntervalMs()).isEqualTo(100L);
    }

    @Test
    @DisplayName("resolve() returns custom defaults when defaults are overridden in configuration")
    void resolve_returnsCustomDefaults_whenDefaultsAreOverriddenInConfiguration() {
        PurposeSettings customDefaults = new PurposeSettings();
        customDefaults.setKeyPrefix("custom:prefix");
        customDefaults.setWaitTimeoutMs(10_000L);
        customDefaults.setLeaseDurationMs(60_000L);
        customDefaults.setRetryIntervalMs(200L);
        properties.setDefaults(customDefaults);

        PurposeSettings resolved = properties.resolve(LockPurpose.SERVICE_REQUEST_REQUISITION);

        assertThat(resolved.getKeyPrefix()).isEqualTo("custom:prefix");
        assertThat(resolved.getWaitTimeoutMs()).isEqualTo(10_000L);
    }

    @Test
    @DisplayName("resolve() merges purpose override on top of defaults — only non-null fields override")
    void resolve_mergesPurposeOverrideOnTopOfDefaults() {
        PurposeSettings override = new PurposeSettings();
        override.setKeyPrefix("fhir-odoo:service-request"); // override only the prefix
        // waitTimeoutMs, leaseDurationMs, retryIntervalMs intentionally left null

        properties.setPurposes(Map.of(LockPurpose.SERVICE_REQUEST_REQUISITION, override));

        PurposeSettings resolved = properties.resolve(LockPurpose.SERVICE_REQUEST_REQUISITION);

        assertThat(resolved.getKeyPrefix())
                .as("override field should take precedence")
                .isEqualTo("fhir-odoo:service-request");
        assertThat(resolved.getWaitTimeoutMs())
                .as("non-overridden field should fall back to default")
                .isEqualTo(5_000L);
        assertThat(resolved.getLeaseDurationMs())
                .as("non-overridden field should fall back to default")
                .isEqualTo(30_000L);
        assertThat(resolved.getRetryIntervalMs())
                .as("non-overridden field should fall back to default")
                .isEqualTo(100L);
    }

    @Test
    @DisplayName("resolve() merges full purpose override — all fields take precedence over defaults")
    void resolve_mergesFullPurposeOverride_allFieldsTakePrecedence() {
        PurposeSettings override = new PurposeSettings();
        override.setKeyPrefix("custom:sr");
        override.setWaitTimeoutMs(1_000L);
        override.setLeaseDurationMs(10_000L);
        override.setRetryIntervalMs(50L);

        properties.setPurposes(Map.of(LockPurpose.SERVICE_REQUEST_REQUISITION, override));

        PurposeSettings resolved = properties.resolve(LockPurpose.SERVICE_REQUEST_REQUISITION);

        assertThat(resolved.getKeyPrefix()).isEqualTo("custom:sr");
        assertThat(resolved.getWaitTimeoutMs()).isEqualTo(1_000L);
        assertThat(resolved.getLeaseDurationMs()).isEqualTo(10_000L);
        assertThat(resolved.getRetryIntervalMs()).isEqualTo(50L);
    }

    @Test
    @DisplayName("resolve() returns a new instance — mutations do not affect defaults or the override")
    void resolve_returnsNewInstance_mutationsDoNotAffectOriginals() {
        PurposeSettings override = new PurposeSettings();
        override.setKeyPrefix("override:prefix");
        properties.setPurposes(Map.of(LockPurpose.SERVICE_REQUEST_REQUISITION, override));

        PurposeSettings resolved = properties.resolve(LockPurpose.SERVICE_REQUEST_REQUISITION);
        resolved.setKeyPrefix("mutated");

        assertThat(override.getKeyPrefix()).isEqualTo("override:prefix");
        assertThat(properties.getDefaults().getKeyPrefix()).isEqualTo("fhir-odoo:lock");
    }

    @Test
    @DisplayName("resolve() throws IllegalArgumentException when LockPurpose.DEFAULT is passed")
    void resolve_throwsIllegalArgumentException_whenDefaultPurposeIsPassed() {
        assertThatThrownBy(() -> properties.resolve(LockPurpose.DEFAULT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sentinel");
    }

    @Test
    @DisplayName("isDefaultRetryLessThanLease() returns true when retry < lease (valid config)")
    void isDefaultRetryLessThanLease_returnsTrue_whenRetryLessThanLease() {
        assertThat(properties.isDefaultRetryLessThanLease()).isTrue();
    }

    @Test
    @DisplayName("isDefaultRetryLessThanLease() returns false when retry >= lease (invalid config)")
    void isDefaultRetryLessThanLease_returnsFalse_whenRetryGreaterThanOrEqualToLease() {
        PurposeSettings invalid = new PurposeSettings();
        invalid.setKeyPrefix("fhir-odoo:lock");
        invalid.setWaitTimeoutMs(5_000L);
        invalid.setLeaseDurationMs(100L);
        invalid.setRetryIntervalMs(200L); // retry >= lease — invalid!
        properties.setDefaults(invalid);

        assertThat(properties.isDefaultRetryLessThanLease()).isFalse();
    }

    @Test
    @DisplayName("isDefaultKeyPrefixValid() returns false when keyPrefix is blank")
    void isDefaultKeyPrefixValid_returnsFalse_whenKeyPrefixIsBlank() {
        PurposeSettings blank = PurposeSettings.withBuiltInDefaults();
        blank.setKeyPrefix("   ");
        properties.setDefaults(blank);

        assertThat(properties.isDefaultKeyPrefixValid()).isFalse();
    }

    @Test
    @DisplayName("isDefaultWaitTimeoutValid() returns false when waitTimeoutMs is zero")
    void isDefaultWaitTimeoutValid_returnsFalse_whenWaitTimeoutIsZero() {
        PurposeSettings s = PurposeSettings.withBuiltInDefaults();
        s.setWaitTimeoutMs(0L);
        properties.setDefaults(s);

        assertThat(properties.isDefaultWaitTimeoutValid()).isFalse();
    }

    @Test
    @DisplayName("StartupSettings defaults to 60000ms maxWaitMs")
    void startupSettings_defaultMaxWaitMs() {
        assertThat(properties.getStartup().getMaxWaitMs()).isEqualTo(60_000L);
    }

    @Test
    @DisplayName("HealthSettings defaults to 5000ms probeIntervalMs and 30000ms periodicWarnIntervalMs")
    void healthSettings_defaultIntervals() {
        assertThat(properties.getHealth().getProbeIntervalMs()).isEqualTo(5_000L);
        assertThat(properties.getHealth().getPeriodicWarnIntervalMs()).isEqualTo(30_000L);
    }
}
