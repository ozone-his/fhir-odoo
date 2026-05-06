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

/**
 * Abstraction for acquiring and executing actions under a distributed lock.
 * <p>All operations are purpose-driven: the {@link LockPurpose} determines which configuration
 * profile (key prefix, wait timeout, lease duration, retry interval) is resolved for each call, allowing independent tuning
 * per use-case without changing call-site code.
 * <p>The backing implementation is Redis-only. Lock behaviour guarantees:
 * <ul>
 *   <li>Mutual exclusion across JVM instances sharing the same Redis cluster.</li>
 *   <li>Automatic lease renewal so long-running actions do not lose the lock mid-execution.</li>
 *   <li>Atomic release via Lua script — ownership is verified before deletion.</li>
 *   <li>Immediate rejection of lock attempts when Redis is unreachable
 *       (see {@link RedisUnavailableException}).</li>
 * </ul>
 * <p>{@link LockPurpose#DEFAULT} must <strong>never</strong> be passed as a call-site purpose;
 * it is a sentinel for the fallback configuration profile.
 */
public interface DistributedLockManager {

    /**
     * Executes {@code action} while holding the distributed lock identified by {@code (purpose, lockKey)}. Blocks until the
     * lock is acquired or the purpose-configured wait timeout elapses.
     *
     * @param purpose the lock purpose resolving configuration; must not be {@link LockPurpose#DEFAULT}
     * @param lockKey the business key for the lock (e.g. a requisition ID); must not be blank
     * @param action  the action to execute under the lock; must not be null
     * @param <T>     the return type of the action
     * @return the value returned by {@code action}
     * @throws LockAcquisitionException  if the lock cannot be acquired within the configured wait timeout or the thread is
     *                                   interrupted
     * @throws RedisUnavailableException if Redis is currently unreachable
     * @throws IllegalArgumentException  if {@code purpose} is {@link LockPurpose#DEFAULT} or {@code lockKey} is blank
     */
    <T> T executeWithLock(LockPurpose purpose, String lockKey, Supplier<T> action);

    /**
     * Attempts to execute {@code action} while holding the distributed lock identified by {@code (purpose, lockKey)}.
     * Returns {@link Optional#empty()} immediately — without retrying — if the lock is currently held by another owner.
     *
     * @param purpose the lock purpose resolving configuration; must not be {@link LockPurpose#DEFAULT}
     * @param lockKey the business key for the lock; must not be blank
     * @param action  the action to execute under the lock; must not be null
     * @param <T>     the return type of the action
     * @return the wrapped result of {@code action}, or {@link Optional#empty()} if the lock was already held
     * @throws RedisUnavailableException if Redis is currently unreachable
     * @throws IllegalArgumentException  if {@code purpose} is {@link LockPurpose#DEFAULT} or {@code lockKey} is blank
     */
    <T> Optional<T> tryWithLock(LockPurpose purpose, String lockKey, Supplier<T> action);

    /**
     * Convenience overload of {@link #executeWithLock(LockPurpose, String, Supplier)} for fire-and-forget {@link Runnable}
     * actions that do not return a value.
     */
    default void executeWithLock(LockPurpose purpose, String lockKey, Runnable action) {
        executeWithLock(purpose, lockKey, () -> {
            action.run();
            return null;
        });
    }
}
