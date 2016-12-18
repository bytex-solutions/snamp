package com.bytex.snamp.cluster;

import com.bytex.snamp.core.DistributedService;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class HazelcastDistributedService implements DistributedService {
    @Override
    public boolean isPersistent() {
        return false;
    }
}
