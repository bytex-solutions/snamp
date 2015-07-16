package com.itworks.snamp.management.impl;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.CustomAttributeInfo;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.TabularDataBuilderRowFill;
import com.itworks.snamp.jmx.TabularTypeBuilder;
import com.itworks.snamp.jmx.WellKnownType;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularType;
import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class AvailableAttributesOperation extends AvailableFeaturesOperation<MBeanAttributeInfo> {
    static final String NAME = "getAvailableAttributes";

    private static final String USER_DEFINED_NAME_COLUMN = "userDefinedName";
    private static final String DESCRIPTION_COLUMN = "description";
    private static final String PARAMETERS_COLUMN = "parameters";
    private static final String TYPE_COLUMN = "type";
    private static final String READABLE_COLUMN = "readable";
    private static final String WRITABLE_COLUMN = "writable";
    private static final String NAME_COLUMN = "name";

    private static final TabularType RETURN_TYPE = Utils.interfaceStaticInitialize(new Callable<TabularType>() {
        @Override
        public TabularType call() throws OpenDataException {
            return new TabularTypeBuilder()
                    .setTypeName("AvailableAttributes", true)
                    .setDescription("A set of available attributes", true)
                    .addColumn(USER_DEFINED_NAME_COLUMN, "User-defined name of the attribute", SimpleType.STRING, true)
                    .addColumn(DESCRIPTION_COLUMN, "Description of the attribute", SimpleType.STRING, false)
                    .addColumn(PARAMETERS_COLUMN, "Configuration parameters", SimpleType.STRING, false)
                    .addColumn(TYPE_COLUMN, "Type of the attribute", SimpleType.STRING, false)
                    .addColumn(READABLE_COLUMN, "Is attribute readable?", SimpleType.BOOLEAN, false)
                    .addColumn(WRITABLE_COLUMN, "Is attribute writable?", SimpleType.BOOLEAN, false)
                    .addColumn(NAME_COLUMN, "The name of the attribute", SimpleType.STRING, false)
                    .build();
        }
    });

    AvailableAttributesOperation() {
        super(NAME, RETURN_TYPE);
    }

    private static void fillRow(final MBeanAttributeInfo attributeInfo, final TabularDataBuilderRowFill.RowBuilder row) throws OpenDataException {
        final String description = attributeInfo.getDescription();
        final String attributeName = AttributeDescriptor.getAttributeName(attributeInfo);
        final WellKnownType attributeType = CustomAttributeInfo.getType(attributeInfo);
        row
                .cell(USER_DEFINED_NAME_COLUMN, attributeInfo.getName())
                .cell(READABLE_COLUMN, attributeInfo.isReadable())
                .cell(WRITABLE_COLUMN, attributeInfo.isWritable())
                .cell(PARAMETERS_COLUMN, toTabularData(attributeInfo))
                .cell(DESCRIPTION_COLUMN, MoreObjects.firstNonNull(description, ""))
                .cell(TYPE_COLUMN, attributeType == null ? attributeInfo.getType() : attributeType.getDisplayName())
                .cell(NAME_COLUMN, MoreObjects.firstNonNull(attributeName, ""))
                .flush();
    }

    @Override
    protected TabularData invoke(final MBeanInfo metadata) throws OpenDataException{
        final TabularDataBuilderRowFill rows = new TabularDataBuilderRowFill(RETURN_TYPE);
        for(final MBeanAttributeInfo attributeInfo: metadata.getAttributes())
            fillRow(attributeInfo, rows.newRow());
        return rows.get();
    }
}
