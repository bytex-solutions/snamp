package com.bytex.snamp.gateway.nsca;

import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.gateway.AbstractGateway;
import com.bytex.snamp.gateway.NotificationEvent;
import com.bytex.snamp.gateway.NotificationListener;
import com.bytex.snamp.gateway.modeling.FeatureAccessor;
import com.bytex.snamp.gateway.modeling.ModelOfAttributes;
import com.bytex.snamp.gateway.modeling.ModelOfNotifications;
import com.bytex.snamp.gateway.modeling.PeriodicPassiveChecker;
import com.bytex.snamp.internal.Utils;
import com.google.common.collect.Multimap;
import com.googlecode.jsendnsca.core.MessagePayload;
import com.googlecode.jsendnsca.core.NagiosSettings;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanNotificationInfo;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

/**
 * Represents NSCA gateway.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
final class NSCAGateway extends AbstractGateway {

    private static final class NSCAAttributeModelOfAttributes extends ModelOfAttributes<NSCAAttributeAccessor> {

        @Override
        protected NSCAAttributeAccessor createAccessor(final String resourceName, final MBeanAttributeInfo metadata) {
            return new NSCAAttributeAccessor(metadata);
        }
    }

    private static final class NSCANotificationModel extends ModelOfNotifications<NSCANotificationAccessor> implements NotificationListener{
        private ConcurrentPassiveCheckSender checkSender;

        @Override
        protected NSCANotificationAccessor createAccessor(final String resourceName, final MBeanNotificationInfo metadata) {
            return new NSCANotificationAccessor(resourceName, metadata, this);
        }

        private void setCheckSender(final ConcurrentPassiveCheckSender value){
            this.checkSender = value;
        }

        @Override
        public void handleNotification(final NotificationEvent event) {
            final int level = NSCANotificationAccessor.getLevel(event.getMetadata());
            final String serviceName = NSCANotificationAccessor.getServiceName(event.getMetadata());
            final MessagePayload payload = new MessagePayload();
            payload.setLevel(level);
            payload.setMessage(event.getNotification().getMessage());
            payload.setMessage(event.getResourceName());
            payload.setServiceName(serviceName);
            final ConcurrentPassiveCheckSender checkSender = this.checkSender;
            if (checkSender != null)
                checkSender.send(payload);
        }

        @Override
        protected void cleared() {
            checkSender = null;
        }
    }

    private static final class NSCAPeriodPassiveCheckSender extends PeriodicPassiveChecker<NSCAAttributeAccessor> {
        private final ConcurrentPassiveCheckSender checkSender;
        private final ClusterMember clusterMember;

        NSCAPeriodPassiveCheckSender(final Duration period,
                                     final ConcurrentPassiveCheckSender sender,
                                     final NSCAAttributeModelOfAttributes attributes) {
            super(period, attributes);
            checkSender = Objects.requireNonNull(sender);
            clusterMember = ClusterMember.get(Utils.getBundleContextOfObject(this));
        }

        @Override
        public boolean accept(final String resourceName, final NSCAAttributeAccessor accessor) {
            if(clusterMember.isActive()) {
                checkSender.send(accessor, resourceName);
                return true;
            } else return false;
        }
    }

    private final NSCAAttributeModelOfAttributes attributes;
    private NSCAPeriodPassiveCheckSender attributeChecker;
    private final NSCANotificationModel notifications;

    NSCAGateway(final String instanceName) {
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
    protected Stream<? extends FeatureAccessor<?>> removeAllFeatures(final String resourceName) throws Exception {
        return Stream.concat(
                notifications.clear(resourceName).stream(),
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

    private void start(final Duration checkPeriod,
                       final NagiosSettings settings,
                       final ExecutorService threadPool) {
        final ConcurrentPassiveCheckSender checkSender = new ConcurrentPassiveCheckSender(settings, threadPool);
        notifications.setCheckSender(checkSender);
        attributeChecker = new NSCAPeriodPassiveCheckSender(checkPeriod, checkSender, attributes);
        attributeChecker.run();
    }

    @Override
    protected void start(final Map<String, String> parameters) throws AbsentNSCAConfigurationParameterException {
        final NSCAGatewayConfigurationDescriptor parser = NSCAGatewayConfigurationDescriptor.getInstance();
        start(parser.getPassiveCheckSendPeriod(parameters),
                parser.parseSettings(parameters),
                parser.parseThreadPool(parameters));
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
