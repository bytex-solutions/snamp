package com.itworks.snamp.management.impl;

import com.google.common.base.MoreObjects;
import com.itworks.snamp.connectors.notifications.NotificationDescriptor;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.TabularDataBuilderRowFill;
import com.itworks.snamp.jmx.TabularTypeBuilder;
import com.itworks.snamp.jmx.WellKnownType;

import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularType;
import java.util.concurrent.Callable;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class AvailableEventsOperation extends AvailableFeaturesOperation<MBeanNotificationInfo> {
    static final String NAME = "getAvailableEvents";

    private static final String LIST_ID_COLUMN = "listID";
    private static final String DESCRIPTION_COLUMN = "description";
    private static final String PARAMETERS_COLUMN = "parameters";
    private static final String ATTACHMENT_TYPE_COLUMN = "attachmentType";
    private static final String CATEGORY_COLUMN = "category";
    private static final String SEVERITY_COLUMN = "severity";

    private static final TabularType RETURN_TYPE = Utils.interfaceStaticInitialize(new Callable<TabularType>() {
        @Override
        public TabularType call() throws OpenDataException {
            return new TabularTypeBuilder()
                    .setTypeName("AvailableNotifications", true)
                    .setDescription("A set of available notifications", true)
                    .addColumn(LIST_ID_COLUMN, "User-defined name of the event", SimpleType.STRING, true)
                    .addColumn(DESCRIPTION_COLUMN, "Description of the event", SimpleType.STRING, false)
                    .addColumn(PARAMETERS_COLUMN, "Configuration parameters", PARAMETERS_TYPE, false)
                    .addColumn(ATTACHMENT_TYPE_COLUMN, "Type of the notification attachment", SimpleType.STRING, false)
                    .addColumn(CATEGORY_COLUMN, "Category of the event", SimpleType.STRING, false)
                    .addColumn(SEVERITY_COLUMN, "Severity of the notification", SimpleType.STRING, false)
                    .build();
        }
    });

    AvailableEventsOperation() {
        super(NAME, RETURN_TYPE);
    }

    private static void fillRow(final MBeanNotificationInfo notificationInfo,
                                final TabularDataBuilderRowFill.RowBuilder row) throws OpenDataException {
        final String description = notificationInfo.getDescription();
        final String category = NotificationDescriptor.getNotificationCategory(notificationInfo);
        final WellKnownType attachmentType = WellKnownType.getType(NotificationDescriptor.getUserDataType(notificationInfo));
        row
                .cell(LIST_ID_COLUMN, notificationInfo.getNotifTypes()[0])
                .cell(PARAMETERS_COLUMN, toTabularData(notificationInfo))
                .cell(DESCRIPTION_COLUMN, MoreObjects.firstNonNull(description, ""))
                .cell(ATTACHMENT_TYPE_COLUMN, attachmentType == null ? "" : attachmentType.getDisplayName())
                .cell(CATEGORY_COLUMN, MoreObjects.firstNonNull(category, ""))
                .cell(SEVERITY_COLUMN, NotificationDescriptor.getSeverity(notificationInfo).toString())
                .flush();
    }

    @Override
    protected TabularData invoke(final MBeanInfo metadata) throws OpenDataException{
        final TabularDataBuilderRowFill rows = new TabularDataBuilderRowFill(RETURN_TYPE);
        for(final MBeanNotificationInfo notificationInfo: metadata.getNotifications())
            fillRow(notificationInfo, rows.newRow());
        return rows.get();
    }
}
