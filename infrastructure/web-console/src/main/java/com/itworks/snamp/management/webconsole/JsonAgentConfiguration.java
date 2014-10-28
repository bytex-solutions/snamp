package com.itworks.snamp.management.webconsole;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.configuration.AgentConfiguration;
import org.apache.commons.collections4.Factory;

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

    private static ResourceAdapterConfiguration deserializeResourceAdapter(final JsonObject source, final Factory<ResourceAdapterConfiguration> configFactory){
        final ResourceAdapterConfiguration adapter = configFactory.create();
        adapter.setAdapterName(toString(source.remove(ADAPTER_NAME_FIELD), ""));
        //deserialize other properties
        for(final Map.Entry<String, JsonElement> entry: source.entrySet())
            adapter.getParameters().put(entry.getKey(), entry.getValue().getAsString());
        return adapter;
    }

    private static void writeResourceAdapters(final JsonObject source,
                                              final Map<String, ResourceAdapterConfiguration> dest,
                                              final Factory<ResourceAdapterConfiguration> configFactory){
        for(final Map.Entry<String, JsonElement> entry: source.entrySet())
            if(entry.getValue().isJsonObject())
            dest.put(entry.getKey(), deserializeResourceAdapter(entry.getValue().getAsJsonObject(), configFactory));
    }

    private static void writeResourceAdapters(final JsonElement source,
                                              final Map<String, ResourceAdapterConfiguration> dest,
                                              final Factory<ResourceAdapterConfiguration> configFactory){
        if (source != null && source.isJsonObject() && dest != null) {
            writeResourceAdapters(source.getAsJsonObject(), dest, configFactory);
        }
    }

    private static AttributeConfiguration deserializeAttribute(final JsonObject source, final Factory<AttributeConfiguration> configFactory){
        final AttributeConfiguration attribute = configFactory.create();
        attribute.setAttributeName(toString(source.remove(ATTRIBUTE_NAME_FIELD), ""));
        attribute.setReadWriteTimeout(source.has(READ_WRITE_TIMEOUT_FIELD) ?
                toTimeSpan(source.remove(READ_WRITE_TIMEOUT_FIELD)) : TimeSpan.INFINITE);
        for(final Map.Entry<String, JsonElement> entry: source.entrySet())
            if(entry.getValue().isJsonPrimitive())
                attribute.getParameters().put(entry.getKey(), entry.getValue().getAsString());
        return attribute;
    }

    private static EventConfiguration deserializeEvent(final JsonObject source, final Factory<EventConfiguration> configFactory){
        final EventConfiguration event = configFactory.create();
        event.setCategory(toString(source.remove(EVENT_CATEGORY_FIELD), "unknown"));
        for(final Map.Entry<String, JsonElement> entry: source.entrySet())
            if(entry.getValue().isJsonPrimitive())
                event.getParameters().put(entry.getKey(), entry.getValue().getAsString());
        return event;
    }

    private static void deserializeAttributes(final JsonObject source, final Map<String, AttributeConfiguration> dest, final Factory<AttributeConfiguration> configFactory){
        if(dest == null) return;
        for(final Map.Entry<String, JsonElement> entry: source.entrySet())
            if(entry.getValue().isJsonObject())
                dest.put(entry.getKey(), deserializeAttribute(entry.getValue().getAsJsonObject(), configFactory));
    }

    private static void deserializeEvents(final JsonObject source, final Map<String, EventConfiguration> dest, final Factory<EventConfiguration> configFactory){
        if(dest == null) return;
        for(final Map.Entry<String, JsonElement> entry: source.entrySet())
            if(entry.getValue().isJsonObject())
                dest.put(entry.getKey(), deserializeEvent(entry.getValue().getAsJsonObject(), configFactory));
    }

    private static ManagedResourceConfiguration deserializeResourceConnector(final JsonObject source,
                                                                             final Factory<ManagedResourceConfiguration> configFactory){
        final ManagedResourceConfiguration resource = configFactory.create();
        resource.setConnectionString(toString(source.remove(CONNECTION_STRING_FIELD), ""));
        resource.setConnectionType(toString(source.remove(CONNECTION_TYPE_FIELD), "unknown"));
        final JsonElement attributes = source.remove(ATTRIBUTES_SECTION);
        final JsonElement events = source.remove(EVENTS_SECTION);
        for(final Map.Entry<String, JsonElement> entry: source.entrySet())
            if(entry.getValue().isJsonPrimitive())
                resource.getParameters().put(entry.getKey(), entry.getValue().getAsString());
        if(attributes != null && attributes.isJsonObject())
            deserializeAttributes(attributes.getAsJsonObject(), resource.getElements(AttributeConfiguration.class), new Factory<AttributeConfiguration>(){
                @Override
                public AttributeConfiguration create() {
                    return resource.newElement(AttributeConfiguration.class);
                }
            });
        if(events != null && events.isJsonObject())
            deserializeEvents(events.getAsJsonObject(), resource.getElements(EventConfiguration.class), new Factory<EventConfiguration>(){
                @Override
                public EventConfiguration create() {
                    return resource.newElement(EventConfiguration.class);
                }
            });
        return resource;
    }

    private static void writeResourceConnectors(final JsonObject source,
                                                final Map<String, ManagedResourceConfiguration> dest,
                                                final Factory<ManagedResourceConfiguration> configFactory){
        for(final Map.Entry<String, JsonElement> entry: source.entrySet())
            dest.put(entry.getKey(), deserializeResourceConnector(entry.getValue().getAsJsonObject(), configFactory));
    }

    private static void writeResourceConnectors(final JsonElement source,
                                           final Map<String, ManagedResourceConfiguration> dest,
                                           final Factory<ManagedResourceConfiguration> configFactory){
        if(source != null && dest != null){
            writeResourceConnectors(source.getAsJsonObject(), dest, configFactory);
        }
    }

    private static void write(final JsonObject source, final AgentConfiguration dest){
        dest.clear();
        writeResourceAdapters(source.get(RESOURCE_ADAPTERS_SECTION), dest.getResourceAdapters(), new Factory<ResourceAdapterConfiguration>(){
            @Override
            public ResourceAdapterConfiguration create() {
                return dest.newConfigurationEntity(ResourceAdapterConfiguration.class);
            }
        });
        writeResourceConnectors(source.get(RESOURCES_SECTION), dest.getManagedResources(), new Factory<ManagedResourceConfiguration>(){

            @Override
            public ManagedResourceConfiguration create() {
                return dest.newConfigurationEntity(ManagedResourceConfiguration.class);
            }
        });
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
}
