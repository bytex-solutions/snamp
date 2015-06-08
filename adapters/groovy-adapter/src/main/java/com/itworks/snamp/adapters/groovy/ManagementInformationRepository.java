package com.itworks.snamp.adapters.groovy;

import javax.management.*;
import java.util.Collection;
import java.util.Set;

/**
 * Represents management information provider.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ManagementInformationRepository {
    Set<String> getHostedResources();
    Set<String> getResourceAttributes(final String resourceName);
    Object getAttributeValue(final String resourceName, final String attributeName) throws MBeanException, AttributeNotFoundException, ReflectionException;
    Collection<MBeanAttributeInfo> getAttributes(final String resourceName);
    Collection<MBeanNotificationInfo> getNotifications(final String resourceName);
    void setAttributeValue(final String resourceName, final String attributeName, final Object value) throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException;
}