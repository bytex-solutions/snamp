package com.itworks.snamp.adapters.jmx;

import com.itworks.snamp.adapters.AbstractResourceAdapter;
import com.itworks.snamp.connectors.attributes.AttributeMetadata;

import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxAdapterHelpers {
    public static final String ADAPTER_NAME = "jmx";
    private static final String JMX_ENTITY_OPTION = "jmx-compliant";

    private JmxAdapterHelpers(){

    }

    public static Logger getLogger(){
        return AbstractResourceAdapter.getLogger(ADAPTER_NAME);
    }

    public static boolean isJmxCompliantAttribute(final AttributeMetadata attr){
        return attr.containsKey(JMX_ENTITY_OPTION) && Boolean.valueOf(attr.get(JMX_ENTITY_OPTION));
    }
}
