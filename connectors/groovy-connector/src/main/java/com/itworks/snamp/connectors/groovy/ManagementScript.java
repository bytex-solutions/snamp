package com.itworks.snamp.connectors.groovy;

import com.google.common.base.Supplier;
import com.itworks.snamp.Consumer;
import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.connectors.ManagedResourceConnector;
import com.itworks.snamp.connectors.ManagedResourceConnectorClient;
import com.itworks.snamp.core.OSGiLoggingContext;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.internal.annotations.SpecialUse;
import com.itworks.snamp.jmx.DescriptorUtils;
import com.itworks.snamp.jmx.JMExceptionUtils;
import groovy.lang.Script;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import java.util.Dictionary;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Represents an abstract class for all Groovy-bases scenarios.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class ManagementScript extends Script {
    private static final String LOGGER_NAME = ResourceConnectorInfo.getLoggerName();

    private static abstract class AttributeOperation<E extends JMException> implements Consumer<ManagedResourceConnector, E>{
        private final String attributeName;

        private AttributeOperation(final String name){
            this.attributeName = name;
        }

        @Override
        public abstract void accept(final ManagedResourceConnector connector) throws E;
    }

    private static final class AttributeValueReader extends AttributeOperation<JMException> implements Supplier {
        private Object attributeValue;

        private AttributeValueReader(final String name) {
            super(name);
        }

        @Override
        public void accept(final ManagedResourceConnector connector) throws JMException {
            attributeValue = connector.getAttribute(super.attributeName);
        }

        @Override
        public Object get() {
            return attributeValue;
        }
    }

    private static final class AttributeMetaReader extends AttributeOperation<AttributeNotFoundException> implements Supplier<Dictionary<String, ?>> {
        private Dictionary<String, ?> metadata;

        private AttributeMetaReader(final String name) {
            super(name);
        }

        @Override
        public void accept(final ManagedResourceConnector connector) throws AttributeNotFoundException {
            for (final MBeanAttributeInfo attributeInfo : connector.getMBeanInfo().getAttributes())
                if (Objects.equals(attributeInfo.getName(), super.attributeName)) {
                    metadata = DescriptorUtils.asDictionary(attributeInfo.getDescriptor());
                    return;
                }
            throw JMExceptionUtils.attributeNotFound(super.attributeName);
        }

        @Override
        public Dictionary<String, ?> get() {
            return metadata;
        }
    }

    private static final class AttributeValueWriter extends Attribute implements Consumer<ManagedResourceConnector, JMException> {

        private AttributeValueWriter(final String name, final Object value) {
            super(name, value);
        }

        @Override
        public void accept(final ManagedResourceConnector connector) throws JMException {
            connector.setAttribute(this);
        }
    }

    @SpecialUse
    protected final void error(final String message) {
        OSGiLoggingContext.within(LOGGER_NAME, new SafeConsumer<Logger>() {
            @Override
            public void accept(final Logger logger) {
                logger.severe(message);
            }
        });
    }

    @SpecialUse
    protected final void warning(final String message) {
        OSGiLoggingContext.within(LOGGER_NAME, new SafeConsumer<Logger>() {
            @Override
            public void accept(final Logger logger) {
                logger.warning(message);
            }
        });
    }

    @SpecialUse
    protected final void info(final String message) {
        OSGiLoggingContext.within(LOGGER_NAME, new SafeConsumer<Logger>() {
            @Override
            public void accept(final Logger logger) {
                logger.info(message);
            }
        });
    }

    @SpecialUse
    protected final void debug(final String message) {
        OSGiLoggingContext.within(LOGGER_NAME, new SafeConsumer<Logger>() {
            @Override
            public void accept(final Logger logger) {
                logger.config(message);
            }
        });
    }

    @SpecialUse
    protected final void fine(final String message) {
        OSGiLoggingContext.within(LOGGER_NAME, new SafeConsumer<Logger>() {
            @Override
            public void accept(final Logger logger) {
                logger.fine(message);
            }
        });
    }

    private BundleContext getBundleContext() {
        return Utils.getBundleContextByObject(this);
    }

    final <E extends Throwable> void processResourceConnector(final String resourceName,
                                                              final Consumer<ManagedResourceConnector, E> consumer) throws E {
        final BundleContext context = getBundleContext();
        final ServiceReference<ManagedResourceConnector> connectorRef =
                ManagedResourceConnectorClient.getResourceConnector(context, resourceName);
        if (connectorRef != null) {
            final ManagedResourceConnector connector = context.getService(connectorRef);
            if (connector != null)
                try {
                    consumer.accept(connector);
                } finally {
                    context.ungetService(connectorRef);
                }
        }
        throw new IllegalArgumentException(String.format("Managed resource %s doesn't exist", resourceName));
    }

    /**
     * Reads value of the managed resource attribute.
     *
     * @param resourceName  The name of the managed resource.
     * @param attributeName The name of the attribute.
     * @return The value of the attribute.
     * @throws JMException Unable to
     */
    @SpecialUse
    protected final Object getResourceAttribute(final String resourceName,
                                                final String attributeName) throws JMException {
        final AttributeValueReader reader = new AttributeValueReader(attributeName);
        processResourceConnector(resourceName, reader);
        return reader.get();
    }

    /**
     * Reads attribute metadata.
     * @param resourceName The name of the managed resource.
     * @param attributeName The name of the attribute.
     * @return A dictionary of attribute parameters.
     * @throws AttributeNotFoundException The attribute doesn't exist in the specified managed resource.
     */
    @SpecialUse
    protected final Dictionary<String, ?> getResourceAttributeInfo(final String resourceName,
                                                                   final String attributeName) throws AttributeNotFoundException{
        final AttributeMetaReader reader = new AttributeMetaReader(attributeName);
        processResourceConnector(resourceName, reader);
        return reader.get();
    }

    /**
     * Sets value of the managed resource attribute.
     * @param resourceName The name of the managed resource.
     * @param attributeName The name of the attribute.
     * @param value The value of the attribute.
     * @throws JMException Unable to set attribute value.
     */
    @SpecialUse
    protected final void setResourceAttribute(final String resourceName,
                                              final String attributeName,
                                              final Object value) throws JMException {
        processResourceConnector(resourceName, new AttributeValueWriter(attributeName, value));
    }
}
