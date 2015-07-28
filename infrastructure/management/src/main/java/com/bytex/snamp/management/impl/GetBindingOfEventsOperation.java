package com.bytex.snamp.management.impl;

import com.bytex.snamp.connectors.notifications.NotificationDescriptor;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.TabularDataBuilderRowFill;
import com.bytex.snamp.jmx.TabularTypeBuilder;

import javax.management.MBeanNotificationInfo;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularType;
import java.util.Objects;
import java.util.concurrent.Callable;
import static com.bytex.snamp.adapters.ResourceAdapter.FeatureBindingInfo;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class GetBindingOfEventsOperation extends AbstractBindingInfoOperation<MBeanNotificationInfo> {
    private static final String NAME = "getBindingOfNotifications";

    private static final String CATEGORY_COLUMN = "category";
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
                    .addColumn(DETAILS_COLUMN, "Binding details", DETAILS_TYPE, false)
                    .build();
        }
    });

    GetBindingOfEventsOperation(){
        super(NAME, RETURN_TYPE, MBeanNotificationInfo.class);
    }

    @Override
    protected void fillRow(final TabularDataBuilderRowFill.RowBuilder row, final FeatureBindingInfo<MBeanNotificationInfo> bindingInfo) throws OpenDataException {
        final String attachmentType = Objects.toString(bindingInfo.getProperty(FeatureBindingInfo.MAPPED_TYPE), "");
        final String category = NotificationDescriptor.getNotificationCategory(bindingInfo.getMetadata());

        row
                .cell(LIST_ID_COLUMN, bindingInfo.getMetadata().getNotifTypes()[0])
                .cell(CATEGORY_COLUMN, category)
                .cell(ATTACHMENT_TYPE_COLUMN, attachmentType);
    }
}
