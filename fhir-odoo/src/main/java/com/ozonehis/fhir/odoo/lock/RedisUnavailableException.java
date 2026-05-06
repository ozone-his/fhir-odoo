/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.lock;

/**
 * Thrown when a lock operation is rejected because the Redis backend is currently unreachable.
 *
 * <p>This exception is distinct from {@link LockAcquisitionException} (which signals that Redis
 * <em>is</em> reachable but the lock is held by another owner). A {@code RedisUnavailableException}
 * means the infrastructure itself is unavailable and the caller should not retry the lock operation
 * — it should instead propagate the failure and let the higher-level retry mechanism decide.
 */
public class RedisUnavailableException extends LockAcquisitionException {

    public RedisUnavailableException(String message) {
        super(message);
    }

    public RedisUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
