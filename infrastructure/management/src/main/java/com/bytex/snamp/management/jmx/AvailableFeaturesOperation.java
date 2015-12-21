package com.bytex.snamp.management.jmx;

import com.bytex.snamp.connectors.ManagedResourceConnectorClient;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.KeyValueTypeBuilder;
import com.bytex.snamp.jmx.TabularDataUtils;
import org.osgi.framework.BundleContext;

import javax.management.*;
import javax.management.openmbean.*;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.bytex.snamp.jmx.OpenMBean.OpenOperation;
import static com.bytex.snamp.jmx.DescriptorUtils.toMap;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class AvailableFeaturesOperation<F extends MBeanFeatureInfo> extends OpenOperation<TabularData, TabularType> {
    protected static final TabularType PARAMETERS_TYPE = Utils.interfaceStaticInitialize(new Callable<TabularType>() {
        @Override
        public TabularType call() throws Exception {
            return new KeyValueTypeBuilder<String, String>()
                    .setTypeName("FeatureConfigParameters")
                    .setTypeDescription("A set of configuration parameters")
                    .setKeyColumn("parameter", "Parameter name", SimpleType.STRING)
                    .setValueColumn("value", "Parameter value", SimpleType.STRING)
                    .build();
        }
    });

    private static final TypedParameterInfo<String> RESOURCE_NAME_PARAM =
            new TypedParameterInfo<>("resourceName", "Name of the resource", SimpleType.STRING, false);

    protected AvailableFeaturesOperation(final String operationName,
                                         final TabularType returnType) {
        super(operationName, returnType, RESOURCE_NAME_PARAM);
    }

    /**
     * Gets description of this attribute.
     *
     * @return The description of this attribute.
     */
    @Override
    protected String getDescription() {
        return "Set of resource features";
    }

    protected static TabularData toTabularData(final DescriptorRead read) throws OpenDataException {
        return TabularDataUtils.makeKeyValuePairs(PARAMETERS_TYPE, toMap(read.getDescriptor(), String.class, true));
    }

    protected abstract TabularData invoke(final MBeanInfo metadata) throws OpenDataException;

    private TabularData invoke(final String resourceName) throws InstanceNotFoundException, OpenDataException {
        final BundleContext context = Utils.getBundleContextOfObject(this);
        final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(context, resourceName);
        final TabularData result;
        try {
            result = invoke(client.getMBeanInfo());
        } finally {
            client.release(context);
        }
        return result;
    }

    @Override
    public final TabularData invoke(final Map<String, ?> arguments) throws InstanceNotFoundException, OpenDataException {
        return invoke(RESOURCE_NAME_PARAM.getArgument(arguments));
    }
}