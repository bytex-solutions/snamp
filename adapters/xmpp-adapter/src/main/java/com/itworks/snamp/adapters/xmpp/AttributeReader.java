package com.itworks.snamp.adapters.xmpp;

import javax.management.JMException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface AttributeReader {
    String getAttribute(final String resourceName,
                        final String attributeID,
                        final AttributeValueFormat format) throws JMException;
}
