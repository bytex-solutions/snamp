package com.bytex.snamp.management.jmx;

import com.bytex.snamp.jmx.OpenMBean;
import com.bytex.snamp.jmx.OpenMBeanParameterInfoSupplier;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import java.util.Map;

/**
 * Represents management operation on SNAMP Gateway.
 * @since 2.0
 * @version 2.0
 */
abstract class AbstractGatewayOperation extends OpenMBean.OpenOperation<Void, SimpleType<Void>> {
    private static final OpenMBeanParameterInfoSupplier<String> GATEWAY_TYPE_PARAM = new OpenMBeanParameterInfoSupplier<>(
            "gatewayType",
            "Type of gateway",
            SimpleType.STRING,
            false
    );

    AbstractGatewayOperation(final String operationName) throws OpenDataException {
        super(operationName, SimpleType.VOID, GATEWAY_TYPE_PARAM);
    }

    abstract void invoke(final String gatewayType) throws Exception;

    @Override
    public final Void invoke(final Map<String, ?> arguments) throws Exception {
        invoke(GATEWAY_TYPE_PARAM.getArgument(arguments));
        return null;
    }
}
