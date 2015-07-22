package com.itworks.snamp.management.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.adapters.ResourceAdapter;
import com.itworks.snamp.adapters.ResourceAdapterClient;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.internal.RecordReader;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.TabularDataBuilderRowFill;
import com.itworks.snamp.jmx.TabularTypeBuilder;
import org.osgi.framework.BundleContext;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

import static com.itworks.snamp.jmx.OpenMBean.OpenOperation;
import static com.itworks.snamp.adapters.ResourceAdapter.FeatureBindingInfo;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class GetBindingOfAttributesOperation extends AbstractBindingInfoOperation<MBeanAttributeInfo> {
    private static final String NAME = "getBindingOfAttributes";

    private static final String USER_DEFINED_NAME_COLUMN = "userDefinedName";
    private static final String NAME_COLUMN = "name";
    private static final String MAPPED_TYPE_COLUMN = "mappedType";

    private static final TabularType RETURN_TYPE = Utils.interfaceStaticInitialize(new Callable<TabularType>() {
        @Override
        public TabularType call() throws OpenDataException {
            return new TabularTypeBuilder()
                    .setTypeName("BindingOfAttributes", true)
                    .setDescription("A set of exposed attributes", true)
                    .addColumn(RESOURCE_NAME_COLUMN, "The name of the connected resource", SimpleType.STRING, true)
                    .addColumn(USER_DEFINED_NAME_COLUMN, "The name of the attribute specified by administrator", SimpleType.STRING, true)
                    .addColumn(NAME_COLUMN, "The name of the attribute declared by the connected resource", SimpleType.STRING, false)
                    .addColumn(MAPPED_TYPE_COLUMN, "Adapter-specific type of the attribute", SimpleType.STRING, false)
                    .addColumn(DETAILS_COLUMN, "Binding details", SimpleType.STRING, false)
                    .build();
        }
    });

    GetBindingOfAttributesOperation(){
        super(NAME, RETURN_TYPE, MBeanAttributeInfo.class);
    }

    @Override
    protected void fillRow(final TabularDataBuilderRowFill.RowBuilder row,
                           final FeatureBindingInfo<MBeanAttributeInfo> bindingInfo) throws OpenDataException {
        final String mappedType = Objects.toString(bindingInfo.getProperty(FeatureBindingInfo.MAPPED_TYPE), "");
        row
                .cell(USER_DEFINED_NAME_COLUMN, bindingInfo.getMetadata().getName())
                .cell(NAME_COLUMN, AttributeDescriptor.getAttributeName(bindingInfo.getMetadata()))
                .cell(MAPPED_TYPE_COLUMN, mappedType);
    }
}
