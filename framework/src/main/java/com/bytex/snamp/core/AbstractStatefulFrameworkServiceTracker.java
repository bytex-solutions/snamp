package com.bytex.snamp.core;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.concurrent.LazyStrongReference;
import com.bytex.snamp.connector.StatefulManagedResourceTracker;
import org.osgi.framework.Filter;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Роман on 14.04.2017.
 */
public abstract class AbstractStatefulFrameworkServiceTracker<S extends FrameworkService, C extends ServiceHolder<S> & SafeCloseable, F extends Map<String, String>> extends AbstractFrameworkServiceTracker<S,C> implements StatefulFrameworkService {
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

    private volatile InternalState<F> mutableState;
    private final F initialConfig;
    private final LazyStrongReference<Filter> resourceFilterCache = new LazyStrongReference<>();

    protected AbstractStatefulFrameworkServiceTracker(@Nonnull final Class<S> serviceType, @Nonnull final F initialState){
        super(serviceType);
        initialConfig = initialState;
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
}
