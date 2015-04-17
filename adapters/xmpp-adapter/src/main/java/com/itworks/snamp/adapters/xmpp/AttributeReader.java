package com.itworks.snamp.adapters.xmpp;

import org.jivesoftware.smack.packet.ExtensionElement;

import javax.management.JMException;
import java.util.Collection;
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
                        final Collection<ExtensionElement> extras) throws JMException;

    Set<String> getHostedResources();

    Set<String> getResourceAttributes(final String resourceName);

    String printOptions(final String resourceName,
                      final String attributeID,
                      final boolean withNames,
                      final boolean details);
}
