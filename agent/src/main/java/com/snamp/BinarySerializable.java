package com.snamp;

import java.io.*;

/**
 * Represents an object that can store/restore its internal state from the stream.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public interface BinarySerializable {
    /**
     * Dumps the state of this object into the specified stream.
     * @param output An output stream for writing object's internal state.
     * @throws UnsupportedOperationException Serialization is not supported.
     * @throws IOException Cannot write to the specified stream.
     */
    public void save(final OutputStream output) throws IOException;

    /**
     * Restores the state of this object from the specified stream.
     * @param input An input streams that contains the serialized state of this object.
     * @throws UnsupportedOperationException Deserialization is not supported.
     * @throws IOException Cannot invoke from the specified stream.
     */
    public void load(final InputStream input) throws IOException;
}
