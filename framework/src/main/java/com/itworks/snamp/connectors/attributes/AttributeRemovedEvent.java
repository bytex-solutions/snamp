package com.itworks.snamp.connectors.attributes;

import com.itworks.snamp.connectors.FeatureRemovedEvent;
import com.itworks.snamp.internal.Utils;

import javax.management.MBeanAttributeInfo;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class AttributeRemovedEvent extends FeatureRemovedEvent<MBeanAttributeInfo> {
    private static final long serialVersionUID = -8111810353719221794L;

    public AttributeRemovedEvent(final AttributeSupport sender,
                                 final String resourceName,
                                 final MBeanAttributeInfo removedFeature) {
        super(sender, resourceName, removedFeature);
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
