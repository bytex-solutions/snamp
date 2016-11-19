package com.bytex.snamp.gateway.modeling;

import com.bytex.snamp.jmx.JMExceptionUtils;

import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.OperationsException;
import javax.management.ReflectionException;
import java.io.Serializable;

/**
 * Represents a collection of managed resource operations.
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
public class ResourceOperationList<TAccessor extends OperationAccessor> extends ResourceFeatureList<MBeanOperationInfo, TAccessor> implements Serializable {

    private static final long serialVersionUID = -6169374997225096776L;

    public ResourceOperationList() {
        super(10);
    }

    public final Object invoke( final String operationName, final Object[] params, final String[] signature) throws ReflectionException, MBeanException, OperationsException {
        if (containsKey(operationName)) {
            final TAccessor operationAccessor = get(operationName);
            if (!operationAccessor.isConnected()) {
                throw JMExceptionUtils.operationDisconnected(operationName);
            } else {
                return operationAccessor.getOperationSupport().invoke(operationName, params, signature);
            }
        } else {
            throw JMExceptionUtils.operationNotFound(operationName);
        }
    }

    /**
     * Gets identity of the managed resource operation.
     *
     * @param feature The managed resource feature.
     * @return The identity of the managed resource feature.
     * @see javax.management.MBeanOperationInfo#getName()
     */
    @Override
    protected String getKey(final MBeanOperationInfo feature) {
        return feature.getName();
    }
}
