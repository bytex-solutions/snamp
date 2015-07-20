package com.itworks.snamp.adapters.snmp.binding;

import com.google.common.base.Function;
import com.google.common.collect.Multimap;
import com.itworks.snamp.adapters.binding.FeatureBindingInfo;
import com.itworks.snamp.adapters.snmp.SnmpAttributeAccessorImpl;
import com.itworks.snamp.adapters.snmp.SnmpNotificationAcessor;
import com.itworks.snamp.adapters.snmp.SnmpType;
import com.itworks.snamp.jmx.WellKnownType;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SnmpAdapterRuntimeInfo {

    private static Collection<SnmpAttributeBindingInfo> getAttributes(final Multimap<String, SnmpAttributeAccessorImpl> accessors,
                                                                 final Function<WellKnownType, SnmpType> typeMapper) {
        final List<SnmpAttributeBindingInfo> result = new LinkedList<>();
        for (final String declaredResource : accessors.keySet())
            for (final SnmpAttributeAccessorImpl accessor : accessors.get(declaredResource))
                result.add(new SnmpAttributeBindingInfo(declaredResource, accessor, typeMapper));
        return result;
    }

    private static Collection<SnmpNotificationBindingInfo> getNotifications(final Multimap<String, SnmpNotificationAcessor> accessors,
                                                                  final Function<WellKnownType, SnmpType> typeMapper) {
        final List<SnmpNotificationBindingInfo> result = new LinkedList<>();
        for (final String declaredResource : accessors.keySet())
            for (final SnmpNotificationAcessor accessor : accessors.get(declaredResource))
                result.add(new SnmpNotificationBindingInfo(declaredResource, accessor, typeMapper));
        return result;
    }

    public static <B extends FeatureBindingInfo> Collection<? extends B> getBindings(final Class<B> bindingType,
                                                                                 final Multimap<String, SnmpAttributeAccessorImpl> attributes,
                                                                                 final Multimap<String, SnmpNotificationAcessor> notifications,
                                                                                 final Function<WellKnownType, SnmpType> typeMapper) {
        if (bindingType.isAssignableFrom(SnmpAttributeBindingInfo.class))
            return (Collection<B>) getAttributes(attributes, typeMapper);
        else if (bindingType.isAssignableFrom(SnmpNotificationBindingInfo.class))
            return (Collection<B>) getNotifications(notifications, typeMapper);
        else return Collections.emptyList();
    }
}
