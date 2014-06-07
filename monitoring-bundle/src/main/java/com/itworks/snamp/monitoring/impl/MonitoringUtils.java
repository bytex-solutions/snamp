package com.itworks.snamp.monitoring.impl;

import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class MonitoringUtils {
    private MonitoringUtils(){

    }

    public static Logger getLogger(){
        return Logger.getLogger("itworks.snamp.management.impl");
    }
}
