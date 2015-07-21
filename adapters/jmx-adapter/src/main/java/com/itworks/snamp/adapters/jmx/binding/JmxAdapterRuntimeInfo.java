package com.itworks.snamp.adapters.jmx.binding;

import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.adapters.modeling.AttributeSet;
import com.itworks.snamp.adapters.modeling.NotificationSet;
import com.itworks.snamp.adapters.jmx.JmxAttributeAccessor;
import com.itworks.snamp.adapters.jmx.JmxNotificationAccessor;
import com.itworks.snamp.internal.RecordReader;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class JmxAdapterRuntimeInfo {

    private static Collection<JmxAttributeBindingInfo> getAttributes(final AttributeSet<JmxAttributeAccessor> attributes){
        final List<JmxAttributeBindingInfo> result = new LinkedList<>();
        attributes.forEachAttribute(new RecordReader<String, JmxAttributeAccessor, ExceptionPlaceholder>() {
            @Override
            public boolean read(final String resourceName, final JmxAttributeAccessor accessor) {
                result.add(new JmxAttributeBindingInfo(resourceName, accessor));
                return true;
            }
        });
        return result;
    }

    private static Collection<JmxNotificationBindingInfo> getNotifications(final NotificationSet<JmxNotificationAccessor> notifs){
        final List<JmxNotificationBindingInfo> result = new LinkedList<>();
        notifs.forEachNotification(new RecordReader<String, JmxNotificationAccessor, ExceptionPlaceholder>() {
            @Override
            public boolean read(final String resourceName, final JmxNotificationAccessor accessor) {
                result.add(new JmxNotificationBindingInfo(resourceName, accessor));
                return true;
            }
        });
        return result;
    }

    public static <B extends FeatureBindingInfo> Collection<? extends B> getBindingInfo(final Class<B> bindingType,
                                                                                        final AttributeSet<JmxAttributeAccessor> attributes,
                                                                                        final NotificationSet<JmxNotificationAccessor> notifs) {
        if (bindingType.isAssignableFrom(JmxAttributeBindingInfo.class))
            return (Collection<B>) getAttributes(attributes);
        else if (bindingType.isAssignableFrom(JmxNotificationBindingInfo.class))
            return (Collection<B>) getNotifications(notifs);
        else return Collections.emptyList();
    }
}
