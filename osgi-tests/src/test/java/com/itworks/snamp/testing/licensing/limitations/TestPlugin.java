package com.itworks.snamp.testing.licensing.limitations;

import com.itworks.snamp.core.AbstractFrameworkService;

import java.util.logging.Logger;

public final class TestPlugin extends AbstractFrameworkService {

    /**
     * Initializes a new instance of the platform service.
     */
    protected TestPlugin() {
    }

    /**
     * Gets a logger associated with this platform service.
     *
     * @return A logger associated with this platform service.
     */
    @Override
    public Logger getLogger() {
        return Logger.getAnonymousLogger();
    }
}
