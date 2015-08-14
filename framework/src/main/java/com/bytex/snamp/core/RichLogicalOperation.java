package com.bytex.snamp.core;

import com.google.common.collect.ImmutableMap;
import org.osgi.framework.BundleContext;

import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Represents a rich version of logical operation that have
 * associated immutable map of parameters and uses pass-through lookup of requested parameter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class RichLogicalOperation extends LogicalOperation {
    private final ImmutableMap<String, ?> properties;

    protected RichLogicalOperation(final Logger logger,
                                   final String name,
                               final CorrelationIdentifierGenerator correlationID,
                               final ImmutableMap<String, ?> params) {
        super(logger, name, correlationID);
        this.properties = params != null ? params : ImmutableMap.<String, Object>of();
    }

    public RichLogicalOperation(final Logger logger,
                                final String name,
                                final ImmutableMap<String, ?> params){
        this(logger, name, CORREL_ID_GEN, params);
    }

    public RichLogicalOperation(final String loggerName,
                                final String name,
                                final ImmutableMap<String, ?> params){
        this(loggerName, name, CORREL_ID_GEN, params);
    }

    protected RichLogicalOperation(final String loggerName,
                                   final String name,
                                   final CorrelationIdentifierGenerator correlationID,
                                   final ImmutableMap<String, ?> params) {
        this(Logger.getLogger(loggerName), name, correlationID, params);
    }

    /**
     * Gets all properties available through this logical operation.
     * <p>
     *     The resulting set includes the properties from this operation
     *     and its parents.
     * </p>
     * @return A set of properties.
     */
    public final Set<String> getProperties(){
        return properties.keySet();
    }

    /**
     * Gets named property of the logical operation.
     * @param name The name of the property.
     * @param defVal The default value returned from this method if the specified property doesn't exist.
     * @return The value of the property.
     */
    public final Object getProperty(final String name, final Object defVal){
        return properties.containsKey(name) ? properties.get(name) : defVal;
    }

    /**
     * Gets named property of the logical operation.
     * <p>
     *     Note that the behavior of this method differs from behavior of {@link #getProperty(String, Object)}
     *     method. This method searches for the property which name is equal to the specified <b>and</b>
     *     property value is an instance of the specified type.
     * </p>
     * @param name The name of the property.
     * @param propertyType The requested property type.
     * @param defVal The default value returned from this method if the specified property doesn't
     *               exist or no one property doesn't have an appropriate type.
     * @param <P> The requested property type.
     * @return The value of the property.
     */
    public final <P> P getProperty(final String name, final Class<P> propertyType, final P defVal){
        final Object result = properties.get(name);
        try{
            return propertyType.cast(result);
        }
        catch (final ClassCastException ignored){
            return defVal;
        }
    }

    /**
     * Collects string data used to create textual representation of this operation.
     *
     * @param output An output map to populate with string data.
     */
    @Override
    protected void collectStringData(final Map<String, Object> output) {
        super.collectStringData(output);
        output.putAll(properties);
    }
}
