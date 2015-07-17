package com.itworks.snamp.adapters.snmp.runtime;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.itworks.snamp.adapters.runtime.FeatureBinding;
import com.itworks.snamp.adapters.snmp.SnmpAttributeAccessorImpl;
import com.itworks.snamp.adapters.snmp.SnmpNotificationAcessor;
import com.itworks.snamp.adapters.snmp.SnmpType;
import com.itworks.snamp.jmx.WellKnownType;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SnmpAdapterRuntimeInfo {

    private static Collection<SnmpAttributeBinding> getAttributes(final Multimap<String, SnmpAttributeAccessorImpl> accessors,
                                                                 final Function<WellKnownType, SnmpType> typeMapper) {
        final List<SnmpAttributeBinding> result = Lists.newArrayListWithExpectedSize(accessors.size());
        for (final String declaredResource : accessors.keySet())
            for (final SnmpAttributeAccessorImpl accessor : accessors.get(declaredResource))
                result.add(new SnmpAttributeBinding(declaredResource, accessor, typeMapper));
        return result;
    }

    private static Collection<SnmpNotificationBinding> getNotifications(final Multimap<String, SnmpNotificationAcessor> accessors,
                                                                  final Function<WellKnownType, SnmpType> typeMapper) {
        final List<SnmpNotificationBinding> result = Lists.newArrayListWithExpectedSize(accessors.size());
        for (final String declaredResource : accessors.keySet())
            for (final SnmpNotificationAcessor accessor : accessors.get(declaredResource))
                result.add(new SnmpNotificationBinding(declaredResource, accessor, typeMapper));
        return result;
    }

    public static <B extends FeatureBinding> Collection<? extends B> getBindings(final Class<B> bindingType,
                                                                                 final Multimap<String, SnmpAttributeAccessorImpl> attributes,
                                                                                 final Multimap<String, SnmpNotificationAcessor> notifications,
                                                                                 final Function<WellKnownType, SnmpType> typeMapper) {
        if (bindingType.isAssignableFrom(SnmpAttributeBinding.class))
            return (Collection<B>) getAttributes(attributes, typeMapper);
        else if (bindingType.isAssignableFrom(SnmpNotificationBinding.class))
            return (Collection<B>) getNotifications(notifications, typeMapper);
        else return Collections.emptyList();
    }
}
