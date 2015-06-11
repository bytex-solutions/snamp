package com.itworks.snamp.adapters.nrdp;

import ch.shamu.jsendnrdp.NRDPServerConnectionSettings;
import ch.shamu.jsendnrdp.domain.NagiosCheckResult;
import ch.shamu.jsendnrdp.domain.State;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.*;
import com.itworks.snamp.adapters.NotificationListener;
import com.itworks.snamp.concurrent.ThreadSafeObject;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.notifications.NotificationDescriptor;

import javax.management.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import static com.itworks.snamp.adapters.nrdp.NRDPAdapterConfigurationDescriptor.*;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class NRDPAdapter extends AbstractResourceAdapter {
    static String NAME = "nrdp";

    private static final class NRDPAttributeAccessor extends AttributeAccessor {
        private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat();

        private NRDPAttributeAccessor(final MBeanAttributeInfo metadata) {
            super(metadata);
        }

        private NagiosCheckResult getCheckResult(final String host){
            State state;
            String message;
            final String service = getServiceName(getMetadata().getDescriptor(),
                    AttributeDescriptor.getAttributeName(getMetadata().getDescriptor()));
            try{
                final Object attributeValue = getValue();
                if(attributeValue instanceof Number)
                    state = isInRange((Number)attributeValue, DECIMAL_FORMAT) ?
                            State.OK : State.CRITICAL;
                else state = State.OK;
                message = Objects.toString(attributeValue, "0") +
                        getUnitOfMeasurement(getMetadata().getDescriptor());
            }
            catch (final AttributeNotFoundException | ParseException e){
                message = e.getMessage();
                state = State.WARNING;
            }
            catch (final JMException e){
                message = e.getMessage();
                state = State.CRITICAL;
            }
            return new NagiosCheckResult(host, service, state, message);
        }

        @Override
        public boolean canWrite() {
            return false;
        }
    }

    private static final class NRDPAttributeModel extends AbstractAttributesModel<NRDPAttributeAccessor> {

        @Override
        protected NRDPAttributeAccessor createAccessor(final MBeanAttributeInfo metadata) throws Exception {
            return new NRDPAttributeAccessor(metadata);
        }
    }

    private static final class NRDPNotificationAccessor extends NotificationRouter {
        private final String resourceName;

        private <L extends ThreadSafeObject & NotificationListener> NRDPNotificationAccessor(final String resourceName,
                                                                                             final MBeanNotificationInfo metadata,
                                                                                             final L listener) {
            super(metadata, listener);
            this.resourceName = resourceName;
        }

        @Override
        protected Notification intercept(final Notification notification) {
            notification.setSource(resourceName);
            return notification;
        }

        private static State getLevel(final MBeanNotificationInfo metadata){
            switch (NotificationDescriptor.getSeverity(metadata)){
                case NOTICE:
                case WARNING: return State.WARNING;
                case ALERT:
                case ERROR:
                case PANIC: return State.CRITICAL;
                case INFO:
                case DEBUG:
                case UNKNOWN:
                    return State.OK;
                default: return State.UNKNOWN;
            }
        }

        private static String getServiceName(final MBeanNotificationInfo metadata){
            return NRDPAdapterConfigurationDescriptor.getServiceName(metadata.getDescriptor(),
                    NotificationDescriptor.getNotificationCategory(metadata));
        }
    }

    private static final class NRDPNotificationModel extends ThreadSafeObject implements NotificationListener{
        private final Map<String, ResourceNotificationList<NRDPNotificationAccessor>> notifications;
        private ConcurrentPassiveCheckSender checkSender;

        private NRDPNotificationModel() {
            this.notifications = new HashMap<>(10);
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

        private NotificationAccessor addNotification(final String resourceName,
                                                     final MBeanNotificationInfo metadata){
            try(final LockScope ignored = beginWrite()){
                final ResourceNotificationList<NRDPNotificationAccessor> list;
                if(notifications.containsKey(resourceName))
                    list = notifications.get(resourceName);
                else notifications.put(resourceName, list = new ResourceNotificationList<>());
                final NRDPNotificationAccessor accessor;
                list.put(accessor = new NRDPNotificationAccessor(resourceName, metadata, this));
                return accessor;
            }
        }

        private NotificationAccessor removeNotification(final String resourceName,
                                                        final MBeanNotificationInfo metadata){
            try(final LockScope ignored = beginWrite()){
                final ResourceNotificationList<NRDPNotificationAccessor> list;
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
            final ConcurrentPassiveCheckSender sender = checkSender;
            if (sender != null)
                sender.close();
            checkSender = null;
        }
    }

    private static final class NSCAPeriodPassiveCheckSender extends PeriodicPassiveChecker<NRDPAttributeAccessor> {
        private final ConcurrentPassiveCheckSender checkSender;

        NSCAPeriodPassiveCheckSender(final TimeSpan period,
                                     final ConcurrentPassiveCheckSender sender,
                                     final NRDPAttributeModel attributes) {
            super(period, attributes);
            checkSender = Objects.requireNonNull(sender);
        }

        @Override
        protected void processAttribute(final String resourceName, final NRDPAttributeAccessor accessor) {
            final NagiosCheckResult payload = accessor.getCheckResult(resourceName);
            checkSender.send(payload);
        }
    }

    private final NRDPAttributeModel attributes;
    private NSCAPeriodPassiveCheckSender attributeChecker;
    private final NRDPNotificationModel notifications;

    NRDPAdapter(final String instanceName) {
        super(instanceName);
        attributes = new NRDPAttributeModel();
        notifications = new NRDPNotificationModel();
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

    private void start(final TimeSpan checkPeriod,
                       final NRDPServerConnectionSettings settings,
                       final Supplier<ExecutorService> threadPoolFactory) {
        final ConcurrentPassiveCheckSender checkSender = new ConcurrentPassiveCheckSender(settings, threadPoolFactory);
        notifications.setCheckSender(checkSender);
        attributeChecker = new NSCAPeriodPassiveCheckSender(checkPeriod, checkSender, attributes);
        attributeChecker.run();
    }

    @Override
    protected void start(final Map<String, String> parameters) throws AbsentNRDPConfigurationParameterException {
        start(getPassiveCheckSendPeriod(parameters),
                parseSettings(parameters),
                new SenderThreadPoolConfig(parameters, NAME, getInstanceName()));
    }

    @Override
    protected void stop() {
        if(attributeChecker != null){
            attributeChecker.close();
        }
        attributes.clear();
        notifications.clear();
        attributeChecker = null;
    }

    @Override
    public Logger getLogger() {
        return getLogger(NAME);
    }
}
