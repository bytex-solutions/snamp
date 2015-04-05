package com.itworks.snamp.adapters;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.itworks.snamp.Consumer;
import com.itworks.snamp.concurrent.ThreadSafeObject;
import com.itworks.snamp.internal.annotations.ThreadSafe;

import javax.management.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * Represents an abstract object that helps you to organize storage
 * of all attributes inside of your resource adapter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@ThreadSafe
public abstract class AbstractAttributesModel<TAccessor extends AttributeAccessor> extends ThreadSafeObject {
    private final HashMap<String, ResourceAttributeList<TAccessor>> attributes;

    /**
     * Initializes a new storage.
     */
    protected AbstractAttributesModel(){
        attributes = new HashMap<>(10);
    }

    /**
     * Creates a new instance of the attribute accessor.
     * @param metadata The metadata of the attribute.
     * @return A new instance of the attribute accessor.
     * @throws java.lang.Exception Unable to instantiate attribute accessor.
     */
    protected abstract TAccessor createAccessor(final MBeanAttributeInfo metadata) throws Exception;

    /**
     * Registers a new attribute in this model.
     * @param resourceName The name of the managed resource.
     * @param metadata The metadata of the managed resource attribute.
     * @return An instance of the managed resource attribute accessor.
     * @throws Exception Unable to instantiate attribute accessor.
     */
    @ThreadSafe
    public final TAccessor addAttribute(final String resourceName,
                                  final MBeanAttributeInfo metadata) throws Exception{
        try(final LockScope ignored = beginWrite()){
            //find resource storage
            final ResourceAttributeList<TAccessor> list;
            if(attributes.containsKey(resourceName))
                list = attributes.get(resourceName);
            else attributes.put(resourceName, list = new ResourceAttributeList<>());
            //find attribute
            final TAccessor accessor;
            if(list.containsKey(metadata))
                accessor = list.get(metadata);
            else list.put(accessor = createAccessor(metadata));
            return accessor;
        }
    }

    @ThreadSafe
    public final TAccessor removeAttribute(final String resourceName,
                                           final MBeanAttributeInfo metadata){
        try(final LockScope ignored = beginWrite()){
            final ResourceAttributeList<TAccessor> list;
            if(attributes.containsKey(resourceName))
                list = attributes.get(resourceName);
            else return null;
            final TAccessor accessor = list.remove(metadata);
            if(list.isEmpty())
                attributes.remove(resourceName);
            return accessor;
        }
    }

    protected final Object getAttributeValue(final String resourceName,
                                             final String attributeName) throws AttributeNotFoundException, ReflectionException, MBeanException {
        try (final LockScope ignored = beginRead()) {
            if (attributes.containsKey(resourceName))
                return attributes.get(resourceName).getAttribute(attributeName);
            else
                throw new AttributeNotFoundException(String.format("Attribute %s in managed resource %s doesn't exist", attributeName, resourceName));
        }
    }

    protected final void setAttributeValue(final String resourceName,
                                           final String attributeName,
                                           final Object value) throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException {
        try (final LockScope ignored = beginRead()) {
            if (attributes.containsKey(resourceName))
                attributes.get(resourceName).setAttribute(attributeName, value);
            else
                throw new AttributeNotFoundException(String.format("Attribute %s in managed resource %s doesn't exist", attributeName, resourceName));
        }
    }

    public final <E extends Throwable> boolean processAttribute(final String resourceName,
                                          final String attributeName,
                                          final Consumer<? super TAccessor, E> processor) throws E{
        try(final LockScope ignored = beginRead()){
            final TAccessor accessor =  attributes.containsKey(resourceName)?
                    attributes.get(resourceName).get(attributeName):
                    null;
            if(accessor != null){
                processor.accept(accessor);
                return true;
            }
            else return false;
        }
    }

    /**
     * Returns a read-only set of connected managed resources.
     * @return The read-only set of connected managed resources.
     */
    @ThreadSafe
    public final Set<String> getHostedResources(){
        try(final LockScope ignored = beginRead()){
            return attributes.keySet();
        }
    }

    @ThreadSafe
    public final Set<String> getResourceAttributes(final String resourceName) {
        try (final LockScope ignored = beginRead()) {
            return attributes.containsKey(resourceName) ?
                    attributes.get(resourceName).keySet() :
                    ImmutableSet.<String>of();
        }
    }

    /**
     * Removes all attributes from this model and associated with the specified resource.
     * @param resourceName The name of the managed resource.
     * @return The read-only collection of removed attributes.
     */
    @ThreadSafe
    public final Collection<TAccessor> clear(final String resourceName){
        try(final LockScope ignored = beginWrite()){
            return attributes.containsKey(resourceName) ?
                    attributes.remove(resourceName).values():
                    ImmutableList.<TAccessor>of();
        }
    }

    /**
     * Removes all attributes from this model.
     */
    @ThreadSafe
    public final void clear(){
        try(final LockScope ignored = beginWrite()){
            for(final ResourceAttributeList<?> list: attributes.values())
                list.clear();
            attributes.clear();
        }
    }
}
