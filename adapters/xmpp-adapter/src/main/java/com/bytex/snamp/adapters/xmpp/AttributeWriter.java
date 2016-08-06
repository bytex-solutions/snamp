package com.bytex.snamp.adapters.xmpp;

import javax.management.JMException;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
interface AttributeWriter {
    void setAttribute(final String resourceName,
                      final String attributeID,
                      final String value) throws JMException;
}
