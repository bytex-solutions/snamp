package com.bytex.snamp.management.jmx;

import com.bytex.snamp.jmx.OpenMBean;
import com.bytex.snamp.jmx.OpenMBeanParameterInfoSupplier;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import java.util.Map;

/**
 * Represents management operation for SNAMP Connector.
 * @since 2.0
 * @version 2.1
 */
abstract class AbstractConnectorOperation extends OpenMBean.OpenOperation<Void, SimpleType<Void>> {
    /**
     * The CONNECTOR name param.
     */
    private static final OpenMBeanParameterInfoSupplier<String> RESOURCE_NAME_PARAM = new OpenMBeanParameterInfoSupplier<>(
            "resourceName",
            "Name of managed resource",
            SimpleType.STRING,
            false);

    AbstractConnectorOperation(final String operationName) throws OpenDataException {
        super(operationName, SimpleType.VOID, RESOURCE_NAME_PARAM);
    }

    abstract void invoke(final String resourceName) throws Exception;

    @Override
    public final Void invoke(final Map<String, ?> arguments) throws Exception {
        invoke(RESOURCE_NAME_PARAM.getArgument(arguments));
        return null;
    }
}
