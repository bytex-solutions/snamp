package com.snamp.hosting;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents configuration parser.
 * @author roman
 */
interface ConfigurationParser {
    /**
     * Reads configuration from the stream.
     * @param input The stream that contains configuration of the agent in serialized form.
     * @return The object representation of the configuration.
     */
    public AgentConfiguration parse(final InputStream input);

    /**
     * Saves the configuration file back to the stream.
     * @param configuration The configuration to save.
     * @param output The output stream.
     */
    public void save(final AgentConfiguration configuration, final OutputStream output) throws IOException;
}
