package com.bytex.snamp.core;

import com.bytex.snamp.concurrent.LazyReference;

import javax.annotation.Nonnull;
import java.lang.management.ManagementFactory;

/**
 * Represents information about local cluster member.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class LocalMember {

    private static final class InMemoryCounterRepository extends AbstractSharedObjectRepository<InMemoryCounter> {
        private static final long serialVersionUID = 3523031113716415788L;

        @Nonnull
        @Override
        protected InMemoryCounter createSharedObject(final String name) {
            return new InMemoryCounter(name);
        }
    }

    private static final class InMemoryBoxRepository extends AbstractSharedObjectRepository<InMemoryBox> {
        private static final long serialVersionUID = 8067873729210156851L;

        @Nonnull
        @Override
        protected InMemoryBox createSharedObject(final String name) {
            return new InMemoryBox(name);
        }
    }

    private static final class InMemoryCommunicatorRepository extends AbstractSharedObjectRepository<InMemoryCommunicator> {
        private static final long serialVersionUID = 2054285825771659407L;

        @Nonnull
        @Override
        protected InMemoryCommunicator createSharedObject(final String name) {
            return new InMemoryCommunicator(name);
        }
    }

    private static final class InMemoryKeyValueStorageRepository extends AbstractSharedObjectRepository<InMemoryKeyValueStorage> {
        private static final long serialVersionUID = -1811858060915829040L;

        @Nonnull
        @Override
        protected InMemoryKeyValueStorage createSharedObject(final String name) {
            return new InMemoryKeyValueStorage(name);
        }
    }

    private static final LazyReference<InMemoryCounterRepository> COUNTERS = LazyReference.soft();
    private static final LazyReference<InMemoryBoxRepository> BOXES = LazyReference.soft();
    private static final LazyReference<InMemoryCommunicatorRepository> COMMUNICATORS = LazyReference.soft();
    private static final LazyReference<InMemoryKeyValueStorageRepository> STORES = LazyReference.soft();

    private LocalMember(){
        throw new InstantiationError();
    }

    static InMemoryKeyValueStorageRepository getNonPersistentStores(){
        return STORES.get(InMemoryKeyValueStorageRepository::new);
    }

    static InMemoryCounterRepository getCounters() {
        return COUNTERS.get(InMemoryCounterRepository::new);
    }

    static InMemoryBoxRepository getBoxes(){
        return BOXES.get(InMemoryBoxRepository::new);
    }

    static InMemoryCommunicatorRepository getCommunicators(){
        return COMMUNICATORS.get(InMemoryCommunicatorRepository::new);
    }

    static String getName() {
        return ManagementFactory.getRuntimeMXBean().getName();
    }
}
