package com.itworks.snamp.connectors.jmx;

import com.google.common.base.Suppliers;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.MapBuilder;
import com.itworks.snamp.connectors.ManagedEntityType;
import com.itworks.snamp.connectors.ManagedEntityTypeBuilder.AbstractManagedEntityTabularType;
import com.itworks.snamp.connectors.ManagedEntityValue;

import javax.management.AttributeChangeNotification;
import javax.management.Descriptor;
import javax.management.Notification;
import javax.management.monitor.MonitorNotification;
import javax.management.openmbean.OpenType;
import javax.management.relation.RelationNotification;
import javax.management.timer.TimerNotification;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class AttachmentResolverFactory {
    private static final String ATTRIBUTE_CHANGE_NOTIFICATION_CLASS = AttributeChangeNotification.class.getName();
    private static final String MONITOR_NOTIFICATION_CLASS = MonitorNotification.class.getName();
    private static final String RELATION_NOTIFICATION_CLASS = RelationNotification.class.getName();
    private static final String TIMER_NOTIFICATION_CLASS = TimerNotification.class.getName();

    private AttachmentResolverFactory(){

    }

    private static void unableToReflectNotificationType(final Class<? extends Notification> notifType,
                                                        final Exception e){
        JmxConnectorHelpers.getLogger().log(Level.WARNING, String.format("Unable to reflect notification type %s", notifType), e);
    }

    private static final class DynamicAttachmentResolver implements AttachmentResolver {

        @Override
        public ManagedEntityType resolveType(final JmxTypeSystem typeSystem) {
            return null;
        }

        @Override
        public Object extractAttachment(final Notification notif,
                                        final JmxTypeSystem typeSystem) {
            try {
                final BeanInfo info = Introspector.getBeanInfo(notif.getClass(), Notification.class);
                final PropertyDescriptor[] properties = info.getPropertyDescriptors();
                final Map<String, Object> result = MapBuilder.createStringHashMap(properties.length);
                final Map<String, ManagedEntityType> attachmentType = new HashMap<>(properties.length);
                for (final PropertyDescriptor prop : properties) {
                    final Method getter = prop.getReadMethod();
                    if (getter == null) continue;
                    final Object attachmentValue = getter.invoke(notif);
                    final ManagedEntityType propertyType = typeSystem.createEntityType(attachmentValue.getClass(), Suppliers.<OpenType<?>>ofInstance(null));
                    if (propertyType == null) continue;
                    result.put(prop.getName(), attachmentValue);
                    attachmentType.put(prop.getName(), propertyType);
                }
                return new ManagedEntityValue<>(result, typeSystem.createEntityDictionaryType(attachmentType));
            } catch (final IntrospectionException | ReflectiveOperationException e) {
                unableToReflectNotificationType(notif.getClass(), e);
                return null;
            }
        }
    }

    private static final class UserDataBasedResolver implements AttachmentResolver{
        private final OpenType<?> userDataType;

        private UserDataBasedResolver(final OpenType<?> t){
            this.userDataType = t;
        }

        @Override
        public ManagedEntityType resolveType(final JmxTypeSystem typeSystem) {
            return typeSystem.createEntityType(userDataType);
        }

        @Override
        public Object extractAttachment(final Notification notif, final JmxTypeSystem typeSystem) {
            return notif.getUserData();
        }
    }

    private static final class ClassBasedAttachmentResolver<T extends Notification> implements AttachmentResolver{
        private final Class<T> notificationType;

        private ClassBasedAttachmentResolver(final Class<T> notifType){
            this.notificationType = notifType;
        }

        @Override
        public Object extractAttachment(final Notification notif, final JmxTypeSystem typeSystem) {
            if(notificationType.isInstance(notif))
                try{
                    final BeanInfo info = Introspector.getBeanInfo(notificationType, Notification.class);
                    final PropertyDescriptor[] properties = info.getPropertyDescriptors();
                    final Map<String, Object> attachments = MapBuilder.createStringHashMap(properties.length);
                    reflectProperties(properties, typeSystem, new PropertyReflector() {
                        @Override
                        public void reflect(final PropertyDescriptor prop, final ManagedEntityType type) throws ReflectiveOperationException {
                            attachments.put(prop.getName(), prop.getReadMethod().invoke(notif));
                        }
                    });
                    return attachments;
            }
            catch (final IntrospectionException | ReflectiveOperationException e){
                unableToReflectNotificationType(notif.getClass(), e);
            }
            return null;
        }

        @Override
        public AbstractManagedEntityTabularType resolveType(final JmxTypeSystem typeSystem) {
            try {
                final BeanInfo info = Introspector.getBeanInfo(notificationType, Notification.class);
                final PropertyDescriptor[] properties = info.getPropertyDescriptors();
                final Map<String, ManagedEntityType> attachmentType = new HashMap<>(properties.length);
                reflectProperties(properties, typeSystem, new PropertyReflector() {
                    @Override
                    public void reflect(final PropertyDescriptor prop, final ManagedEntityType propertyType) throws ReflectiveOperationException {
                        attachmentType.put(prop.getName(), propertyType);
                    }
                });
                return typeSystem.createEntityDictionaryType(attachmentType);
            }
            catch (final IntrospectionException | ReflectiveOperationException e) {
                unableToReflectNotificationType(notificationType, e);
                return null;
            }
        }
    }

    private static interface PropertyReflector{
        void reflect(final PropertyDescriptor descr, final ManagedEntityType type) throws ReflectiveOperationException;
    }

    private static void reflectProperties(final PropertyDescriptor[] properties,
                                          final JmxTypeSystem typeSystem,
                                          final PropertyReflector reflector) throws ReflectiveOperationException {
        for (final PropertyDescriptor prop : properties) {
            final Method getter = prop.getReadMethod();
            if (getter == null) continue;
            final ManagedEntityType propertyType = typeSystem.createEntityType(getter.getReturnType(), Suppliers.<OpenType<?>>ofInstance(null));
            if (propertyType == null) continue;
            reflector.reflect(prop, propertyType);
        }
    }

    static AttachmentResolver createResolver(final String notificationClassName,
                                                       final Descriptor notificationDescriptor) {
        if(Objects.equals(notificationClassName, ATTRIBUTE_CHANGE_NOTIFICATION_CLASS))
            return new ClassBasedAttachmentResolver<>(AttributeChangeNotification.class);
        else if(Objects.equals(notificationClassName, MONITOR_NOTIFICATION_CLASS))
            return new ClassBasedAttachmentResolver<>(MonitorNotification.class);
        else if(Objects.equals(notificationClassName, TIMER_NOTIFICATION_CLASS))
            return new ClassBasedAttachmentResolver<>(TimerNotification.class);
        else if(Objects.equals(notificationClassName, RELATION_NOTIFICATION_CLASS))
            return new ClassBasedAttachmentResolver<>(RelationNotification.class);
        else if (ArrayUtils.contains(notificationDescriptor.getFieldNames(), JmxTypeSystem.OPEN_TYPE_DESCR_FIELD))
            return new UserDataBasedResolver((OpenType<?>)notificationDescriptor.getFieldValue(JmxTypeSystem.OPEN_TYPE_DESCR_FIELD));
        else return new DynamicAttachmentResolver();
    }
}
