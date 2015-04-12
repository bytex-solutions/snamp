package com.itworks.snamp.connectors.aggregator;

import com.google.common.base.Function;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.connectors.AbstractManagedResourceConnector;
import com.itworks.snamp.connectors.ResourceEventListener;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.attributes.OpenAttributeSupport;

import javax.management.openmbean.CompositeData;
import java.beans.IntrospectionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents an aggregator of other managed resources.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class AggregatorResourceConnector extends AbstractManagedResourceConnector implements AttributeSupport {
    static final String NAME = "aggregator";

    private static final class AggregationSupport extends OpenAttributeSupport<AbstractAttributeAggregation>{
        private final Logger logger;

        private AggregationSupport(final String resourceName,
                                   final Logger logger){
            super(resourceName, AbstractAttributeAggregation.class);
            this.logger = logger;
        }

        @Override
        protected AbstractAttributeAggregation connectAttribute(final String attributeID, final AttributeDescriptor descriptor) throws Exception {
            switch (descriptor.getAttributeName()){
                case PatternMatcher.NAME: return new PatternMatcher(attributeID, descriptor);
                case UnaryComparison.NAME: return new UnaryComparison(attributeID, descriptor);
                case BinaryComparison.NAME: return new BinaryComparison(attributeID, descriptor);
                case BinaryPercent.NAME: return new BinaryPercent(attributeID, descriptor);
                case UnaryPercent.NAME: return new UnaryPercent(attributeID, descriptor);
                case Counter.NAME: return new Counter(attributeID, descriptor);
                case Average.NAME: return new Average(attributeID, descriptor);
                case Peak.NAME: return new Peak(attributeID, descriptor);
                case Decomposer.NAME: return new Decomposer(attributeID, descriptor);
                default: return null;
            }
        }

        @Override
        protected void failedToConnectAttribute(final String attributeID, final String attributeName, final Exception e) {
            failedToConnectAttribute(logger, Level.SEVERE, attributeID, attributeName, e);
        }

        @Override
        protected void failedToGetAttribute(final String attributeID, final Exception e) {
            failedToGetAttribute(logger, Level.SEVERE, attributeID, e);
        }

        @Override
        protected void failedToSetAttribute(final String attributeID, final Object value, final Exception e) {
            failedToSetAttribute(logger, Level.SEVERE, attributeID, value, e);
        }
    }

    private final AggregationSupport attributes;

    AggregatorResourceConnector(final String resourceName) throws IntrospectionException {
        attributes = new AggregationSupport(resourceName, getLoggerImpl());
    }

    /**
     * Adds a new listener for the connector-related events.
     * <p/>
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

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the aggregated object.
     * @return An instance of the requested object; or {@literal null} if object is not available.
     */
    @Override
    public <T> T queryObject(final Class<T> objectType) {
        return findObject(objectType,
                new Function<Class<T>, T>() {
                    @Override
                    public T apply(final Class<T> objectType) {
                        return AggregatorResourceConnector.super.queryObject(objectType);
                    }
                }, attributes);
    }

    void addAttribute(final String attributeID, final String attributeName, final TimeSpan readWriteTimeout, final CompositeData options) {
        attributes.addAttribute(attributeID, attributeName, readWriteTimeout, options);
    }

    private static Logger getLoggerImpl(){
        return getLogger(NAME);
    }

    @Override
    public Logger getLogger() {
        return getLoggerImpl();
    }
}
