package com.bytex.snamp.adapters.groovy.impl;

import com.bytex.snamp.Consumer;
import com.bytex.snamp.EntryReader;
import com.bytex.snamp.adapters.NotificationListener;
import com.bytex.snamp.adapters.groovy.AttributesRootAPI;
import com.bytex.snamp.adapters.groovy.EventsRootAPI;
import com.bytex.snamp.adapters.groovy.ResourceAttributesAnalyzer;
import com.bytex.snamp.adapters.groovy.dsl.GroovyManagementModel;
import com.bytex.snamp.adapters.modeling.*;
import com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import com.bytex.snamp.connectors.ManagedResourceConnectorClient;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.osgi.framework.BundleContext;

import javax.management.*;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents repository of t
 * @author Roman Sakno
 * @version 1.2
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

        private Collection<ScriptNotificationAccessor> clear(final String resourceName) {
            return write(resourceName, notifications, (resName, notifs) -> {
                final ResourceNotificationList<ScriptNotificationAccessor> list = notifs.remove(resName);
                return list != null ? list.values() : ImmutableList.of();
            });
        }

        private void clear() {
            write(notifications, notifs -> {
                notifs.values().forEach(ResourceFeatureList::clear);
                notifs.clear();
            });
        }

        private NotificationRouter put(final String resourceName,
                                              final MBeanNotificationInfo metadata,
                                              final NotificationListener listener) {
            return write((Supplier<NotificationRouter>) () -> {
                final ResourceNotificationList<ScriptNotificationAccessor> list;
                if (notifications.containsKey(resourceName))
                    list = notifications.get(resourceName);
                else notifications.put(resourceName, list = new ResourceNotificationList<>());
                final ScriptNotificationAccessor result = new ScriptNotificationAccessor(resourceName, metadata, listener);
                list.put(result);
                return result;
            });
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
            return write(resourceName, metadata, this::removeImpl);
        }

        private Collection<MBeanNotificationInfo> getNotifications(final String resourceName) {
            return read(resourceName, notifications, (resName, notifs) -> {
                final ResourceNotificationList<?> list = notifs.get(resourceName);
                if (list != null) {
                    return list.values().stream()
                            .map(FeatureAccessor::getMetadata)
                            .collect(Collectors.toList());
                } else
                    return ImmutableList.of();
            });
        }

        private Set<String> getResourceEvents(final String resourceName) {
            return read(resourceName, notifications, (resName, notifs) -> {
                if (notifs.containsKey(resName)) {
                    final Set<String> result = new HashSet<>(20);
                    for (final FeatureAccessor<MBeanNotificationInfo> accessor : notifs.get(resName).values())
                        Collections.addAll(result, accessor.getMetadata().getNotifTypes());
                    return result;
                } else
                    return ImmutableSet.of();
            });
        }

        private <E extends Exception> void forEachNotificationImpl(final EntryReader<String, ? super ScriptNotificationAccessor, E> notificationReader) throws E{
            for (final ResourceNotificationList<ScriptNotificationAccessor> notifs : notifications.values())
                for (final ScriptNotificationAccessor accessor : notifs.values())
                    if(!notificationReader.read(accessor.getResourceName(), accessor)) return;
        }

        /**
         * Iterates over all registered notifications.
         *
         * @param notificationReader
         * @throws E
         */
        @Override
        public <E extends Exception> void forEachNotification(final EntryReader<String, ? super ScriptNotificationAccessor, E> notificationReader) throws E {
            read(notificationReader, (Consumer<EntryReader<String,? super ScriptNotificationAccessor,E>, E>) this::forEachNotificationImpl);
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
            return config != null ? ImmutableMap.copyOf(config.getParameters()) : ImmutableMap.<String, String>of();
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
    public <E extends Exception> void forEachAttribute(final EntryReader<String, ? super ScriptAttributeAccessor, E> attributeReader) throws E {
        attributes.forEachAttribute(attributeReader);
    }

    @Override
    public <E extends Exception> void forEachNotification(final EntryReader<String, ? super ScriptNotificationAccessor, E> notificationReader) throws E {
        notifications.forEachNotification(notificationReader);
    }
}
