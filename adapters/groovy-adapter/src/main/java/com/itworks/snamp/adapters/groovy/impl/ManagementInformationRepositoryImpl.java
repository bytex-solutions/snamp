package com.itworks.snamp.adapters.groovy.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.itworks.snamp.adapters.*;
import com.itworks.snamp.adapters.groovy.ManagementInformationRepository;
import com.itworks.snamp.adapters.NotificationListener;

import javax.management.*;
import java.util.HashMap;
import java.util.Set;

/**
 * Represents repository of t
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ManagementInformationRepositoryImpl implements ManagementInformationRepository {
    private static final class ScriptAttributesModel extends AbstractAttributesModel<ScriptAttributeAccessor> {
        @Override
        protected ScriptAttributeAccessor createAccessor(final MBeanAttributeInfo metadata) {
            return new ScriptAttributeAccessor(metadata);
        }

        private Object getValue(final String resourceName, final String attributeName) throws MBeanException, AttributeNotFoundException, ReflectionException {
            return getAttributeValue(resourceName, attributeName);
        }

        private void setValue(final String resourceName, final String attributeName, final Object value) throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException {
            setAttributeValue(resourceName, attributeName, value);
        }
    }

    private static final class ScriptNotificationsModel extends HashMap<String, ResourceNotificationList<UnicastNotificationRouter>> {

        private synchronized Iterable<UnicastNotificationRouter> clear(final String resourceName) {
                final ResourceNotificationList<UnicastNotificationRouter> list = remove(resourceName);
                return list != null ? list.values() : ImmutableList.<UnicastNotificationRouter>of();
        }

        @Override
        public synchronized void clear(){
            for(final ResourceNotificationList<?> list: values())
                list.clear();
            super.clear();
        }

        private synchronized UnicastNotificationRouter put(final String resourceName,
                                                           final MBeanNotificationInfo metadata,
                                                           final NotificationListener listener) {
            final ResourceNotificationList<UnicastNotificationRouter> list;
            if(containsKey(resourceName))
                list = get(resourceName);
            else put(resourceName, list = new ResourceNotificationList<>());
            final UnicastNotificationRouter result = new UnicastNotificationRouter(metadata, listener);
            list.put(result);
            return result;
        }

        private synchronized UnicastNotificationRouter remove(final String resourceName,
                                                              final MBeanNotificationInfo metadata){
            final ResourceNotificationList<UnicastNotificationRouter> list = get(resourceName);
            if(list == null) return null;
            final UnicastNotificationRouter result = list.remove(metadata);
            if(list.isEmpty()) remove(resourceName);
            return result;
        }
    }

    private final ScriptAttributesModel attributes = new ScriptAttributesModel();
    private final ScriptNotificationsModel notifications = new ScriptNotificationsModel();

    @Override
    public Set<String> getHostedResources() {
        return attributes.getHostedResources();
    }

    @Override
    public Set<String> getResourceAttributes(final String resourceName) {
        return attributes.getResourceAttributes(resourceName);
    }

    @Override
    public Object getAttributeValue(final String resourceName, final String attributeName) throws MBeanException, AttributeNotFoundException, ReflectionException {
        return attributes.getValue(resourceName, attributeName);
    }

    @Override
    public void setAttributeValue(final String resourceName, final String attributeName, final Object value) throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException {
        attributes.setValue(resourceName, attributeName, value);
    }

    UnicastNotificationRouter addNotification(final String resourceName,
                                              final MBeanNotificationInfo metadata,
                                              final NotificationListener listener){
        return notifications.put(resourceName, metadata, listener);
    }

    UnicastNotificationRouter removeNotification(final String resourceName,
                                                 final MBeanNotificationInfo metadata){
        return notifications.remove(resourceName, metadata);
    }

    ScriptAttributeAccessor addAttribute(final String resourceName, final MBeanAttributeInfo feature) throws Exception {
        return attributes.addAttribute(resourceName, feature);
    }

    Iterable<? extends FeatureAccessor<?, ?>> clear(final String resourceName) {
        return Iterables.concat(attributes.clear(resourceName), notifications.clear(resourceName));
    }

    AttributeAccessor removeAttribute(final String resourceName, final MBeanAttributeInfo feature) {
        return attributes.removeAttribute(resourceName, feature);
    }

    void clear(){
        attributes.clear();
        notifications.clear();
    }
}
