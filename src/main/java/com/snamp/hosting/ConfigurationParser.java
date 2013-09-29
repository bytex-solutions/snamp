package com.snamp.hosting;

import java.io.InputStream;

/**
 * Represents configuration parser.
 * @author roman
 */
interface ConfigurationParser {
    /**
     * Reads configuration from the stream.
     * @param stream The stream that contains configuration of the agent in serialized form.
     * @return The object representation of the configuration.
     */
    public AgentConfiguration parse(final InputStream stream);
}
