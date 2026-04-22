/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.fhir.odoo.lock;

import java.util.Optional;
import java.util.function.Supplier;

public interface DistributedLockManager {

    /**
     * Execute {@code action} while holding the distributed lock identified by {@code lockKey}.
     * Blocks until the lock is acquired or the implementation-defined timeout elapses.
     *
     * @throws LockAcquisitionException if the lock cannot be acquired within the timeout or the
     *                                  thread is interrupted while waiting.
     */
    <T> T executeWithLock(String lockKey, Supplier<T> action);

    /**
     * Execute {@code action} while holding the distributed lock identified by {@code lockKey},
     * overriding the default wait timeout with {@code waitTimeoutMs}.
     *
     * @throws LockAcquisitionException if the lock cannot be acquired within {@code waitTimeoutMs}
     *                                  or the thread is interrupted while waiting.
     */
    <T> T executeWithLock(String lockKey, long waitTimeoutMs, Supplier<T> action);

    /**
     * Try to execute {@code action} while holding the distributed lock identified by
     * {@code lockKey}. Returns {@link Optional#empty()} immediately if the lock is held by
     * another owner (non-blocking).
     */
    <T> Optional<T> tryWithLock(String lockKey, Supplier<T> action);

    default void executeWithLock(String lockKey, Runnable action) {
        executeWithLock(lockKey, () -> {
            action.run();
            return null;
        });
    }

    default void executeWithLock(String lockKey, long waitTimeoutMs, Runnable action) {
        executeWithLock(lockKey, waitTimeoutMs, () -> {
            action.run();
            return null;
        });
    }
}
