package com.itworks.snamp.licensing;

import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.core.OSGiLoggingContext;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class LicenseLogger {
    private static final String LOGGER_NAME = "com.itworks.snamp.licensing";

    private LicenseLogger(){

    }

    static void error(final String message, final Exception e){
        OSGiLoggingContext.within(LOGGER_NAME, new SafeConsumer<Logger>() {
            @Override
            public void accept(final Logger logger) {
                logger.log(Level.SEVERE, message, e);
            }
        });
    }
}
