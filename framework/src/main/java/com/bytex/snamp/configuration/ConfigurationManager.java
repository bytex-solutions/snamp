package com.bytex.snamp.configuration;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.configuration.internal.CMGatewayParser;
import com.bytex.snamp.core.FrameworkService;
import com.bytex.snamp.core.ServiceHolder;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

/**
 * Represents SNAMP configuration manager that is accessible as OSGi service.
 * <p>
 *     This interface must return an instance of {@link com.bytex.snamp.configuration.internal.CMManagedResourceParser} or
 *     {@link CMGatewayParser} when {@link com.bytex.snamp.Aggregator#queryObject(Class)} is called
 *     with suitable arguments.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface ConfigurationManager extends FrameworkService {
    /**
     * A functional interface used to process SNAMP configuration.
     * @param <E> Type of exception that can be thrown by custom processor.
     * @since 1.2
     */
    @FunctionalInterface
    interface ConfigurationProcessor<E extends Throwable>{
        /**
         * Process SNAMP configuration.
         * @param config A configuration to process.
         * @return {@literal true} to save changes; otherwise, {@literal false}.
         * @throws E An exception thrown by custom processor.
         */
        boolean process(final AgentConfiguration config) throws E;

        static <E extends Throwable> ConfigurationProcessor<E> of(final Acceptor<? super AgentConfiguration, E> acceptor) {
            return config -> {
                acceptor.accept(config);
                return false;
            };
        }
    }

    /**
     * Process SNAMP configuration.
     * @param handler A handler used to process configuration. Cannot be {@literal null}.
     * @param <E> Type of user-defined exception that can be thrown by handler.
     * @throws E An exception thrown by handler.
     * @throws IOException Unrecoverable exception thrown by configuration infrastructure.
     * @since 1.2
     */
    <E extends Throwable> void processConfiguration(final ConfigurationProcessor<E> handler) throws E, IOException;

    /**
     * Read SNAMP configuration.
     * @param handler A handler used to read configuration. Cannot be {@literal null}.
     * @param <E> Type of user-defined exception that can be thrown by handler.
     * @throws E An exception thrown by handler.
     * @throws IOException Unrecoverable exception thrown by configuration infrastructure.
     * @since 1.2
     */
    <E extends Throwable> void readConfiguration(final Acceptor<? super AgentConfiguration, E> handler) throws E, IOException;

    /**
     * Read SNAMP configuration and transform it into custom object.
     * @param handler A handler used to read configuration. Cannot be {@literal null}.
     * @param <O> Type of transformation result.
     * @throws IOException Unrecoverable exception thrown by configuration infrastructure.
     * @since 1.2
     */
    <O> O transformConfiguration(final Function<? super AgentConfiguration, O> handler) throws IOException;

    @Nonnull
    @Override
    Map<String, String> getConfiguration();

    /**
     * Creates a new instance of entity configuration.
     * @param context Class loader of caller code. Cannot be {@literal null}.
     * @param entityType Type of entity. Can be {@link ManagedResourceConfiguration},
     *                  {@link GatewayConfiguration}. {@link AttributeConfiguration}, {@link EventConfiguration}, {@link OperationConfiguration}.
     * @param <E> Type of requested entity.
     * @return A new instance of entity configuration; or {@literal null}, if entity is not supported.
     * @since 1.2
     */
    static <E extends EntityConfiguration> E createEntityConfiguration(final ClassLoader context, final Class<E> entityType) {
        return ServiceHolder.tryCreate(context, ConfigurationManager.class).map(manager -> {
            try {
                return manager.get().transformConfiguration(config -> config.createEntityConfiguration(entityType));
            } catch (final IOException ignored) {
                return null;
            } finally {
                manager.release(context);
            }
        })
                .orElse(null);
    }
}
