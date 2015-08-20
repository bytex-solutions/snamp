package com.bytex.snamp.adapters.syslog;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import com.cloudbees.syslog.SyslogMessage;
import com.cloudbees.syslog.sender.SyslogMessageSender;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.adapters.*;
import com.bytex.snamp.adapters.modeling.*;
import com.bytex.snamp.internal.EntryReader;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanNotificationInfo;
import java.io.CharArrayWriter;
import java.util.HashMap;
import java.util.Map;

import static com.bytex.snamp.adapters.syslog.SysLogConfigurationDescriptor.createSender;
import static com.bytex.snamp.adapters.syslog.SysLogConfigurationDescriptor.getPassiveCheckSendPeriod;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SysLogAdapter extends AbstractResourceAdapter {
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

        @Override
        public <E extends Exception> void forEachNotification(final EntryReader<String, ? super SysLogNotificationAccessor, E> notificationReader) throws E {
            try(final LockScope ignored = beginRead()){
                for(final ResourceNotificationList<SysLogNotificationAccessor> list: notifications.values())
                    for(final SysLogNotificationAccessor accessor: list.values())
                        if(!notificationReader.read(accessor.resourceName, accessor)) return;
            }
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

        private NotificationAccessor addNotification(final String resourceName,
                                                     final MBeanNotificationInfo metadata){
            try(final LockScope ignored = beginWrite()){
                final ResourceNotificationList<SysLogNotificationAccessor> list;
                if(notifications.containsKey(resourceName))
                    list = notifications.get(resourceName);
                else notifications.put(resourceName, list = new ResourceNotificationList<>());
                final SysLogNotificationAccessor accessor;
                list.put(accessor = new SysLogNotificationAccessor(resourceName, metadata, this));
                return accessor;
            }
        }

        private NotificationAccessor removeNotification(final String resourceName,
                                                        final MBeanNotificationInfo metadata){
            try(final LockScope ignored = beginWrite()){
                final ResourceNotificationList<SysLogNotificationAccessor> list;
                if(notifications.containsKey(resourceName))
                    list = notifications.get(resourceName);
                else return null;
                final NotificationAccessor accessor = list.remove(metadata);
                if(list.isEmpty()) notifications.remove(resourceName);
                return accessor;
            }
        }

        private Iterable<? extends NotificationAccessor> removeNotifications(final String resourceName){
            try(final LockScope ignored = beginWrite()){
                if(notifications.containsKey(resourceName))
                    return notifications.remove(resourceName).values();
                else return ImmutableList.of();
            }
        }

        private void clear() {
            try (final LockScope ignored = beginWrite()) {
                for (final ResourceNotificationList<?> list : notifications.values())
                    for (final NotificationAccessor accessor : list.values())
                        accessor.close();
            }
            final ConcurrentSyslogMessageSender sender = checkSender;
            if (sender != null)
                sender.close();
            checkSender = null;
        }
    }

    private final SysLogModelOfAttributes attributes;
    private SysLogAttributeSender attributeSender;
    private final SysLogModelOfNotifications notifications;

    SysLogAdapter(final String adapterInstanceName){
        super(adapterInstanceName);
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
    protected Iterable<? extends FeatureAccessor<?>> removeAllFeatures(final String resourceName) throws Exception {
        return Iterables.concat(notifications.removeNotifications(resourceName),
                attributes.clear(resourceName));
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
                       final TimeSpan passiveCheckSendPeriod){
        final ConcurrentSyslogMessageSender parallelSender =
                new ConcurrentSyslogMessageSender(sender);
        attributeSender = new SysLogAttributeSender(passiveCheckSendPeriod,
                parallelSender,
                attributes);
        notifications.setCheckSender(parallelSender);
        attributeSender.run();
    }

    @Override
    protected void start(final Map<String, String> parameters) throws Exception {
        start(createSender(parameters),
                getPassiveCheckSendPeriod(parameters));
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
