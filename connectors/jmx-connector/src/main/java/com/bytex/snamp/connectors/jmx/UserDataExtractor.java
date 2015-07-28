package com.bytex.snamp.connectors.jmx;

import com.bytex.snamp.jmx.CompositeDataBuilder;
import com.bytex.snamp.jmx.WellKnownType;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.monitor.MonitorNotification;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.relation.RelationNotification;
import javax.management.timer.TimerNotification;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class UserDataExtractor {

    private UserDataExtractor(){

    }

    private static CompositeData getUserData(final AttributeChangeNotification n) throws OpenDataException {
        final String TYPE_NAME = "AttributeChangeNotificationData";
        final String DESCRIPTION = "Advanced data associated with AttributeChangeNotification event";
        CompositeDataBuilder result = new CompositeDataBuilder(TYPE_NAME, DESCRIPTION)
                .put("attributeName", "The name of the attribute which has changed", n.getAttributeName())
                .put("attributeType", "The type of the attribute which has changed.", n.getAttributeType());
        final WellKnownType oldValueType = WellKnownType.fromValue(n.getOldValue());
        final WellKnownType newValueType = WellKnownType.fromValue(n.getNewValue());
        if(oldValueType != null && oldValueType.isOpenType())
            result = result.put("oldValue", "The old value of the attribute which has changed", (OpenType)oldValueType.getOpenType(), n.getOldValue());
        if(newValueType != null && newValueType.isOpenType())
            result = result.put("newValue", "The new value of the attribute which has changed", (OpenType)newValueType.getOpenType(), n.getNewValue());
        return result.build();
    }

    private static CompositeData getUserData(final MonitorNotification n) throws OpenDataException{
        final String TYPE_NAME = "MonitorNotificationData";
        final String DESCRIPTION = "Advanced data associated with MonitorNotification event";
        return new CompositeDataBuilder(TYPE_NAME, DESCRIPTION)
                .put("observedAttribute", "The observed attribute of this monitor notification.", n.getObservedAttribute())
                .put("observedObject", "The observed object of this monitor notification.", n.getObservedObject())
                .build();
    }

    private static Integer getUserData(final TimerNotification n) throws OpenDataException{
        return n.getNotificationID();
    }

    private static CompositeData getUserData(final RelationNotification n) throws OpenDataException{
        final String TYPE_NAME = "RelationNotificationData";
        final String DESCRIPTION = "Advanced data associated with RelationNotification event";
        return new CompositeDataBuilder(TYPE_NAME, DESCRIPTION)
                .put("roleName", "Name of updated role of updated relation", n.getRoleName())
                .put("relationType", "The relation type name of created/removed/updated relation.", n.getRelationTypeName())
                .put("relationID", "The relation identifier of created/removed/updated relation.", n.getRelationId())
                .put("objectName", "The ObjectName of the created/removed/updated relation", n.getObjectName())
                .build();
    }

    static Object getUserData(final Notification n) throws OpenDataException {
        //extract user data for predefined notification types
        if(n instanceof AttributeChangeNotification)
            return getUserData((AttributeChangeNotification) n);
        else if(n instanceof MonitorNotification)
            return getUserData((MonitorNotification) n);
        else if(n instanceof TimerNotification)
            return getUserData((TimerNotification) n);
        else if(n instanceof RelationNotification)
            return getUserData((RelationNotification)n);
        else return n.getUserData();
    }
}
