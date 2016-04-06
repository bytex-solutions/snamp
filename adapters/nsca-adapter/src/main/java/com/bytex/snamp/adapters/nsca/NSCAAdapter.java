package com.bytex.snamp.adapters.nsca;

import com.bytex.snamp.EntryReader;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.adapters.AbstractResourceAdapter;
import com.bytex.snamp.adapters.NotificationEvent;
import com.bytex.snamp.adapters.NotificationListener;
import com.bytex.snamp.adapters.modeling.*;
import com.bytex.snamp.adapters.nsca.configuration.AbsentNSCAConfigurationParameterException;
import com.bytex.snamp.adapters.nsca.configuration.NSCAAdapterConfigurationParser;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.internal.Utils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.googlecode.jsendnsca.core.MessagePayload;
import com.googlecode.jsendnsca.core.NagiosSettings;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanNotificationInfo;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * Represents NSCA adapter.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class NSCAAdapter extends AbstractResourceAdapter {

    private static final class NSCAAttributeModelOfAttributes extends ModelOfAttributes<NSCAAttributeAccessor> {

        @Override
        protected NSCAAttributeAccessor createAccessor(final MBeanAttributeInfo metadata) throws Exception {
            return new NSCAAttributeAccessor(metadata);
        }
    }

    private static final class NSCANotificationModel extends ModelOfNotifications<NSCANotificationAccessor> implements NotificationListener{
        private final Map<String, ResourceNotificationList<NSCANotificationAccessor>> notifications;
        private ConcurrentPassiveCheckSender checkSender;

        private NSCANotificationModel() {
            this.notifications = new HashMap<>(10);
        }

        private void setCheckSender(final ConcurrentPassiveCheckSender value){
            this.checkSender = value;
        }

        @Override
        public void handleNotification(final NotificationEvent event) {
            final int level = NSCANotificationAccessor.getLevel(event.getSource());
            final String resourceName = (String)event.getNotification().getSource();
            final String serviceName = NSCANotificationAccessor.getServiceName(event.getSource());
            final MessagePayload payload = new MessagePayload();
            payload.setLevel(level);
            payload.setMessage(event.getNotification().getMessage());
            payload.setMessage(resourceName);
            payload.setServiceName(serviceName);
            final ConcurrentPassiveCheckSender checkSender = this.checkSender;
            if (checkSender != null)
                checkSender.send(payload);
        }

        private NotificationAccessor addNotification(final String resourceName,
                                                     final MBeanNotificationInfo metadata){
            try(final LockScope ignored = beginWrite()){
                final ResourceNotificationList<NSCANotificationAccessor> list;
                if(notifications.containsKey(resourceName))
                    list = notifications.get(resourceName);
                else notifications.put(resourceName, list = new ResourceNotificationList<>());
                final NSCANotificationAccessor accessor;
                list.put(accessor = new NSCANotificationAccessor(resourceName, metadata, this));
                return accessor;
            }
        }

        private NotificationAccessor removeNotification(final String resourceName,
                                                        final MBeanNotificationInfo metadata){
            try(final LockScope ignored = beginWrite()){
                final ResourceNotificationList<NSCANotificationAccessor> list;
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
                notifications.clear();
            }
            checkSender = null;
        }

        @Override
        public <E extends Exception> void forEachNotification(final EntryReader<String, ? super NSCANotificationAccessor, E> notificationReader) throws E {
            try (final LockScope ignored = beginRead()) {
                for (final ResourceNotificationList<NSCANotificationAccessor> list : notifications.values())
                    for (final NSCANotificationAccessor accessor : list.values())
                        if (!notificationReader.read(accessor.resourceName, accessor)) return;
            }
        }
    }

    private static final class NSCAPeriodPassiveCheckSender extends PeriodicPassiveChecker<NSCAAttributeAccessor> {
        private final ConcurrentPassiveCheckSender checkSender;

        NSCAPeriodPassiveCheckSender(final TimeSpan period,
                                     final ConcurrentPassiveCheckSender sender,
                                     final NSCAAttributeModelOfAttributes attributes) {
            super(period, attributes);
            checkSender = Objects.requireNonNull(sender);
        }

        @Override
        protected boolean processAttribute(final String resourceName, final NSCAAttributeAccessor accessor) {
            if(DistributedServices.isActiveNode(Utils.getBundleContextOfObject(this))) {
                checkSender.send(accessor, resourceName);
                return true;
            } else return false;
        }
    }

    private final NSCAAttributeModelOfAttributes attributes;
    private NSCAPeriodPassiveCheckSender attributeChecker;
    private final NSCANotificationModel notifications;

    NSCAAdapter(final String instanceName) {
        super(instanceName);
        attributes = new NSCAAttributeModelOfAttributes();
        notifications = new NSCANotificationModel();
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

    private void start(final TimeSpan checkPeriod,
                       final NagiosSettings settings,
                       final ExecutorService threadPool) {
        final ConcurrentPassiveCheckSender checkSender = new ConcurrentPassiveCheckSender(settings, threadPool);
        notifications.setCheckSender(checkSender);
        attributeChecker = new NSCAPeriodPassiveCheckSender(checkPeriod, checkSender, attributes);
        attributeChecker.run();
    }

    @Override
    protected void start(final Map<String, String> parameters) throws AbsentNSCAConfigurationParameterException {
        final NSCAAdapterConfigurationParser parser = new NSCAAdapterConfigurationParser();
        start(parser.getPassiveCheckSendPeriod(parameters),
                parser.parseSettings(parameters),
                parser.getThreadPool(parameters));
    }

    @Override
    protected void stop() throws InterruptedException {
        if(attributeChecker != null){
            attributeChecker.close();
        }
        attributes.clear();
        notifications.clear();
        attributeChecker = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <M extends MBeanFeatureInfo> Multimap<String, ? extends FeatureBindingInfo<M>> getBindings(final Class<M> featureType) {
        if(featureType.isAssignableFrom(MBeanAttributeInfo.class))
            return (Multimap<String, ? extends FeatureBindingInfo<M>>)getBindings(attributes);
        else if(featureType.isAssignableFrom(MBeanNotificationInfo.class))
            return (Multimap<String, ? extends FeatureBindingInfo<M>>)getBindings(notifications);
        return super.getBindings(featureType);
    }
}
