/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.lock;

/**
 * Identifies the intended purpose of a distributed lock. Each value maps to a dedicated configuration block under
 * {@code fhir.odoo.lock.redis.purposes}, allowing independent tuning of key prefix, wait timeout, lease duration, and retry
 * interval per use-case.
 * <p>{@link #DEFAULT} is a <em>sentinel</em> value representing the fallback configuration profile.
 * It must <strong>never</strong> be passed as a call-site purpose to {@link DistributedLockManager#executeWithLock} or
 * {@link DistributedLockManager#tryWithLock}; doing so throws {@link IllegalArgumentException}.
 */
public enum LockPurpose {

    /**
     * Sentinel value for the fallback configuration profile. Must not be used as a call-site lock purpose.
     */
    DEFAULT,

    /**
     * Guards creation of a Odoo SaleOrder for a given FHIR ServiceRequest requisition, preventing duplicate orders under
     * concurrent requests sharing the same requisition ID.
     */
    SERVICE_REQUEST_REQUISITION
}
