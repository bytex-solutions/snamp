package com.bytex.snamp.adapters.modeling;

import com.bytex.snamp.Consumer;
import com.bytex.snamp.EntryReader;
import com.bytex.snamp.ThreadSafe;
import com.bytex.snamp.concurrent.ThreadSafeObject;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import javax.management.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents an abstract object that helps you to organize storage
 * of all attributes inside of your resource adapter.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@ThreadSafe
public abstract class ModelOfAttributes<TAccessor extends AttributeAccessor> extends ThreadSafeObject implements AttributeSet<TAccessor> {
    private final HashMap<String, ResourceAttributeList<TAccessor>> attributes;

    /**
     * Initializes a new storage.
     */
    protected ModelOfAttributes(){
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
                    ImmutableSet.of();
        }
    }

    @ThreadSafe
    public final Collection<MBeanAttributeInfo> getResourceAttributesMetadata(final String resourceName){
        try(final LockScope ignored = beginRead()){
            final ResourceAttributeList<?> resource = attributes.get(resourceName);
            if(resource != null){
                return resource.values().stream()
                        .map(FeatureAccessor::getMetadata)
                        .collect(Collectors.toCollection(LinkedList::new));
            }
            else return ImmutableList.of();
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
                    ImmutableList.of();
        }
    }

    /**
     * Reads all attributes sequentially.
     * @param attributeReader An object that accepts attribute and its resource.
     * @param <E> Type of the exception that may be produced by reader.
     * @throws E Unable to process attribute.
     */
    public final <E extends Exception> void forEachAttribute(final EntryReader<String, ? super TAccessor, E> attributeReader) throws E{
        try(final LockScope ignored = beginRead()) {
            for (final Map.Entry<String, ResourceAttributeList<TAccessor>> entry: attributes.entrySet())
                for(final TAccessor accessor: entry.getValue().values())
                    if(!attributeReader.read(entry.getKey(), accessor)) return;
        }
    }

    /**
     * Removes all attributes from this model.
     */
    @ThreadSafe
    public final void clear(){
        try(final LockScope ignored = beginWrite()){
            attributes.values().forEach(ResourceFeatureList::clear);
            attributes.clear();
        }
    }
}
