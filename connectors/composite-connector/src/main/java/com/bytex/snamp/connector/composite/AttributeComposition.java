package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.attributes.DistributedAttributeRepository;
import com.bytex.snamp.connector.composite.functions.AggregationFunction;
import com.bytex.snamp.connector.composite.functions.NameResolver;
import com.bytex.snamp.jmx.WellKnownType;
import com.bytex.snamp.parser.ParseException;

import javax.management.*;
import javax.management.openmbean.SimpleType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.internal.Utils.*;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class AttributeComposition extends DistributedAttributeRepository<AbstractCompositeAttribute> implements NameResolver, NotificationListener {
    private final AttributeSupportProvider attributeSupportProvider;
    private final Logger logger;
    private final ExecutorService threadPool;

    AttributeComposition(final String resourceName,
                         final AttributeSupportProvider provider,
                         final ExecutorService threadPool,
                         final Duration syncPeriod,
                         final Logger logger){
        super(resourceName, AbstractCompositeAttribute.class, false, syncPeriod);
        attributeSupportProvider = Objects.requireNonNull(provider);
        this.logger = Objects.requireNonNull(logger);
        this.threadPool = Objects.requireNonNull(threadPool);
    }

    /**
     * Gets thread pool used to synchronize attribute states across cluster.
     *
     * @return Thread pool instance.
     */
    @Override
    protected ExecutorService getThreadPool() {
        return threadPool;
    }

    private Object resolveAs(final String operand, final WellKnownType expectedType) throws Exception{
        if(getAttributeInfo(operand) == null)
            throw new IllegalArgumentException(String.format("Could not find suitable attribute for operand '%s'", operand));
        final Object value = getAttribute(operand);
        switch (expectedType){
            case INT:
                return convertToInt(value, Number.class, Number::intValue, v -> Integer.parseInt(v.toString()));
            case BYTE:
                return convertTo(value, Number.class, Number::byteValue, v -> Byte.parseByte(v.toString()));
            case SHORT:
                return convertTo(value, Number.class, Number::shortValue, v -> Short.parseShort(v.toString()));
            case LONG:
                return convertToLong(value, Number.class, Number::longValue, v -> Long.parseLong(v.toString()));
            case FLOAT:
                return convertTo(value, Number.class, Number::floatValue, v -> Float.parseFloat(v.toString()));
            case DOUBLE:
                return convertToDouble(value, Number.class, Number::doubleValue, v -> Double.parseDouble(v.toString()));
            case BIG_INT:
                return new BigInteger(value.toString());
            case BIG_DECIMAL:
                return new BigDecimal(value.toString());
            case BOOL:
                return convertTo(value, Boolean.class, Function.identity(), v -> Boolean.parseBoolean(v.toString()));
            case STRING:
                return value.toString();
            case CHAR:
                final String str = value.toString();
                return str.length() > 0 ? str.charAt(0) : '\0';
            default:
                throw new ClassCastException(String.format("Unable cast '%s' to '%s'", value, expectedType));
        }
    }

    /**
     * Takes snapshot of the attribute to distribute it across cluster.
     *
     * @param attribute The attribute that should be synchronized across cluster.
     * @return Serializable state of the attribute; or {@literal null}, if attribute doesn't support synchronization across cluster.
     */
    @Override
    protected Serializable takeSnapshot(final AbstractCompositeAttribute attribute) {
        return convertTo(attribute, DistributedAttribute.class, DistributedAttribute::takeSnapshot);
    }

    /**
     * Initializes state of the attribute using its serializable snapshot.
     *
     * @param attribute The attribute to initialize.
     * @param snapshot  Serializable snapshot used for initialization.
     */
    @Override
    protected void loadFromSnapshot(final AbstractCompositeAttribute attribute, final Serializable snapshot) {
        if (attribute instanceof DistributedAttribute)
            ((DistributedAttribute) attribute).loadFromSnapshot(snapshot);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T resolveAs(final String name, final SimpleType<T> expectedType) throws Exception {
        return (T)resolveAs(name, WellKnownType.getType(expectedType));
    }

    /**
     * Removes the attribute from the connector.
     *
     * @param attributeInfo An attribute metadata.
     */
    @Override
    protected void disconnectAttribute(final AbstractCompositeAttribute attributeInfo) {
        if (attributeInfo instanceof CompositeFeature) {
            final AttributeSupport support = attributeSupportProvider.getAttributeSupport(((CompositeFeature) attributeInfo).getConnectorType());
            if (support != null)
                support.removeAttribute(attributeInfo.getName());
        }
    }

    @Override
    protected AbstractCompositeAttribute connectAttribute(final String attributeName,
                                                          final AttributeDescriptor descriptor) throws Exception {
        if (CompositeResourceConfigurationDescriptor.isRateFormula(descriptor)) //rate attribute
            return new NotificationRateAttribute(attributeName, descriptor);
        else if(CompositeResourceConfigurationDescriptor.isGroovyFormula(descriptor))   //groovy attribute
            return new GroovyAttribute(attributeName, getClass().getClassLoader(), logger, descriptor);
        //aggregation
        final AggregationFunction<?> function = CompositeResourceConfigurationDescriptor.parseFormula(descriptor);
        if (function != null)
            return new AggregationAttribute(attributeName, function, this, descriptor);
        //regular attribute
        final String connectorType = CompositeResourceConfigurationDescriptor.parseSource(descriptor);
        final AttributeSupport support = attributeSupportProvider.getAttributeSupport(connectorType);
        if (support == null)
            throw new ReflectionException(new UnsupportedOperationException(String.format("Connector '%s' doesn't support attributes", connectorType)));
        else {
            //process regular attribute
            final MBeanAttributeInfo underlyingAttribute = support.addAttribute(attributeName, descriptor);
            if (underlyingAttribute == null)
                throw AliasAttribute.attributeNotFound(connectorType, attributeName);
            //check whether the type of function is compatible with type of attribute
            return new AliasAttribute(connectorType, underlyingAttribute);
        }
    }


    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        parallelForEach(attribute -> {
            if (attribute instanceof NotificationListener)
                ((NotificationListener) attribute).handleNotification(notification, handback);
        }, getThreadPool());
    }

    @Override
    protected void failedToConnectAttribute(final String attributeName, final Exception e) {
        failedToConnectAttribute(logger, Level.WARNING, attributeName, e);
    }

    @Override
    protected Object getAttribute(final AbstractCompositeAttribute metadata) throws Exception {
        if(metadata instanceof AliasAttribute)
            return ((AliasAttribute) metadata).getValue(attributeSupportProvider);
        else if(metadata instanceof ProcessingAttribute)
            return ((AggregationAttribute) metadata).getValue(this);
        else if(metadata instanceof MetricAttribute<?>)
            return ((MetricAttribute<?>) metadata).getValue();
        else
            throw new UnsupportedOperationException();
    }

    @Override
    protected void failedToGetAttribute(final String attributeID, final Exception e) {
        failedToGetAttribute(logger, Level.WARNING, attributeID, e);
    }

    @Override
    protected void setAttribute(final AbstractCompositeAttribute attribute, final Object value) throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException {
        if(attribute instanceof AliasAttribute)
            ((AliasAttribute) attribute).setValue(attributeSupportProvider, value);
        else
            throw new UnsupportedOperationException();
    }

    @Override
    protected void failedToSetAttribute(final String attributeID, final Object value, final Exception e) {
        failedToSetAttribute(logger, Level.WARNING, attributeID, value, e);
    }
}
