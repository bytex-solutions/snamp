package com.bytex.snamp.jmx;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an abstract class for constructing homogeneous composite data.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class AbstractCompositeData<V> extends HashMap<String, V> implements CompositeData {
    private static final long serialVersionUID = -3430619815874482606L;
    private CompositeType cachedType;

    /**
     * Initializes a new empty composite data.
     */
    protected AbstractCompositeData() {
        cachedType = null;
    }

    /**
     * Initializes a new composite data with predefined values.
     * @param values The map to be cloned into the newly constructed composite data.
     */
    protected AbstractCompositeData(final Map<String, V> values){
        super(values);
        cachedType = null;
    }

    /**
     * Gets name of the composite type.
     * @return The name of the composite type.
     */
    protected abstract String getTypeName();

    /**
     * Gets description of the composite type.
     * @return The description of the composite type.
     */
    protected abstract String getTypeDescription();

    /**
     * Gets type of all values in this composite data.
     * @return The type of all values in this composite data.
     */
    protected abstract OpenType<V> getItemType();

    /**
     * Gets description of the item.
     * @param itemName The name of the item.
     * @return The description of the item.
     */
    protected abstract String getItemDescription(final String itemName);

    private CompositeType getCompositeTypeImpl() throws OpenDataException {
        final String[] itemNames = keySet().stream().toArray(String[]::new);
        final String[] itemDescriptions = new String[itemNames.length];
        final OpenType<?>[] itemTypes = new OpenType<?>[itemNames.length];
        for (int i = 0; i < itemNames.length; i++) {
            itemDescriptions[i] = getItemDescription(itemNames[i]);
            itemTypes[i] = getItemType();
        }
        return new CompositeType(getTypeName(),
                getTypeDescription(),
                itemNames,
                itemDescriptions,
                itemTypes);
    }

    /**
     * Removes all of the mappings from this map.
     * The map will be empty after this call returns.
     */
    @Override
    public void clear() {
        cachedType = null;
        super.clear();
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     * <tt>null</tt> if there was no mapping for <tt>key</tt>.
     * (A <tt>null</tt> return can also indicate that the map
     * previously associated <tt>null</tt> with <tt>key</tt>.)
     */
    @Override
    public V put(final String key, final V value) {
        cachedType = null;
        return super.put(key, value);
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     *
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with <tt>key</tt>, or
     * <tt>null</tt> if there was no mapping for <tt>key</tt>.
     * (A <tt>null</tt> return can also indicate that the map
     * previously associated <tt>null</tt> with <tt>key</tt>.)
     */
    @Override
    public V remove(final Object key) {
        cachedType = null;
        return super.remove(key);
    }

    /**
     * Copies all of the mappings from the specified map to this map.
     * These mappings will replace any mappings that this map had for
     * any of the keys currently in the specified map.
     *
     * @param m mappings to be stored in this map
     * @throws NullPointerException if the specified map is null
     */
    @Override
    public void putAll(final Map<? extends String, ? extends V> m) {
        cachedType = null;
        super.putAll(m);
    }

    /**
     * Returns the <i>composite type </i> of this <i>composite data</i> instance.
     *
     * @return the type of this CompositeData.
     * @throws java.lang.IllegalStateException Invalid dictionary data.
     */
    @Override
    public final CompositeType getCompositeType() throws IllegalStateException {
        if (cachedType == null)
            try {
                cachedType = getCompositeTypeImpl();
            } catch (final OpenDataException e) {
                throw new IllegalStateException(e);
            }
        return cachedType;
    }

    /**
     * Checks the validity of this composite data.
     * @return {@literal true}, if this dictionary is valid; otherwise, {@literal false}.
     */
    public final boolean check(){
        try{
            getCompositeType();
            return true;
        }
        catch (final IllegalStateException ignored){
            return false;
        }
    }

    /**
     * Returns the value of the item whose name is <tt>key</tt>.
     *
     * @param key the name of the item.
     * @return the value associated with this key.
     * @throws IllegalArgumentException if <tt>key</tt> is a null or empty String.
     */
    @Override
    public final Object get(final String key) {
        if (containsKey(key))
            return get((Object) key);
        else throw new IllegalArgumentException(String.format("Key %s doesn't exist.", key));
    }

    /**
     * Returns an array of the values of the items whose names are specified by <tt>keys</tt>, in the same order as <tt>keys</tt>.
     *
     * @param keys the names of the items.
     * @return the values corresponding to the keys.
     * @throws IllegalArgumentException if an element in <tt>keys</tt> is a null or empty String.
     */
    @Override
    public final Object[] getAll(final String[] keys) {
        final Object[] result = new Object[keys.length];
        for (int i = 0; i < keys.length; i++)
            result[i] = get(keys[i]);
        return result;
    }

    /**
     * Returns <tt>true</tt> if and only if this <tt>CompositeData</tt> instance contains
     * an item whose name is <tt>key</tt>.
     * If <tt>key</tt> is a null or empty String, this method simply returns false.
     *
     * @param key the key to be tested.
     * @return true if this <tt>CompositeData</tt> contains the key.
     */
    @Override
    public final boolean containsKey(final String key) {
        return containsKey((Object)key);
    }
}
