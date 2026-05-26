/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.lock;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties("fhir.odoo.lock.redis")
public class RedisLockProperties {

    /** Global switch to enable or disable the Redis lock subsystem. Defaults to {@code true}. */
    private boolean enabled = true;

    /** Startup wait configuration — how long to wait for Redis before failing application boot. */
    @Valid
    @NotNull private StartupSettings startup = new StartupSettings();

    /** Background health probe configuration. */
    @Valid
    @NotNull private HealthSettings health = new HealthSettings();

    /**
     * Default purpose settings applied when no purpose-specific override is configured. All fields in this block are
     * required.
     */
    @NotNull private PurposeSettings defaults = PurposeSettings.withBuiltInDefaults();

    /**
     * Per-purpose overrides keyed by uppercase {@link LockPurpose} name (e.g. {@code SERVICE_REQUEST_REQUISITION}). Only
     * non-null fields override the defaults.
     */
    private Map<LockPurpose, PurposeSettings> purposes = new HashMap<>();

    /** Validates that the default key-prefix is not blank. */
    public boolean isDefaultKeyPrefixValid() {
        return defaults != null && StringUtils.hasText(defaults.keyPrefix);
    }

    /** Validates that the default wait-timeout is positive. */
    public boolean isDefaultWaitTimeoutValid() {
        return defaults != null && defaults.waitTimeoutMs != null && defaults.waitTimeoutMs > 0;
    }

    /** Validates that the default lease-duration is positive. */
    public boolean isDefaultLeaseDurationValid() {
        return defaults != null && defaults.leaseDurationMs != null && defaults.leaseDurationMs > 0;
    }

    /** Validates that the default retry-interval is positive. */
    public boolean isDefaultRetryIntervalValid() {
        return defaults != null && defaults.retryIntervalMs != null && defaults.retryIntervalMs > 0;
    }

    /**
     * Validates that the default retry-interval is strictly less than the default lease-duration to ensure a lock can always
     * be renewed at least once before it expires.
     */
    public boolean isDefaultRetryLessThanLease() {
        return defaults == null
                || defaults.retryIntervalMs == null
                || defaults.leaseDurationMs == null
                || defaults.retryIntervalMs < defaults.leaseDurationMs;
    }

    /**
     * Resolves the effective {@link PurposeSettings} for the given purpose.
     * <p>If no purpose-specific override is configured, returns {@link #defaults}. Otherwise
     * merges the purpose-specific override on top of {@link #defaults}; only non-null override fields take precedence.
     *
     * @param purpose must not be {@link LockPurpose#DEFAULT}
     * @throws IllegalArgumentException if {@code purpose} is {@link LockPurpose#DEFAULT}
     */
    public PurposeSettings resolve(LockPurpose purpose) {
        if (purpose == LockPurpose.DEFAULT) {
            throw new IllegalArgumentException(
                    "LockPurpose.DEFAULT is a sentinel for the fallback configuration profile "
                            + "and must not be used as a call-site lock purpose.");
        }
        return Optional.ofNullable(purposes.get(purpose))
                .map(override -> override.mergeOver(defaults))
                .orElse(defaults);
    }

    /**
     * Per-purpose (or default-profile) lock settings. All fields are nullable to support partial purpose-specific overrides;
     * the {@link #defaults} profile must have all fields populated.
     */
    @Data
    public static class PurposeSettings {

        /**
         * Redis key namespace prefix. The full Redis key is {@code <keyPrefix>:<lockKey>}. Null in a purpose override means
         * "inherit from defaults".
         */
        private String keyPrefix;

        /**
         * Maximum time in milliseconds to wait for a lock before throwing {@link LockAcquisitionException}. Null means
         * "inherit from defaults".
         */
        private Long waitTimeoutMs;

        /**
         * Lock lease duration in milliseconds. The lock auto-expires after this time if not renewed. Null means "inherit
         * from defaults".
         */
        private Long leaseDurationMs;

        /**
         * Interval in milliseconds between lock-acquisition retry attempts while spinning. Null means "inherit from
         * defaults".
         */
        private Long retryIntervalMs;

        /**
         * Returns a fully-populated {@code PurposeSettings} instance with sensible built-in defaults used as the baseline
         * when no property overrides are provided.
         */
        static PurposeSettings withBuiltInDefaults() {
            PurposeSettings s = new PurposeSettings();
            s.keyPrefix = "fhir-odoo:lock";
            s.waitTimeoutMs = 5_000L;
            s.leaseDurationMs = 30_000L;
            s.retryIntervalMs = 100L;
            return s;
        }

        /**
         * Produces a merged {@code PurposeSettings} where non-null fields of {@code this} (the override) take precedence
         * over {@code base}. Returns a new instance; neither input is mutated.
         */
        PurposeSettings mergeOver(PurposeSettings base) {
            PurposeSettings merged = new PurposeSettings();
            merged.keyPrefix = keyPrefix != null ? keyPrefix : base.keyPrefix;
            merged.waitTimeoutMs = waitTimeoutMs != null ? waitTimeoutMs : base.waitTimeoutMs;
            merged.leaseDurationMs = leaseDurationMs != null ? leaseDurationMs : base.leaseDurationMs;
            merged.retryIntervalMs = retryIntervalMs != null ? retryIntervalMs : base.retryIntervalMs;
            return merged;
        }
    }

    /** Controls how long the application waits for Redis to become available at startup. */
    @Data
    public static class StartupSettings {

        /**
         * Maximum time in milliseconds to wait for Redis before failing application startup. Defaults to 60 seconds.
         */
        @Positive private long maxWaitMs = 60_000L;
    }

    /** Controls the background health probe behaviour. */
    @Data
    public static class HealthSettings {

        /**
         * Interval in milliseconds between consecutive Redis health probes. Used both during startup retries and for the
         * ongoing background probe. Defaults to 5 seconds.
         */
        @Positive private long probeIntervalMs = 5_000L;

        /**
         * How often in milliseconds to emit a WARN log while Redis remains continuously unreachable. Defaults to 30
         * seconds.
         */
        @Positive private long periodicWarnIntervalMs = 30_000L;
    }
}
