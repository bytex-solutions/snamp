package com.itworks.snamp.connectors.operations;

import com.itworks.snamp.connectors.FeatureRemovingEvent;
import com.itworks.snamp.internal.Utils;

import javax.management.MBeanOperationInfo;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class OperationRemovingEvent extends FeatureRemovingEvent<MBeanOperationInfo> {
    private static final long serialVersionUID = -3696035917789862315L;

    public OperationRemovingEvent(final OperationSupport sender,
                                  final String resourceName,
                                  final MBeanOperationInfo feature) {
        super(sender, resourceName, feature);
    }

    /**
     * The object on which the Event initially occurred.
     *
     * @return The object on which the Event initially occurred.
     */
    @Override
    public final OperationSupport getSource() {
        return Utils.safeCast(source, OperationSupport.class);
    }
}
