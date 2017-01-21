package com.bytex.snamp.connector.attributes;

import com.bytex.snamp.connector.FeatureModifiedEvent;

import javax.management.MBeanAttributeInfo;

/**
 * Indicates that the attribute provided by managed resource was modified.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class AttributeModifiedEvent extends FeatureModifiedEvent<MBeanAttributeInfo> {
    private static final long serialVersionUID = -1581308390888740320L;

    protected AttributeModifiedEvent(final AttributeSupport sender,
                                     final String resourceName,
                                     final MBeanAttributeInfo feature,
                                     final ModificationType type) {
        super(sender, resourceName, feature, type);
    }

    /**
     * The object on which the Event initially occurred.
     *
     * @return The object on which the Event initially occurred.
     */
    @Override
    public final AttributeSupport getSource() {
        return (AttributeSupport) super.getSource();
    }

    public static AttributeModifiedEvent attributedAdded(final AttributeSupport sender,
                                                         final String resourceName,
                                                         final MBeanAttributeInfo feature){
        return new AttributeModifiedEvent(sender, resourceName, feature, ModificationType.ADDED);
    }

    public static AttributeModifiedEvent attributedRemoving(final AttributeSupport sender,
                                                         final String resourceName,
                                                         final MBeanAttributeInfo feature){
        return new AttributeModifiedEvent(sender, resourceName, feature, ModificationType.REMOVING);
    }
}
