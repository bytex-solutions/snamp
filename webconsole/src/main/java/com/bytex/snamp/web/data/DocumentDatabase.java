package com.bytex.snamp.web.data;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents an interface for simple document database.
 * @param <D> Type of the documents in this database.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface DocumentDatabase<D> extends Closeable {

    /**
     * Clears cache and reload all documents from the persistent storage.
     * @throws IOException Unable to reload documents from the persistent storage.
     */
    void reload() throws IOException;

    /**
     * Remove all documents from the persistent storage.
     * @throws IOException Unable to flush changes to the persistent storage.
     */
    void drop() throws IOException;

    <O> Optional<O> getDocument(final String name, final Function<? super D, ? extends O> converter) throws IOException;

    <O> Optional<O> getOrCreateDocument(final String name, final Supplier<? extends D> documentFactory, final Function<? super D, ? extends O> converter) throws IOException;

    boolean modifyDocument(final String name, final Consumer<? super D> modifier) throws IOException;

    boolean delete(final String name) throws IOException;

    boolean exists(final String name);
}
