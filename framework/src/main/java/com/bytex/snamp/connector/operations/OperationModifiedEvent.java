package com.bytex.snamp.connector.operations;

import com.bytex.snamp.connector.FeatureModifiedEvent;

import javax.management.MBeanOperationInfo;

/**
 * Indicates that the operation provided by managed resource was modified.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class OperationModifiedEvent extends FeatureModifiedEvent<MBeanOperationInfo> {
    private static final long serialVersionUID = 188103517392485039L;

    protected OperationModifiedEvent(final OperationSupport sender, final String resourceName, final MBeanOperationInfo feature, final Modifier type) {
        super(sender, resourceName, feature, type);
    }

    /**
     * The object on which the Event initially occurred.
     *
     * @return The object on which the Event initially occurred.
     */
    @Override
    public final OperationSupport getSource() {
        return (OperationSupport) super.getSource();
    }

    public static OperationModifiedEvent operationAdded(final OperationSupport sender, final String resourceName, final MBeanOperationInfo feature){
        return new OperationModifiedEvent(sender, resourceName, feature, Modifier.ADDED);
    }

    public static OperationModifiedEvent operationRemoving(final OperationSupport sender, final String resourceName, final MBeanOperationInfo feature){
        return new OperationModifiedEvent(sender, resourceName, feature, Modifier.REMOVING);
    }
}
