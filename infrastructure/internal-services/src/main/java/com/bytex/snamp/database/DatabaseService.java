package com.bytex.snamp.database;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface DatabaseService {

    /**
     * Restarts database.
     */
    void restart() throws IOException;

    /**
     * Sets a new configuration.
     * @param input Reader of configuration content.
     * @throws IOException Unable to parse new configuration.
     */
    void setupConfiguration(final Reader input) throws IOException;

    void readConfiguration(final Writer output) throws IOException;
}
