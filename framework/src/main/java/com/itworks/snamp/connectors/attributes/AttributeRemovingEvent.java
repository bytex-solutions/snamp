package com.itworks.snamp.connectors.attributes;

import com.itworks.snamp.connectors.FeatureRemovingEvent;
import com.itworks.snamp.internal.Utils;

import javax.management.MBeanAttributeInfo;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class AttributeRemovingEvent extends FeatureRemovingEvent<MBeanAttributeInfo> {
    private static final long serialVersionUID = -1489881728507021721L;

    public AttributeRemovingEvent(final AttributeSupport sender, final String resourceName, final MBeanAttributeInfo feature) {
        super(sender, resourceName, feature);
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
