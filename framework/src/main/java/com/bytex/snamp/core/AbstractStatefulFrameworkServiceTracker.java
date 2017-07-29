package com.bytex.snamp.core;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.concurrent.LazyReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.concurrent.Immutable;
import javax.management.InstanceNotFoundException;
import java.util.Map;
import java.util.Objects;

/**
 * Represents configurable tracking service.
 * @author Roman Sakno
 * @param <S> Type of service to track.
 * @param <C> Type of service client.
 * @param <F> Configuration scheme
 * @since 2.0
 * @version 2.1
 */
public abstract class AbstractStatefulFrameworkServiceTracker<S extends FrameworkService, C extends ServiceHolder<S> & SafeCloseable, F extends Map<String, ?>> extends AbstractFrameworkServiceTracker<S,C> implements StatefulFrameworkService {
    /**
     * Represents internal state of the service.
     * @param <C> Configuration scheme.
     */
    @Immutable
    protected static class InternalState<C extends Map<String, ?>> {
        protected final C configuration;
        protected final FrameworkServiceState state;

        protected InternalState(@Nonnull final FrameworkServiceState state, @Nonnull final C params) {
            this.state = state;
            this.configuration = params;
        }

        public InternalState(@Nonnull final C emptyParams){
            this(FrameworkServiceState.CREATED, emptyParams);
        }

        public InternalState<C> setConfiguration(@Nonnull final C value) {
            return new InternalState<>(state, value);
        }

        public InternalState<C> transition(final FrameworkServiceState value) {
            switch (value) {
                case CLOSED:
                    return null;
                default:
                    return new InternalState<>(value, configuration);
            }
        }

        protected boolean configurationAreEqual(final Map<String, ?> other) {
            if (configuration.size() == other.size()) {
                for (final Map.Entry<String, ?> entry : other.entrySet())
                    if (!Objects.equals(configuration.get(entry.getKey()), entry.getValue()))
                        return false;
                return true;
            } else
                return false;
        }

        private boolean equals(final InternalState<?> other) {
            return state.equals(other.state) && configurationAreEqual(other.configuration);
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof InternalState<?> && equals((InternalState<?>) other);
        }

        @Override
        public String toString() {
            return state.toString();
        }

        @Override
        public int hashCode() {
            return Objects.hash(state, configuration);
        }
    }

    private volatile InternalState<F> mutableState;
    private final F initialConfig;
    private final LazyReference<Filter> resourceFilterCache = LazyReference.strong();

    protected AbstractStatefulFrameworkServiceTracker(@Nonnull final Class<S> serviceType, @Nonnull final InternalState<F> initialState){
        super(serviceType);
        mutableState = initialState;
        initialConfig = initialState.configuration;
    }

    /**
     * Gets runtime configuration of this service.
     *
     * @return Runtime configuration of this service.
     * @implSpec Returning map is always immutable.
     */
    @Nonnull
    @Override
    public final F getConfiguration() {
        final InternalState<F> currentState = mutableState;
        return currentState == null ? initialConfig : currentState.configuration;
    }

    /**
     * Gets state of this service.
     *
     * @return Service type.
     */
    @Override
    public final FrameworkServiceState getState() {
        final InternalState<?> currentState = mutableState;
        return currentState == null ? FrameworkServiceState.CLOSED : currentState.state;
    }

    /**
     * Returns filter used to query services from OSGi Service Registry.
     * @return A filter used to query services from OSGi Service Registry.
     */
    @Nonnull
    protected abstract ServiceSelector createServiceFilter();

    /**
     * Stops tracking services.
     * <p>
     *     This method will be called by SNAMP infrastructure automatically.
     * @throws java.lang.Exception Unable to stop tracking resources.
     */
    protected abstract void stop() throws Exception;

    protected void stopped(){

    }

    private synchronized void doStop() throws Exception {
        final BundleContext context = getBundleContext();
        context.removeServiceListener(this);
        final InternalState<F> currentState = mutableState;
        switch (currentState.state) {
            case STARTED:
                final ServiceSelector filter = createServiceFilter();
                try {
                    stop();
                    for (final ServiceReference<S> serviceRef : filter.getServiceReferences(context, serviceContract)) {
                        try (final C client = createClient(serviceRef)) {
                            removeService(client);
                        } catch (final InstanceNotFoundException e){
                            logInvalidServiceRef(getLogger(), e);
                        }
                    }
                } finally {
                    mutableState = currentState.transition(FrameworkServiceState.STOPPED);
                    trackedServices.clear();
                    resourceFilterCache.reset();
                }
                stopped();
        }
    }

    /**
     * Starts the tracking resources.
     * <p>
     *     This method will be called by SNAMP infrastructure automatically.
     * </p>
     * @param configuration Tracker startup parameters.
     * @throws java.lang.Exception Unable to start tracking.
     */
    protected abstract void start(final F configuration) throws Exception;

    protected void started(){

    }

    @Nonnull
    @Override
    protected final Filter getServiceFilter() {
        return resourceFilterCache.lazyGet(this, tracker -> tracker.createServiceFilter().get());
    }

    private synchronized void doStart(final F newConfiguration) throws Exception {
        final InternalState<F> currentState = mutableState;
        switch (currentState.state) {
            case CREATED:
            case STOPPED:
                final ServiceSelector filter = createServiceFilter();
                //explore all available resources
                final BundleContext context = getBundleContext();
                filter.addServiceListener(context, this);
                try {
                    for (final ServiceReference<S> serviceRef : filter.getServiceReferences(context, serviceContract)) {
                        try (final C client = createClient(serviceRef)) {
                            addService(client);
                            trackedServices.add(getServiceId(client));
                        } catch (final InstanceNotFoundException e){
                            logInvalidServiceRef(getLogger(), e);
                        }
                    }
                    start(newConfiguration);
                    mutableState = currentState.transition(FrameworkServiceState.STARTED).setConfiguration(newConfiguration);
                    started();
                } catch (final Exception e) {
                    context.removeServiceListener(this);    //remove subscription when exception to avoid memory leaks
                    throw e;
                }
        }
    }

    /**
     * Updates configuration of this tracker.
     * @param current The current configuration of the tracker.
     * @param newParameters A newly supplied configuration of the tracker.
     * @return {@literal true} to replace existing configuration with newly supplied; otherwise, {@literal false}.
     * @throws Exception Unable to update tracker.
     */
    protected boolean update(final F current,
                             final F newParameters) throws Exception {
        doStop();
        doStart(newParameters);
        return false;
    }

    /**
     * Updates this tracker with new configuration.
     * @param configuration A new configuration. Cannot be {@literal null}.
     * @throws Exception Unable to update tracker.
     */
    public final void update(@Nonnull final F configuration) throws Exception{
        final InternalState<F> currentState = mutableState;
        switch (currentState.state) {
            case CREATED:
                doStart(configuration);
            case STARTED:
                //compare parameters
                if (!currentState.configurationAreEqual(configuration) && update(currentState.configuration, configuration))
                    mutableState = currentState.setConfiguration(configuration);
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void close() throws Exception {
        try {
            doStop();
        } finally {
            mutableState = mutableState.transition(FrameworkServiceState.CLOSED);
            clearCache();
        }
    }
}
