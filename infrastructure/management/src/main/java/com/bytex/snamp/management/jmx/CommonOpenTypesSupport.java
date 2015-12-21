package com.bytex.snamp.management.jmx;

import com.google.common.base.Supplier;
import com.bytex.snamp.jmx.CompositeTypeBuilder;
import com.bytex.snamp.jmx.TabularTypeBuilder;

import javax.management.MBeanFeatureInfo;
import javax.management.openmbean.*;
import java.util.Locale;
import java.util.concurrent.Callable;
import static com.bytex.snamp.internal.Utils.interfaceStaticInitialize;
import static com.bytex.snamp.jmx.OpenMBean.OpenOperation.TypedParameterInfo;

/**
 * Description here
 *
 * @author Evgeniy Kirichenko
 */
interface CommonOpenTypesSupport<T extends MBeanFeatureInfo> extends Supplier<T> {

    /**
     * The LOCALE parameter.
     */
    TypedParameterInfo<String> LOCALE_PARAM = new TypedParameterInfo<>(
            "locale",
            "The expected localization of the configuration schema",
            SimpleType.STRING,
            true,
            Locale.getDefault().toLanguageTag());

    /**
     * The CONNECTOR name param.
     */
    TypedParameterInfo<String> CONNECTOR_NAME_PARAM = new TypedParameterInfo<>(
            "connectorName",
            "Snamp connector name",
            SimpleType.STRING,
            false);

    /**
     * The ADAPTER name param.
     */
    TypedParameterInfo<String> ADAPTER_NAME_PARAM = new TypedParameterInfo<>(
            "adapterName",
            "The name of the managed resource adapter",
            SimpleType.STRING,
            false
    );

    /**
     * The constant CONNECTION_PARAMS_SCHEMA.
     */
    TabularType SIMPLE_MAP_TYPE = interfaceStaticInitialize(
            new Callable<TabularType>() {
        @Override
        public TabularType call() throws OpenDataException {
            return new TabularTypeBuilder("com.bytex.management.map",
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
            return new CompositeTypeBuilder("com.bytex.management.EventMetadata", "SNAMP Connector Event Metadata")
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
                    return new CompositeTypeBuilder("com.bytex.management.AttributeMetadata", "SNAMP connector attribute metadata scheme")
                            .addItem("ReadWriteTimeout", "Read write timeout for connector attribute", SimpleType.LONG)
                            .addItem("AdditionalProperties", "User defined property for attribute", SIMPLE_MAP_TYPE);
                }
            });

}
