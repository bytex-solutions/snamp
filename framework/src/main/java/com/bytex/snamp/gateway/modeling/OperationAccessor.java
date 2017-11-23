package com.bytex.snamp.gateway.modeling;

import com.bytex.snamp.connector.FeatureModifiedEvent;
import com.bytex.snamp.connector.ManagedResourceConnector;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

/**
 * Exposes access to the individual operation.
 * @author Evgeniy Kirichenko
 * @since 1.0
 * @version 2.1
 */
public abstract class OperationAccessor extends FeatureAccessor<MBeanOperationInfo> {
    private ManagedResourceConnector operationSupport;

    /**
     * Initializes a new managed resource notification accessor.
     * @param metadata The metadata of the managed resource notification. Cannot be {@literal null}.
     */
    protected OperationAccessor(final MBeanOperationInfo metadata) {
        super(metadata);
        this.operationSupport = null;
    }

    final Object invoke(final String operationName,
                        final Object[] arguments,
                        final String[] signature) throws ReflectionException, MBeanException {
        return operationSupport.invoke(operationName, arguments, signature);
    }

    private void connect(final ManagedResourceConnector value) {
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
    public final boolean processEvent(final FeatureModifiedEvent event) {
        if (event.getFeature() instanceof MBeanOperationInfo)
            switch (event.getModifier()) {
                case ADDED:
                    connect(event.getSource());
                    return true;
                case REMOVING:
                    close();
                    return true;
            }
        return false;
    }
}
