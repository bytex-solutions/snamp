package com.itworks.snamp.adapters.http.binding;

import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.adapters.AbstractAttributesModel;
import com.itworks.snamp.adapters.AbstractNotificationsModel;
import com.itworks.snamp.adapters.http.HttpAttributeAccessor;
import com.itworks.snamp.adapters.http.HttpNotificationAccessor;
import com.itworks.snamp.adapters.binding.FeatureBindingInfo;
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
public final class HttpAdapterRuntimeInfo {

    private static Collection<HttpAttributeBindingInfo> getAttributes(final String servletContext,
                                                                  final AbstractAttributesModel<HttpAttributeAccessor> attributes){
        final List<HttpAttributeBindingInfo> result = new LinkedList<>();
        attributes.forEachAttribute(new RecordReader<String, HttpAttributeAccessor, ExceptionPlaceholder>() {
            @Override
            public void read(final String resourceName, final HttpAttributeAccessor accessor) {
                result.add(new HttpAttributeBindingInfo(servletContext, resourceName, accessor));
            }
        });
        return result;
    }

    private static Collection<HttpNotificationBindingInfo> getNotifications(final String servletContext,
                                                                        final AbstractNotificationsModel<HttpNotificationAccessor> notifs){
        final List<HttpNotificationBindingInfo> result = new LinkedList<>();
        notifs.forEachNotification(new RecordReader<String, HttpNotificationAccessor, ExceptionPlaceholder>() {
            @Override
            public void read(final String resourceName, final HttpNotificationAccessor accessor) {
                result.add(new HttpNotificationBindingInfo(servletContext, resourceName, accessor));
            }
        });
        return result;
    }

    public static <B extends FeatureBindingInfo> Collection<? extends B> getBindings(final Class<B> bindingType,
                                                                                 final String servletContext,
                                                                                 final AbstractAttributesModel<HttpAttributeAccessor> attributes,
                                                                                 final AbstractNotificationsModel<HttpNotificationAccessor> notifications) {
        if (bindingType.isAssignableFrom(HttpAttributeBindingInfo.class))
            return (Collection<B>) getAttributes(servletContext, attributes);
        else if (bindingType.isAssignableFrom(HttpNotificationBindingInfo.class))
            return (Collection<B>) getNotifications(servletContext, notifications);
        else return Collections.emptyList();
    }
}
