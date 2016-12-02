package com.bytex.snamp.instrumentation;

import org.codehaus.jackson.annotate.*;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents abstract POJO for all measurements.
 * @since 1.0
 * @version 1.0
 * @author Roman Sakno
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(IntegerMeasurement.class),
        @JsonSubTypes.Type(FloatingPointMeasurement.class),
        @JsonSubTypes.Type(StringMeasurement.class),
        @JsonSubTypes.Type(BooleanMeasurement.class),
        @JsonSubTypes.Type(TimeMeasurement.class),
        @JsonSubTypes.Type(Span.class)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Measurement implements Externalizable {
    private static final class ObjectMapperHolder{
        private static final ObjectMapper INSTANCE = new ObjectMapper();
    }

    static final String VALUE_JSON_PROPERTY = "v";
    private static final long serialVersionUID = -5122847206545823797L;
    private static final ComponentNameSource[] COMPONENT_NAME_SOURCES = ComponentNameSource.values();   //cache array of performance purposes
    private static final ComponentInstanceSource[] COMPONENT_INSTANCE_SOURCES = ComponentInstanceSource.values();

    private static final String MESSAGE_FIELD = "message";
    private String instanceName;
    private String componentName;
    private long timestamp;
    private final LinkedHashMap<String, String> userData;
    private String name;

    Measurement(){
        timestamp = System.currentTimeMillis();
        userData = new LinkedHashMap<String, String>();
        instanceName = name = "";
        componentName = getDefaultComponentName();
    }

    public final String getName(){
        return name;
    }

    @JsonProperty("n")
    public void setName(final String value){
        if(name == null)
            throw new IllegalArgumentException();
        else
            name = value;
    }

    @JsonIgnore
    public final String getMessage(final String defaultMessage){
        final String message = userData.get(MESSAGE_FIELD);
        return message == null || message.isEmpty() ? defaultMessage : message;
    }

    @JsonProperty("data")
    public final Map<String, String> getUserData(){
        return userData;
    }

    public final void setUserData(final Map<String, String> value){
        userData.clear();
        userData.putAll(value);
    }

    /**
     * Fills user data from system properties passed to this JVM.
     */
    public final void useSystemPropertiesAsUserData(){
        for(final String propertyName: System.getProperties().stringPropertyNames()){
            userData.put(propertyName, System.getProperty(propertyName));
        }
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeUTF(name);
        out.writeUTF(instanceName);
        out.writeUTF(componentName);
        out.writeLong(timestamp);
        //save user data
        out.writeInt(userData.size());
        for(final Map.Entry<String, String> entry: userData.entrySet()){
            out.writeUTF(entry.getKey());
            out.writeUTF(entry.getValue());
        }
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        name = in.readUTF();
        instanceName = in.readUTF();
        componentName = in.readUTF();
        timestamp = in.readLong();
        //load user data
        for (int size = in.readInt(); size > 0; size--)
            userData.put(in.readUTF(), in.readUTF());
    }

    /**
     * Gets default instance name based on IP address.
     * @return Instance name based on IP address.
     */
    public static String getDefaultInstanceName() {
        for (final ComponentInstanceSource source : COMPONENT_INSTANCE_SOURCES) {
            final String name = source.getInstance();
            if (name != null && !name.isEmpty())
                return name;
        }
        return "";
    }

    public static String getDefaultComponentName() {
        for (final ComponentNameSource source : COMPONENT_NAME_SOURCES) {
            final String name = source.getName();
            if (name != null && !name.isEmpty())
                return name;
        }
        return "";
    }

    @JsonIgnore
    public static String toJsonString(final Measurement... measurements) throws IOException {
        final StringWriter writer = new StringWriter();
        try {
            ObjectMapperHolder.INSTANCE.writeValue(writer, measurements);
            return writer.toString();
        } finally {
            writer.close();
        }
    }

    @JsonIgnore
    public final String toJsonString() throws IOException {
        final StringWriter writer = new StringWriter();
        try {
            ObjectMapperHolder.INSTANCE.writeValue(writer, this);
            return writer.toString();
        } finally {
            writer.close();
        }
    }

    /**
     * Gets name of the component acting as a source for this measurement.
     * @return Name of the component.
     */
    @JsonProperty("c")
    public final String getComponentName(){
        return componentName;
    }

    public final void setDefaultComponentName(){
        setComponentName(getDefaultComponentName());
    }

    public final void setDefaultInstanceName(){
        setInstanceName(getDefaultInstanceName());
    }

    /**
     * Sets name of the component acting as a source for this measurement.
     * @param value Name of the component.
     */
    public final void setComponentName(final String value){
        componentName = value;
    }

    @JsonProperty("i")
    public final String getInstanceName(){
        return instanceName;
    }

    public final void setInstanceName(final String value){
        instanceName = value;
    }

    /**
     * Gets timestamp of this measurement.
     * @return Timestamp of this measurement, in milliseconds.
     */
    @JsonProperty("t")
    public final long getTimeStamp(){
        return timestamp;
    }

    public final void setTimeStamp(final long value){
        timestamp = value;
    }

    @JsonIgnore
    public final void setTimeStamp(final Date value){
        setTimeStamp(value.getTime());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' +
                "instanceName='" + instanceName + '\'' +
                ", componentName='" + componentName + '\'' +
                ", timestamp=" + timestamp +
                ", userData=" + userData +
                '}';
    }
}
