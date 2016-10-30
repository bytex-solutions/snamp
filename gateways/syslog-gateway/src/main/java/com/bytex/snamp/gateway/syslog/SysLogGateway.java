package com.bytex.snamp.gateway.syslog;

import com.bytex.snamp.EntryReader;
import com.bytex.snamp.gateway.AbstractGateway;
import com.bytex.snamp.gateway.NotificationEvent;
import com.bytex.snamp.gateway.NotificationListener;
import com.bytex.snamp.gateway.modeling.*;
import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import com.cloudbees.syslog.SyslogMessage;
import com.cloudbees.syslog.sender.SyslogMessageSender;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanNotificationInfo;
import java.io.CharArrayWriter;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class SysLogGateway extends AbstractGateway {
    private static final class SysLogModelOfAttributes extends ModelOfAttributes<SysLogAttributeAccessor> {

        @Override
        protected SysLogAttributeAccessor createAccessor(final MBeanAttributeInfo metadata) {
            return new SysLogAttributeAccessor(metadata);
        }
    }

    private static final class SysLogModelOfNotifications extends ModelOfNotifications<SysLogNotificationAccessor> implements NotificationListener {
        private final Map<String, ResourceNotificationList<SysLogNotificationAccessor>> notifications;
        private ConcurrentSyslogMessageSender checkSender;

        private SysLogModelOfNotifications() {
            this.notifications = new HashMap<>(10);
        }

        private <E extends Exception> void forEachNotificationImpl(final EntryReader<String, ? super SysLogNotificationAccessor, E> notificationReader) throws E{
            for(final ResourceNotificationList<SysLogNotificationAccessor> list: notifications.values())
                for(final SysLogNotificationAccessor accessor: list.values())
                    if(!notificationReader.read(accessor.resourceName, accessor)) return;
        }

        @Override
        public <E extends Exception> void forEachNotification(final EntryReader<String, ? super SysLogNotificationAccessor, E> notificationReader) throws E {
            readLock.accept(SingleResourceGroup.INSTANCE, notificationReader, this::forEachNotificationImpl);
        }

        private void setCheckSender(final ConcurrentSyslogMessageSender value){
            this.checkSender = value;
        }

        @Override
        public void handleNotification(final NotificationEvent event) {
            final String resourceName = (String)event.getNotification().getSource();
            final Severity severity = SysLogNotificationAccessor.getSeverity(event.getSource());
            final Facility facility = SysLogNotificationAccessor.getFacility(event.getSource());
            final String applicationName = SysLogNotificationAccessor.getApplicationName(event.getSource(), resourceName);
            final ConcurrentSyslogMessageSender checkSender = this.checkSender;
            if (checkSender != null){
                final SyslogMessage message = new SyslogMessage()
                    .withSeverity(severity)
                    .withFacility(facility)
                    .withMsgId(event.getNotification().getType())
                    .withAppName(applicationName)
                    .withTimestamp(event.getNotification().getTimeStamp())
                    .withMsg(new CharArrayWriter().append(event.getNotification().getMessage()))
                    .withProcId(SysLogUtils.getProcessId(applicationName));
                checkSender.sendMessage(message);
            }
        }

        private NotificationAccessor addNotificationImpl(final String resourceName,
                                             final MBeanNotificationInfo metadata) {
            final ResourceNotificationList<SysLogNotificationAccessor> list;
            if (notifications.containsKey(resourceName))
                list = notifications.get(resourceName);
            else notifications.put(resourceName, list = new ResourceNotificationList<>());
            final SysLogNotificationAccessor accessor;
            list.put(accessor = new SysLogNotificationAccessor(resourceName, metadata, this));
            return accessor;
        }

        private NotificationAccessor addNotification(final String resourceName,
                                                     final MBeanNotificationInfo metadata) {
            return writeLock.apply(SingleResourceGroup.INSTANCE, resourceName, metadata, this::addNotificationImpl);
        }

        private NotificationAccessor removeNotificationImpl(final String resourceName,
                                                        final MBeanNotificationInfo metadata){
            final ResourceNotificationList<SysLogNotificationAccessor> list;
            if(notifications.containsKey(resourceName))
                list = notifications.get(resourceName);
            else return null;
            final NotificationAccessor accessor = list.remove(metadata);
            if(list.isEmpty()) notifications.remove(resourceName);
            return accessor;
        }

        private NotificationAccessor removeNotification(final String resourceName,
                                                        final MBeanNotificationInfo metadata){
            return writeLock.apply(SingleResourceGroup.INSTANCE, resourceName, metadata, this::removeNotificationImpl);
        }

        private Collection<? extends NotificationAccessor> removeNotifications(final String resourceName) {
            return writeLock.apply(SingleResourceGroup.INSTANCE, resourceName, notifications, (resName, notifs) -> {
                if (notifs.containsKey(resName))
                    return notifs.remove(resName).values();
                else return ImmutableList.of();
            });
        }

        private void clear() {
            writeLock.accept(SingleResourceGroup.INSTANCE, notifications, notifs -> {
                notifs.values().forEach(list -> list.values().forEach(NotificationAccessor::close));
                notifs.clear();
            });
            final ConcurrentSyslogMessageSender sender = checkSender;
            if (sender != null)
                sender.close();
            checkSender = null;
        }
    }

    private final SysLogModelOfAttributes attributes;
    private SysLogAttributeSender attributeSender;
    private final SysLogModelOfNotifications notifications;

    SysLogGateway(final String gatewayInstance){
        super(gatewayInstance);
        attributes = new SysLogModelOfAttributes();
        notifications = new SysLogModelOfNotifications();
        attributeSender = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> addFeature(final String resourceName, final M feature) throws Exception {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M>)attributes.addAttribute(resourceName, (MBeanAttributeInfo)feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M>)notifications.addNotification(resourceName, (MBeanNotificationInfo)feature);
        else return null;
    }

    @Override
    protected Stream<? extends FeatureAccessor<?>> removeAllFeatures(final String resourceName) throws Exception {
        return Stream.concat(
                notifications.removeNotifications(resourceName).stream(),
                attributes.clear(resourceName).stream()
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> removeFeature(final String resourceName, final M feature) throws Exception {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M>)attributes.removeAttribute(resourceName, (MBeanAttributeInfo)feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M>)notifications.removeNotification(resourceName, (MBeanNotificationInfo)feature);
        else return null;
    }

    private void start(final SyslogMessageSender sender,
                       final Duration passiveCheckSendPeriod,
                       final ExecutorService threadPool){
        final ConcurrentSyslogMessageSender parallelSender =
                new ConcurrentSyslogMessageSender(sender, threadPool);
        attributeSender = new SysLogAttributeSender(passiveCheckSendPeriod,
                parallelSender,
                attributes);
        notifications.setCheckSender(parallelSender);
        attributeSender.run();
    }

    @Override
    protected void start(final Map<String, String> parameters) throws Exception {
        final SysLogConfigurationDescriptor parser = SysLogConfigurationDescriptor.getInstance();
        start(parser.createSender(parameters),
                parser.getPassiveCheckSendPeriod(parameters),
                parser.getThreadPool(parameters));
    }

    @Override
    protected void stop() throws Exception {
        attributeSender.close();
        attributeSender = null;
        attributes.clear();
        notifications.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <M extends MBeanFeatureInfo> Multimap<String, ? extends FeatureBindingInfo<M>> getBindings(final Class<M> featureType) {
        if(featureType.isAssignableFrom(MBeanAttributeInfo.class))
            return (Multimap<String, ? extends FeatureBindingInfo<M>>)getBindings(attributes);
        else if(featureType.isAssignableFrom(MBeanNotificationInfo.class))
            return (Multimap<String, ? extends FeatureBindingInfo<M>>)getBindings(notifications);
        else return super.getBindings(featureType);
    }
}