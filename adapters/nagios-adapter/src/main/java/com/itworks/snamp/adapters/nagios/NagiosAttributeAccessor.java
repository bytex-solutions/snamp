package com.itworks.snamp.adapters.nagios;

import com.itworks.snamp.adapters.AttributeAccessor;

import javax.management.MBeanAttributeInfo;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class NagiosAttributeAccessor extends AttributeAccessor {

    NagiosAttributeAccessor(final MBeanAttributeInfo metadata) {
        super(metadata);
    }

    NagiosPluginOutput toNagiosOutput(){
        final NagiosPluginOutput result = new NagiosPluginOutput();
        result.setMetadata(getMetadata());
        return result;
    }
}
