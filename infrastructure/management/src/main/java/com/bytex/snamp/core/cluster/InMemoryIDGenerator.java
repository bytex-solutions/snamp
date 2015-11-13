package com.bytex.snamp.core.cluster;

import com.bytex.snamp.core.IDGenerator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class InMemoryIDGenerator extends AtomicLong implements IDGenerator {
    private static final long serialVersionUID = 6292757138298315653L;

    InMemoryIDGenerator() {
        super(0L);
    }

    @Override
    public long generateID() {
        return getAndIncrement();
    }
}
