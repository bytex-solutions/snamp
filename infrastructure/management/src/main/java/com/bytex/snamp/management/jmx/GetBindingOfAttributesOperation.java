package com.bytex.snamp.management.jmx;

import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.TabularDataBuilderRowFill;
import com.bytex.snamp.jmx.TabularTypeBuilder;

import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularType;
import java.util.Objects;
import java.util.concurrent.Callable;

import static com.bytex.snamp.adapters.ResourceAdapter.FeatureBindingInfo;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class GetBindingOfAttributesOperation extends AbstractBindingInfoOperation<MBeanAttributeInfo> {
    private static final String NAME = "getBindingOfAttributes";

    private static final String NAME_COLUMN = "name";
    private static final String MAPPED_TYPE_COLUMN = "mappedType";

    private static final TabularType RETURN_TYPE = Utils.interfaceStaticInitialize(new Callable<TabularType>() {
        @Override
        public TabularType call() throws OpenDataException {
            return new TabularTypeBuilder()
                    .setTypeName("BindingOfAttributes", true)
                    .setDescription("A set of exposed attributes", true)
                    .addColumn(RESOURCE_NAME_COLUMN, "The name of the connected resource", SimpleType.STRING, true)
                    .addColumn(NAME_COLUMN, "The name of the attribute declared by the connected resource", SimpleType.STRING, true)
                    .addColumn(MAPPED_TYPE_COLUMN, "Adapter-specific type of the attribute", SimpleType.STRING, false)
                    .addColumn(DETAILS_COLUMN, "Binding details", DETAILS_TYPE, false)
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
                .cell(NAME_COLUMN, bindingInfo.getMetadata().getName())
                .cell(MAPPED_TYPE_COLUMN, mappedType);
    }
}
