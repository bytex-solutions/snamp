package com.bytex.snamp.connectors.mda;

import com.bytex.snamp.connectors.notifications.CustomNotificationInfo;
import com.bytex.snamp.connectors.notifications.NotificationDescriptor;

import javax.management.openmbean.OpenType;

/**
 * Represents notification that can be delivered by remote Agent.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class MDANotificationInfo extends CustomNotificationInfo {
    private static final long serialVersionUID = 5469420074111348698L;
    private OpenType<?> attachmentType;

    /**
     * Constructs an <CODE>MBeanNotificationInfo</CODE> object.
     *
     * @param notifType   The name of the notification that can be produced by managed resource.
     * @param description A human readable description of the data.
     * @param descriptor  The descriptor for the notifications.  This may be null
     */
    public MDANotificationInfo(final String notifType, final String description, final NotificationDescriptor descriptor) {
        super(notifType, description, descriptor);
    }

    protected final OpenType<?> getAttachmentType(){
        return attachmentType;
    }

    final void init(final OpenType<?> attachmentType){
        this.attachmentType = attachmentType;
    }
}
