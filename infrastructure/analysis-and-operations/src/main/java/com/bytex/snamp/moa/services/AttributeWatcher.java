package com.bytex.snamp.moa.services;

import com.bytex.snamp.gateway.modeling.AttributeAccessor;

import javax.management.*;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class AttributeWatcher extends AttributeAccessor {

    AttributeWatcher(final MBeanAttributeInfo metadata) {
        super(metadata);
    }

    void checkAttribute(final String resourceName, final UpdatableGroupWatcher watcher) {
        final Attribute attribute;
        try {
            attribute = getRawValue();
        } catch (final AttributeNotFoundException e) {
            watcher.removeAttribute(getName());
            return;
        } catch (final ReflectionException | MBeanException e) {
            watcher.updateStatus(resourceName, e);
            return;
        }
        watcher.updateStatus(resourceName, attribute);
    }
}
