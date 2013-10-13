package com.snamp;

import java.io.*;

/**
 * Represents a serializable object.
 * @author roman
 */
public interface BinarySerializable {
    /**
     * Saves the current configuration into the specified stream.
     * @param output
     * @throws UnsupportedOperationException Serialization is not supported.
     * @throws java.io.IOException Cannot write to the specified stream.
     */
    public void save(final OutputStream output) throws IOException;

    /**
     * Reads the file and fills the current instance.
     * @param input
     * @throws UnsupportedOperationException Deserialization is not supported.
     * @throws IOException Cannot read from the specified stream.
     */
    public void load(final InputStream input) throws IOException;
}
