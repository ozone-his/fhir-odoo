/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.lock;

/**
 * Base exception for distributed lock failures.
 * <ul>
 *   <li>Thrown when a lock cannot be acquired within the configured wait timeout.</li>
 *   <li>Thrown when the thread is interrupted while waiting for the lock.</li>
 *   <li>Subclassed by {@link RedisUnavailableException} when the Redis backend itself is
 *       unreachable and lock operations should not even be attempted.</li>
 * </ul>
 */
public class LockAcquisitionException extends RuntimeException {

    public LockAcquisitionException(String message) {
        super(message);
    }

    public LockAcquisitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
