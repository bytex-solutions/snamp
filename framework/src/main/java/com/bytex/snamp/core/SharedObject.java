package com.bytex.snamp.core;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents object which can be shared across cluster nodes.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public interface SharedObject {
    /**
     * Represents identifier of shared object.
     * @param <S> Type of shared object.
     */
    @Immutable
    abstract class ID<S extends SharedObject> implements Serializable {
        private static final long serialVersionUID = -8433588386775375036L;

        /**
         * Name of shared object.
         */
        public final String name;

        protected ID(final String name) {
            this.name = Objects.requireNonNull(name);
        }

        @Override
        public String toString() {
            final Class<?> outer = getClass().getEnclosingClass();
            return outer == null ? name : outer.getSimpleName() + ':' + name;
        }

        @Override
        public int hashCode() {
            return Objects.hash(getClass(), name);
        }

        private boolean equals(final ID other) {
            return name.equals(other.name);
        }

        @Override
        public boolean equals(final Object other) {
            return getClass().isInstance(other) && equals(getClass().cast(other));
        }

        /**
         * Creates default implementation of the shared object.
         * @return Default implementation of the shared object.
         */
        @Nonnull
        protected abstract S createDefaultImplementation();
    }

    /**
     * Gets name of the distributed service.
     * @return Name of this distributed service.
     */
    String getName();
}
