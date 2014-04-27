package com.itworks.snamp.connectors.util;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.connectors.AttributeMetadata;
import com.itworks.snamp.connectors.AttributeSupport;
import com.itworks.snamp.connectors.ManagementEntityType;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration;
import com.itworks.snamp.internal.Internal;
import com.itworks.snamp.internal.MethodThreadSafety;
import com.itworks.snamp.internal.ThreadSafety;

import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * Represents a map of exposed managementAttributes to the adapter.
 * <p>This is an utility class and can be used to organize connected managementAttributes
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
     * Disconnects all managementAttributes associated with the specified namespace.
     * @param prefix The namespace of the managementAttributes to disconnect.
     */
    public final void disconnect(final String prefix) {
        for(final String postfix: keySet())
            connector.disconnectAttribute(makeAttributeId(prefix, postfix));
    }
}
