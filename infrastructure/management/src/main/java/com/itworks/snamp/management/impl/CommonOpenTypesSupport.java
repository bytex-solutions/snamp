package com.itworks.snamp.management.impl;

import com.google.common.base.Supplier;
import com.itworks.snamp.jmx.CompositeTypeBuilder;
import com.itworks.snamp.jmx.TabularTypeBuilder;

import javax.management.MBeanFeatureInfo;
import javax.management.openmbean.*;
import java.util.concurrent.Callable;
import static com.itworks.snamp.internal.Utils.interfaceStaticInitialize;

/**
 * Description here
 *
 * @author Evgeniy Kirichenko
 */
interface CommonOpenTypesSupport<T extends MBeanFeatureInfo> extends Supplier<T> {

    /**
     * The LOCALE parameter.
     */
    OpenMBeanParameterInfo LOCALE_PARAM = new OpenMBeanParameterInfoSupport(
            "locale",
            "The expected localization of the configuration schema",
            SimpleType.STRING);

    /**
     * The CONNECTOR name param.
     */
    OpenMBeanParameterInfo CONNECTOR_NAME_PARAM = new OpenMBeanParameterInfoSupport(
            "connectorName",
            "Snamp connector name",
            SimpleType.STRING);

    /**
     * The ADAPTER name param.
     */
    OpenMBeanParameterInfo ADAPTER_NAME_PARAM = new OpenMBeanParameterInfoSupport(
            "adapterName",
            "The name of the managed resource adapter",
            SimpleType.STRING
    );

    /**
     * The constant CONNECTION_PARAMS_SCHEMA.
     */
    TabularType SIMPLE_MAP_TYPE = interfaceStaticInitialize(
            new Callable<TabularType>() {
        @Override
        public TabularType call() throws OpenDataException {
            return new TabularTypeBuilder("com.itworks.management.map",
                    "Simple key-value tabular type for the common goals")
                    .addColumn("Key", "Parameter key", SimpleType.STRING, true)
                    .addColumn("Value", "Parameter value", SimpleType.STRING, false)
                    .build();
        }
    });

    /**
     * The EVENT_METADATA_BUILDER.
     */
    CompositeTypeBuilder EVENT_METADATA_BUILDER = interfaceStaticInitialize(
            new Callable<CompositeTypeBuilder>() {
        @Override
        public CompositeTypeBuilder call() throws OpenDataException {
            return new CompositeTypeBuilder("com.itworks.management.EventMetadata", "SNAMP Connector Event Metadata")
                    .addItem("Category", "Connector event category", SimpleType.STRING)
                    .addItem("AdditionalProperties", "User defined property for event", SIMPLE_MAP_TYPE);
        }
    });

    /**
     * The ATTRIBUTE_METADATA_BUILDER.
     */
    CompositeTypeBuilder ATTRIBUTE_METADATA_BUILDER = interfaceStaticInitialize(
            new Callable<CompositeTypeBuilder>() {
                @Override
                public CompositeTypeBuilder call() throws OpenDataException {
                    return new CompositeTypeBuilder("com.itworks.management.AttributeMetadata", "SNAMP connector attribute metadata scheme")
                            .addItem("Name", "Connector attribute name", SimpleType.STRING)
                            .addItem("ReadWriteTimeout", "Read write timeout for connector attribute", SimpleType.LONG)
                            .addItem("AdditionalProperties", "User defined property for attribute", SIMPLE_MAP_TYPE);
                }
            });

}
