package com.bytex.snamp.instrumentation.measurements;

import com.bytex.snamp.instrumentation.ApplicationInfo;
import org.codehaus.jackson.annotate.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

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

        static ObjectWriter getWriter(final boolean prettyPrint) {
            return prettyPrint ? INSTANCE.writerWithDefaultPrettyPrinter() : INSTANCE.writer();
        }
    }

    public static final String DESCRIPTION_ANNOTATION = "description";
    static final String VALUE_JSON_PROPERTY = "v";
    private static final long serialVersionUID = -5122847206545823797L;

    private static final String MESSAGE_FIELD = "message";
    private String instanceName;
    private String componentName;
    private long timestamp;
    private final LinkedHashMap<String, String> annotations;
    private String name;

    Measurement(){
        timestamp = System.currentTimeMillis();
        annotations = new LinkedHashMap<>();
        name = "";
        componentName = ApplicationInfo.getName();
        instanceName = ApplicationInfo.getInstance();
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
        final String message = annotations.get(MESSAGE_FIELD);
        return message == null || message.isEmpty() ? defaultMessage : message;
    }

    @JsonProperty("annotations")
    public final Map<String, String> getAnnotations(){
        return annotations;
    }

    public final void setAnnotations(final Map<String, String> value){
        annotations.clear();
        addAnnotations(value);
    }

    public final void addAnnotations(final Map<String, String> value){
        annotations.putAll(value);
    }

    public final void addAnnotation(final String name, final String value){
        annotations.put(name, value);
    }

    @JsonIgnore
    public final void setDescription(final String description){
        addAnnotation(DESCRIPTION_ANNOTATION, description);
    }

    public final String getDescription(){
        return annotations.get(DESCRIPTION_ANNOTATION);
    }

    /**
     * Fills user data from system properties passed to this JVM.
     */
    public final void useSystemPropertiesAsUserData(){
        for(final String propertyName: System.getProperties().stringPropertyNames()){
            annotations.put(propertyName, System.getProperty(propertyName));
        }
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeUTF(name);
        out.writeUTF(instanceName);
        out.writeUTF(componentName);
        out.writeLong(timestamp);
        //save user data
        out.writeInt(annotations.size());
        for(final Map.Entry<String, String> entry: annotations.entrySet()){
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
            annotations.put(in.readUTF(), in.readUTF());
    }

    @JsonIgnore
    public static String toJsonString(final boolean prettyPrint, final Measurement... measurements) throws IOException {
        return ObjectMapperHolder.getWriter(prettyPrint).writeValueAsString(measurements);
    }

    @JsonIgnore
    public final String toJsonString(final boolean prettyPrint) throws IOException {
        return ObjectMapperHolder.getWriter(prettyPrint).writeValueAsString(this);
    }

    /**
     * Gets name of the component acting as a source for this measurement.
     * @return Name of the component.
     */
    @JsonProperty("c")
    public final String getComponentName(){
        return componentName;
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
                ", annotations=" + annotations +
                '}';
    }
}
