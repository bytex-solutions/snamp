package com.itworks.snamp.management.webconsole;

import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.SelectableAdapterParameterDescriptor;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescription;
import com.itworks.snamp.configuration.diff.ConfigurationDiffEngine;
import com.itworks.snamp.connectors.SelectableConnectorParameterDescriptor;
import com.itworks.snamp.connectors.discovery.DiscoveryService;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;

/**
 * Represents JSON serializer and deserializer for SNAMP configuration.
 * <p>
 *     The following JSON shows example of serialized SNAMP configuration:
 *     <pre><it>
 *         {
 *             resourceAdapters:{
 *                  "adapter-instance":{
 *                      name: "snmp",   //REQUIRED
 *                      host: "localhost",  //USER-DEFINED
 *                      port: 12897     //USER-DEFINED
 *                  }
 *             },
 *             managedResources:{
 *                  "javaee-server-1":{
 *                      connectionType: "jmx",        //REQUIRED
 *                      connectionString: "service:jmx:rmi:///jndi/rmi://localhost:3331/jmxrmi", //REQUIRED
 *                      "additional-option": 1276,  //USER-DEFINED
 *                      attributes:{                              //REQUIRED
 *                          "fault-count":{
 *                              name: "faultCount",                         //REQUIRED
 *                              readWriteTimeout: 1000, //optional field!!!
 *                              objectName: "java.lang:type=Memory"             //USER-DEFINED
 *                              oid: "1.1.0.0"
 *                          }
 *                      },
 *                      events:{
 *                          "my-event":{
 *                              category: "javax.management.AttributeChange",    //REQUIRED
 *                              filter: "(severity=panic)"                       //USER-DEFINED
 *                          }
 *                      }
 *                  }
 *             }
 *         }
 *     </it></pre>
 * </p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JsonAgentConfiguration {
    private static final String RESOURCE_ADAPTERS_SECTION = "resourceAdapters";
    private static final String RESOURCES_SECTION = "managedResources";
    private static final String ADAPTER_NAME_FIELD = "name";
    private static final String CONNECTION_TYPE_FIELD = "connectionType";
    private static final String CONNECTION_STRING_FIELD = "connectionString";
    private static final String ATTRIBUTES_SECTION = "attributes";
    private static final String EVENTS_SECTION = "events";
    private static final String READ_WRITE_TIMEOUT_FIELD = "readWriteTimeout";
    private static final String ATTRIBUTE_NAME_FIELD = "name";
    private static final String EVENT_CATEGORY_FIELD = "category";
    private static final String USER_DEFINED_PROPERTIES = "additionalProperties";

    private JsonAgentConfiguration(){

    }

    private static JsonObject readResourceAdapter(final ResourceAdapterConfiguration adapterConfig){
        final JsonObject result = new JsonObject();
        result.addProperty(ADAPTER_NAME_FIELD, adapterConfig.getAdapterName());
        if (!adapterConfig.getParameters().keySet().isEmpty()) {
            final JsonObject objectMap = new JsonObject();

            for (final String propertyName : adapterConfig.getParameters().keySet())
                objectMap.addProperty(propertyName, adapterConfig.getParameters().get(propertyName));

            result.add(USER_DEFINED_PROPERTIES, objectMap);
        }
        return result;
    }

    private static JsonObject readResourceAdapters(final Map<String, ResourceAdapterConfiguration> adapters){
        final JsonObject result = new JsonObject();
        for(final String adapterUserDefinedName: adapters.keySet())
            result.add(adapterUserDefinedName, readResourceAdapter(adapters.get(adapterUserDefinedName)));
        return result;
    }

    private static JsonObject readAttribute(final AttributeConfiguration attribute){
        final JsonObject result = new JsonObject();
        result.addProperty(ATTRIBUTE_NAME_FIELD, attribute.getAttributeName());
        if(attribute.getReadWriteTimeout() != TimeSpan.INFINITE)
        result.addProperty(READ_WRITE_TIMEOUT_FIELD,  attribute.getReadWriteTimeout().convert(TimeUnit.MILLISECONDS).duration);
        //read other properties
        if (!attribute.getParameters().keySet().isEmpty())
        {
            final JsonObject objectMap = new JsonObject();

            for (final String parameter : attribute.getParameters().keySet())
                objectMap.addProperty(parameter, attribute.getParameters().get(parameter));

            result.add(USER_DEFINED_PROPERTIES, objectMap);
        }
        return result;
    }

    private static JsonObject readAttributes(final Map<String, AttributeConfiguration> attributes){
        final JsonObject result = new JsonObject();
        if(attributes != null)
            for(final String attributeName: attributes.keySet())
                result.add(attributeName, readAttribute(attributes.get(attributeName)));
        return result;
    }

    private static JsonObject readEvent(final EventConfiguration event){
        final JsonObject result = new JsonObject();
        result.addProperty(EVENT_CATEGORY_FIELD, event.getCategory());
        //add other parameters
        if (!event.getParameters().keySet().isEmpty())
        {
            final JsonObject objectMap = new JsonObject();

            for (final String parameter : event.getParameters().keySet())
                objectMap.addProperty(parameter, event.getParameters().get(parameter));

             result.add(USER_DEFINED_PROPERTIES, objectMap);
        }
        return result;
    }

    private static JsonObject readEvents(final Map<String, EventConfiguration> events){
        final JsonObject result = new JsonObject();
        if(events != null)
            for(final String eventName: events.keySet())
                result.add(eventName, readEvent(events.get(eventName)));
        return result;
    }

    private static JsonObject readManagedResource(final ManagedResourceConfiguration resource){
        final JsonObject result = new JsonObject();
        result.addProperty(CONNECTION_TYPE_FIELD, resource.getConnectionType());
        result.addProperty(CONNECTION_STRING_FIELD, resource.getConnectionString());
        //add other properties
        if (!resource.getParameters().keySet().isEmpty())
        {
            final JsonObject objectMap = new JsonObject();

            for (final String propertyName : resource.getParameters().keySet())
                objectMap.addProperty(propertyName, resource.getParameters().get(propertyName));

            result.add(USER_DEFINED_PROPERTIES, objectMap);
        }
        //add attributes
        result.add(ATTRIBUTES_SECTION, readAttributes(resource.getElements(AttributeConfiguration.class)));
        //add events
        result.add(EVENTS_SECTION, readEvents(resource.getElements(EventConfiguration.class)));
        return result;
    }

    private static JsonObject readManagedResources(final Map<String, ManagedResourceConfiguration> resources){
        final JsonObject result = new JsonObject();
        for(final String resourceName: resources.keySet())
            result.add(resourceName, readManagedResource(resources.get(resourceName)));
        return result;
    }

    /**
     * Serializes SNAMP configuration into JSON.
     * @param config The configuration to serialize.
     * @return JSON representation of the SNAMP configuration.
     */
    public static JsonElement read(final AgentConfiguration config){
        final JsonObject result = new JsonObject();
        result.add(RESOURCE_ADAPTERS_SECTION, readResourceAdapters(config.getResourceAdapters()));
        result.add(RESOURCES_SECTION, readManagedResources(config.getManagedResources()));
        return result;
    }

    private static String toString(final JsonElement value, final String defaultValue){
        return value != null && value.isJsonPrimitive() ? value.getAsString() : defaultValue;
    }

    private static TimeSpan toTimeSpan(final JsonElement value){
        return value != null && value.isJsonPrimitive() ? new TimeSpan(value.getAsLong()) : TimeSpan.INFINITE;
    }

    private static ResourceAdapterConfiguration deserializeResourceAdapter(final JsonObject source, final Supplier<ResourceAdapterConfiguration> configFactory){
        final ResourceAdapterConfiguration adapter = configFactory.get();
        adapter.setAdapterName(toString(source.remove(ADAPTER_NAME_FIELD), ""));
        //deserialize other properties
        for(final Map.Entry<String, JsonElement> entry: source.entrySet())
            adapter.getParameters().put(entry.getKey(), entry.getValue().getAsString());
        return adapter;
    }

    private static void writeResourceAdapters(final JsonObject source,
                                              final Map<String, ResourceAdapterConfiguration> dest,
                                              final Supplier<ResourceAdapterConfiguration> configFactory){
        for(final Map.Entry<String, JsonElement> entry: source.entrySet())
            if(entry.getValue().isJsonObject())
            dest.put(entry.getKey(), deserializeResourceAdapter(entry.getValue().getAsJsonObject(), configFactory));
    }

    private static void parseResourceAdapters(final JsonElement source,
                                              final Map<String, ResourceAdapterConfiguration> dest,
                                              final Supplier<ResourceAdapterConfiguration> configFactory){
        if (source != null && source.isJsonObject() && dest != null) {
            writeResourceAdapters(source.getAsJsonObject(), dest, configFactory);
        }
    }

    private static AttributeConfiguration deserializeAttribute(final JsonObject source, final Supplier<AttributeConfiguration> configFactory){
        final AttributeConfiguration attribute = configFactory.get();
        attribute.setAttributeName(toString(source.remove(ATTRIBUTE_NAME_FIELD), ""));
        attribute.setReadWriteTimeout(source.has(READ_WRITE_TIMEOUT_FIELD) ?
                toTimeSpan(source.remove(READ_WRITE_TIMEOUT_FIELD)) : TimeSpan.INFINITE);
        for(final Map.Entry<String, JsonElement> entry: source.entrySet())
            if(entry.getValue().isJsonPrimitive())
                attribute.getParameters().put(entry.getKey(), entry.getValue().getAsString());
        return attribute;
    }

    private static EventConfiguration deserializeEvent(final JsonObject source, final Supplier<EventConfiguration> configFactory){
        final EventConfiguration event = configFactory.get();
        event.setCategory(toString(source.remove(EVENT_CATEGORY_FIELD), "unknown"));
        for(final Map.Entry<String, JsonElement> entry: source.entrySet())
            if(entry.getValue().isJsonPrimitive())
                event.getParameters().put(entry.getKey(), entry.getValue().getAsString());
        return event;
    }

    private static void deserializeAttributes(final JsonObject source, final Map<String, AttributeConfiguration> dest, final Supplier<AttributeConfiguration> configFactory){
        if(dest == null) return;
        for(final Map.Entry<String, JsonElement> entry: source.entrySet())
            if(entry.getValue().isJsonObject())
                dest.put(entry.getKey(), deserializeAttribute(entry.getValue().getAsJsonObject(), configFactory));
    }

    private static void deserializeEvents(final JsonObject source, final Map<String, EventConfiguration> dest, final Supplier<EventConfiguration> configFactory){
        if(dest == null) return;
        for(final Map.Entry<String, JsonElement> entry: source.entrySet())
            if(entry.getValue().isJsonObject())
                dest.put(entry.getKey(), deserializeEvent(entry.getValue().getAsJsonObject(), configFactory));
    }

    private static ManagedResourceConfiguration deserializeResourceConnector(final JsonObject source,
                                                                             final Supplier<ManagedResourceConfiguration> configFactory){
        final ManagedResourceConfiguration resource = configFactory.get();
        resource.setConnectionString(toString(source.remove(CONNECTION_STRING_FIELD), ""));
        resource.setConnectionType(toString(source.remove(CONNECTION_TYPE_FIELD), "unknown"));
        final JsonElement attributes = source.remove(ATTRIBUTES_SECTION);
        final JsonElement events = source.remove(EVENTS_SECTION);
        for(final Map.Entry<String, JsonElement> entry: source.entrySet())
            if(entry.getValue().isJsonPrimitive())
                resource.getParameters().put(entry.getKey(), entry.getValue().getAsString());
        if(attributes != null && attributes.isJsonObject())
            deserializeAttributes(attributes.getAsJsonObject(), resource.getElements(AttributeConfiguration.class), new Supplier<AttributeConfiguration>(){
                @Override
                public AttributeConfiguration get() {
                    return resource.newElement(AttributeConfiguration.class);
                }
            });
        if(events != null && events.isJsonObject())
            deserializeEvents(events.getAsJsonObject(), resource.getElements(EventConfiguration.class), new Supplier<EventConfiguration>(){
                @Override
                public EventConfiguration get() {
                    return resource.newElement(EventConfiguration.class);
                }
            });
        return resource;
    }

    private static void writeResourceConnectors(final JsonObject source,
                                                final Map<String, ManagedResourceConfiguration> dest,
                                                final Supplier<ManagedResourceConfiguration> configFactory){
        for(final Map.Entry<String, JsonElement> entry: source.entrySet())
            dest.put(entry.getKey(), deserializeResourceConnector(entry.getValue().getAsJsonObject(), configFactory));
    }

    private static void parseResourceConnectors(final JsonElement source,
                                                final Map<String, ManagedResourceConfiguration> dest,
                                                final Supplier<ManagedResourceConfiguration> configFactory){
        if(source != null && dest != null){
            writeResourceConnectors(source.getAsJsonObject(), dest, configFactory);
        }
    }

    private static void write(final JsonObject source, final AgentConfiguration baseline){
        final AgentConfiguration target = baseline.clone();
        target.clear();
        parseResourceAdapters(source.get(RESOURCE_ADAPTERS_SECTION), target.getResourceAdapters(), new Supplier<ResourceAdapterConfiguration>() {
            @Override
            public ResourceAdapterConfiguration get() {
                return baseline.newConfigurationEntity(ResourceAdapterConfiguration.class);
            }
        });
        parseResourceConnectors(source.get(RESOURCES_SECTION), target.getManagedResources(), new Supplier<ManagedResourceConfiguration>() {
            @Override
            public ManagedResourceConfiguration get() {
                return baseline.newConfigurationEntity(ManagedResourceConfiguration.class);
            }
        });
        ConfigurationDiffEngine.merge(target, baseline);
    }

    /**
     * Deserializes SNAMP configuration from JSON.
     * @param source The configuration to deserialize.
     * @param dest The configuration to change.
     */
    public static void write(final JsonElement source, final AgentConfiguration dest){
        if (source != null && dest != null) {
            write(source.getAsJsonObject(), dest);
        }
    }

    private static JsonArray toJsonArray(final String[] values) {
        final JsonArray result = new JsonArray();
        if (values != null)
            for (final String value : values) result.add(new JsonPrimitive(value));
        return result;
    }

    private static Map<String, String> toMap(final Map<String, JsonElement> m){
        return Maps.transformEntries(m, new Maps.EntryTransformer<String, JsonElement, String>() {
            @Override
            public String transformEntry(final String key, final JsonElement value) {
                return JsonAgentConfiguration.toString(value, "");
            }
        });
    }

    private static JsonArray getSuggestedValues(final SelectableAdapterParameterDescriptor adapterParameter,
                                             final Map<String, JsonElement> adapterOptions,
                                             final Locale loc) throws Exception{
        return toJsonArray(adapterParameter.suggestValues(toMap(adapterOptions), loc));
    }

    public static JsonArray getSuggestedValues(final ConfigurationEntityDescription.ParameterDescription adapterParameter,
                                            final Map<String, JsonElement> adapterOptions,
                                            final Locale loc) throws Exception {
        if (adapterParameter == null)
            return new JsonArray();
        else if (adapterParameter instanceof SelectableAdapterParameterDescriptor)
            return getSuggestedValues((SelectableAdapterParameterDescriptor) adapterParameter,
                    adapterOptions, loc);
        else return new JsonArray();
    }

    private static JsonArray getSuggestedValues(final SelectableConnectorParameterDescriptor connectorParameter,
                                                final JsonPrimitive connectionString,
                                                final Map<String, JsonElement> connectionOptions,
                                                final Locale loc) throws Exception {
        return toJsonArray(connectorParameter.suggestValues(connectionString.getAsString(),
                toMap(connectionOptions),
                loc));
    }

    public static JsonArray getSuggestedValues(final ConfigurationEntityDescription.ParameterDescription connectorParameter,
                                               final JsonElement connectionString,
                                               final Map<String, JsonElement> connectionOptions,
                                               final Locale loc) throws Exception{
        if (connectorParameter == null)
            return new JsonArray();
        else if (connectorParameter instanceof SelectableConnectorParameterDescriptor)
            return getSuggestedValues((SelectableConnectorParameterDescriptor) connectorParameter,
                    connectionString.getAsJsonPrimitive(),
                    connectionOptions,
                    loc);
        else return new JsonArray();
    }

    private static JsonArray getAttributes(final Collection<AttributeConfiguration> attributes){
        final JsonArray result = new JsonArray();
        for(final AttributeConfiguration attr: attributes)
            result.add(readAttribute(attr));
        return result;
    }

    private static JsonArray getEvents(final Collection<EventConfiguration> events){
        final JsonArray result = new JsonArray();
        for(final EventConfiguration ev: events)
            result.add(readEvent(ev));
        return result;
    }

    public static JsonObject discoverManagementMetadata(final DiscoveryService discovery,
                                                        final JsonElement connectionString,
                                                        final Map<String, JsonElement> connectionOptions){
        @SuppressWarnings("unchecked")
        final DiscoveryService.DiscoveryResult metadata = discovery.discover(
                toString(connectionString, ""),
                toMap(connectionOptions),
                AttributeConfiguration.class,
                EventConfiguration.class);
        final JsonObject result = new JsonObject();
        result.add("attributes", getAttributes(metadata.getSubResult(AttributeConfiguration.class)));
        result.add("events", getEvents(metadata.getSubResult(EventConfiguration.class)));
        return result;
    }
}