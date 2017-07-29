package com.bytex.snamp.moa.topology;

import com.bytex.snamp.instrumentation.measurements.Span;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializableWithType;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;

import java.io.IOException;
import java.util.Objects;

/**
 * Represents identifier of the vertex.
 * <p>
 *     This identifier is constructed from component name and name of its internal module.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class ComponentVertexIdentity implements JsonSerializableWithType {
    private final String componentName;
    private final String moduleName;

    ComponentVertexIdentity(final Span span){
        this(span.getComponentName(), span.getModuleName());
    }

    ComponentVertexIdentity(final String cname, final String mname){
        componentName = Objects.requireNonNull(cname);
        moduleName = Objects.requireNonNull(mname);
    }

    String getComponentName(){
        return componentName;
    }

    String getModuleName(){
        return moduleName;
    }

    boolean represents(final Span span){
        return span.getComponentName().equals(componentName) && span.getModuleName().equals(moduleName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(componentName, moduleName);
    }

    private boolean equals(final ComponentVertexIdentity other){
        return other.componentName.equals(componentName) && other.moduleName.equals(moduleName);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof ComponentVertexIdentity && equals((ComponentVertexIdentity) other);
    }

    @Override
    public String toString() {
        return moduleName.isEmpty() ? componentName : componentName + '/' + moduleName;
    }

    @Override
    public void serializeWithType(final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException {
        serialize(jgen, provider);
    }

    @Override
    public void serialize(final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
        jgen.writeString(toString());
    }
}
