package com.bytex.snamp.jmx;

import com.bytex.snamp.Convert;
import com.bytex.snamp.ResettableIterator;

import javax.annotation.concurrent.NotThreadSafe;
import javax.management.Descriptor;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Objects;

/**
 * Represents bridge between {@link javax.management.Descriptor} and {@link Dictionary}.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@NotThreadSafe
final class DescriptorDictionary extends Dictionary<String, Object> {
    private final Descriptor descr;
    private String[] fields;    //cached fields for fast access

    DescriptorDictionary(final Descriptor descr){
        this.descr = Objects.requireNonNull(descr);
        this.fields = descr.getFieldNames();
    }

    /**
     * Returns the number of entries (distinct keys) in this dictionary.
     *
     * @return the number of keys in this dictionary.
     */
    @Override
    public int size() {
        return fields.length;
    }

    /**
     * Tests if this dictionary maps no keys to value. The general contract
     * for the <tt>isEmpty</tt> method is that the result is true if and only
     * if this dictionary contains no entries.
     *
     * @return <code>true</code> if this dictionary maps no keys to values;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean isEmpty() {
        return fields.length == 0;
    }

    /**
     * Returns an enumeration of the keys in this dictionary. The general
     * contract for the keys method is that an <tt>Enumeration</tt> object
     * is returned that will generate all the keys for which this dictionary
     * contains entries.
     *
     * @return an enumeration of the keys in this dictionary.
     * @see Dictionary#elements()
     * @see Enumeration
     */
    @Override
    public Enumeration<String> keys() {
        return ResettableIterator.of(fields);
    }

    /**
     * Returns an enumeration of the values in this dictionary. The general
     * contract for the <tt>elements</tt> method is that an
     * <tt>Enumeration</tt> is returned that will generate all the elements
     * contained in entries in this dictionary.
     *
     * @return an enumeration of the values in this dictionary.
     * @see Dictionary#keys()
     * @see Enumeration
     */
    @Override
    public Enumeration<Object> elements() {
        return ResettableIterator.of(descr.getFieldValues(fields));
    }

    public Object get(final String fieldName){
        return descr.getFieldValue(fieldName);
    }

    /**
     * Returns the value to which the key is mapped in this dictionary.
     * The general contract for the <tt>isEmpty</tt> method is that if this
     * dictionary contains an entry for the specified key, the associated
     * value is returned; otherwise, <tt>null</tt> is returned.
     *
     * @param key a key in this dictionary.
     *            <code>null</code> if the key is not mapped to any value in
     *            this dictionary.
     * @return the value to which the key is mapped in this dictionary;
     * @see Dictionary#put(Object, Object)
     */
    @Override
    public Object get(final Object key) {
        return get(Convert.toType(key, String.class).orElseThrow(ClassCastException::new));
    }

    /**
     * Maps the specified <code>key</code> to the specified
     * <code>value</code> in this dictionary. Neither the key nor the
     * value can be <code>null</code>.
     * <p/>
     * If this dictionary already contains an entry for the specified
     * <tt>key</tt>, the value already in this dictionary for that
     * <tt>key</tt> is returned, after modifying the entry to contain the
     * new element. <p>If this dictionary does not already have an entry
     * for the specified <tt>key</tt>, an entry is created for the
     * specified <tt>key</tt> and <tt>value</tt>, and <tt>null</tt> is
     * returned.
     * <p/>
     * The <code>value</code> can be retrieved by calling the
     * <code>get</code> method with a <code>key</code> that is equal to
     * the original <code>key</code>.
     *
     * @param fieldName   the hashtable key.
     * @param value the value.
     * @return the previous value to which the <code>key</code> was mapped
     * in this dictionary, or <code>null</code> if the key did not
     * have a previous mapping.
     * @throws NullPointerException if the <code>key</code> or
     *                              <code>value</code> is <code>null</code>.
     * @see Object#equals(Object)
     * @see Dictionary#get(Object)
     */
    @Override
    public Object put(final String fieldName, final Object value) {
        descr.setField(fieldName, value);
        fields = descr.getFields();
        return null;
    }

    public Object remove(final String fieldName){
        final Object value = descr.getFieldValue(fieldName);
        descr.removeField(fieldName);
        fields = descr.getFields();
        return value;
    }

    /**
     * Removes the <code>key</code> (and its corresponding
     * <code>value</code>) from this dictionary. This method does nothing
     * if the <code>key</code> is not in this dictionary.
     *
     * @param key the key that needs to be removed.
     * @return the value to which the <code>key</code> had been mapped in this
     * dictionary, or <code>null</code> if the key did not have a
     * mapping.
     * @throws NullPointerException if <tt>key</tt> is <tt>null</tt>.
     */
    @Override
    public Object remove(final Object key) {
        return remove(Convert.toType(key, String.class).orElseThrow(ClassCastException::new));
    }
}
