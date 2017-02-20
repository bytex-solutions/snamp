package com.bytex.snamp.gateway.modeling;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.EntryReader;

import javax.management.MBeanNotificationInfo;
import java.util.Collection;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class ModelOfNotifications<TAccessor extends NotificationAccessor> extends ModelOfFeatures<MBeanNotificationInfo, TAccessor, ResourceNotificationList<TAccessor>> implements NotificationSet<TAccessor> {
    /**
     * Initializes a new thread-safe object.
     *
     * @param resourceGroupDef The type of the enum which represents a set of field groups.
     * @param <G> Enum definition.
     */
    protected <G extends Enum<G>> ModelOfNotifications(final Class<G> resourceGroupDef, final Enum<G> listGroup) {
        super(ResourceNotificationList::new, resourceGroupDef, listGroup);
    }

    /**
     * Initializes a new thread-safe object in which all fields represents the single resource.
     */
    protected ModelOfNotifications() {
        this(SingleResourceGroup.class, SingleResourceGroup.INSTANCE);
    }

    public final TAccessor addNotification(final String resourceName,
                                        final MBeanNotificationInfo metadata) throws Exception{
        return addFeature(resourceName, metadata);
    }

    public final TAccessor removeNotification(final String resourceName,
                                           final MBeanNotificationInfo metadata){
        return removeFeature(resourceName, metadata);
    }

    @Override
    protected abstract TAccessor createAccessor(final String resourceName, final MBeanNotificationInfo metadata) throws Exception;

    public final <E extends Throwable> void forEachNotification(final EntryReader<String, ? super TAccessor, E> notificationReader) throws E{
        forEachFeature(notificationReader);
    }

    public final <E extends Throwable> boolean processNotification(final String resourceName,
                                                                final String notificationType,
                                                                final Acceptor<? super TAccessor, E> processor) throws E, InterruptedException {
        return processFeature(resourceName, notificationType, processor);
    }

    public final Collection<MBeanNotificationInfo> getResourceNotificationsMetadata(final String resourceName){
        return getResourceFeaturesMetadata(resourceName);
    }
}
