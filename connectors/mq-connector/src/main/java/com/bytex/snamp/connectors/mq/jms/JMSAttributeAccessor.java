package com.bytex.snamp.connectors.mq.jms;

import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.attributes.AttributeSpecifier;
import com.bytex.snamp.connectors.mda.MDAAttributeInfo;

import javax.management.openmbean.OpenType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JMSAttributeAccessor extends MDAAttributeInfo {
    private static final long serialVersionUID = -1593756174265070204L;

    protected JMSAttributeAccessor(final String name,
                                   final OpenType<?> type,
                                   final AttributeSpecifier specifier,
                                   final AttributeDescriptor descriptor) {
        super(name, type, specifier, descriptor);
    }
}
