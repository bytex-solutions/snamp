package com.bytex.snamp.connector.actuator;

import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeManager;
import com.bytex.snamp.connector.attributes.AttributeRepository;
import com.bytex.snamp.connector.health.*;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.bytex.snamp.json.JsonUtils;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jackson.JsonNode;

import javax.annotation.Nonnull;
import javax.management.*;
import javax.management.openmbean.OpenDataException;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;

/**
 * Collects information from Spring Actuator endpoints including health information and metrics.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class ActuatorConnector extends AbstractManagedResourceConnector implements AttributeManager {
    private static final String STATUS_FIELD = "status";
    private static final Predicate<String> IGNORE_STATUS_FIELD = Predicate.<String>isEqual(STATUS_FIELD).negate();

    private static final class ActuatorAttributes extends AttributeRepository<SpringMetric> implements AttributeRepository.AttributeReader<SpringMetric>{
        private final WebResource metricsResource;

        private ActuatorAttributes(final WebResource metricsResource){
            this.metricsResource = Objects.requireNonNull(metricsResource);
        }

        Map<String, AttributeDescriptor> discoverAttributes() {
            final JsonNode metrics = metricsResource.get(JsonNode.class);
            final Map<String, AttributeDescriptor> result = new HashMap<>();
            metrics.getFields().forEachRemaining(field -> result.put(field.getKey(), AttributeDescriptor.EMPTY_DESCRIPTOR));
            return result;
        }

        @Override
        public Object getAttributeValue(final SpringMetric attribute) throws Exception {
            final JsonNode node = metricsResource.get(JsonNode.class);
            if (node.isObject()) {
                final JsonNode valueNode = node.get(attribute.getName());
                if (valueNode.isValueNode())
                    return attribute.getValue(valueNode);
                else
                    throw new OpenDataException(String.format("'%s' is not a scalar value", valueNode));
            } else
                throw new OpenDataException(String.format("Unexpected metrics: %s", node));
        }

        Object getAttributeValue(final String attributeName) throws MBeanException, AttributeNotFoundException, ReflectionException {
            return getAttribute(attributeName, this);
        }

        private static Optional<SpringMetric<?>> createAttribute(final String attributeName,
                                                                 final JsonNode valueNode,
                                                                 final AttributeDescriptor descriptor) {
            if (valueNode == null)
                return Optional.empty();
            else if (valueNode.isInt() || valueNode.isLong())
                return Optional.of(new IntegerSpringMetric(attributeName, descriptor));
            else if (valueNode.isTextual())
                return Optional.of(new TextSpringMetric(attributeName, descriptor));
            else if (valueNode.isDouble())
                return Optional.of(new DoubleSpringMetric(attributeName, descriptor));
            else if (valueNode.isBigInteger())
                return Optional.of(new BigIntegerSpringMetric(attributeName, descriptor));
            else if (valueNode.isBigDecimal())
                return Optional.of(new DecimalSpringMetric(attributeName, descriptor));
            else if (valueNode.isBoolean())
                return Optional.of(new BooleanSpringMetric(attributeName, descriptor));
            else if (valueNode.isBinary())
                return Optional.of(new BinarySpringMetric(attributeName, descriptor));
            else
                return Optional.empty();
        }

        @Nonnull
        SpringMetric<?> createAttribute(final String attributeName, final AttributeDescriptor descriptor) throws Exception {
            final JsonNode node = metricsResource.get(JsonNode.class);
            final String metricName = descriptor.getAlternativeName().orElse(attributeName);
            if (node.isObject())
                return createAttribute(attributeName, node.get(metricName), descriptor)
                        .orElseThrow(() -> new OpenDataException(String.format("Unsupported format of metric %s. JSON: %s", metricName, node)));
            else
                throw new OpenDataException(String.format("Unexpected metrics: %s", node));
        }
    }

    private final WebResource healthResource;
    private final ActuatorAttributes attributes;

    ActuatorConnector(final String resourceName,
                              final ActuatorConnectionOptions options) {
        super(resourceName);
        healthResource = options.getHealthResource();
        attributes = new ActuatorAttributes(options.getMetricsResource());
    }

    /**
     * Registers a new attribute in the managed resource connector.
     *
     * @param attributeName The name of the attribute in the managed resource.
     * @param descriptor    Descriptor of created attribute.
     * @throws JMException Unable to instantiate attribute.
     * @since 2.0
     */
    @Override
    public void addAttribute(final String attributeName, final AttributeDescriptor descriptor) throws JMException {
        addFeature(attributes, attributeName, descriptor, attributes::createAttribute);
    }

    /**
     * Removes attribute from the managed resource.
     *
     * @param attributeName Name of the attribute to remove.
     * @return {@literal true}, if attribute is removed successfully; otherwise, {@literal false}.
     * @since 2.0
     */
    @Override
    public boolean removeAttribute(final String attributeName) {
        return removeFeature(attributes, attributeName);
    }

    /**
     * Removes all attributes except specified in the collection.
     *
     * @param attributes A set of attributes which should not be deleted.
     * @since 2.0
     */
    @Override
    public void retainAttributes(final Set<String> attributes) {
        retainFeatures(this.attributes, attributes);
    }

    @Override
    public Map<String, AttributeDescriptor> discoverAttributes() {
        return attributes.discoverAttributes();
    }

    /**
     * Obtain the value of a specific attribute of the managed resource.
     *
     * @param attributeName The name of the attribute to be retrieved
     * @return The value of the attribute retrieved.
     * @throws AttributeNotFoundException Attribute doesn't exist.
     * @throws MBeanException             Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's getter.
     * @throws ReflectionException        Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the getter.
     * @see #setAttribute(Attribute)
     */
    @Override
    public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException, ReflectionException {
        return attributes.getAttributeValue(attributeName);
    }

    /**
     * Gets an array of supported attributes.
     *
     * @return An array of supported attributes.
     */
    @Override
    public SpringMetric<?>[] getAttributeInfo() {
        return getFeatureInfo(attributes, SpringMetric.class);
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

    @Override
    protected MetricsSupport createMetricsReader() {
        return assembleMetricsReader(attributes.metrics);
    }

    /**
     * Releases all resources associated with this connector.
     *
     * @throws Exception Unable to release resources associated with this connector.
     */
    @Override
    public void close() throws Exception {
        removeFeatures(attributes);
        super.close();
    }
}
