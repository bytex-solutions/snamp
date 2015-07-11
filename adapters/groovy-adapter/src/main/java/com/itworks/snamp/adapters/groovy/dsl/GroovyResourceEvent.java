package com.itworks.snamp.adapters.groovy.dsl;

import com.itworks.snamp.internal.annotations.SpecialUse;

import javax.management.MBeanNotificationInfo;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class GroovyResourceEvent extends GroovyFeatureMetadata<MBeanNotificationInfo> {
    GroovyResourceEvent(final MBeanNotificationInfo metadata) {
        super(metadata);
    }
}
