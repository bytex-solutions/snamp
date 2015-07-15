package com.itworks.snamp.management.impl;

import com.google.common.collect.Maps;
import com.itworks.snamp.connectors.ManagedResourceConnectorClient;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.DescriptorUtils;
import com.itworks.snamp.jmx.KeyValueTypeBuilder;
import com.itworks.snamp.jmx.TabularDataUtils;
import org.osgi.framework.BundleContext;

import javax.management.Descriptor;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanInfo;
import javax.management.openmbean.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

import static com.itworks.snamp.jmx.OpenMBean.OpenOperation;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class AvailableFeaturesOperation<F extends MBeanFeatureInfo> extends OpenOperation<TabularData, TabularType> {
    private static final TabularType ENTRY_TYPE = Utils.interfaceStaticInitialize(new Callable<TabularType>() {
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
    private static final TabularType RETURN_TYPE = Utils.interfaceStaticInitialize(new Callable<TabularType>() {
        @Override
        public TabularType call() throws OpenDataException {
            return new KeyValueTypeBuilder<String, TabularData>()
                    .setTypeName("FeatureSet")
                    .setTypeDescription("Set of resource features")
                    .setKeyColumn("name", "Feature name", SimpleType.STRING)
                    .setValueColumn("parameters", "Configuration parameters", ENTRY_TYPE)
                    .build();
        }
    });
    private static final OpenMBeanParameterInfoSupport RESOURCE_NAME_PARAM =
            new OpenMBeanParameterInfoSupport("resourceName", "Name of the resource", SimpleType.STRING);

    protected AvailableFeaturesOperation(final String operationName) {
        super(operationName, RETURN_TYPE, RESOURCE_NAME_PARAM);
    }

    /**
     * Gets description of this attribute.
     *
     * @return The description of this attribute.
     */
    @Override
    protected final String getDescription() {
        return "Set of resource features";
    }

    protected abstract Collection<F> extractFeatures(final MBeanInfo metadata);

    private static Map<String, String> toMap(final Descriptor descr){
        if(descr == null) return Collections.emptyMap();
        final String[] fields = descr.getFieldNames();
        final Map<String, String> result = Maps.newHashMapWithExpectedSize(fields.length);
        for(final String fieldName: fields){
            final Object fieldValue = descr.getFieldValue(fieldName);
            if(fieldValue == null) continue;
            else result.put(fieldName, Objects.toString(fieldValue));
        }
        return result;
    }

    private TabularData invoke(final String resourceName) throws InstanceNotFoundException, OpenDataException {
        final BundleContext context = Utils.getBundleContextByObject(this);
        final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(context, resourceName);
        final Collection<F> features;
        try {
            features = extractFeatures(client.getMBeanInfo());
        } finally {
            client.release(context);
        }
        final Map<String, TabularData> result = Maps.newHashMapWithExpectedSize(features.size());
        for (final MBeanFeatureInfo f : features) {
            final TabularData parameters = TabularDataUtils.makeKeyValuePairs(ENTRY_TYPE, toMap(f.getDescriptor()));
            result.put(f.getName(), parameters);
        }
        return TabularDataUtils.makeKeyValuePairs(RETURN_TYPE, result);
    }

    @Override
    public final TabularData invoke(final Map<String, ?> arguments) throws InstanceNotFoundException, OpenDataException {
        return invoke(getArgument(RESOURCE_NAME_PARAM.getName(), String.class, arguments));
    }
}
