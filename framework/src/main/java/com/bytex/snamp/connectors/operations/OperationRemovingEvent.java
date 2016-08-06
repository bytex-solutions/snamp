package com.bytex.snamp.connectors.operations;

import com.bytex.snamp.connectors.FeatureRemovingEvent;

import javax.management.MBeanOperationInfo;

/**
 * @author Roman Sakno
 * @version 1.2
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
