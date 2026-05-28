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
import lombok.extern.slf4j.Slf4j;

/**
 * No-operation {@link DistributedLockManager} registered automatically when Redis locking is
 * disabled ({@code fhir.odoo.lock.redis.enabled=false}).
 *
 * <p>Actions are executed directly — no lock is acquired, no Redis connection is required.
 * This allows the application to start and operate without Redis in environments where distributed
 * mutual exclusion is not needed. To enable real locking, set
 * {@code fhir.odoo.lock.redis.enabled=true}.
 *
 * <p><strong>Warning:</strong> this implementation provides <em>no</em> mutual exclusion. It must
 * not be used in multi-instance deployments where concurrent access to shared resources must be
 * serialized.
 */
@Slf4j
public class NoOpDistributedLockManager implements DistributedLockManager {

    public NoOpDistributedLockManager() {
        log.warn("Redis distributed locking is DISABLED. Actions will execute without any mutual exclusion. "
                + "Set fhir.odoo.lock.redis.enabled=true to enable Redis-backed locking.");
    }

    @Override
    public <T> T executeWithLock(LockPurpose purpose, String lockKey, Supplier<T> action) {
        log.debug("NoOp lock: executing action for purpose={} key={} without acquiring a lock", purpose, lockKey);
        return action.get();
    }

    @Override
    public <T> Optional<T> tryWithLock(LockPurpose purpose, String lockKey, Supplier<T> action) {
        log.debug(
                "NoOp lock: executing tryWithLock action for purpose={} key={} without acquiring a lock",
                purpose,
                lockKey);
        return Optional.ofNullable(action.get());
    }
}
