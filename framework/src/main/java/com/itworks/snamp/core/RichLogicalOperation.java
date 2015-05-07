package com.itworks.snamp.core;

import com.google.common.base.Ticker;
import com.google.common.collect.ImmutableMap;

import java.util.*;

/**
 * Represents a rich version of logical operation that have
 * associated immutable map of parameters and uses pass-through lookup of requested parameter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class RichLogicalOperation extends LogicalOperation {
    private final ImmutableMap<String, ?> properties;

    protected RichLogicalOperation(final String name,
                               final CorrelationIdentifierGenerator correlationID,
                               final Ticker ticker,
                               final ImmutableMap<String, ?> params) {
        super(name, correlationID, ticker);
        this.properties = params != null ? params : ImmutableMap.<String, Object>of();
    }

    public RichLogicalOperation(final String name,
                                final CorrelationIdentifierGenerator correlationID,
                                final ImmutableMap<String, ?> params){
        this(name, correlationID, Ticker.systemTicker(), params);
    }

    public RichLogicalOperation(final String name,
                                final ImmutableMap<String, ?> params){
        this(name, correlIdGenerator, params);
    }

    public RichLogicalOperation(final String name,
                                final String propertyName,
                                final Object propertyValue){
        this(name, ImmutableMap.of(propertyName, propertyValue));
    }

    public RichLogicalOperation(final String name,
                                final String propertyName1,
                                final Object propertyValue1,
                                final String propertyName2,
                                final Object propertyValue2){
        this(name, ImmutableMap.of(propertyName1, propertyValue1,
                propertyName2, propertyValue2));
    }

    public RichLogicalOperation(final String name,
                                final String propertyName1,
                                final Object propertyValue1,
                                final String propertyName2,
                                final Object propertyValue2,
                                final String propertyName3,
                                final Object propertyValue3) {
        this(name, ImmutableMap.of(propertyName1, propertyValue1,
                propertyName2, propertyValue2,
                propertyName3, propertyValue3));
    }

    public RichLogicalOperation(final String name,
                                final String propertyName1,
                                final Object propertyValue1,
                                final String propertyName2,
                                final Object propertyValue2,
                                final String propertyName3,
                                final Object propertyValue3,
                                final String propertyName4,
                                final Object propertyValue4) {
        this(name, ImmutableMap.of(propertyName1, propertyValue1,
                propertyName2, propertyValue2,
                propertyName3, propertyValue3,
                propertyName4, propertyValue4));
    }

    private void fillProperties(final Set<String> output){
        output.addAll(properties.keySet());
        final RichLogicalOperation lookup = findParent(RichLogicalOperation.class);
        if(lookup != null) lookup.fillProperties(output);
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
        final Set<String> result = new HashSet<>(properties.size());
        fillProperties(result);
        return result;
    }

    /**
     * Gets named property of the logical operation.
     * @param name The name of the property.
     * @param defVal The default value returned from this method if the specified property doesn't exist.
     * @return The value of the property.
     */
    public final Object getProperty(final String name, final Object defVal){
        if(properties.containsKey(name))
            return properties.get(name);
        final RichLogicalOperation lookup = findParent(RichLogicalOperation.class);
        return lookup != null ? lookup.getProperty(name, defVal) : defVal;
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
        if(properties.containsKey(name)){
            final Object property = properties.get(name);
            if(propertyType.isInstance(property)) return propertyType.cast(property);
        }
        final RichLogicalOperation lookup = findParent(RichLogicalOperation.class);
        return lookup != null ? lookup.getProperty(name, propertyType, defVal) : defVal;
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

    /**
     * Captures all properties from the current logical operation stack.
     * @return A map of captured properties.
     */
    public static Map<String, ?> dumpProperties(){
        final RichLogicalOperation lookup = find(RichLogicalOperation.class);
        if(lookup == null) return Collections.emptyMap();
        final Map<String, Object> result = new HashMap<>(10);
        for(final String propertyName: lookup.getProperties())
            result.put(propertyName, lookup.getProperty(propertyName, null));
        return result;
    }
}
