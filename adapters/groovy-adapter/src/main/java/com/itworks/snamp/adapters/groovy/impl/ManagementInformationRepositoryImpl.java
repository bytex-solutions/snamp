package com.itworks.snamp.adapters.groovy.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.itworks.snamp.adapters.*;
import com.itworks.snamp.adapters.NotificationListener;
import com.itworks.snamp.adapters.groovy.ManagementInformationRepository;
import com.itworks.snamp.concurrent.ThreadSafeObject;

import javax.management.*;
import java.util.*;

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

    private static final class ScriptNotificationsModel extends ThreadSafeObject {
        private final Map<String, ResourceNotificationList<ScriptNotificationAccessor>> notifications =
                new HashMap<>(10);

        private Iterable<ScriptNotificationAccessor> clear(final String resourceName) {
            try(final LockScope ignored = beginWrite()) {
                final ResourceNotificationList<ScriptNotificationAccessor> list = notifications.remove(resourceName);
                return list != null ? list.values() : ImmutableList.<ScriptNotificationAccessor>of();
            }
        }

        private void clear(){
            try(final LockScope ignored = beginWrite()) {
                for (final ResourceNotificationList<?> list : notifications.values())
                    list.clear();
                notifications.clear();
            }
        }

        private UnicastNotificationRouter put(final String resourceName,
                                                           final MBeanNotificationInfo metadata,
                                                           final NotificationListener listener) {
            try(final LockScope ignored = beginWrite()) {
                final ResourceNotificationList<ScriptNotificationAccessor> list;
                if (notifications.containsKey(resourceName))
                    list = notifications.get(resourceName);
                else notifications.put(resourceName, list = new ResourceNotificationList<>());
                final ScriptNotificationAccessor result = new ScriptNotificationAccessor(resourceName, metadata, listener);
                list.put(result);
                return result;
            }
        }

        private ScriptNotificationAccessor remove(final String resourceName,
                                                              final MBeanNotificationInfo metadata){
            try(final LockScope ignored = beginWrite()) {
                final ResourceNotificationList<ScriptNotificationAccessor> list = notifications.get(resourceName);
                if (list == null) return null;
                final ScriptNotificationAccessor result = list.remove(metadata);
                if (list.isEmpty()) notifications.remove(resourceName);
                return result;
            }
        }

        private Collection<MBeanNotificationInfo> getNotifications(final String resourceName) {
            try(final LockScope ignored = beginRead()){
                final ResourceNotificationList<?> list = notifications.get(resourceName);
                if(list != null){
                    final List<MBeanNotificationInfo> result = Lists.newArrayListWithExpectedSize(list.size());
                    for(final FeatureAccessor<MBeanNotificationInfo, ?> accessor: list.values())
                        result.add(accessor.getMetadata());
                    return result;
                }
                else return ImmutableList.of();
            }
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

    @Override
    public Collection<MBeanAttributeInfo> getAttributes(final String resourceName) {
        return attributes.getResourceAttributesMetadata(resourceName);
    }

    @Override
    public Collection<MBeanNotificationInfo> getNotifications(final String resourceName) {
        return notifications.getNotifications(resourceName);
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
