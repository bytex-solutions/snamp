package com.snamp.connectors.util;

import com.snamp.*;
import com.snamp.connectors.*;
import static com.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration;
import com.snamp.internal.Internal;
import com.snamp.internal.MethodThreadSafety;
import com.snamp.internal.ThreadSafety;

import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * Represents a map of exposed attributes to the adapter.
 * <p>This is an utility class and can be used to organize connected attributes
 * in some types of custom adapters.</p>
 * @param <TAttributeDescriptor> Type of the attribute descriptor.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Internal
public abstract class ConnectedAttributes<TAttributeDescriptor> extends HashMap<String, TAttributeDescriptor> {
    protected final AttributeSupport connector;

    protected ConnectedAttributes(final AttributeSupport connector){
        if(connector == null) throw new IllegalArgumentException("connector is null.");
        this.connector = connector;
    }

    Object getAttribute(final String id, final TimeSpan readTimeout, final Object defaultValue) throws TimeoutException{
        return connector.getAttribute(id, readTimeout, defaultValue);
    }

    boolean setAttribute(final String id, final TimeSpan writeTimeout, final Object value) throws TimeoutException{
        return connector.setAttribute(id, writeTimeout, value);
    }

    public final AttributeMetadata getAttributeInfo(final String prefix, final String postfix){
        return connector.getAttributeInfo(makeAttributeId(prefix, postfix));
    }

    public final ManagementEntityType getAttributeType(final String prefix, final String postfix){
        final AttributeMetadata meta = getAttributeInfo(prefix, postfix);
        return meta != null ? meta.getType() : null;
    }

    /**
     * Constructs a new attribute identifier based on its namespace and postfix.
     * @param prefix The attribute namespace (namespace in management target configuration).
     * @param postfix The attribute postfix (id in attribute configuration).
     * @return A new combination of the attribute namespace and postfix.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public abstract String makeAttributeId(final String prefix, final String postfix);

    /**
     * Creates a new attribute description.
     * @param prefix The attribute namespace (namespace in management target configuration).
     * @param postfix
     * @param config
     * @return
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public abstract TAttributeDescriptor createDescription(final String prefix, final String postfix, final AttributeConfiguration config);

    /**
     * Disconnects all attributes associated with the specified namespace.
     * @param prefix The namespace of the attributes to disconnect.
     */
    public final void disconnect(final String prefix) {
        for(final String postfix: keySet())
            connector.disconnectAttribute(makeAttributeId(prefix, postfix));
    }
}
