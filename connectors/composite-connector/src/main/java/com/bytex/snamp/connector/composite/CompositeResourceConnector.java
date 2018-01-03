package com.bytex.snamp.connector.composite;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.ResourceEventListener;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeManager;
import com.bytex.snamp.connector.attributes.AttributeRepository;
import com.bytex.snamp.connector.composite.functions.AggregationFunction;
import com.bytex.snamp.connector.composite.functions.EvaluationContext;
import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.bytex.snamp.core.ReplicationSupport;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import javax.annotation.Nonnull;
import javax.management.*;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Represents resource connector that can combine many resource connectors.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
final class CompositeResourceConnector extends AbstractManagedResourceConnector implements ReplicationSupport<Replica>,
        EvaluationContext,
        AttributeManager{

    private final ScriptLoader scriptLoader;
    private String connectionStringSeparator;
    private final Composition connectors;
    private final AttributeRepository<AbstractCompositeAttribute> attributes;
    @Aggregation(cached = true)
    private final NotificationComposition notifications;
    @Aggregation(cached = true)
    private final OperationComposition operations;

    CompositeResourceConnector(final String resourceName,
                               final ExecutorService threadPool,
                               final URL[] groovyPath) {
        super(resourceName);
        connectionStringSeparator = ";";
        connectors = new Composition(resourceName);
        scriptLoader = new ScriptLoader(getClass().getClassLoader(), groovyPath);
        attributes = new AttributeRepository<>();
        notifications = new NotificationComposition(resourceName, connectors, threadPool);
        notifications.addNotificationListener(attributes, null, null);
        notifications.setSource(this);
        operations = new OperationComposition(resourceName, connectors);
    }

    private AbstractCompositeAttribute createAttribute(final String attributeName, final AttributeDescriptor descriptor) throws Exception {
        if (CompositeResourceConfigurationDescriptor.isRateFormula(descriptor)) //rate attribute
            return new NotificationRateAttribute(attributeName, descriptor);
        else if (CompositeResourceConfigurationDescriptor.isGroovyFormula(descriptor))   //groovy attribute
            return new GroovyAttribute(attributeName, scriptLoader, descriptor);
        //aggregation
        final AggregationFunction<?> function = CompositeResourceConfigurationDescriptor.parseFormula(descriptor);
        if (function != null)
            return new AggregationAttribute(attributeName, function, this, descriptor);
        //regular attribute
        final String connectorType = CompositeResourceConfigurationDescriptor.parseSource(descriptor);
        final MBeanAttributeInfo underlyingAttribute = connectors.addAttribute(connectorType, attributeName, descriptor);
        //check whether the type of function is compatible with type of attribute
        return new AliasAttribute(connectorType, underlyingAttribute);
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
        addFeature(attributes, attributeName, descriptor, this::createAttribute);
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

    /**
     * Gets an array of supported attributes.
     *
     * @return An array of supported attributes.
     */
    @Override
    public AbstractCompositeAttribute[] getAttributeInfo() {
        return getFeatureInfo(attributes, AbstractCompositeAttribute.class);
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
        return super.getAttribute(attributeName);
    }

    /**
     * Set the value of a specific attribute of the managed resource.
     *
     * @param attribute The identification of the attribute to
     *                  be set and  the value it is to be set to.
     * @throws AttributeNotFoundException     Attribute doesn't exist.
     * @throws InvalidAttributeValueException
     * @throws MBeanException                 Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's setter.
     * @throws ReflectionException            Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the MBean's setter.
     * @see #getAttribute
     */
    @Override
    public void setAttribute(final Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        super.setAttribute(attribute);
    }

    /**
     * Discover attributes.
     *
     * @return A map of discovered attributed that can be added using method {@link #addAttribute(String, AttributeDescriptor)}.
     * @since 2.0
     */
    @Override
    public Map<String, AttributeDescriptor> discoverAttributes() {
        return Collections.emptyMap();
    }

    void setConnectionStringSeparator(final String value){
        connectionStringSeparator = Objects.requireNonNull(value);
    }

    @Override
    public String getReplicaName() {
        return resourceName;
    }

    @Nonnull
    @Override
    public Replica createReplica() {
        final Replica replica = new Replica();
        replica.addToReplica(attributes);
        return replica;
    }

    @Override
    public void loadFromReplica(@Nonnull final Replica replica) {
        replica.restoreFromReplica(attributes);
    }

    void update(final String connectionString, final Map<String, String> parameters) throws Exception {
        final ComposedConfiguration parsedParams = new ComposedConfiguration(connectionStringSeparator);
        parsedParams.parse(connectionString, parameters);
        //do update
        //update supplied connectors
        for(final String connectorType: parsedParams.getConnectorTypes()){
            connectors.updateConnector(connectorType, parsedParams.getConnectionString(connectorType), parsedParams.getParameters(connectorType));
        }
        //dispose connectors that are not specified in the connection string
        connectors.retainConnectors(parsedParams.getConnectorTypes());
    }

    /**
     * Determines whether the connected managed resource is alive.
     *
     * @return Status of the remove managed resource.
     */
    @Override
    @Nonnull
    public HealthStatus getStatus() {
        return connectors.getStatus();
    }

    @Override
    protected MetricsSupport createMetricsReader() {
        return assembleMetricsReader(attributes, operations, notifications);
    }

    /**
     * Releases all resources associated with this connector.
     *
     * @throws Exception Unable to release resources associated with this connector.
     */
    @Override
    public void close() throws Exception {
        connectors.close();
        attributes.close();
        notifications.close();
        operations.close();
        super.close();
    }
}
