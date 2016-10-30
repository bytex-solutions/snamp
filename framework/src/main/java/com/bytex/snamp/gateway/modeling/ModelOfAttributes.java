package com.bytex.snamp.gateway.modeling;

import com.bytex.snamp.*;
import com.bytex.snamp.concurrent.ThreadSafeObject;
import com.bytex.snamp.internal.KeyedObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import javax.management.*;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Represents an abstract object that helps you to organize storage
 * of all attributes inside of gateway instance.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@ThreadSafe
public abstract class ModelOfAttributes<TAccessor extends AttributeAccessor> extends ThreadSafeObject implements AttributeSet<TAccessor> {
    private final HashMap<String, ResourceAttributeList<TAccessor>> attributes;

    /**
     * Initializes a new storage.
     */
    protected ModelOfAttributes(){
        super(SingleResourceGroup.class);
        attributes = new HashMap<>(10);
    }

    /**
     * Creates a new instance of the attribute accessor.
     * @param metadata The metadata of the attribute.
     * @return A new instance of the attribute accessor.
     * @throws java.lang.Exception Unable to instantiate attribute accessor.
     */
    protected abstract TAccessor createAccessor(final MBeanAttributeInfo metadata) throws Exception;

    private TAccessor addAttributeImpl(final String resourceName,
                                   final MBeanAttributeInfo metadata) throws Exception{
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
        return writeLock.call(SingleResourceGroup.INSTANCE, () -> addAttributeImpl(resourceName, metadata), null);
    }

    private TAccessor removeAttributeImpl(final String resourceName,
                                          final MBeanAttributeInfo metadata){
        final ResourceAttributeList<TAccessor> list;
        if(attributes.containsKey(resourceName))
            list = attributes.get(resourceName);
        else return null;
        final TAccessor accessor = list.remove(metadata);
        if(list.isEmpty())
            attributes.remove(resourceName);
        return accessor;
    }

    @ThreadSafe
    public final TAccessor removeAttribute(final String resourceName,
                                           final MBeanAttributeInfo metadata){
        return writeLock.apply(SingleResourceGroup.INSTANCE, resourceName, metadata, this::removeAttributeImpl);
    }

    protected final Object getAttributeValue(final String resourceName,
                                             final String attributeName) throws AttributeNotFoundException, ReflectionException, MBeanException {
        try (final SafeCloseable ignored = readLock.acquireLock(SingleResourceGroup.INSTANCE, null)) {
            if (attributes.containsKey(resourceName))
                return attributes.get(resourceName).getAttribute(attributeName);
            else
                throw new AttributeNotFoundException(String.format("Attribute %s in managed resource %s doesn't exist", attributeName, resourceName));
        } catch (final InterruptedException | TimeoutException e) {
            throw new ReflectionException(e);
        }
    }

    protected final void setAttributeValue(final String resourceName,
                                           final String attributeName,
                                           final Object value) throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException {
        try (final SafeCloseable ignored = readLock.acquireLock(SingleResourceGroup.INSTANCE, null)) {
            if (attributes.containsKey(resourceName))
                attributes.get(resourceName).setAttribute(attributeName, value);
            else
                throw new AttributeNotFoundException(String.format("Attribute %s in managed resource %s doesn't exist", attributeName, resourceName));
        } catch (final InterruptedException | TimeoutException e) {
            throw new ReflectionException(e);
        }
    }

    public final <E extends Throwable> boolean processAttribute(final String resourceName,
                                          final String attributeName,
                                          final Acceptor<? super TAccessor, E> processor) throws E {
        try (final SafeCloseable ignored = readLock.acquireLock(SingleResourceGroup.INSTANCE)) {
            final TAccessor accessor = attributes.containsKey(resourceName) ?
                    attributes.get(resourceName).get(attributeName) :
                    null;
            if (accessor != null) {
                processor.accept(accessor);
                return true;
            } else return false;
        }
    }

    /**
     * Returns a read-only set of connected managed resources.
     * @return The read-only set of connected managed resources.
     */
    @ThreadSafe
    public final Set<String> getHostedResources(){
        return readLock.apply(SingleResourceGroup.INSTANCE, attributes, attrs -> ImmutableSet.copyOf(attrs.keySet()));
    }

    private static Set<String> getResourceAttributesImpl(final String resourceName,
                                                     final Map<String, ? extends KeyedObjects<String, ?>> attributes) {
        return attributes.containsKey(resourceName) ?
                attributes.get(resourceName).keySet() :
                ImmutableSet.of();
    }

    @ThreadSafe
    public final Set<String> getResourceAttributes(final String resourceName) {
        return readLock.apply(SingleResourceGroup.INSTANCE, resourceName, attributes, ModelOfAttributes::getResourceAttributesImpl);
    }

    private static <TAccessor extends AttributeAccessor> Collection<MBeanAttributeInfo> getResourceAttributesMetadataImpl(final String resourceName,
                                                                                                                          final Map<String, ? extends KeyedObjects<String, TAccessor>> attributes) {
        final KeyedObjects<String, TAccessor> resource = attributes.get(resourceName);
        if (resource != null) {
            return resource.values().stream()
                    .map(FeatureAccessor::getMetadata)
                    .collect(Collectors.toList());
        } else return ImmutableList.of();
    }

    @ThreadSafe
    public final Collection<MBeanAttributeInfo> getResourceAttributesMetadata(final String resourceName){
        return readLock.apply(SingleResourceGroup.INSTANCE, resourceName, attributes, ModelOfAttributes::getResourceAttributesMetadataImpl);
    }

    private static <TAccessor extends AttributeAccessor> Collection<TAccessor> clearImpl(final String resourceName,
                                                                   final Map<String, ? extends KeyedObjects<String, TAccessor>> attributes){
        return attributes.containsKey(resourceName) ?
                attributes.remove(resourceName).values():
                ImmutableList.of();
    }

    /**
     * Removes all attributes from this model and associated with the specified resource.
     * @param resourceName The name of the managed resource.
     * @return The read-only collection of removed attributes.
     */
    @ThreadSafe
    public final Collection<TAccessor> clear(final String resourceName) {
        return writeLock.apply(SingleResourceGroup.INSTANCE, resourceName, attributes, ModelOfAttributes::clearImpl);
    }

    private <E extends Exception> void forEachAttributeImpl(final EntryReader<String, ? super TAccessor, E> attributeReader) throws E{
        for (final Map.Entry<String, ResourceAttributeList<TAccessor>> entry: attributes.entrySet())
            for(final TAccessor accessor: entry.getValue().values())
                if(!attributeReader.read(entry.getKey(), accessor)) return;
    }

    /**
     * Reads all attributes sequentially.
     * @param attributeReader An object that accepts attribute and its resource.
     * @param <E> Type of the exception that may be produced by reader.
     * @throws E Unable to process attribute.
     */
    public final <E extends Exception> void forEachAttribute(final EntryReader<String, ? super TAccessor, E> attributeReader) throws E {
        readLock.accept(SingleResourceGroup.INSTANCE, attributeReader, this::forEachAttributeImpl);
    }

    private static void clearImpl(final Map<String, ? extends ResourceFeatureList<?, ?>> attributes){
        attributes.values().forEach(ResourceFeatureList::clear);
        attributes.clear();
    }

    /**
     * Removes all attributes from this model.
     */
    @ThreadSafe
    public final void clear(){
        writeLock.accept(SingleResourceGroup.INSTANCE, attributes, ModelOfAttributes::clearImpl);
    }
}