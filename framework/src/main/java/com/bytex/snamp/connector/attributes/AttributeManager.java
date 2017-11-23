package com.bytex.snamp.connector.attributes;

import javax.management.JMException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Provides API for adding and removing attributes.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public interface AttributeManager {
    /**
     * Registers a new attribute in the managed resource connector.
     * @param attributeName The name of the attribute in the managed resource.
     * @param descriptor Descriptor of created attribute.
     * @since 2.0
     * @throws JMException Unable to instantiate attribute.
     */
    void addAttribute(final String attributeName, final AttributeDescriptor descriptor) throws JMException;

    /**
     * Removes attribute from the managed resource.
     * @param attributeName Name of the attribute to remove.
     * @return {@literal true}, if attribute is removed successfully; otherwise, {@literal false}.
     * @since 2.0
     */
    boolean removeAttribute(final String attributeName);

    /**
     * Removes all attributes except specified in the collection.
     * @param attributes A set of attributes which should not be deleted.
     * @since 2.0
     */
    void retainAttributes(final Set<String> attributes);

    /**
     * Discover attributes.
     *
     * @return A map of discovered attributed that can be added using method {@link #addAttribute(String, AttributeDescriptor)}.
     * @since 2.0
     */
    default Map<String, AttributeDescriptor> discoverAttributes(){
        return Collections.emptyMap();
    }
}
