package com.itworks.snamp.connectors.wmq;

import com.itworks.snamp.internal.annotations.SpecialUse;
import com.itworks.snamp.management.AbstractMaintainable;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class MQConnectorMaintainer extends AbstractMaintainable<MaintenanceActions> {

    public MQConnectorMaintainer(){
        super(MaintenanceActions.class);
    }

    @Override
    protected Object[] parseArguments(final MaintenanceActions action,
                                      final String arguments,
                                      final Locale loc) {
        return new Object[0];
    }

    static boolean isWmqInstalledImpl(){
        try {
            return Class.forName("com.ibm.mq.constants.CMQCFC") != null;
        } catch (final ClassNotFoundException ignored) {
            return false;
        }
    }

    @SpecialUse
    public boolean isWmqInstalled() {
        return isWmqInstalledImpl();
    }

    @Override
    public Logger getLogger() {
        return Logger.getLogger(getClass().getName());
    }
}
