package com.bytex.snamp.adapters.decanter;

import com.bytex.snamp.adapters.modeling.AttributeAccessor;
import com.google.common.collect.ImmutableMap;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;

import static com.bytex.snamp.adapters.decanter.DataConverter.convertUserData;

/**
 * Represents bridge between SNAMP attribute and Decanter collector.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class DecanterAttributeAccessor extends AttributeAccessor {
    DecanterAttributeAccessor(final MBeanAttributeInfo metadata) {
        super(metadata);
    }

    void collectData(final EventAdmin admin, final String resourceTopic) throws JMException{
        final ImmutableMap<String, Object> data = ImmutableMap.of(
            "value", convertUserData(getValue()),
            "javaType", getMetadata().getType(),
            "snampType", getType().getDisplayName()
        );
        admin.postEvent(new Event(resourceTopic.concat(getName()), data));
    }
}
