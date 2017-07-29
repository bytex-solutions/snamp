package com.bytex.snamp.gateway.xmpp;

import javax.management.JMException;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
interface AttributeWriter {
    void setAttribute(final String resourceName,
                      final String attributeID,
                      final String value) throws JMException;
}
