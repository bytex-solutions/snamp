package com.bytex.snamp.gateway.nrdp;

import ch.shamu.jsendnrdp.NRDPServerConnectionSettings;
import ch.shamu.jsendnrdp.domain.NagiosCheckResult;
import ch.shamu.jsendnrdp.domain.State;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.gateway.AbstractGateway;
import com.bytex.snamp.gateway.NotificationEvent;
import com.bytex.snamp.gateway.NotificationListener;
import com.bytex.snamp.gateway.modeling.FeatureAccessor;
import com.bytex.snamp.gateway.modeling.ModelOfAttributes;
import com.bytex.snamp.gateway.modeling.ModelOfNotifications;
import com.bytex.snamp.gateway.modeling.PeriodicPassiveChecker;
import com.bytex.snamp.internal.Utils;
import com.google.common.collect.Multimap;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanNotificationInfo;
import java.time.Duration;
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
        protected NRDPAttributeAccessor createAccessor(final String resourceName, final MBeanAttributeInfo metadata) throws Exception {
            return new NRDPAttributeAccessor(metadata);
        }
    }

    private static final class NRDPNotificationModel extends ModelOfNotifications<NRDPNotificationAccessor> implements NotificationListener{
        private ConcurrentPassiveCheckSender checkSender;

        @Override
        protected NRDPNotificationAccessor createAccessor(final String resourceName, final MBeanNotificationInfo metadata) throws Exception {
            return new NRDPNotificationAccessor(resourceName, metadata, this);
        }

        private void setCheckSender(final ConcurrentPassiveCheckSender value){
            this.checkSender = value;
        }

        @Override
        public void handleNotification(final NotificationEvent event) {
            final State level = NRDPNotificationAccessor.getLevel(event.getMetadata());
            final String serviceName = NRDPNotificationAccessor.getServiceName(event.getMetadata());
            final ConcurrentPassiveCheckSender checkSender = this.checkSender;
            if (checkSender != null)
                checkSender.send(new NagiosCheckResult(event.getResourceName(),
                        serviceName,
                        level,
                        event.getNotification().getMessage()));
        }

        @Override
        protected void cleared() {
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
        public boolean accept(final String resourceName, final NRDPAttributeAccessor accessor) {
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
                parser.parseThreadPool(parameters));
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
