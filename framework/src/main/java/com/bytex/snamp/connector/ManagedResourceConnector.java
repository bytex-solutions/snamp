package com.bytex.snamp.connector;

import com.bytex.snamp.connector.attributes.AttributeManager;
import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.health.OkStatus;
import com.bytex.snamp.connector.notifications.NotificationManager;
import com.bytex.snamp.connector.operations.OperationManager;
import com.bytex.snamp.core.FrameworkService;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleRevision;

import javax.annotation.Nonnull;
import javax.management.*;
import java.util.Objects;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents management connector that provides unified access to the management information.
 * <p>
 *     By default, managed resource connector doesn't expose default management mechanisms.
 *     The class that implements this interface must implement one or more following interfaces
 *     to provide management mechanisms:
 *     <ul>
 *         <li>{@link AttributeManager} to provide management
 *         via resource attributes.</li>
 *         <li>{@link NotificationManager} to receiver
 *         management notifications.</li>
 *         <li>{@link OperationManager} to provide management
 *         via operations.</li>
 *         <li>{@link #getStatus()} to provide health status
 *         of remote component</li>
 *     </ul>
 * @author Roman Sakno
 * @since 1.0
 * @version 2.1
 */
public interface ManagedResourceConnector extends AutoCloseable, FrameworkService, DynamicMBean {
    /**
     * This namespace must be defined in Provide-Capability manifest header inside of the bundle containing implementation
     * of Managed Resource Connector.
     * <p>
     *     Example: Provide-Capability: com.bytex.snamp.connector; type=jmx
     */
    String CAPABILITY_NAMESPACE = "com.bytex.snamp.connector";

    /**
     * This property must be defined in Provide-Capability manifest header and specify type of Managed Resource Connector.
     * @see #CAPABILITY_NAMESPACE
     */
    String TYPE_CAPABILITY_ATTRIBUTE = "type";

    /**
     * Adds a new listener for the connector-related events.
     * <p>
     *     The managed resource connector should holds a weak reference to all added event listeners.
     * @param listener An event listener to add.
     */
    void addResourceEventListener(final ResourceEventListener listener);

    /**
     * Removes connector event listener.
     * @param listener The listener to remove.
     */
    void removeResourceEventListener(final ResourceEventListener listener);

    /**
     * Gets the values of all attributes.
     * @return The values of all attributes
     * @since 2.1
     */
    default AttributeList getAttributes() throws MBeanException, ReflectionException {
        final AttributeList result = new AttributeList();
        for (final MBeanAttributeInfo attribute : getMBeanInfo().getAttributes()) {
            final Object attributeValue;
            try {
                attributeValue = getAttribute(attribute.getName());
            } catch (final AttributeNotFoundException e) {
                continue;
            }
            result.add(new Attribute(attribute.getName(), attributeValue));
        }
        return result;
    }

    /**
     * Determines whether the connected managed resource is alive.
     * @return Status of the remove managed resource.
     * @since 2.1
     */
    @Nonnull
    default HealthStatus getStatus() {
        return new OkStatus();
    }

    static String getConnectorType(final Bundle bnd) {
        final BundleRevision revision = bnd.adapt(BundleRevision.class);
        assert revision != null;
        return revision.getCapabilities(CAPABILITY_NAMESPACE)
                .stream()
                .map(capability -> capability.getAttributes().get(TYPE_CAPABILITY_ATTRIBUTE))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst()
                .orElse("")
                .intern();
    }

    /**
     * Returns system name of the connector using its implementation class.
     * @param connectorImpl A class that represents implementation of resource connector.
     * @return System name of the connector.
     */
    static String getConnectorType(final Class<? extends ManagedResourceConnector> connectorImpl){
        return getConnectorType(FrameworkUtil.getBundle(connectorImpl));
    }

    static boolean isResourceConnectorBundle(final Bundle bnd) {
        return bnd != null && !isNullOrEmpty(getConnectorType(bnd));
    }
}
