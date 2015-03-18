package com.itworks.snamp.management.impl;

import com.itworks.snamp.ExceptionalCallable;
import com.itworks.snamp.jmx.TabularTypeBuilder;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenMBeanParameterInfo;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularType;

/**
 * Description here
 *
 * @author Evgeniy Kirichenko
 */
public interface CommonOpenTypesSupport {

    /**
     * The LOCALE parameter.
     */
    static final OpenMBeanParameterInfo LOCALE_PARAM = new OpenMBeanParameterInfoSupport(
            "locale",
            "The expected localization of the configuration schema",
            SimpleType.STRING);

    /**
     * The CONNECTOR name param.
     */
    static final OpenMBeanParameterInfo CONNECTOR_NAME_PARAM = new OpenMBeanParameterInfoSupport(
            "connectorName",
            "Snamp connector name",
            SimpleType.STRING);

    /**
     * The ADAPTER name param.
     */
    static final OpenMBeanParameterInfo ADAPTER_NAME_PARAM = new OpenMBeanParameterInfoSupport(
            "adapterName",
            "The name of the managed resource adapter",
            SimpleType.STRING
    );

    /**
     * The constant CONNECTION_PARAMS_SCHEMA.
     */
    static final TabularType SIMPLE_MAP_TYPE = SnampCoreMBean.interfaceStaticInitialize(
            new ExceptionalCallable<TabularType, OpenDataException>() {
        @Override
        public TabularType call() throws OpenDataException {
            return new TabularTypeBuilder("com.itworks.management.map",
                    "Simple key-value tabular type for the common goals")
                    .addColumn("key", "Parameter key", SimpleType.STRING, true)
                    .addColumn("value", "Parameter value", SimpleType.STRING, false)
                    .build();
        }
    });

}
