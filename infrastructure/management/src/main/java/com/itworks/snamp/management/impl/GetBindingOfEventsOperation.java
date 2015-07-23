package com.itworks.snamp.management.impl;

import com.itworks.snamp.adapters.ResourceAdapter;
import com.itworks.snamp.connectors.notifications.NotificationDescriptor;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.TabularDataBuilderRowFill;
import com.itworks.snamp.jmx.TabularTypeBuilder;

import javax.management.MBeanNotificationInfo;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularType;
import java.util.Objects;
import java.util.concurrent.Callable;
import static com.itworks.snamp.adapters.ResourceAdapter.FeatureBindingInfo;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class GetBindingOfEventsOperation extends AbstractBindingInfoOperation<MBeanNotificationInfo> {
    private static final String NAME = "getBindingOfNotifications";

    private static final String CATEGORY_COLUMN = "category";
    private static final String SEVERITY_COLUMN = "severity";
    private static final String LIST_ID_COLUMN = "subscriptionListID";
    private static final String ATTACHMENT_TYPE_COLUMN = "mappedAttachmentType";

    private static final TabularType RETURN_TYPE = Utils.interfaceStaticInitialize(new Callable<TabularType>() {
        @Override
        public TabularType call() throws OpenDataException {
            return new TabularTypeBuilder()
                    .setTypeName("BindingOfEvents", true)
                    .setDescription("A set of exposed events", true)
                    .addColumn(RESOURCE_NAME_COLUMN, "The name of the connected resource", SimpleType.STRING, true)
                    .addColumn(LIST_ID_COLUMN, "The ID of the subscription list specified by administrator", SimpleType.STRING, true)
                    .addColumn(CATEGORY_COLUMN, "The category of the event as it is provided by the connected resource", SimpleType.STRING, false)
                    .addColumn(ATTACHMENT_TYPE_COLUMN, "The mapped type of the notification attachments", SimpleType.STRING, false)
                    .addColumn(DETAILS_COLUMN, "Binding details", SimpleType.STRING, false)
                    .build();
        }
    });

    GetBindingOfEventsOperation(){
        super(NAME, RETURN_TYPE, MBeanNotificationInfo.class);
    }

    @Override
    protected void fillRow(final TabularDataBuilderRowFill.RowBuilder row, final FeatureBindingInfo<MBeanNotificationInfo> bindingInfo) throws OpenDataException {
        final String severity = NotificationDescriptor.getSeverity(bindingInfo.getMetadata()).toString();
        final String attachmentType = Objects.toString(bindingInfo.getProperty(FeatureBindingInfo.MAPPED_TYPE), "");
        final String category = NotificationDescriptor.getNotificationCategory(bindingInfo.getMetadata());

        row
                .cell(LIST_ID_COLUMN, bindingInfo.getMetadata().getNotifTypes()[0])
                .cell(SEVERITY_COLUMN, severity)
                .cell(CATEGORY_COLUMN, category)
                .cell(ATTACHMENT_TYPE_COLUMN, attachmentType);
    }
}