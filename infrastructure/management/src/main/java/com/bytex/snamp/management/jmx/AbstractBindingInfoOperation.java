package com.bytex.snamp.management.jmx;


import com.google.common.collect.Maps;
import com.bytex.snamp.adapters.ResourceAdapterClient;
import com.bytex.snamp.EntryReader;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.KeyValueTypeBuilder;
import com.bytex.snamp.jmx.TabularDataBuilderRowFill;
import com.bytex.snamp.jmx.TabularDataUtils;
import org.osgi.framework.BundleContext;

import javax.management.JMException;
import javax.management.MBeanFeatureInfo;
import javax.management.openmbean.*;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

import static com.bytex.snamp.jmx.OpenMBean.OpenOperation;
import static com.bytex.snamp.adapters.ResourceAdapter.FeatureBindingInfo;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractBindingInfoOperation<F extends MBeanFeatureInfo> extends OpenOperation<TabularData, TabularType> {
    protected static final String RESOURCE_NAME_COLUMN = "resourceName";
    protected static final String DETAILS_COLUMN = "details";
    protected static final TypedParameterInfo<String> INSTANCE_NAME_PARAM = new TypedParameterInfo<>(
            "instanceName",
            "The name of the adapter instance",
            SimpleType.STRING,
            false
    );
    protected static final TabularType DETAILS_TYPE = Utils.interfaceStaticInitialize(new Callable<TabularType>() {
        @Override
        public TabularType call() throws Exception {
            return new KeyValueTypeBuilder<String, String>()
                    .setTypeName("FeatureBindingDetails")
                    .setTypeDescription("A set of configuration parameters")
                    .setKeyColumn("parameter", "Parameter name", SimpleType.STRING)
                    .setValueColumn("value", "Parameter value", SimpleType.STRING)
                    .build();
        }
    });

    private final Class<F> featureType;

    protected AbstractBindingInfoOperation(final String operationName,
                                           final TabularType returnType,
                                           final Class<F> featureType){
        super(operationName, returnType, INSTANCE_NAME_PARAM);
        this.featureType = Objects.requireNonNull(featureType);
    }

    private static TabularData toTabularData(final FeatureBindingInfo<?> bindingInfo) throws OpenDataException {
        final Map<String, String> params = Maps.newHashMapWithExpectedSize(bindingInfo.getProperties().size());
        for (final String propertyName : bindingInfo.getProperties()) {
            final Object propertyValue = bindingInfo.getProperty(propertyName);
            if (propertyValue != null)
                params.put(propertyName, propertyValue.toString());
        }
        return TabularDataUtils.makeKeyValuePairs(DETAILS_TYPE, params);
    }

    protected abstract void fillRow(final TabularDataBuilderRowFill.RowBuilder row,
                                    final FeatureBindingInfo<F> bindingInfo) throws OpenDataException;

    private TabularData invoke(final BundleContext context,
                                     final String instanceName) throws JMException {
        final ResourceAdapterClient client = new ResourceAdapterClient(context, instanceName);
        try {
            final TabularDataBuilderRowFill result = new TabularDataBuilderRowFill(returnType);
            client.forEachFeature(featureType, new EntryReader<String, FeatureBindingInfo<F>, OpenDataException>() {
                @Override
                public boolean read(final String resourceName, final FeatureBindingInfo<F> bindingInfo) throws OpenDataException {
                    final TabularDataBuilderRowFill.RowBuilder row = result.newRow()
                            .cell(RESOURCE_NAME_COLUMN, resourceName)
                            .cell(DETAILS_COLUMN, toTabularData(bindingInfo));
                    fillRow(row, bindingInfo);
                    row.flush();
                    return true;
                }
            });
            return result.get();
        }
        finally {
            client.release(context);
        }
    }

    @Override
    public final TabularData invoke(final Map<String, ?> arguments) throws JMException {
        return invoke(Utils.getBundleContextOfObject(this),
                INSTANCE_NAME_PARAM.getArgument(arguments));
    }
}
