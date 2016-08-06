package com.bytex.snamp.adapters.ssh;

import javax.management.JMException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * Represents form of the attribute suitable for printing via text-based streams.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface SshAttributeMapping {
    /**
     * Prints attribute value.
     * @param output An output stream that accepts the attribute value.
     * @param format The format of the attribute value.
     * @throws JMException Unable to read attribute value.
     */
    void printValue(final Writer output, final AttributeValueFormat format) throws JMException, IOException;

    /**
     * Prints attribute value.
     * @param input An input stream that contains attribute
     * @throws JMException
     */
    void setValue(final Reader input) throws JMException, IOException;

    /**
     * Prints attribute options.
     * @param output An output stream that accepts the attribute options.
     */
    void printOptions(final Writer output) throws IOException;

    /**
     * Gets name of the attribute.
     * @return The name of the attribute.
     */
    String getName();

    String getOriginalName();

    /**
     * Determines whether the attribute can supply value.
     * @return {@literal true}, if the attribute can supply value.
     */
    boolean canRead();

    /**
     * Determines whether the attribute can be changed.
     * @return {@literal true}, if attribute can be changed; otherwise, {@literal false}.
     */
    boolean canWrite();
}
