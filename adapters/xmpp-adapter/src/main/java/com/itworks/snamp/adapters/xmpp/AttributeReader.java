package com.itworks.snamp.adapters.xmpp;

import javax.management.JMException;
import java.util.Set;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface AttributeReader {
    String getAttribute(final String resourceName,
                        final String attributeID,
                        final AttributeValueFormat format,
                        final XMPPAttributePayload payload) throws JMException;

    Set<String> getHostedResources();

    Set<String> getResourceAttributes(final String resourceName);

    String printOptions(final String resourceName,
                      final String attributeID,
                      final boolean withNames,
                      final boolean details);
}
