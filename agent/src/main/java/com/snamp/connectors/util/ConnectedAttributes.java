package com.snamp.connectors.util;

import com.snamp.*;
import com.snamp.connectors.*;
import com.snamp.internal.Internal;
import com.snamp.internal.MethodThreadSafety;
import com.snamp.internal.ThreadSafety;

import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * Represents a map of exposed attributes to the adapter.
 * <p>This is an utility class and can be used to organize connected attributes
 * in some types of custom adapters.</p>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Internal
public abstract class ConnectedAttributes extends HashMap<String, AttributeMetadata> {
    private final AttributeSupport connector;

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

    /**
     * Constructs a new attribute identifier based on its namespace and postfix.
     * @param prefix The attribute namespace (namespace in management target configuration).
     * @param postfix The attribute postfix (id in attribute configuration).
     * @return A new combination of the attribute namespace and postfix.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public abstract String makeAttributeId(final String prefix, final String postfix);
}
