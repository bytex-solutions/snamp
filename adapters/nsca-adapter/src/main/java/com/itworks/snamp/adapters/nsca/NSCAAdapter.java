package com.itworks.snamp.adapters.nsca;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.googlecode.jsendnsca.core.MessagePayload;
import com.googlecode.jsendnsca.core.NagiosException;
import com.googlecode.jsendnsca.core.NagiosPassiveCheckSender;
import com.googlecode.jsendnsca.core.NagiosSettings;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.*;
import com.itworks.snamp.concurrent.ThreadSafeObject;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.notifications.NotificationDescriptor;
import com.itworks.snamp.core.OSGiLoggingContext;
import com.itworks.snamp.internal.Utils;
import org.osgi.framework.BundleContext;

import javax.management.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.adapters.nsca.NSCAAdapterConfigurationDescriptor.*;

/**
 * Represents NSCA adapter.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class NSCAAdapter extends AbstractResourceAdapter {
    static final String NAME = "nsca";

    private static final class NSCAAttributeAccessor extends AttributeAccessor{

        private NSCAAttributeAccessor(final MBeanAttributeInfo metadata) {
            super(metadata);
        }

        private MessagePayload getMessage(){
            final MessagePayload payload = new MessagePayload();
            payload.setServiceName(getServiceName(getMetadata().getDescriptor(),
                    AttributeDescriptor.getAttributeName(getMetadata().getDescriptor())));
            try{
                payload.setMessage(Objects.toString(getValue(), ""));
            }
            catch (final AttributeNotFoundException e){
                payload.setMessage(e.getMessage());
                payload.setLevel(MessagePayload.LEVEL_WARNING);
            }
            catch (final JMException e){
                payload.setMessage(e.getMessage());
                payload.setLevel(MessagePayload.LEVEL_CRITICAL);
            }
            return payload;
        }
    }

    private static final class NSCAAttributeModel extends AbstractAttributesModel<NSCAAttributeAccessor>{

        @Override
        protected NSCAAttributeAccessor createAccessor(final MBeanAttributeInfo metadata) throws Exception {
            return new NSCAAttributeAccessor(metadata);
        }
    }

    private static final class NSCASourceInfo{
        private final String hostName;
        private final String serviceName;
        private final int level;

        private NSCASourceInfo(final String resourceName,
                               final Descriptor metadata){
            serviceName = getServiceName(metadata,
                    NotificationDescriptor.getNotificationCategory(metadata));
            hostName = resourceName;
            switch (NotificationDescriptor.getSeverity(metadata)){
                case ALERT:
                case WARNING: level = MessagePayload.LEVEL_WARNING; break;
                case ERROR:
                case PANIC: level = MessagePayload.LEVEL_CRITICAL; break;
                case NOTICE:
                case INFO:
                case DEBUG:
                case UNKNOWN:
                    level = MessagePayload.LEVEL_OK; break;
                default: level = MessagePayload.LEVEL_UNKNOWN;
            }
        }
    }

    private static final class NSCANotificationAccessor extends NotificationRouter{
        private final String resourceName;

        private <L extends ThreadSafeObject & NotificationListener> NSCANotificationAccessor(final String resourceName,
                                                                                                     final MBeanNotificationInfo metadata,
                                                                                                     final L listener) {
            super(metadata, listener);
            this.resourceName = resourceName;
        }

        @Override
        protected Notification intercept(final Notification notification) {
            notification.setSource(new NSCASourceInfo(resourceName, getMetadata().getDescriptor()));
            return notification;
        }
    }

    private static final class NSCANotificationModel extends ThreadSafeObject implements NotificationListener{
        private final Logger logger;
        private final Map<String, ResourceNotificationList<NSCANotificationAccessor>> notifications;
        private NagiosPassiveCheckSender checkSender;

        private NSCANotificationModel(final Logger logger) {
            this.logger = logger;
            this.notifications = new HashMap<>(10);
        }

        private BundleContext getBundleContext(){
            return Utils.getBundleContextByObject(this);
        }

        private void setCheckSender(final NagiosPassiveCheckSender value){
            this.checkSender = value;
        }

        @Override
        public void handleNotification(final Notification notification, final Object handback) {
            final NSCASourceInfo source = (NSCASourceInfo) notification.getSource();
            final MessagePayload payload = new MessagePayload();
            payload.setLevel(source.level);
            payload.setMessage(notification.getMessage());
            payload.setMessage(source.hostName);
            payload.setServiceName(source.serviceName);
            final NagiosPassiveCheckSender checkSender = this.checkSender;
            if (checkSender != null)
                try {
                    checkSender.send(payload);
                } catch (final NagiosException e) {
                    try (final OSGiLoggingContext context = OSGiLoggingContext.get(logger, getBundleContext())) {
                        context.log(Level.WARNING, "Unable to send passive check", e);
                    }
                } catch (final IOException e) {
                    try (final OSGiLoggingContext context = OSGiLoggingContext.get(logger, getBundleContext())) {
                        context.log(Level.SEVERE, "Unable to establish connection", e);
                    }
                }
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

        private void clear(){
            try(final LockScope ignored = beginWrite()){
                for(final ResourceNotificationList<?> list: notifications.values())
                    for(final NotificationAccessor accessor: list.values())
                        accessor.disconnect();
            }
            checkSender = null;
        }
    }

    private static final class NSCAPeriodPassiveCheckSender extends PeriodicPassiveCheckSender<NSCAAttributeAccessor>{
        private final Logger logger;
        private final NagiosPassiveCheckSender checkSender;

        NSCAPeriodPassiveCheckSender(final TimeSpan period,
                                     final NagiosPassiveCheckSender sender,
                                     final NSCAAttributeModel attributes,
                                     final Logger logger) {
            super(period, attributes);
            checkSender = Objects.requireNonNull(sender);
            this.logger = logger;
        }

        private BundleContext getBundleContext(){
            return Utils.getBundleContextByObject(this);
        }

        @Override
        protected void sendCheck(final String resourceName, final NSCAAttributeAccessor accessor) {
            final MessagePayload payload = accessor.getMessage();
            payload.setHostname(resourceName);
            try {
                checkSender.send(payload);
            } catch (final NagiosException e) {
                try (final OSGiLoggingContext context = OSGiLoggingContext.get(logger, getBundleContext())) {
                    context.log(Level.WARNING, "Unable to send passive check", e);
                }
            } catch (final IOException e) {
                try (final OSGiLoggingContext context = OSGiLoggingContext.get(logger, getBundleContext())) {
                    context.log(Level.SEVERE, "Unable to establish connection", e);
                }
            }
        }
    }

    private final NSCAAttributeModel attributes;
    private NSCAPeriodPassiveCheckSender attributeChecker;
    private final NSCANotificationModel notifications;

    NSCAAdapter(final String instanceName) {
        super(instanceName);
        attributes = new NSCAAttributeModel();
        notifications = new NSCANotificationModel(getLogger());
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
                       final NagiosSettings settings) {
        final NagiosPassiveCheckSender checkSender = new NagiosPassiveCheckSender(settings);
        notifications.setCheckSender(checkSender);
        attributeChecker = new NSCAPeriodPassiveCheckSender(checkPeriod, checkSender, attributes, getLogger());
        attributeChecker.run();
    }

    @Override
    protected void start(final Map<String, String> parameters) throws AbsentNSCAConfigurationParameterException {
        start(getPassiveCheckSendPeriod(parameters),
                parseSettings(parameters));
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
