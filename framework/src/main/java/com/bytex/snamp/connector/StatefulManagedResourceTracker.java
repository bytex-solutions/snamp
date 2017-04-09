package com.bytex.snamp.connector;

import com.bytex.snamp.core.FrameworkServiceState;
import com.bytex.snamp.core.StatefulFrameworkService;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.concurrent.Immutable;
import java.util.Map;
import java.util.Objects;

/**
 * Represents resource tracker
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class StatefulManagedResourceTracker<C extends Map<String, String>> extends AbstractManagedResourceTracker implements StatefulFrameworkService {
    @Immutable
    protected static class InternalState<C extends Map<String, String>> {
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

        protected boolean configurationAreEqual(final Map<String, String> other) {
            if (configuration.size() == other.size()) {
                for (final Map.Entry<String, String> entry : other.entrySet())
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

    private volatile InternalState<C> mutableState;
    private final C initialConfig;

    protected StatefulManagedResourceTracker(@Nonnull final InternalState<C> initialState){
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
    public final C getConfiguration() {
        final InternalState<C> currentState = mutableState;
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
     * Stops tracking resources.
     * <p>
     *     This method will be called by SNAMP infrastructure automatically.
     * @throws java.lang.Exception Unable to stop tracking resources.
     */
    protected abstract void stop() throws Exception;

    protected void stopped(){
        
    }

    private synchronized void doStop() throws Exception {
        getBundleContext().removeServiceListener(this);
        final InternalState<C> currentState = mutableState;
        switch (currentState.state) {
            case STARTED:
                try {
                    stop();
                    final BundleContext context = getBundleContext();
                    for (final String resourceName : createResourceFilter().getResources(context)) {
                        final ManagedResourceConnectorClient client = ManagedResourceConnectorClient.tryCreate(context, resourceName);
                        if (client != null)
                            try {
                                removeResource(client);
                            } finally {
                                client.close();
                            }
                    }
                } finally {
                    mutableState = currentState.transition(FrameworkServiceState.STOPPED);
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
    protected abstract void start(final C configuration) throws Exception;

    protected void started(){

    }

    /**
     * Returns filter used to query managed resource connectors from OSGi environment.
     * @return A filter used to query managed resource connectors from OSGi environment.
     */
    @Nonnull
    @Override
    protected ManagedResourceFilterBuilder createResourceFilter() {
        return ManagedResourceConnectorClient.filterBuilder();
    }

    private synchronized void doStart(final C newConfiguration) throws Exception {
        final InternalState<C> currentState = mutableState;
        switch (currentState.state) {
            case CREATED:
            case STOPPED:
                final ManagedResourceFilterBuilder filter = createResourceFilter();
                //explore all available resources
                final BundleContext context = getBundleContext();
                for (final String resourceName : filter.getResources(context)) {
                    final ManagedResourceConnectorClient client = ManagedResourceConnectorClient.tryCreate(context, resourceName);
                    if (client != null)
                        try {
                            addResource(client);
                        } finally {
                            client.close();
                        }
                }
                start(newConfiguration);
                mutableState = currentState.transition(FrameworkServiceState.STARTED).setConfiguration(newConfiguration);
                started();
                filter.addServiceListener(context, this);
        }
    }

    /**
     * Updates configuration of this tracker.
     * @param current The current configuration of the tracker.
     * @param newParameters A newly supplied configuration of the tracker.
     * @return {@literal true} to replace existing configuration with newly supplied; otherwise, {@literal false}.
     * @throws Exception Unable to update tracker.
     */
    protected boolean update(final C current,
                             final C newParameters) throws Exception {
        doStop();
        doStart(newParameters);
        return false;
    }

    /**
     * Updates this tracker with new configuration.
     * @param configuration A new configuration. Cannot be {@literal null}.
     * @throws Exception Unable to update tracker.
     */
    public final void update(@Nonnull final C configuration) throws Exception{
        final InternalState<C> currentState = mutableState;
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
