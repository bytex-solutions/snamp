package com.bytex.snamp.gateway.modeling;

import com.bytex.snamp.connector.FeatureModifiedEvent;
import com.bytex.snamp.connector.operations.OperationSupport;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.management.MBeanOperationInfo;

/**
 * Exposes access to the individual operation.
 * @author Evgeniy Kirichenko
 * @since 1.0
 * @version 2.1
 */
public abstract class OperationAccessor extends FeatureAccessor<MBeanOperationInfo> {
    private OperationSupport operationSupport;

    /**
     * Initializes a new managed resource notification accessor.
     * @param metadata The metadata of the managed resource notification. Cannot be {@literal null}.
     */
    protected OperationAccessor(final MBeanOperationInfo metadata) {
        super(metadata);
        this.operationSupport = null;
    }

    public OperationSupport getOperationSupport() {
        return operationSupport;
    }


    private void connect(final OperationSupport value) {
        this.operationSupport = value;
    }

    /**
     * Determines whether the feature of the managed resource is accessible
     * through this object.
     *
     * @return {@literal true}, if this feature is accessible; otherwise, {@literal false}.
     */
    @Override
    public final boolean isConnected() {
        return operationSupport != null;
    }

    /**
     * Disconnects notification accessor from the managed resource.
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void close() {
        this.operationSupport = null;
    }

    public static int removeAll(final Iterable<? extends OperationAccessor> notifications,
                             final MBeanOperationInfo metadata){
        return FeatureAccessor.removeAll(notifications, metadata);
    }

    public static <N extends OperationAccessor> N remove(final Iterable<N> attributes,
                                                         final MBeanOperationInfo metadata){
        return FeatureAccessor.remove(attributes, metadata);
    }

    @Override
    public final boolean processEvent(final FeatureModifiedEvent<MBeanOperationInfo> event) {
        assert event.getSource() instanceof OperationSupport;
        switch (event.getModifier()){
            case ADDED:
                connect((OperationSupport)event.getSource());
                return true;
            case REMOVING:
                close();
                return true;
            default:
                return false;
        }
    }
}
