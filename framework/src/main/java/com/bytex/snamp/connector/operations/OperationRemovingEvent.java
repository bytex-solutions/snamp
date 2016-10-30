package com.bytex.snamp.connector.operations;

import com.bytex.snamp.connector.FeatureRemovingEvent;

import javax.management.MBeanOperationInfo;

/**
 * @author Roman Sakno
 * @version 2.0
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
        assert source instanceof OperationSupport : source;
        return (OperationSupport) source;
    }
}