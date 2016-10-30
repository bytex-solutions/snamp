package com.bytex.snamp.gateway.nrdp;

import ch.shamu.jsendnrdp.NRDPServerConnectionSettings;
import ch.shamu.jsendnrdp.domain.NagiosCheckResult;
import ch.shamu.jsendnrdp.domain.State;
import com.bytex.snamp.EntryReader;
import com.bytex.snamp.gateway.AbstractGateway;
import com.bytex.snamp.gateway.NotificationEvent;
import com.bytex.snamp.gateway.NotificationListener;
import com.bytex.snamp.gateway.modeling.*;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.internal.Utils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanNotificationInfo;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class NRDPGateway extends AbstractGateway {

    private static final class NRDPAttributeModelOfAttributes extends ModelOfAttributes<NRDPAttributeAccessor> {

        @Override
        protected NRDPAttributeAccessor createAccessor(final MBeanAttributeInfo metadata) throws Exception {
            return new NRDPAttributeAccessor(metadata);
        }
    }

    private static final class NRDPNotificationModel extends ModelOfNotifications<NRDPNotificationAccessor> implements NotificationListener{
        private final Map<String, ResourceNotificationList<NRDPNotificationAccessor>> notifications;
        private ConcurrentPassiveCheckSender checkSender;

        private NRDPNotificationModel() {
            this.notifications = new HashMap<>(10);
        }

        private <E extends Exception> void forEachNotificationImpl(final EntryReader<String, ? super NRDPNotificationAccessor, E> notificationReader) throws E {
            for (final ResourceNotificationList<NRDPNotificationAccessor> list : notifications.values())
                for (final NRDPNotificationAccessor accessor : list.values())
                    if (!notificationReader.read(accessor.resourceName, accessor)) return;
        }

        @Override
        public <E extends Exception> void forEachNotification(final EntryReader<String, ? super NRDPNotificationAccessor, E> notificationReader) throws E {
            readLock.accept(SingleResourceGroup.INSTANCE, notificationReader, this::forEachNotificationImpl);
        }

        private void setCheckSender(final ConcurrentPassiveCheckSender value){
            this.checkSender = value;
        }

        @Override
        public void handleNotification(final NotificationEvent event) {
            final State level = NRDPNotificationAccessor.getLevel(event.getSource());
            final String resourceName = (String)event.getNotification().getSource();
            final String serviceName = NRDPNotificationAccessor.getServiceName(event.getSource());
            final ConcurrentPassiveCheckSender checkSender = this.checkSender;
            if (checkSender != null)
                checkSender.send(new NagiosCheckResult(resourceName,
                        serviceName,
                        level,
                        event.getNotification().getMessage()));
        }

        private NotificationAccessor addNotificationImpl(final String resourceName,
                                                     final MBeanNotificationInfo metadata){
            final ResourceNotificationList<NRDPNotificationAccessor> list;
            if(notifications.containsKey(resourceName))
                list = notifications.get(resourceName);
            else notifications.put(resourceName, list = new ResourceNotificationList<>());
            final NRDPNotificationAccessor accessor;
            list.put(accessor = new NRDPNotificationAccessor(resourceName, metadata, this));
            return accessor;
        }

        private NotificationAccessor addNotification(final String resourceName,
                                                     final MBeanNotificationInfo metadata){
            return writeLock.apply(SingleResourceGroup.INSTANCE, resourceName, metadata, this::addNotificationImpl);
        }

        private NotificationAccessor removeNotificationImpl(final String resourceName,
                                                        final MBeanNotificationInfo metadata){
            final ResourceNotificationList<NRDPNotificationAccessor> list;
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

        private Collection<? extends NotificationAccessor> removeNotifications(final String resourceName){
            return writeLock.apply(SingleResourceGroup.INSTANCE, resourceName, notifications,
                    (resName, notifs) -> notifs.containsKey(resName) ? notifs.remove(resName).values() : ImmutableList.of());
        }

        private void clear() {
            writeLock.accept(SingleResourceGroup.INSTANCE, notifications, notifs -> {
                notifs.values().forEach(list -> list.values().forEach(NotificationAccessor::close));
                notifs.clear();
            });
            checkSender = null;
        }
    }

    private static final class NSCAPeriodPassiveCheckSender extends PeriodicPassiveChecker<NRDPAttributeAccessor> {
        private final ConcurrentPassiveCheckSender checkSender;

        NSCAPeriodPassiveCheckSender(final Duration period,
                                     final ConcurrentPassiveCheckSender sender,
                                     final NRDPAttributeModelOfAttributes attributes) {
            super(period, attributes);
            checkSender = Objects.requireNonNull(sender);
        }

        @Override
        protected boolean processAttribute(final String resourceName, final NRDPAttributeAccessor accessor) {
            if (DistributedServices.isActiveNode(Utils.getBundleContextOfObject(this))) {
                checkSender.send(accessor, resourceName);
                return true;
            } else return false;
        }
    }

    private final NRDPAttributeModelOfAttributes attributes;
    private NSCAPeriodPassiveCheckSender attributeChecker;
    private final NRDPNotificationModel notifications;

    NRDPGateway(final String instanceName) {
        super(instanceName);
        attributes = new NRDPAttributeModelOfAttributes();
        notifications = new NRDPNotificationModel();
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

    private synchronized void start(final Duration checkPeriod,
                       final NRDPServerConnectionSettings settings,
                       final ExecutorService threadPool) {
        final ConcurrentPassiveCheckSender checkSender = new ConcurrentPassiveCheckSender(settings, threadPool);
        notifications.setCheckSender(checkSender);
        attributeChecker = new NSCAPeriodPassiveCheckSender(checkPeriod, checkSender, attributes);
        attributeChecker.run();
    }

    @Override
    protected void start(final Map<String, String> parameters) throws AbsentNRDPConfigurationParameterException {
        final NRDPGatewayConfigurationDescriptor parser = NRDPGatewayConfigurationDescriptor.getInstance();
        start(parser.getPassiveCheckSendPeriod(parameters),
                parser.parseSettings(parameters),
                parser.getThreadPool(parameters));
    }

    @Override
    protected synchronized void stop() throws InterruptedException {
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