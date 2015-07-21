package com.itworks.snamp.adapters.groovy.impl.binding;

import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.adapters.AbstractAttributesModel;
import com.itworks.snamp.adapters.AbstractNotificationsModel;
import com.itworks.snamp.adapters.AttributesModelReader;
import com.itworks.snamp.adapters.NotificationsModelReader;
import com.itworks.snamp.adapters.groovy.impl.ScriptAttributeAccessor;
import com.itworks.snamp.adapters.groovy.impl.ScriptNotificationAccessor;
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
public final class GroovyAdapterRuntimeInfo {
    private GroovyAdapterRuntimeInfo(){

    }

    private static Collection<ScriptAttributeBindingInfo> getAttributes(final AttributesModelReader<ScriptAttributeAccessor> model){
        final List<ScriptAttributeBindingInfo> result = new LinkedList<>();
        model.forEachAttribute(new RecordReader<String, ScriptAttributeAccessor, ExceptionPlaceholder>() {
            @Override
            public boolean read(final String resourceName, final ScriptAttributeAccessor accessor) {
                result.add(new ScriptAttributeBindingInfo(resourceName, accessor));
                return true;
            }
        });
        return result;
    }

    private static Collection<ScriptNotificationBindingInfo> getNotifications(final NotificationsModelReader<ScriptNotificationAccessor> model){
        final List<ScriptNotificationBindingInfo> result = new LinkedList<>();
        model.forEachNotification(new RecordReader<String, ScriptNotificationAccessor, ExceptionPlaceholder>() {
            @Override
            public boolean read(final String resourceName, final ScriptNotificationAccessor accessor) {
                result.add(new ScriptNotificationBindingInfo(accessor));
                return true;
            }
        });
        return result;
    }

    public static <B extends FeatureBindingInfo> Collection<? extends B> getBindings(final Class<B> bindingType,
                                                                       final AttributesModelReader<ScriptAttributeAccessor> attributes,
                                                                       final NotificationsModelReader<ScriptNotificationAccessor> notifs) {
        if (bindingType.isAssignableFrom(ScriptAttributeBindingInfo.class))
            return (Collection<B>) getAttributes(attributes);
        else if (bindingType.isAssignableFrom(ScriptNotificationBindingInfo.class))
            return (Collection<B>) getNotifications(notifs);
        else return Collections.emptyList();
    }
}