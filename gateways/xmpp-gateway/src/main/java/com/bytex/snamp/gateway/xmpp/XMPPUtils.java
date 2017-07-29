package com.bytex.snamp.gateway.xmpp;

import org.jivesoftware.smackx.jiveproperties.packet.JivePropertiesExtension;

import javax.management.Descriptor;
import java.util.Date;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
final class XMPPUtils {
    private XMPPUtils(){

    }

    static void copyDescriptorFields(final Descriptor from,
                                     final JivePropertiesExtension to){
        for(final String fieldName: from.getFieldNames()){
            final Object fieldValue = from.getFieldValue(fieldName);
            if(fieldValue == null) continue;
            else if(fieldValue instanceof Number || fieldValue instanceof String || fieldValue instanceof Character)
                to.setProperty(fieldName, fieldValue);
            else if(fieldValue instanceof Date)
                to.setProperty(fieldName, ((Date)fieldValue).getTime());
            else if(fieldValue instanceof Enum<?>)
                to.setProperty(fieldName, ((Enum<?>)fieldValue).ordinal());
            else to.setProperty(fieldName, fieldValue);
        }
    }
}
