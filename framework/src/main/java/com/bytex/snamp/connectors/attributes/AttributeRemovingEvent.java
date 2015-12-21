package com.bytex.snamp.connectors.attributes;

import com.bytex.snamp.connectors.FeatureRemovingEvent;

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
        assert source instanceof AttributeSupport: source;
        return (AttributeSupport) source;
    }
}
