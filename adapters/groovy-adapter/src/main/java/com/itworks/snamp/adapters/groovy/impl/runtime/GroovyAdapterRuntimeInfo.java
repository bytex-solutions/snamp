package com.itworks.snamp.adapters.groovy.impl.runtime;

import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.adapters.AbstractAttributesModel;
import com.itworks.snamp.adapters.AbstractNotificationsModel;
import com.itworks.snamp.adapters.groovy.impl.ScriptAttributeAccessor;
import com.itworks.snamp.adapters.groovy.impl.ScriptNotificationAccessor;
import com.itworks.snamp.adapters.runtime.FeatureBinding;
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

    private static Collection<ScriptAttributeBinding> getAttributes(final AbstractAttributesModel<ScriptAttributeAccessor> model){
        final List<ScriptAttributeBinding> result = new LinkedList<>();
        model.forEachAttribute(new RecordReader<String, ScriptAttributeAccessor, ExceptionPlaceholder>() {
            @Override
            public void read(final String resourceName, final ScriptAttributeAccessor accessor) {
                result.add(new ScriptAttributeBinding(resourceName, accessor));
            }
        });
        return result;
    }

    private static Collection<ScriptNotificationBinding> getNotifications(final AbstractNotificationsModel<ScriptNotificationAccessor> model){
        final List<ScriptNotificationBinding> result = new LinkedList<>();
        model.forEachNotification(new RecordReader<String, ScriptNotificationAccessor, ExceptionPlaceholder>() {
            @Override
            public void read(final String resourceName, final ScriptNotificationAccessor accessor) {
                result.add(new ScriptNotificationBinding(accessor));
            }
        });
        return result;
    }

    public static <B extends FeatureBinding> Collection<? extends B> getBindings(final Class<B> bindingType,
                                                                       final AbstractAttributesModel<ScriptAttributeAccessor> attributes,
                                                                       final AbstractNotificationsModel<ScriptNotificationAccessor> notifs) {
        if (bindingType.isAssignableFrom(ScriptAttributeBinding.class))
            return (Collection<B>) getAttributes(attributes);
        else if (bindingType.isAssignableFrom(ScriptNotificationBinding.class))
            return (Collection<B>) getNotifications(notifs);
        else return Collections.emptyList();
    }
}