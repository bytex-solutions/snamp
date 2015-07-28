package com.bytex.snamp.jmx.json;

import com.google.gson.GsonBuilder;

import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.TabularData;
import java.nio.*;

/**
 * Represents a single entry point for all formatters provided by this library.
 * @author Roman Sakno
 */
public class Formatters {
    private Formatters(){

    }

    /**
     * Registers all possible formatters for {@link Buffer} class and it derivatives.
     * @param builder The GSON builder to modify.
     * @return The modified GSON builder.
     */
    public static GsonBuilder enableBufferSupport(final GsonBuilder builder){
        return builder
                .registerTypeHierarchyAdapter(ByteBuffer.class, new ByteBufferFormatter())
                .registerTypeHierarchyAdapter(CharBuffer.class, new CharBufferFormatter())
                .registerTypeHierarchyAdapter(ShortBuffer.class, new ShortBufferFormatter())
                .registerTypeHierarchyAdapter(IntBuffer.class, new IntBufferFormatter())
                .registerTypeHierarchyAdapter(LongBuffer.class, new LongBufferFormatter())
                .registerTypeHierarchyAdapter(FloatBuffer.class, new FloatBufferFormatter())
                .registerTypeHierarchyAdapter(DoubleBuffer.class, new DoubleBufferFormatter());
    }

    /**
     * Registers GSON formatter for {@link CompositeData}, {@link TabularData} and {@link ObjectName} JMX-specific types.
     * @param builder The GSON builder to modify.
     * @return The modified GSON builder.
     */
    public static GsonBuilder enableOpenTypeSystemSupport(final GsonBuilder builder){
        return builder
                .registerTypeHierarchyAdapter(CompositeData.class, new CompositeDataFormatter())
                .registerTypeAdapter(ObjectName.class, new ObjectNameFormatter())
                .registerTypeHierarchyAdapter(TabularData.class, new TabularDataFormatter());
    }

    /**
     * Registers advanced formatter for {@link Notification} and {@link OpenType} (include it derivatives).
     * @param builder The GSON builder to modify.
     * @return The modified GSON builder.
     */
    public static GsonBuilder enableMiscJmxSupport(final GsonBuilder builder){
        return builder
                .registerTypeHierarchyAdapter(Notification.class, new NotificationSerializer())
                .registerTypeHierarchyAdapter(OpenType.class, new OpenTypeFormatter());
    }

    /**
     * Registers all available serializers/deserializers in this library.
     * @param builder The GSON builder to modify.
     * @return The modified GSON builder.
     */
    public static GsonBuilder enableAll(final GsonBuilder builder){
        return enableBufferSupport(enableOpenTypeSystemSupport(enableMiscJmxSupport(builder)));
    }
}
