package com.bytex.snamp.adapters.groovy.impl;

import com.google.common.collect.*;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.adapters.NotificationListener;
import com.bytex.snamp.adapters.groovy.AttributesRootAPI;
import com.bytex.snamp.adapters.groovy.EventsRootAPI;
import com.bytex.snamp.adapters.groovy.ResourceAttributesAnalyzer;
import com.bytex.snamp.adapters.groovy.dsl.GroovyManagementModel;
import com.bytex.snamp.adapters.modeling.*;
import com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import com.bytex.snamp.connectors.ManagedResourceConnectorClient;
import com.bytex.snamp.internal.RecordReader;
import org.osgi.framework.BundleContext;

import javax.management.*;
import java.io.IOException;
import java.util.*;

/**
 * Represents repository of t
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ManagementInformationRepository extends GroovyManagementModel implements AttributesRootAPI, EventsRootAPI, AttributeSet<ScriptAttributeAccessor>, NotificationSet<ScriptNotificationAccessor> {
    private static final class ScriptModelOfAttributes extends ModelOfAttributes<ScriptAttributeAccessor> {
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

    private static final class ScriptNotificationsModelOfNotifications extends ModelOfNotifications<ScriptNotificationAccessor> {
        private final Map<String, ResourceNotificationList<ScriptNotificationAccessor>> notifications =
                new HashMap<>(10);

        private Iterable<ScriptNotificationAccessor> clear(final String resourceName) {
            try (final LockScope ignored = beginWrite()) {
                final ResourceNotificationList<ScriptNotificationAccessor> list = notifications.remove(resourceName);
                return list != null ? list.values() : ImmutableList.<ScriptNotificationAccessor>of();
            }
        }

        private void clear() {
            try (final LockScope ignored = beginWrite()) {
                for (final ResourceNotificationList<?> list : notifications.values())
                    list.clear();
                notifications.clear();
            }
        }

        private NotificationRouter put(final String resourceName,
                                              final MBeanNotificationInfo metadata,
                                              final NotificationListener listener) {
            try (final LockScope ignored = beginWrite()) {
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
                                                  final MBeanNotificationInfo metadata) {
            try (final LockScope ignored = beginWrite()) {
                final ResourceNotificationList<ScriptNotificationAccessor> list = notifications.get(resourceName);
                if (list == null) return null;
                final ScriptNotificationAccessor result = list.remove(metadata);
                if (list.isEmpty()) notifications.remove(resourceName);
                return result;
            }
        }

        private Collection<MBeanNotificationInfo> getNotifications(final String resourceName) {
            try (final LockScope ignored = beginRead()) {
                final ResourceNotificationList<?> list = notifications.get(resourceName);
                if (list != null) {
                    final List<MBeanNotificationInfo> result = Lists.newArrayListWithExpectedSize(list.size());
                    for (final FeatureAccessor<MBeanNotificationInfo> accessor : list.values())
                        result.add(accessor.getMetadata());
                    return result;
                } else return ImmutableList.of();
            }
        }

        private Set<String> getResourceEvents(final String resourceName) {

            try (final LockScope ignored = beginRead()) {
                if (notifications.containsKey(resourceName)) {
                    final Set<String> result = new HashSet<>(20);
                    for (final FeatureAccessor<MBeanNotificationInfo> accessor : notifications.get(resourceName).values())
                        Collections.addAll(result, accessor.getMetadata().getNotifTypes());
                    return result;
                } else return ImmutableSet.of();
            }
        }

        /**
         * Iterates over all registered notifications.
         *
         * @param notificationReader
         * @throws E
         */
        @Override
        public <E extends Exception> void forEachNotification(final RecordReader<String, ? super ScriptNotificationAccessor, E> notificationReader) throws E {
            try (final LockScope ignored = beginRead()) {
                for (final ResourceNotificationList<ScriptNotificationAccessor> notifs : notifications.values())
                    for (final ScriptNotificationAccessor accessor : notifs.values())
                        notificationReader.read(accessor.getResourceName(), accessor);
            }
        }
    }

    private final ScriptModelOfAttributes attributes = new ScriptModelOfAttributes();
    private final ScriptNotificationsModelOfNotifications notifications = new ScriptNotificationsModelOfNotifications();
    private final BundleContext context;

    ManagementInformationRepository(final BundleContext context){
        this.context = Objects.requireNonNull(context);
    }

    @Override
    public Map<String, ?> getResourceParameters(final String resourceName) {
        try {
            final ManagedResourceConfiguration config = ManagedResourceConnectorClient.getResourceConfiguration(context, resourceName);
            return config != null ? ImmutableMap.<String, String>copyOf(config.getParameters()) : ImmutableMap.<String, String>of();
        } catch (final IOException ignored) {
            return ImmutableMap.of();
        }
    }

    @Override
    public Set<String> getList() {
        return attributes.getHostedResources();
    }

    @Override
    public Set<String> getAttributes(final String resourceName) {
        return attributes.getResourceAttributes(resourceName);
    }

    @Override
    public Collection<MBeanAttributeInfo> getAttributesMetadata(final String resourceName) {
        return attributes.getResourceAttributesMetadata(resourceName);
    }

    @Override
    public Set<String> getEvents(final String resourceName) {
        return notifications.getResourceEvents(resourceName);
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
    public <E extends Exception> void processAttributes(final RecordReader<String, AttributeAccessor, E> handler) throws E {
        forEachAttribute(handler);
    }

    @Override
    public ResourceAttributesAnalyzer<?> attributesAnalyzer(final TimeSpan checkPeriod) {
        return new ScriptAttributesAnalyzer(checkPeriod, attributes);
    }

    @Override
    public ScriptNotificationsAnalyzer eventsAnalyzer() {
        return new ScriptNotificationsAnalyzer();
    }

    @Override
    public <E extends Exception> void processEvents(final RecordReader<String, NotificationAccessor, E> closure) throws E {
        forEachNotification(closure);
    }

    @Override
    public Collection<MBeanNotificationInfo> getEventsMetadata(final String resourceName) {
        return notifications.getNotifications(resourceName);
    }

    NotificationRouter addNotification(final String resourceName,
                                              final MBeanNotificationInfo metadata,
                                              final NotificationListener listener){
        return notifications.put(resourceName, metadata, listener);
    }

    NotificationRouter removeNotification(final String resourceName,
                                                 final MBeanNotificationInfo metadata){
        return notifications.remove(resourceName, metadata);
    }

    ScriptAttributeAccessor addAttribute(final String resourceName, final MBeanAttributeInfo feature) throws Exception {
        return attributes.addAttribute(resourceName, feature);
    }

    Iterable<? extends FeatureAccessor<?>> clear(final String resourceName) {
        return Iterables.concat(attributes.clear(resourceName), notifications.clear(resourceName));
    }

    AttributeAccessor removeAttribute(final String resourceName, final MBeanAttributeInfo feature) {
        return attributes.removeAttribute(resourceName, feature);
    }

    void clear(){
        attributes.clear();
        notifications.clear();
    }

    @Override
    public <E extends Exception> void forEachAttribute(final RecordReader<String, ? super ScriptAttributeAccessor, E> attributeReader) throws E {
        attributes.forEachAttribute(attributeReader);
    }

    @Override
    public <E extends Exception> void forEachNotification(final RecordReader<String, ? super ScriptNotificationAccessor, E> notificationReader) throws E {
        notifications.forEachNotification(notificationReader);
    }
}
