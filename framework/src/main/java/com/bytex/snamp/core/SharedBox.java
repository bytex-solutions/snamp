package com.bytex.snamp.core;

import com.bytex.snamp.Box;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.io.Serializable;

/**
 * Represents {@link Box} that can be shared across cluster.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public interface SharedBox extends Box<Serializable>, SharedObject {
    /**
     * Represents identifier of shared box.
     */
    @Immutable
    final class ID extends SharedObject.ID<SharedBox> {
        private static final long serialVersionUID = -2877318724504150595L;

        private ID(final String name) {
            super(name);
        }

        @Nonnull
        @Override
        protected InMemoryBox createDefaultImplementation() {
            return new InMemoryBox(name);
        }
    }

    /**
     * Creates identifier of shared box based on its name.
     * @param name Name of shared box.
     * @return Identifier of shared box.
     */
    static ID ofName(final String name) {
        return new ID(name);
    }
}
