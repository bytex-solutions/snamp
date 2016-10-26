package com.bytex.snamp.connector.mda;

import com.bytex.snamp.connector.notifications.CustomNotificationInfo;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;

import javax.management.openmbean.OpenType;

/**
 * Represents notification that can be delivered by remote Agent.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class MDANotificationInfo extends CustomNotificationInfo {
    private static final long serialVersionUID = 5469420074111348698L;
    private OpenType<?> attachmentType;

    /**
     * Constructs an <CODE>MBeanNotificationInfo</CODE> object.
     *
     * @param notifType   The name of the notification that can be produced by managed resource.
     * @param descriptor  The descriptor for the notifications.  This may be null
     */
    public MDANotificationInfo(final String notifType, final NotificationDescriptor descriptor) {
        super(notifType, descriptor.getName(notifType), descriptor);
    }

    /**
     * Gets type of the notification attachment.
     * @return Attachment type. May be {@literal null}.
     */
    public final OpenType<?> getAttachmentType(){
        return attachmentType;
    }

    final void init(final OpenType<?> attachmentType){
        this.attachmentType = attachmentType;
    }
}
