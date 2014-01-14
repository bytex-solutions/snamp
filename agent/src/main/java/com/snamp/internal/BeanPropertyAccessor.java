package com.snamp.internal;

import com.snamp.internal.InstanceLifecycle;
import com.snamp.internal.Internal;
import com.snamp.internal.Lifecycle;

import java.io.Serializable;
import java.lang.reflect.*;
import java.util.*;
import java.beans.*;

/**
 * Represents a map that provides access to the Java Bean properties.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Internal
@Lifecycle(InstanceLifecycle.NORMAL)
public class BeanPropertyAccessor<B> implements Map<String, Object>, Serializable {
    /**
     * Represents Java Bean stored in this map.
     */
    public final B bean;
    private final PropertyDescriptor[] properties;

    /**
     * Initializes a new wrapper for Bean properties.
     * @param beanInstance An instance of the Java Bean to wrap. Cannot be {@literal null}.
     * @param stopClass The base class at which to stop the analysis.  Any
     *    methods/properties/events in the stopClass or in its base classes
     *    will be ignored in the analysis.
     * @throws IllegalArgumentException beanInstance is {@literal null}.
     */
    public BeanPropertyAccessor(final B beanInstance, final Class<?> stopClass) throws IntrospectionException {
        if(beanInstance == null) throw new IllegalArgumentException("beanInstance is null.");
        bean = beanInstance;
        properties = Introspector.getBeanInfo(beanInstance.getClass(), stopClass).getPropertyDescriptors();
    }

    /**
     * Initializes a new wrapper for Bean properties.
     * @param beanInstance An instance of the Java Bean to wrap. Cannot be {@literal null}.
     * @throws IllegalArgumentException beanInstance is {@literal null}.
     */
    public BeanPropertyAccessor(final B beanInstance) throws IntrospectionException {
        this(beanInstance, null);
    }

    /**
     * Returns the count of bean properties.
     * @return The count of bean properties.
     */
    @Override
    public final int size() {
        return properties.length;
    }

    /**
     * Determines whether the wrapped bean has no public properties.
     * @return {@literal true}, if the wrapped bean has no public properties; otherwise, {@literal false}.
     */
    @Override
    public final boolean isEmpty() {
        return properties.length == 0;
    }

    /**
     * Determines whether the specified property name defined
     * in the wrapped bean.
     * @param propertyName The property name to check.
     * @return {@literal true}, if the specified property is defined in the bean; otherwise, {@literal false}.
     */
    public final boolean containsKey(final String propertyName){
        for(final PropertyDescriptor pd: properties)
            if(Objects.equals(propertyName, pd.getName())) return true;
        return false;
    }

    /**
     * Determines whether the specified {@link PropertyDescriptor} defined
     * in the wrapped bean.
     * @param property The property {@link PropertyDescriptor} to check.
     * @return {@literal true}, if the specified property is defined in the bean; otherwise, {@literal false}.
     */
    public final boolean containsKey(final PropertyDescriptor property){
        for(final PropertyDescriptor pd: properties)
            if(Objects.equals(property, pd)) return true;
        return false;
    }

    /**
     * Determines whether the specified property name or {@link PropertyDescriptor} defined
     * in the wrapped bean
     * @param property The property name or {@link PropertyDescriptor} to check.
     * @return {@literal true}, if the specified property is defined in the bean; otherwise, {@literal false}.
     */
    @Override
    public final boolean containsKey(final Object property) {
        if(property instanceof String)
            return containsKey((String)property);
        else if(property instanceof PropertyDescriptor)
            return containsKey((PropertyDescriptor)property);
        else return false;
    }

    /**
     * Determines whether the specified value is equal to one of the bean property values.
     * @param value The value to compare.
     * @return {@literal true}, if the specified value is assigned to one of the existed bean
     * properties; otherwise, {@literal false}.
     */
    @Override
    public final boolean containsValue(final Object value) {
        for(final PropertyDescriptor pd: properties)
            if(pd.getReadMethod() != null)
                try{
                    final Method getter = pd.getReadMethod();
                    if(Objects.equals(value, getter.invoke(bean))) return true;
                }
                catch (final ReflectiveOperationException e){
                    continue;
                }
        return false;
    }

    /**
     * Returns the value of the bean property.
     * @param property The name of {@link PropertyDescriptor} to get.
     * @return The property value.
     */
    public final Object get(final PropertyDescriptor property){
        if(property.getReadMethod() != null)
            try{
                final Method getter = property.getReadMethod();
                return getter.invoke(bean);
            }
            catch (final ReflectiveOperationException e){
                return null;
            }
        else return null;
    }

    /**
     * Returns the value of the bean property.
     * @param propertyName The name of the property to get.
     * @return The property value.
     */
    public final Object get(final String propertyName){
        for(final PropertyDescriptor pd: properties)
            if(Objects.equals(propertyName, pd.getName()))
                return get(pd);
        return null;
    }

    /**
     * Returns the value of the bean property.
     * @param property The name of the property of {@link PropertyDescriptor} to get.
     * @return The property value.
     */
    @Override
    public final Object get(final Object property) {
        if(property instanceof String)
            return get((String)property);
        else if(property instanceof PropertyDescriptor)
            return get((PropertyDescriptor)property);
        else return null;
    }

    /**
     * Sets the property value.
     * @param property The property to set.
     * @param value The value of the property to set.
     * @return {@literal true}, if property is public and accessible from this class; otherwise, {@literal false}.
     */
    public final boolean set(final PropertyDescriptor property, final Object value){
        if(property.getWriteMethod() != null)
            try {
                property.getWriteMethod().invoke(bean, value);
                return true;
            }
            catch (final ReflectiveOperationException e){
                return false;
            }
        else return false;
    }

    /**
     * Sets the property value.
     * @param propertyName The property to set.
     * @param value The value of the property to set.
     * @return {@literal true}, if property is public and accessible from this class; otherwise, {@literal false}.
     */
    public final boolean set(final String propertyName, final Object value){
        for(final PropertyDescriptor pd: properties)
            if(Objects.equals(propertyName, pd.getName()))
                return set(pd, value);
        return false;
    }

    /**
     * Sets the property value.
     * @param propertyName The property to set.
     * @param value The value of the property to set.
     * @return {@literal true}, if property is public and accessible from this class; otherwise, {@literal false}.\
     * @deprecated It is recommended to use {@link #set(String, Object)} instead.
     */
    @Override
    @Deprecated
    public final Object put(final String propertyName, final Object value) {
        for(final PropertyDescriptor pd: properties)
            if(Objects.equals(pd.getName(), propertyName) && pd.getWriteMethod() != null)
                try{
                    return pd.getWriteMethod().invoke(bean, value);
                }
                catch (final ReflectiveOperationException e){
                    return e;
                }
        return null;
    }

    /**
     * This operation is not supported.
     * @param key The property name.
     * @return The property value.
     * @throws UnsupportedOperationException This operation is not supported.
     */
    @Override
    public final Object remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets a collection of properties.
     * @param m A collection of properties to set.
     * @deprecated It is recommended to use {@link #set(String, Object)} instead.
     */
    @Override
    @Deprecated
    public final void putAll(final Map<? extends String, ?> m) {
        for(final String s: m.keySet())
            set(s, m.get(s));
    }

    /**
     * This operation is not supported.
     */
    @Override
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the property descriptor by its name.
     * @param propertyName The name of the property.
     * @return The property descriptor; or {@literal null} if property doesn't exist.
     */
    public final PropertyDescriptor getDescriptor(final String propertyName){
        for(final PropertyDescriptor pd: properties)
            if(Objects.equals(propertyName, pd.getName())) return pd;
        return null;
    }

    /**
     * Returns a type of the specified property.
     * @param propertyName The name of the property.
     * @return The property type; or {@literal null} if property doesn't exist.
     */
    public final Class<?> getType(final String propertyName){
        final PropertyDescriptor desc = getDescriptor(propertyName);
        return desc != null ? desc.getPropertyType() : null;
    }

    /**
     * Returns a set of available bean properties.
     * @return A set of available bean properties.
     */
    @Override
    public final Set<String> keySet() {
        final Set<String> result = new HashSet<>(properties.length);
        for(final PropertyDescriptor pd: properties)
            result.add(pd.getName());
        return result;
    }

    /**
     * Returns all properties.
     * @return The values of all properties.
     * @deprecated It is recommended to use {@link #get(String)} instead.
     */
    @Override
    @Deprecated
    public final Collection<Object> values() {
        final Collection<Object> values = new ArrayList<>(properties.length);
        for(final PropertyDescriptor pd: properties)
            values.add(get(pd));
        return values;
    }

    /**
     * Reads values of all properties.
     * @return Values of all properties.
     * @deprecated It is recommended to use {@link #get(String)} instead.
     */
    @Override
    @Deprecated
    public final Set<Entry<String, Object>> entrySet() {
        final Set<Entry<String, Object>> result = new HashSet<>(properties.length);
        for(final PropertyDescriptor pd: properties)
            result.add(new Entry<String, Object>() {
                @Override
                public String getKey() {
                    return pd.getName();
                }

                @Override
                public Object getValue() {
                    return get(pd);
                }

                @Override
                @Deprecated
                public Object setValue(final Object value) {
                    return set(pd.getName(), value);
                };
            });
        return result;
    }
}
