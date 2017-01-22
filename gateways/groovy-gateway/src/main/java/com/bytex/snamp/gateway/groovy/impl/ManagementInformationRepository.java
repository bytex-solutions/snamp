package com.bytex.snamp.gateway.groovy.impl;

import com.bytex.snamp.EntryReader;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.concurrent.ThreadSafeObject;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.gateway.NotificationListener;
import com.bytex.snamp.gateway.groovy.AttributesRootAPI;
import com.bytex.snamp.gateway.groovy.EventsRootAPI;
import com.bytex.snamp.gateway.groovy.ResourceAttributesAnalyzer;
import com.bytex.snamp.gateway.groovy.dsl.GroovyManagementModel;
import com.bytex.snamp.gateway.modeling.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.osgi.framework.BundleContext;

import javax.management.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents repository of t
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class ManagementInformationRepository extends GroovyManagementModel implements AttributesRootAPI, EventsRootAPI, AttributeSet<ScriptAttributeAccessor>, NotificationSet<ScriptNotificationAccessor> {
    private static final class ScriptModelOfAttributes extends ModelOfAttributes<ScriptAttributeAccessor> {
        @Override
        protected ScriptAttributeAccessor createAccessor(final String resourceName, final MBeanAttributeInfo metadata) {
            return new ScriptAttributeAccessor(metadata);
        }

        private Object getValue(final String resourceName, final String attributeName) throws MBeanException, AttributeNotFoundException, ReflectionException {
            return getAttributeValue(resourceName, attributeName);
        }

        private void setValue(final String resourceName, final String attributeName, final Object value) throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException {
            setAttributeValue(resourceName, attributeName, value);
        }
    }

    private static final class ScriptNotificationsModelOfNotifications extends ThreadSafeObject implements NotificationSet<ScriptNotificationAccessor> {
        private final Map<String, ResourceNotificationList<ScriptNotificationAccessor>> notifications;

        private ScriptNotificationsModelOfNotifications(){
            super(SingleResourceGroup.class);
            notifications = new HashMap<>();
        }

        private Collection<ScriptNotificationAccessor> clear(final String resourceName) {
            return writeLock.apply(SingleResourceGroup.INSTANCE, resourceName, notifications, (resName, notifs) -> {
                final ResourceNotificationList<ScriptNotificationAccessor> list = notifs.remove(resName);
                return list != null ? list.values() : ImmutableList.of();
            });
        }

        private void clear() {
            writeLock.accept(SingleResourceGroup.INSTANCE, notifications, notifs -> {
                notifs.values().forEach(ResourceFeatureList::clear);
                notifs.clear();
            });
        }

        private NotificationRouter put(final String resourceName,
                                              final MBeanNotificationInfo metadata,
                                              final NotificationListener listener) {
            try(final SafeCloseable ignored = writeLock.acquireLock(SingleResourceGroup.INSTANCE)){
                final ResourceNotificationList<ScriptNotificationAccessor> list;
                if (notifications.containsKey(resourceName))
                    list = notifications.get(resourceName);
                else notifications.put(resourceName, list = new ResourceNotificationList<>());
                final ScriptNotificationAccessor result = new ScriptNotificationAccessor(resourceName, metadata, listener);
                list.put(result);
                return result;
            }
        }

        private ScriptNotificationAccessor removeImpl(final String resourceName,
                                                  final MBeanNotificationInfo metadata){
            final ResourceNotificationList<ScriptNotificationAccessor> list = notifications.get(resourceName);
            if (list == null) return null;
            final ScriptNotificationAccessor result = list.remove(metadata);
            if (list.isEmpty()) notifications.remove(resourceName);
            return result;
        }

        private ScriptNotificationAccessor remove(final String resourceName,
                                                  final MBeanNotificationInfo metadata) {
            return writeLock.apply(SingleResourceGroup.INSTANCE, resourceName, metadata, this::removeImpl);
        }

        private Collection<MBeanNotificationInfo> getNotifications(final String resourceName) {
            return readLock.apply(SingleResourceGroup.INSTANCE, resourceName, notifications, (resName, notifs) -> {
                final ResourceNotificationList<?> list = notifs.get(resName);
                if (list != null) {
                    return list.values().stream()
                            .map(FeatureAccessor::getMetadata)
                            .collect(Collectors.toList());
                } else
                    return ImmutableList.of();
            });
        }

        private Set<String> getResourceEvents(final String resourceName) {
            return readLock.apply(SingleResourceGroup.INSTANCE, resourceName, notifications, (resName, notifs) -> {
                if (notifs.containsKey(resName)) {
                    final Set<String> result = new HashSet<>(20);
                    for (final FeatureAccessor<MBeanNotificationInfo> accessor : notifs.get(resName).values())
                        Collections.addAll(result, accessor.getMetadata().getNotifTypes());
                    return result;
                } else
                    return ImmutableSet.of();
            });
        }

        private <E extends Throwable> void forEachNotificationImpl(final EntryReader<String, ? super ScriptNotificationAccessor, E> notificationReader) throws E{
            for (final ResourceNotificationList<ScriptNotificationAccessor> notifs : notifications.values())
                for (final ScriptNotificationAccessor accessor : notifs.values())
                    if(!notificationReader.accept(accessor.getResourceName(), accessor)) return;
        }

        /**
         * Iterates over all registered notifications.
         *
         * @param notificationReader
         * @throws E
         */
        @Override
        public <E extends Throwable> void forEachNotification(final EntryReader<String, ? super ScriptNotificationAccessor, E> notificationReader) throws E {
            readLock.accept(SingleResourceGroup.INSTANCE, notificationReader, this::forEachNotificationImpl);
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
        ManagedResourceConnectorClient client = null;
        try {
            client = new ManagedResourceConnectorClient(context, resourceName);
            return client.getConfiguration();
        } catch (final InstanceNotFoundException e) {
            return ManagedResourceConnectorClient.EMPTY_CONFIGURATION;
        } finally {
            if (client != null)
                client.release(context);
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
    public <E extends Exception> void processAttributes(final EntryReader<String, AttributeAccessor, E> handler) throws E {
        forEachAttribute(handler);
    }

    @Override
    public ResourceAttributesAnalyzer<?> attributesAnalyzer(final Duration checkPeriod) {
        return new ScriptAttributesAnalyzer(checkPeriod, attributes);
    }

    @Override
    public ScriptNotificationsAnalyzer eventsAnalyzer() {
        return new ScriptNotificationsAnalyzer();
    }

    @Override
    public <E extends Exception> void processEvents(final EntryReader<String, NotificationAccessor, E> closure) throws E {
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

    Stream<? extends FeatureAccessor<?>> clear(final String resourceName) {
        return Stream.concat(
                attributes.clear(resourceName).stream(),
                notifications.clear(resourceName).stream()
        );
    }

    AttributeAccessor removeAttribute(final String resourceName, final MBeanAttributeInfo feature) {
        return attributes.removeAttribute(resourceName, feature);
    }

    void clear(){
        attributes.clear();
        notifications.clear();
    }

    @Override
    public <E extends Throwable> void forEachAttribute(final EntryReader<String, ? super ScriptAttributeAccessor, E> attributeReader) throws E {
        attributes.forEachAttribute(attributeReader);
    }

    @Override
    public <E extends Throwable> void forEachNotification(final EntryReader<String, ? super ScriptNotificationAccessor, E> notificationReader) throws E {
        notifications.forEachNotification(notificationReader);
    }
}
