package com.itworks.snamp.management.impl;

import com.itworks.snamp.ServiceReferenceHolder;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.PersistentConfigurationManager;
import com.itworks.snamp.configuration.diff.ConfigurationDiffEngine;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.management.jmx.OpenMBean;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;

import javax.management.openmbean.*;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import static com.itworks.snamp.internal.Utils.getBundleContextByObject;

/**
 * Description here
 *
 * @author Evgeniy Kirichenko
 * @date 10.02.2015
 */
final class SnampConfigurationAttribute  extends OpenMBean.OpenAttribute<CompositeData, CompositeType> {

    private static final String NAME = "configuration";

    private static final CompositeType SNAMP_CONFIGURATION_DATA;
    private static final CompositeType CONNECTOR_METADATA;
    private static final CompositeType ADAPTER_METADATA;
    private static final CompositeType EVENT_METADATA;
    private static final CompositeType ATTRIBUTE_METADATA;
    private static final TabularType SIMPLE_MAP_TYPE;
    private static final TabularType ADAPTER_MAP_TYPE;
    private static final TabularType CONNECTOR_MAP_TYPE;



    static{
        try {
            SIMPLE_MAP_TYPE = new TabularType("com.itworks.management.MapType",
                    "Simple type for Map<String, String>",
                    new CompositeType("com.itworks.management.SimpleStringMap",
                            "Additional parameters for filtering suggested values",
                            new String[]{"key", "value"},
                            new String[]{"Parameter key", "Parameter value"},
                            new OpenType<?>[]{SimpleType.STRING, SimpleType.STRING}),
                    new String[]{"key"}
            );


            EVENT_METADATA = new CompositeType("com.itworks.management.EventMetadata",
                    "SNAMP Connector Event Metadata",
                    new String[]{
                            "Category",
                            "AdditionalProperties"
                    },
                    new String[]{
                            "Connector event category",
                            "User defined property for event"},
                    new OpenType<?>[]{
                            SimpleType.STRING,
                            SIMPLE_MAP_TYPE
                    }
            );

            ATTRIBUTE_METADATA = new CompositeType("com.itworks.management.AttributeMetadata",
                    "SNAMP connector attribute metadata scheme",
                    new String[]{
                            "Name",
                            "ReadWriteTimeout",
                            "AdditionalProperties"
                    },
                    new String[]{
                            "Connector attribute name",
                            "Read write timeout for connector attribute",
                            "User defined properties for attribute"},
                    new OpenType<?>[]{
                            SimpleType.STRING,
                            SimpleType.LONG,
                            SIMPLE_MAP_TYPE
                    }
            );

            //COMPONENT_CONFIG_SCHEMA
            CONNECTOR_METADATA = new CompositeType("com.itworks.management.ConnectorMetadata",
                    "SNAMP connector configuration metadata",
                    new String[]{
                            "ConnectionString",
                            "ConnectionType",
                            "Attributes",
                            "Events",
                            "Parameters"
                    },
                    new String[]{
                            "Management target connection string",
                            "Type of the management connector that is used to organize monitoring data exchange between" +
                                    " agent and the management provider",
                            "Attributes",
                            "Events",
                            "User defined properties for connector"},
                    new OpenType<?>[]{
                            SimpleType.STRING,
                            SimpleType.STRING,
                            ArrayType.getArrayType(ATTRIBUTE_METADATA),
                            ArrayType.getArrayType(EVENT_METADATA),
                            SIMPLE_MAP_TYPE
                    }
            );

            //COMPONENT_CONFIG_SCHEMA
            ADAPTER_METADATA = new CompositeType("com.itworks.management.AdapterMetadata",
                    "SNAMP adapter configuration metadata",
                    new String[]{
                            "Name",
                            "Parameters"
                    },
                    new String[]{
                            "SNAMP adapter name",
                            "Additional properties for SNAMP adapter"},
                    new OpenType<?>[]{
                            SimpleType.STRING,
                            SIMPLE_MAP_TYPE
                    }
            );

            ADAPTER_MAP_TYPE = new TabularType("com.itworks.management.AdapterMapType",
                    "Simple type for Map<String, Adapter>",
                    new CompositeType("com.itworks.management.SimpleAdapterMap",
                            "Type for holding snamp adapters configuration",
                            new String[]{"name", "adapter"},
                            new String[]{"User defined name for adapter", "Adapter instance"},
                            new OpenType<?>[]{SimpleType.STRING, ADAPTER_METADATA}),
                    new String[]{"name"}
            );

            CONNECTOR_MAP_TYPE = new TabularType("com.itworks.management.ConnectorMapType",
                    "Simple type for Map<String, Connector>",
                    new CompositeType("com.itworks.management.SimpleConnectorMap",
                            "Type for holding snamp connectors configuration",
                            new String[]{"name", "connector"},
                            new String[]{"User defined name for connector", "Connector instance"},
                            new OpenType<?>[]{SimpleType.STRING, CONNECTOR_METADATA}),
                    new String[]{"name"}
            );

            SNAMP_CONFIGURATION_DATA = new CompositeType("com.itworks.management.SnampConfiguration",
                    "SNAMP main configuration metadata",
                    new String[]{
                            "ResourceAdapters",
                            "ManagedResources"
                    },
                    new String[]{
                            "SNAMP resource adapters configuration",
                            "SNAMP managed resources configuration"},
                    new OpenType<?>[]{
                            ADAPTER_MAP_TYPE,
                            CONNECTOR_MAP_TYPE
                    }
            );
        } catch (final OpenDataException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Initializes a new attribute.
     */
    SnampConfigurationAttribute() {
        super(NAME, SNAMP_CONFIGURATION_DATA);
    }

    private static CompositeData snampConfigurationToJMX(final AgentConfiguration configuration) {
        return null; // @TODO append logic
    }

    private static AgentConfiguration JMXtoSnampConfiguration(final CompositeData data) {
        return null; // @TODO append logic
    }

    @Override
    public CompositeData getValue() throws IOException, ConfigurationException {
        final BundleContext bundleContext = getBundleContextByObject(this);
        final ServiceReferenceHolder<ConfigurationAdmin> adminRef =
                new ServiceReferenceHolder<>(bundleContext,ConfigurationAdmin.class);
        try{
            final PersistentConfigurationManager manager = new PersistentConfigurationManager(adminRef);
            final AgentConfiguration configuration = manager.getCurrentConfiguration();
            if (configuration == null) throw new ConfigurationException("configuration admin",
                    "Configuration admin does not contain appropriate SNAMP configuration");
            return snampConfigurationToJMX(configuration);
        }
        finally {
            adminRef.release(bundleContext);
        }
    }

    @Override
    public void setValue(final CompositeData data) throws IOException {
        if(data == null || data.values().size() == 0) throw new IllegalArgumentException("No valid input data received");

        final BundleContext bundleContext = getBundleContextByObject(this);
        final ServiceReferenceHolder<ConfigurationAdmin> adminRef =
                new ServiceReferenceHolder<>(bundleContext,ConfigurationAdmin.class);
        try{
            final PersistentConfigurationManager manager = new PersistentConfigurationManager(adminRef);
            ConfigurationDiffEngine.merge(manager.getCurrentConfiguration(), JMXtoSnampConfiguration(data));
            manager.save();
        }
        finally {
            adminRef.release(bundleContext);
        }
    }

    @Override
    protected String getDescription() {
        return "Main SNAMP Configuration";
    }
}
