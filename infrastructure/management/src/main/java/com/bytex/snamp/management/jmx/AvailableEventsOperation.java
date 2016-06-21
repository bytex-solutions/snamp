package com.bytex.snamp.management.jmx;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.connectors.notifications.NotificationDescriptor;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.TabularDataBuilderRowFill;
import com.bytex.snamp.jmx.TabularTypeBuilder;
import com.bytex.snamp.jmx.WellKnownType;
import com.google.common.base.MoreObjects;

import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularType;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class AvailableEventsOperation extends AvailableFeaturesOperation<MBeanNotificationInfo> {
    static final String NAME = "getAvailableEvents";

    private static final String DESCRIPTION_COLUMN = "description";
    private static final String PARAMETERS_COLUMN = "parameters";
    private static final String ATTACHMENT_TYPE_COLUMN = "attachmentType";
    private static final String CATEGORY_COLUMN = "category";
    private static final String SEVERITY_COLUMN = "severity";

    private static final TabularType RETURN_TYPE = Utils.interfaceStaticInitialize(() -> new TabularTypeBuilder()
            .setTypeName("AvailableNotifications", true)
            .setDescription("A set of available notifications", true)
            .addColumn(DESCRIPTION_COLUMN, "Description of the event", SimpleType.STRING, false)
            .addColumn(PARAMETERS_COLUMN, "Configuration parameters", PARAMETERS_TYPE, false)
            .addColumn(ATTACHMENT_TYPE_COLUMN, "Type of the notification attachment", SimpleType.STRING, false)
            .addColumn(CATEGORY_COLUMN, "Category of the event", SimpleType.STRING, true)
            .addColumn(SEVERITY_COLUMN, "Severity of the notification", SimpleType.STRING, false)
            .build());

    AvailableEventsOperation() {
        super(NAME, RETURN_TYPE);
    }

    private static void fillRow(final MBeanNotificationInfo notificationInfo,
                                final TabularDataBuilderRowFill.RowBuilder row) throws OpenDataException {
        final String description = notificationInfo.getDescription();
        final WellKnownType attachmentType = WellKnownType.getType(NotificationDescriptor.getUserDataType(notificationInfo));
        row
                .cell(CATEGORY_COLUMN, ArrayUtils.getFirst(notificationInfo.getNotifTypes(), ""))
                .cell(PARAMETERS_COLUMN, toTabularData(notificationInfo))
                .cell(DESCRIPTION_COLUMN, MoreObjects.firstNonNull(description, ""))
                .cell(ATTACHMENT_TYPE_COLUMN, attachmentType == null ? "" : attachmentType.getDisplayName())
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
