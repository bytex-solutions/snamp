package com.bytex.snamp.connectors.operations;

import com.bytex.snamp.connectors.FeatureAddedEvent;

import javax.management.MBeanOperationInfo;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class OperationAddedEvent extends FeatureAddedEvent<MBeanOperationInfo> {
    private static final long serialVersionUID = -36307591670590019L;

    public OperationAddedEvent(final OperationSupport sender,
                               final String resourceName,
                               final MBeanOperationInfo metadata) {
        super(sender, resourceName, metadata);
    }

    /**
     * The object on which the Event initially occurred.
     *
     * @return The object on which the Event initially occurred.
     */
    @Override
    public final OperationSupport getSource() {
        assert source instanceof OperationSupport: source;
        return (OperationSupport) source;
    }
}
