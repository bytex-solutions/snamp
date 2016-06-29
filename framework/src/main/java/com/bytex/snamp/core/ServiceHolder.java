package com.bytex.snamp.core;

import com.google.common.collect.Maps;
import org.osgi.framework.*;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a permanent reference to the OSGi service. You should release this service manually
 * when you no longer need it.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public class ServiceHolder<S> implements ServiceProvider<S> {
    private final ServiceReference<S> serviceRef;
    private S serviceImpl;

    private ServiceHolder(final LocalServiceReference<S> localRef){
        serviceRef = Objects.requireNonNull(localRef);
        serviceImpl = localRef.get();
    }

    /**
     * Initializes a new service reference holder.
     * @param context The context of the bundle which holds this reference. Cannot be {@literal null}.
     * @param serviceRef The service reference to wrap. Cannot be {@literal null}.
     * @throws java.lang.IllegalArgumentException context or serviceRef is {@literal null}.
     */
    public ServiceHolder(final BundleContext context, final ServiceReference<S> serviceRef) throws IllegalArgumentException{
        if(context == null) throw new IllegalArgumentException("context is null.");
        else if(serviceRef == null) throw new IllegalArgumentException("serviceRef is null.");
        else serviceImpl = context.getService(this.serviceRef = serviceRef);
    }

    /**
     * Attempts to create a reference to OSGi service without throwing exception if service was not registered.
     * @param context The context of the bundle which holds this reference. Cannot be {@literal null}.
     * @param serviceType The requested service type. Cannot be {@literal null}.
     * @param <S> Type of service interface.
     * @return A reference to OSGi service; or {@literal null}, if service was not registered.
     * @since 1.2
     */
    public static <S> ServiceHolder<S> tryCreate(final BundleContext context, final Class<S> serviceType){
        final ServiceReference<S> serviceRef = context.getServiceReference(serviceType);
        return serviceRef != null ? new ServiceHolder<>(context, serviceRef) : null;
    }

    /**
     * Attempts to create a reference to OSGi or local (provided via {@link java.util.ServiceLoader}) service without throwing exception
     * if service was not registered.
     * @param context The class loader which holds this reference. Cannot be {@literal null}.
     * @param serviceType The requested service type. Cannot be {@literal null}.
     * @param <S> Type of service interface.
     * @return A reference to OSGi or local service; or {@literal null}, if service was not registered.
     * @since 1.2
     */
    public static <S> ServiceHolder<S> tryCreate(final ClassLoader context, final Class<S> serviceType){
        if(context instanceof BundleReference)
            return tryCreate(getBundleContext((BundleReference)context), serviceType);
        else {
            final LocalServiceReference<S> serviceRef = LocalServiceReference.resolve(context, serviceType);
            return serviceRef != null ? new ServiceHolder<>(serviceRef) : null;
        }
    }

    private static BundleContext getBundleContext(final BundleReference bundleRef){
        return bundleRef.getBundle().getBundleContext();
    }

    /**
     * Gets a strong reference to the service.
     * @return A strong reference to the service; or {@literal null}, if reference is released.
     */
    public final S getService(){
        return get();
    }

    /**
     * Gets a strong reference to the service.
     * @return A strong reference to the service; or {@literal null}, if reference is released.
     */
    @Override
    public final S get() {
        return serviceImpl;
    }

    /**
     * Releases this reference.
     * @param context The context of the bundle which holds this reference.
     * @return {@literal false} if the context bundle's use count for the service
     *         is zero or if the service has been unregistered; {@literal true}
     *         otherwise.
     */
    public final boolean release(final BundleContext context) {
        serviceImpl = null;
        return context.ungetService(serviceRef);
    }

    /**
     * Releases this reference.
     * @param context The class loader that was used to create this service holder.
     * @return {@literal false} if the context bundle's use count for the service
     *         is zero or if the service has been unregistered; {@literal true}
     *         otherwise.
     *  @since 1.2
     */
    public final boolean release(final ClassLoader context){
        if(context instanceof BundleReference)
            return release(getBundleContext((BundleReference)context));
        else {
            serviceImpl = null;
            return true;
        }
    }

    /**
     * Returns the property value to which the specified property key is mapped
     * in the properties {@code Dictionary} object of the service
     * referenced by this {@code ServiceReference} object.
     * <p/>
     * <p/>
     * Property keys are case-insensitive.
     * <p/>
     * <p/>
     * This method must continue to return property values after the service has
     * been unregistered. This is so references to unregistered services (for
     * example, {@code ServiceReference} objects stored in the log) can
     * still be interrogated.
     *
     * @param key The property key.
     * @return The property value to which the key is mapped; {@code null}
     * if there is no property named after the key.
     */
    @Override
    public final Object getProperty(final String key) {
        return serviceRef.getProperty(key);
    }

    /**
     * Returns an array of the keys in the properties {@code Dictionary} object
     * of the service referenced by this {@code ServiceReference} object.
     * <p/>
     * <p/>
     * This method will continue to return the keys after the service has been
     * unregistered. This is so references to unregistered services (for
     * example, {@code ServiceReference} objects stored in the log) can still be
     * interrogated.
     * <p/>
     * <p/>
     * This method is <i>case-preserving </i>; this means that every key in the
     * returned array must have the same case as the corresponding key in the
     * properties {@code Dictionary} that was passed to the
     * {@link BundleContext#registerService(Class, Object, java.util.Dictionary)} or
     * {@link ServiceRegistration#setProperties(java.util.Dictionary)} methods.
     *
     * @return An array of property keys.
     */
    @Override
    public final String[] getPropertyKeys() {
        return serviceRef.getPropertyKeys();
    }

    /**
     * Get all properties associated with this reference.
     * @return All properties associated with this reference.
     */
    public final Map<String, ?> getProperties(){
        final String[] keys = getPropertyKeys();
        final Map<String, Object> result = Maps.newHashMapWithExpectedSize(keys.length);
        for(final String key: keys)
            result.put(key, getProperty(key));
        return result;
    }

    /**
     * Returns the bundle that registered the service referenced by this
     * {@code ServiceReference} object.
     * <p/>
     * <p/>
     * This method must return {@code null} when the service has been
     * unregistered. This can be used to determine if the service has been
     * unregistered.
     *
     * @return The bundle that registered the service referenced by this
     * {@code ServiceReference} object; {@code null} if that
     * service has already been unregistered.
     * @see BundleContext#registerService(String[], Object, java.util.Dictionary)
     */
    @Override
    public final Bundle getBundle() {
        return serviceRef.getBundle();
    }

    /**
     * Returns the bundles that are using the service referenced by this
     * {@code ServiceReference} object. Specifically, this method returns
     * the bundles whose usage count for that service is greater than zero.
     *
     * @return An array of bundles whose usage count for the service referenced
     * by this {@code ServiceReference} object is greater than
     * zero; {@code null} if no bundles are currently using that
     * service.
     */
    @Override
    public final Bundle[] getUsingBundles() {
        return serviceRef.getUsingBundles();
    }

    /**
     * Tests if the bundle that registered the service referenced by this
     * {@code ServiceReference} and the specified bundle use the same
     * source for the package of the specified class name.
     * <p/>
     * This method performs the following checks:
     * <ol>
     * <li>Get the package name from the specified class name.</li>
     * <li>For the bundle that registered the service referenced by this
     * {@code ServiceReference} (registrant bundle); find the source for
     * the package. If no source is found then return {@code true} if the
     * registrant bundle is equal to the specified bundle; otherwise return
     * {@code false}.</li>
     * <li>If the package source of the registrant bundle is equal to the
     * package source of the specified bundle then return {@code true};
     * otherwise return {@code false}.</li>
     * </ol>
     *
     * @param bundle    The {@code Bundle} object to check.
     * @param className The class name to check.
     * @return {@code true} if the bundle which registered the service
     * referenced by this {@code ServiceReference} and the
     * specified bundle use the same source for the package of the
     * specified class name. Otherwise {@code false} is returned.
     * @throws IllegalArgumentException If the specified {@code Bundle} was
     *                                  not created by the same framework instance as this
     *                                  {@code ServiceReference}.
     */
    @Override
    public final boolean isAssignableTo(final Bundle bundle, final String className) {
        return serviceRef.isAssignableTo(bundle, className);
    }

    /**
     * Compares this {@code ServiceReference} with the specified
     * {@code ServiceReference} for order.
     * <p/>
     * <p/>
     * If this {@code ServiceReference} and the specified
     * {@code ServiceReference} have the same {@link Constants#SERVICE_ID
     * service id} they are equal. This {@code ServiceReference} is less
     * than the specified {@code ServiceReference} if it has a lower
     * {@link Constants#SERVICE_RANKING service ranking} and greater if it has a
     * higher service ranking. Otherwise, if this {@code ServiceReference}
     * and the specified {@code ServiceReference} have the same
     * {@link Constants#SERVICE_RANKING service ranking}, this
     * {@code ServiceReference} is less than the specified
     * {@code ServiceReference} if it has a higher
     * {@link Constants#SERVICE_ID service id} and greater if it has a lower
     * service id.
     *
     * @param reference The {@code ServiceReference} to be compared.
     * @return Returns a negative integer, zero, or a positive integer if this
     * {@code ServiceReference} is less than, equal to, or greater
     * than the specified {@code ServiceReference}.
     * @throws IllegalArgumentException If the specified
     *                                  {@code ServiceReference} was not created by the same
     *                                  framework instance as this {@code ServiceReference}.
     */
    @Override
    public final int compareTo(final Object reference) {
        return serviceRef.compareTo(reference);
    }

    public final boolean equals(final ServiceHolder<?> refHolder){
        return refHolder != null && Objects.equals(serviceRef, refHolder.serviceRef);
    }

    @Override
    public final boolean equals(final Object obj) {
        return obj instanceof ServiceHolder<?> &&
                equals((ServiceHolder<?>)obj);
    }

    @Override
    public final int hashCode() {
        return serviceRef.hashCode();
    }

    @Override
    public String toString() {
        return Objects.toString(serviceImpl, "Released");
    }
}
