package com.itworks.snamp.adapters.syslog;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import com.cloudbees.syslog.SyslogMessage;
import com.cloudbees.syslog.sender.SyslogMessageSender;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.*;
import com.itworks.snamp.concurrent.ThreadSafeObject;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanNotificationInfo;
import java.io.CharArrayWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.itworks.snamp.adapters.syslog.SysLogConfigurationDescriptor.createSender;
import static com.itworks.snamp.adapters.syslog.SysLogConfigurationDescriptor.getPassiveCheckSendPeriod;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SysLogAdapter extends AbstractResourceAdapter {
    static final String NAME = "syslog";

    private static final class SysLogAttributeModel extends AbstractAttributesModel<SysLogAttributeAccessor> {

        @Override
        protected SysLogAttributeAccessor createAccessor(final MBeanAttributeInfo metadata) {
            return new SysLogAttributeAccessor(metadata);
        }
    }

    private static final class SysLogNotificationModel extends ThreadSafeObject implements NotificationListener {
        private final Map<String, ResourceNotificationList<SysLogNotificationAccessor>> notifications;
        private ConcurrentSyslogMessageSender checkSender;

        private SysLogNotificationModel() {
            this.notifications = new HashMap<>(10);
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
                        accessor.disconnect();
            }
            final ConcurrentSyslogMessageSender sender = checkSender;
            if (sender != null)
                sender.close();
            checkSender = null;
        }
    }

    private final SysLogAttributeModel attributes;
    private SysLogAttributeSender attributeSender;
    private final SysLogNotificationModel notifications;

    SysLogAdapter(final String adapterInstanceName){
        super(adapterInstanceName);
        attributes = new SysLogAttributeModel();
        notifications = new SysLogNotificationModel();
        attributeSender = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo, S> FeatureAccessor<M, S> addFeature(final String resourceName, final M feature) throws Exception {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M, S>)attributes.addAttribute(resourceName, (MBeanAttributeInfo)feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M, S>)notifications.addNotification(resourceName, (MBeanNotificationInfo)feature);
        else return null;
    }

    @Override
    protected Iterable<? extends FeatureAccessor<?, ?>> removeAllFeatures(final String resourceName) throws Exception {
        return Iterables.concat(notifications.removeNotifications(resourceName),
                attributes.clear(resourceName));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M, ?> removeFeature(final String resourceName, final M feature) throws Exception {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M, ?>)attributes.removeAttribute(resourceName, (MBeanAttributeInfo)feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M, ?>)notifications.removeNotification(resourceName, (MBeanNotificationInfo)feature);
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

    @Override
    public Logger getLogger() {
        return getLogger(NAME);
    }
}