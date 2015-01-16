package com.itworks.snamp.connectors.jmx;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.connectors.ManagedEntityType;
import com.itworks.snamp.connectors.ManagedEntityValue;
import com.itworks.snamp.connectors.attributes.AttributeMetadata;
import com.itworks.snamp.internal.annotations.MethodStub;

import javax.management.AttributeChangeNotification;
import javax.management.Descriptor;
import javax.management.Notification;
import javax.management.monitor.MonitorNotification;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.relation.RelationNotification;
import javax.management.timer.TimerNotification;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.*;
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
        JmxConnectorHelpers.log(Level.WARNING, "Unable to reflect notification type %s", notifType, e);
    }

    private static final class DynamicAttachmentResolver implements AttachmentResolver {

        @Override
        public ManagedEntityType resolveType(final JmxTypeSystem typeSystem) {
            return null;
        }

        private static ManagedEntityValue<?> convertAttachment(final Object value,
                                                               final JmxTypeSystem typeSystem){
            final OpenType<?> type = JmxTypeSystem.getOpenTypeFromValue(value, Suppliers.<OpenType<?>>ofInstance(null));
            return type != null ? new ManagedEntityValue<>(value, typeSystem.createEntityType(type)) : null;
        }

        @Override
        public Object extractAttachment(final Notification notif,
                                        final JmxTypeSystem typeSystem) {
            return convertAttachment(notif.getUserData(), typeSystem);
        }

        @Override
        @MethodStub
        public void exposeTypeInfo(final Map<String, String> options) {

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

        @Override
        public void exposeTypeInfo(final Map<String, String> options) {
            JmxTypeSystem.exposeTypeInfo(userDataType, options);
        }
    }

    private static final class ClassBasedAttachmentResolver<T extends Notification> implements AttachmentResolver{
        private final Class<T> notificationType;
        private static final String TYPE_NAME = "Attachment";
        private static final String TYPE_DESCR = "Attachment Payload";

        private ClassBasedAttachmentResolver(final Class<T> notifType){
            this.notificationType = notifType;
        }

        @Override
        public Object extractAttachment(final Notification notif, final JmxTypeSystem typeSystem) {
            if (notificationType.isInstance(notif))
                try {
                    final BeanInfo info = Introspector.getBeanInfo(notificationType, Notification.class);
                    final PropertyDescriptor[] properties = info.getPropertyDescriptors();
                    final List<String>
                            itemDescriptions = new ArrayList<>(properties.length);
                    final List<OpenType<?>> itemTypes = new ArrayList<>(properties.length);
                    final Map<String, Object> itemValues = Maps.newHashMapWithExpectedSize(properties.length);
                    reflectProperties(properties, typeSystem, new PropertyReflector() {
                        @Override
                        public void reflect(final PropertyDescriptor prop, final JmxManagedEntityOpenType<?> type) throws ReflectiveOperationException {
                            itemDescriptions.add(prop.getName());
                            itemTypes.add(type.getOpenType());
                            itemValues.put(prop.getName(), prop.getReadMethod().invoke(notif));
                        }
                    });
                    return new CompositeDataSupport(new CompositeType(TYPE_NAME,
                            TYPE_DESCR,
                            ArrayUtils.toArray(itemValues.keySet(), String.class),
                            ArrayUtils.toArray(itemDescriptions, String.class),
                            itemTypes.toArray(new OpenType<?>[itemTypes.size()])
                    ), itemValues);
                } catch (final IntrospectionException | ReflectiveOperationException | OpenDataException e) {
                    unableToReflectNotificationType(notif.getClass(), e);
                }
            return null;
        }

        @Override
        @MethodStub
        public void exposeTypeInfo(final Map<String, String> options) {
            options.put(AttributeMetadata.TYPE_NAME, TYPE_NAME);
            options.put(AttributeMetadata.TYPE_DESCRIPTION, TYPE_DESCR);
        }

        private CompositeType resolveTypeCore(final JmxTypeSystem typeSystem) throws IntrospectionException, ReflectiveOperationException, OpenDataException {
            final BeanInfo info = Introspector.getBeanInfo(notificationType, Notification.class);
            final PropertyDescriptor[] properties = info.getPropertyDescriptors();
            final List<String> itemNames = new ArrayList<>(properties.length),
                    itemDescriptions = new ArrayList<>(properties.length);
            final List<OpenType<?>> itemTypes = new ArrayList<>(properties.length);
            reflectProperties(properties, typeSystem, new PropertyReflector() {
                @Override
                public void reflect(final PropertyDescriptor prop, final JmxManagedEntityOpenType<?> propertyType) throws ReflectiveOperationException {
                    itemNames.add(prop.getName());
                    itemDescriptions.add(prop.getName());
                    itemTypes.add(propertyType.getOpenType());
                }
            });
            return new CompositeType(TYPE_NAME,
                    TYPE_DESCR,
                    ArrayUtils.toArray(itemNames, String.class),
                    ArrayUtils.toArray(itemDescriptions, String.class),
                    itemTypes.toArray(new OpenType<?>[itemTypes.size()])
                    );
        }

        @Override
        public JmxManagedEntityOpenType<?> resolveType(final JmxTypeSystem typeSystem) {
            try {
                return typeSystem.createEntityType(resolveTypeCore(typeSystem));
            }
            catch (final IntrospectionException | ReflectiveOperationException | OpenDataException e) {
                unableToReflectNotificationType(notificationType, e);
                return null;
            }
        }
    }

    private static interface PropertyReflector{
        void reflect(final PropertyDescriptor descr, final JmxManagedEntityOpenType<?> type) throws ReflectiveOperationException;
    }

    private static void reflectProperties(final PropertyDescriptor[] properties,
                                          final JmxTypeSystem typeSystem,
                                          final PropertyReflector reflector) throws ReflectiveOperationException {
        for (final PropertyDescriptor prop : properties) {
            final Method getter = prop.getReadMethod();
            if (getter == null) continue;
            final JmxManagedEntityOpenType<?> propertyType = typeSystem.createEntityType(getter.getReturnType(), Suppliers.<OpenType<?>>ofInstance(null));
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
