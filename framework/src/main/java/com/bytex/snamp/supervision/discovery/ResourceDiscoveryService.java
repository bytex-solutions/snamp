package com.bytex.snamp.supervision.discovery;

import com.bytex.snamp.supervision.SupervisorService;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Provides API for programmatic registration of new resources inside of the group.
 * <p>
 *     Resource discovery represents higher abstraction than direct manipulation
 *     using {@link com.bytex.snamp.configuration.ConfigurationManager}.
 *     For example, supervisor may process or modify resource before placing it into the group
 *     or apply different validations. Also, different protocol adaptors may expose
 *     discovery functionality through HTTP or AMQP.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface ResourceDiscoveryService extends SupervisorService {
    /**
     * Recommended endpoint for service discovery over HTTP.
     */
    String HTTP_ENDPOINT = "/snamp/resource-discovery";

    /**
     * Registers a new resource in the supervised group.
     * @param resourceName Name of the resource to register. Cannot be {@literal null}.
     * @param connectionString Resource connection string. Cannot be {@literal null}.
     * @param parameters Additional configuration parameters to be associated with resource. Cannot be {@literal null}.
     * @throws ResourceDiscoveryException Unable to register resource.
     */
    void registerResource(@Nonnull final String resourceName,
                                 @Nonnull final String connectionString,
                                 @Nonnull final Map<String, String> parameters) throws ResourceDiscoveryException;

    /**
     * Removes resource from the supervised group.
     * @param resourceName Name of the resource to remove. Cannot be {@literal null}.
     * @return {@literal true}, if resource was exist and removed successfully; {@literal false} if resource was not exist.
     * @throws ResourceDiscoveryException Unable to remove resource.
     */
    boolean removeResource(@Nonnull final String resourceName) throws ResourceDiscoveryException;

    /**
     * Removes all resources from supervised group.
     * @throws ResourceDiscoveryException Unable to remove all resources.
     */
    void removeAllResources() throws ResourceDiscoveryException;
}
