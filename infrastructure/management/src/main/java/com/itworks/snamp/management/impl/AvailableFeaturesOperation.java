package com.itworks.snamp.management.impl;

import com.google.common.collect.Maps;
import com.itworks.snamp.connectors.ManagedResourceConnectorClient;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.KeyValueTypeBuilder;
import com.itworks.snamp.jmx.TabularDataUtils;
import com.itworks.snamp.jmx.WellKnownType;
import org.osgi.framework.BundleContext;

import javax.management.*;
import javax.management.openmbean.*;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

import static com.itworks.snamp.jmx.OpenMBean.OpenOperation;
import static com.itworks.snamp.jmx.DescriptorUtils.toMap;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class AvailableFeaturesOperation<F extends MBeanFeatureInfo> extends OpenOperation<TabularData, TabularType> {
    protected static final TabularType DESCRIPTOR_TYPE = Utils.interfaceStaticInitialize(new Callable<TabularType>() {
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

    private static final OpenMBeanParameterInfoSupport RESOURCE_NAME_PARAM =
            new OpenMBeanParameterInfoSupport("resourceName", "Name of the resource", SimpleType.STRING);

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
        return TabularDataUtils.makeKeyValuePairs(DESCRIPTOR_TYPE, toMap(read.getDescriptor(), true));
    }

    protected abstract TabularData invoke(final MBeanInfo metadata) throws OpenDataException;

    private TabularData invoke(final String resourceName) throws InstanceNotFoundException, OpenDataException {
        final BundleContext context = Utils.getBundleContextByObject(this);
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
        return invoke(getArgument(RESOURCE_NAME_PARAM.getName(), String.class, arguments));
    }
}
