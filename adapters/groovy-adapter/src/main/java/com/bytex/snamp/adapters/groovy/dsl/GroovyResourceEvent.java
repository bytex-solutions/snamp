package com.bytex.snamp.adapters.groovy.dsl;

import javax.management.MBeanNotificationInfo;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class GroovyResourceEvent extends GroovyFeatureMetadata<MBeanNotificationInfo> {
    GroovyResourceEvent(final MBeanNotificationInfo metadata) {
        super(metadata);
    }
}
