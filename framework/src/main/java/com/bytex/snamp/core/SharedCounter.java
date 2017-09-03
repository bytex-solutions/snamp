package com.bytex.snamp.core;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.function.LongSupplier;

/**
 * Represents cluster-wide generator of sequence numbers.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.1
 */
public interface SharedCounter extends LongSupplier, SharedObject {
    @Immutable
    final class ID extends SharedObject.ID<SharedCounter>{
        private static final long serialVersionUID = 7569762536426502699L;

        private ID(final String name) {
            super(name);
        }

        /**
         * Creates default implementation of the shared object.
         *
         * @return Default implementation of the shared object.
         */
        @Nonnull
        @Override
        protected InMemoryCounter createDefaultImplementation() {
            return new InMemoryCounter(name);
        }
    }

    /**
     * Generates a new cluster-wide unique identifier.
     * @return A new cluster-wide unique identifier.
     */
    @Override
    long getAsLong();

    /**
     * Creates identifier of shared counter based on its name.
     * @param name Name of shared counter.
     * @return Identifier of shared counter.
     */
    static ID ofName(final String name){
        return new ID(name);
    }
}
