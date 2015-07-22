package com.itworks.snamp.management.impl;


import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.itworks.snamp.adapters.ResourceAdapter;
import com.itworks.snamp.adapters.ResourceAdapterClient;
import com.itworks.snamp.internal.RecordReader;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.TabularDataBuilderRowFill;
import org.osgi.framework.BundleContext;

import javax.management.JMException;
import javax.management.MBeanFeatureInfo;
import javax.management.openmbean.*;

import java.util.Map;
import java.util.Objects;

import static com.itworks.snamp.jmx.OpenMBean.OpenOperation;
import static com.itworks.snamp.adapters.ResourceAdapter.FeatureBindingInfo;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractBindingInfoOperation<F extends MBeanFeatureInfo> extends OpenOperation<TabularData, TabularType> {
    private static final Joiner.MapJoiner DETAILS_JOINER = Joiner.on(';').withKeyValueSeparator("=");
    protected static final String RESOURCE_NAME_COLUMN = "resourceName";
    protected static final String DETAILS_COLUMN = "details";
    protected static final OpenMBeanParameterInfoSupport INSTANCE_NAME_PARAM = new OpenMBeanParameterInfoSupport(
            "instanceName",
            "The name of the adapter instance",
            SimpleType.STRING
    );

    private final Class<F> featureType;

    protected AbstractBindingInfoOperation(final String operationName,
                                           final TabularType returnType,
                                           final Class<F> featureType){
        super(operationName, returnType, INSTANCE_NAME_PARAM);
        this.featureType = Objects.requireNonNull(featureType);
    }

    private static String convertToString(final FeatureBindingInfo<?> bindingInfo){
        final Map<String, Object> params = Maps.newHashMapWithExpectedSize(bindingInfo.getProperties().size());
        for(final String propertyName: bindingInfo.getProperties()) {
            final Object propertyValue = bindingInfo.getProperty(propertyName);
            if (propertyValue != null)
                params.put(propertyName, bindingInfo.getProperty(propertyName));
        }
        return DETAILS_JOINER.join(params);
    }

    protected abstract void fillRow(final TabularDataBuilderRowFill.RowBuilder row,
                                    final FeatureBindingInfo<F> bindingInfo) throws OpenDataException;

    private TabularData invoke(final BundleContext context,
                                     final String instanceName) throws JMException {
        final ResourceAdapterClient client = new ResourceAdapterClient(context, instanceName);
        try {
            final TabularDataBuilderRowFill result = new TabularDataBuilderRowFill(returnType);
            client.forEachFeature(featureType, new RecordReader<String, FeatureBindingInfo<F>, OpenDataException>() {
                @Override
                public boolean read(final String resourceName, final FeatureBindingInfo<F> bindingInfo) throws OpenDataException {
                    final TabularDataBuilderRowFill.RowBuilder row = result.newRow()
                            .cell(RESOURCE_NAME_COLUMN, resourceName)
                            .cell(DETAILS_COLUMN, convertToString(bindingInfo));
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
        return invoke(Utils.getBundleContextByObject(this),
                getArgument(INSTANCE_NAME_PARAM.getName(), String.class, arguments));
    }
}
