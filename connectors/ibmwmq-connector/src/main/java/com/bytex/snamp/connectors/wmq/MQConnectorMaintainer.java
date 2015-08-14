package com.bytex.snamp.connectors.wmq;

import com.bytex.snamp.management.AbstractMaintainable;

import java.util.Locale;
import java.util.logging.Logger;
import static com.bytex.snamp.ArrayUtils.emptyArray;

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
        return emptyArray(Object[].class);
    }

    @Action
    public static boolean isWmqInstalled() {
        try {
            return Class.forName("com.ibm.mq.constants.CMQCFC") != null;
        } catch (final ClassNotFoundException ignored) {
            return false;
        }
    }

    @Override
    public Logger getLogger() {
        return Logger.getLogger(getClass().getName());
    }
}
