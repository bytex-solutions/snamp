package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.attributes.DistributedAttributeRepository;
import com.bytex.snamp.connector.composite.functions.AggregationFunction;
import com.bytex.snamp.connector.composite.functions.FunctionParserException;
import com.bytex.snamp.connector.composite.functions.NameResolver;
import com.bytex.snamp.connector.metrics.Metric;
import com.bytex.snamp.jmx.WellKnownType;

import javax.management.*;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class AttributeComposition extends DistributedAttributeRepository<AbstractCompositeAttribute> implements NameResolver, NotificationListener {
    private static abstract class AbstractSnapshot implements AttributeSnapshot{
        private static final long serialVersionUID = 6372669830203116940L;
        private final String name;

        private AbstractSnapshot(final AbstractCompositeAttribute attribute){
            name = attribute.getName();
        }

        @Override
        public final String getName() {
            return name;
        }
    }

    private static final class MetricSnapshot extends AbstractSnapshot{
        private static final long serialVersionUID = -3335155646667594727L;
        private final Metric metric;

        private MetricSnapshot(final MetricAttribute<?> attribute){
            super(attribute);
            metric = attribute.getMetric();
        }
    }

    private final AttributeSupportProvider attributeSupportProvider;
    private final Logger logger;

    AttributeComposition(final String resourceName,
                         final AttributeSupportProvider provider,
                         final ExecutorService threadPool,
                         final Logger logger){
        super(resourceName, AbstractCompositeAttribute.class, false, threadPool);
        attributeSupportProvider = Objects.requireNonNull(provider);
        this.logger = Objects.requireNonNull(logger);
    }

    private Object resolveAs(final String operand, final WellKnownType expectedType) throws Exception{
        final AbstractCompositeAttribute attribute = get(operand);
        if(attribute == null)
            throw new IllegalArgumentException(String.format("Could not find suitable attribute for operand '%s'", operand));
        final Object value = attribute.getValue(attributeSupportProvider);
        switch (expectedType){
            case INT:
                return value instanceof Number ? ((Number)value).intValue() : Integer.parseInt(value.toString());
            case BYTE:
                return value instanceof Number ? ((Number)value).byteValue() : Byte.parseByte(value.toString());
            case SHORT:
                return value instanceof Number ? ((Number)value).shortValue() :Short.parseShort(value.toString());
            case LONG:
                return value instanceof Number ? ((Number)value).longValue() : Long.parseLong(value.toString());
            case FLOAT:
                return value instanceof Number ? ((Number)value).floatValue() : Float.parseFloat(value.toString());
            case DOUBLE:
                return value instanceof Number ? ((Number)value).doubleValue() : Double.parseDouble(value.toString());
            case BIG_INT:
                return new BigInteger(value.toString());
            case BIG_DECIMAL:
                return new BigDecimal(value.toString());
            case BOOL:
                return value instanceof Boolean ? ((Boolean)value) : Boolean.parseBoolean(value.toString());
            case STRING:
                return value.toString();
            case CHAR:
                final String str = value.toString();
                return str.length() > 0 ? str.charAt(0) : '\0';
            default:
                throw new ClassCastException(String.format("Unable cast '%s' to '%s'", value, expectedType));
        }
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
    protected AbstractCompositeAttribute connectAttribute(final String attributeName, final AttributeDescriptor descriptor) throws ReflectionException, AttributeNotFoundException, MBeanException, AbsentCompositeConfigurationParameterException, FunctionParserException {
        if (CompositeResourceConfigurationDescriptor.isRateFormula(descriptor)) //rate attribute
            return new NotificationRateAttribute(attributeName, descriptor);
        //regular attribute
        final String connectorType = CompositeResourceConfigurationDescriptor.parseSource(descriptor);
        final AttributeSupport support = attributeSupportProvider.getAttributeSupport(connectorType);
        if (support == null)
            throw new ReflectionException(new UnsupportedOperationException(String.format("Connector '%s' doesn't support attributes", connectorType)));
        else {
            //process regular attribute
            final MBeanAttributeInfo underlyingAttribute = support.addAttribute(attributeName, descriptor);
            if (underlyingAttribute == null)
                throw CompositeAttribute.attributeNotFound(connectorType, attributeName);
            final AggregationFunction<?> function = CompositeResourceConfigurationDescriptor.parseFormula(descriptor);
            //check whether the type of function is compatible with type of attribute
            if (function == null) {
                return new CompositeAttribute(connectorType, underlyingAttribute);
            } else {
                final OpenType<?> attributeType = AttributeDescriptor.getOpenType(underlyingAttribute);
                if (function.canAccept(0, attributeType))
                    return new AggregationAttribute(connectorType, function, this, underlyingAttribute);
                else
                    throw new MBeanException(new IllegalStateException(String.format("Function '%s' cannot be applied to attribute '%s'", function, underlyingAttribute)));
            }
        }
    }

    @Override
    protected Optional<AttributeSnapshot> takeSnapshot(final AbstractCompositeAttribute attribute) {
        if (attribute instanceof MetricAttribute<?>)
            return Optional.of(new MetricSnapshot((MetricAttribute<?>) attribute));
        else
            return Optional.empty();
    }

    @Override
    protected boolean applySnapshot(final AbstractCompositeAttribute attribute, final AttributeSnapshot snapshot) {
        if (snapshot instanceof MetricSnapshot && attribute instanceof MetricAttribute<?>)
            return ((MetricAttribute<?>) attribute).setMetric(((MetricSnapshot) snapshot).metric);
        else
            return false;
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        parallelForEach(attribute -> {
            if (attribute instanceof NotificationListener)
                ((NotificationListener) attribute).handleNotification(notification, handback);
        }, threadPool);
    }

    @Override
    protected void failedToConnectAttribute(final String attributeName, final Exception e) {
        failedToConnectAttribute(logger, Level.WARNING, attributeName, e);
    }

    @Override
    protected Object getAttribute(final AbstractCompositeAttribute metadata) throws Exception {
        return metadata.getValue(attributeSupportProvider);
    }

    @Override
    protected void failedToGetAttribute(final String attributeID, final Exception e) {
        failedToGetAttribute(logger, Level.WARNING, attributeID, e);
    }

    @Override
    protected void setAttribute(final AbstractCompositeAttribute attribute, final Object value) throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException {
        attribute.setValue(attributeSupportProvider, value);
    }

    @Override
    protected void failedToSetAttribute(final String attributeID, final Object value, final Exception e) {
        failedToSetAttribute(logger, Level.WARNING, attributeID, value, e);
    }
}
