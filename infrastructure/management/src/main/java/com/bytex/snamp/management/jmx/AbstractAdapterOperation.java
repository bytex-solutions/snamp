package com.bytex.snamp.management.jmx;

import com.bytex.snamp.jmx.OpenMBean;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import java.util.Map;

/**
 * Represents management operation on SNAMP Adapter.
 * @since 2.0
 * @version 2.0
 */
abstract class AbstractAdapterOperation extends OpenMBean.OpenOperation<Void, SimpleType<Void>> {
    /**
     * The ADAPTER name param.
     */
    private static final TypedParameterInfo<String> ADAPTER_NAME_PARAM = new TypedParameterInfo<>(
            "gatewayType",
            "The name of the managed resource adapter",
            SimpleType.STRING,
            false
    );

    AbstractAdapterOperation(final String operationName) throws OpenDataException {
        super(operationName, SimpleType.VOID, ADAPTER_NAME_PARAM);
    }

    abstract void invoke(final String adapterInstance) throws Exception;

    @Override
    public final Void invoke(Map<String, ?> arguments) throws Exception {
        invoke(ADAPTER_NAME_PARAM.getArgument(arguments));
        return null;
    }
}
