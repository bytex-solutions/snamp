package com.bytex.snamp.connectors.attributes;

import com.bytex.snamp.connectors.FeatureAddedEvent;
import com.bytex.snamp.internal.Utils;

import javax.management.MBeanAttributeInfo;

/**
 * Represents an event raised by managed resource connector
 * when it extends with a new attribute.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class AttributeAddedEvent extends FeatureAddedEvent<MBeanAttributeInfo> {
    private static final long serialVersionUID = -8216135971221782509L;

    public AttributeAddedEvent(final AttributeSupport sender,
                               final String resourceName,
                               final MBeanAttributeInfo addedAttribute) {
        super(sender, resourceName, addedAttribute);
    }

    /**
     * The object on which the Event initially occurred.
     *
     * @return The object on which the Event initially occurred.
     */
    @Override
    public final AttributeSupport getSource() {
        return Utils.safeCast(source, AttributeSupport.class);
    }
}
