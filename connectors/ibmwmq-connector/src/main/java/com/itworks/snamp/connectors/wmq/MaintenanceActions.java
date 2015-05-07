package com.itworks.snamp.connectors.wmq;

import com.itworks.snamp.internal.annotations.SpecialUse;
import com.itworks.snamp.management.MaintenanceActionInfo;

import java.util.Locale;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
enum MaintenanceActions implements MaintenanceActionInfo {
    @SpecialUse
    MQ_INSTALLED {

        @Override
        public String getDescription(final Locale locale) {
            return "Determines whether WebSphere MQ installed into OSGi correctly";
        }

        @Override
        public String getName() {
            return "isWmqInstalled";
        }
    }
}
