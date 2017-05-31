package com.bytex.snamp.connector.actuator;

import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.ResourceEventListener;
import com.bytex.snamp.connector.health.*;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.bytex.snamp.json.JsonUtils;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jackson.JsonNode;

import javax.annotation.Nonnull;
import javax.management.MBeanException;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Collects information from Spring Actuator endpoints including health information and metrics.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ActuatorConnector extends AbstractManagedResourceConnector implements HealthCheckSupport {
    private static final String STATUS_FIELD = "status";
    private static final Predicate<String> IGNORE_STATUS_FIELD = Predicate.<String>isEqual(STATUS_FIELD).negate();

    private final WebResource healthResource;
    @Aggregation(cached = true)
    private final ActuatorAttributeRepository attributes;

    private ActuatorConnector(final String resourceName,
                              final ActuatorConnectionOptions options) {
        healthResource = options.getHealthResource();
        attributes = new ActuatorAttributeRepository(resourceName, options.getMetricsResource());
    }

    ActuatorConnector(final String resourceName,
                      final URI actuatorUri,
                      final ManagedResourceInfo configuration) {
        this(resourceName, new ActuatorConnectionOptions(actuatorUri, configuration));
        setConfiguration(configuration);
    }

    private static HealthStatus toHealthStatus(final JsonNode healthNode) {
        final ActuatorHealthStatus status = ActuatorHealthStatus.valueOf(healthNode.get(STATUS_FIELD));
        if (!ActuatorHealthStatus.UP.equals(status))
            for (final Iterator<Map.Entry<String, JsonNode>> fields = healthNode.getFields(); fields.hasNext(); ) {
                final Map.Entry<String, JsonNode> field = fields.next();
                if(field.getKey().equals(STATUS_FIELD))
                    continue;
                final ActuatorHealthStatus subsystemStatus = ActuatorHealthStatus.valueOf(field.getValue().get(STATUS_FIELD));
                if (subsystemStatus.equals(status)) { //root cause detected
                    final ResourceMalfunctionStatus healthStatus;
                    switch (subsystemStatus) {
                        case DOWN:
                            healthStatus = new ResourceSubsystemDownStatus(Instant.now(), field.getKey());
                            break;
                        case OUT_OF_SERVICE:
                            healthStatus = new ResourceSubsystemDownStatus(Instant.now(), field.getKey(), MalfunctionStatus.Level.LOW);
                            break;
                        default:
                            healthStatus = new ResourceSubsystemDownStatus(Instant.now(), field.getKey(), MalfunctionStatus.Level.MODERATE);
                    }
                    JsonUtils.exportToMap(field.getValue(), healthStatus.getData(), IGNORE_STATUS_FIELD);
                    return healthStatus;
                }
            }
        return new OkStatus();
    }

    /**
     * Determines whether the connected managed resource is alive.
     *
     * @return Status of the remove managed resource.
     */
    @Override
    @Nonnull
    public HealthStatus getStatus() {
        HealthStatus status;
        try {
            final ClientResponse response = healthResource.get(ClientResponse.class);
            final JsonNode statusNode = response.getEntity(JsonNode.class);
            return statusNode == null ?
                    new ConnectionProblem(new IOException(String.format("Status code %s (%s)", response.getStatus(), response.getStatusInfo().getReasonPhrase()))) :
                    toHealthStatus(statusNode);
        } catch (final UniformInterfaceException e) {
            status = new ConnectionProblem(new IOException(e));
        } catch (final Exception e) {
            status = e.getCause() instanceof IOException ?
                    new ConnectionProblem((IOException) e.getCause()) :
                    new ResourceConnectorMalfunction(new MBeanException(e));
        }
        return status;
    }

    /**
     * Adds a new listener for the connector-related events.
     * <p>
     * The managed resource connector should holds a weak reference to all added event listeners.
     *
     * @param listener An event listener to add.
     */
    @Override
    public void addResourceEventListener(final ResourceEventListener listener) {
        addResourceEventListener(listener, attributes);
    }

    /**
     * Removes connector event listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    public void removeResourceEventListener(final ResourceEventListener listener) {
        removeResourceEventListener(listener, attributes);
    }

    @Override
    protected MetricsSupport createMetricsReader() {
        return assembleMetricsReader(attributes);
    }

    /**
     * Releases all resources associated with this connector.
     *
     * @throws Exception Unable to release resources associated with this connector.
     */
    @Override
    public void close() throws Exception {
        attributes.close();
        super.close();
    }
}
