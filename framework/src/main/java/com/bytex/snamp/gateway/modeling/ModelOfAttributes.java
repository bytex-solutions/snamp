package com.bytex.snamp.gateway.modeling;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.EntryReader;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.ThreadSafe;

import javax.management.*;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * Represents an abstract object that helps you to organize storage
 * of all attributes inside of gateway instance.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@ThreadSafe
public abstract class ModelOfAttributes<TAccessor extends AttributeAccessor> extends ModelOfFeatures<MBeanAttributeInfo, TAccessor, ResourceAttributeList<TAccessor>> implements AttributeSet<TAccessor> {

    /**
     * Initializes a new storage.
     */
    protected ModelOfAttributes(){
        super(ResourceAttributeList::new, SingleResourceGroup.class, SingleResourceGroup.INSTANCE);
    }

    /**
     * Creates a new instance of the attribute accessor.
     * @param resourceName
     * @param metadata The metadata of the attribute.
     * @return A new instance of the attribute accessor.
     * @throws java.lang.Exception Unable to instantiate attribute accessor.
     */
    @Override
    protected abstract TAccessor createAccessor(final String resourceName, final MBeanAttributeInfo metadata) throws Exception;

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
        return addFeature(resourceName, metadata);
    }

    @ThreadSafe
    public final TAccessor removeAttribute(final String resourceName,
                                           final MBeanAttributeInfo metadata){
        return removeFeature(resourceName, metadata);
    }

    protected final Object getAttributeValue(final String resourceName,
                                             final String attributeName) throws AttributeNotFoundException, ReflectionException, MBeanException {
        try (final SafeCloseable ignored = readLock.acquireLock(SingleResourceGroup.INSTANCE, null)) {
            final TAccessor accessor = getAccessor(resourceName, attributeName);
            if(accessor == null)
                throw new AttributeNotFoundException(String.format("Attribute %s in managed resource %s doesn't exist", attributeName, resourceName));
            else
                return accessor.getValue();
        } catch (final InterruptedException | TimeoutException e) {
            throw new ReflectionException(e);
        }
    }

    protected final void setAttributeValue(final String resourceName,
                                           final String attributeName,
                                           final Object value) throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException {
        try (final SafeCloseable ignored = readLock.acquireLock(SingleResourceGroup.INSTANCE, null)) {
            final TAccessor accessor = getAccessor(resourceName, attributeName);
            if(accessor == null)
                throw new AttributeNotFoundException(String.format("Attribute %s in managed resource %s doesn't exist", attributeName, resourceName));
            else
                accessor.setValue(value);
        } catch (final InterruptedException | TimeoutException e) {
            throw new ReflectionException(e);
        }
    }

    public final <E extends Throwable> boolean processAttribute(final String resourceName,
                                          final String attributeName,
                                          final Acceptor<? super TAccessor, E> processor) throws E {
        return processFeature(resourceName, attributeName, processor);
    }

    @ThreadSafe
    public final Set<String> getResourceAttributes(final String resourceName) {
        return getResourceFeatures(resourceName);
    }

    @ThreadSafe
    public final Collection<MBeanAttributeInfo> getResourceAttributesMetadata(final String resourceName){
        return getResourceFeaturesMetadata(resourceName);
    }

    /**
     * Reads all attributes sequentially.
     * @param attributeReader An object that accepts attribute and its resource.
     * @param <E> Type of the exception that may be produced by reader.
     * @throws E Unable to process attribute.
     */
    @Override
    public final <E extends Exception> void forEachAttribute(final EntryReader<String, ? super TAccessor, E> attributeReader) throws E {
        forEachFeature(attributeReader);
    }
}
