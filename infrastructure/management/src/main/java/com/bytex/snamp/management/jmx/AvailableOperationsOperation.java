package com.bytex.snamp.management.jmx;

import com.google.common.base.MoreObjects;
import com.bytex.snamp.connectors.operations.OperationDescriptor;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.TabularDataBuilderRowFill;
import com.bytex.snamp.jmx.TabularTypeBuilder;
import com.bytex.snamp.jmx.WellKnownType;

import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
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
final class AvailableOperationsOperation extends AvailableFeaturesOperation<MBeanOperationInfo> {
    static final String NAME = "getAvailableOperations";

    private static final String NAME_COLUMN = "name";
    private static final String DESCRIPTION_COLUMN = "description";
    private static final String PARAMETERS_COLUMN = "parameters";
    private static final String RETURN_TYPE_COLUMN = "returnType";
    private static final String IMPACT_COLUMN = "impact";

    private static final TabularType RETURN_TYPE = Utils.interfaceStaticInitialize(new Callable<TabularType>() {
        @Override
        public TabularType call() throws OpenDataException {
            return new TabularTypeBuilder()
                    .setTypeName("AvailableNotifications", true)
                    .setDescription("A set of available notifications", true)
                    .addColumn(NAME_COLUMN, "User-defined name of the event", SimpleType.STRING, true)
                    .addColumn(DESCRIPTION_COLUMN, "Description of the event", SimpleType.STRING, false)
                    .addColumn(PARAMETERS_COLUMN, "Configuration parameters", SimpleType.STRING, false)
                    .addColumn(RETURN_TYPE_COLUMN, "Type of the notification attachment", SimpleType.STRING, false)
                    .addColumn(IMPACT_COLUMN, "Severity of the notification", SimpleType.INTEGER, false)
                    .build();
        }
    });

    AvailableOperationsOperation() {
        super(NAME, RETURN_TYPE);
    }

    private static void fillRow(final MBeanOperationInfo operationInfo,
                                final TabularDataBuilderRowFill.RowBuilder row) throws OpenDataException {
        final String description = operationInfo.getDescription();
        final WellKnownType returnType = OperationDescriptor.getReturnType(operationInfo);
        row
                .cell(NAME_COLUMN, operationInfo.getName())
                .cell(PARAMETERS_COLUMN, toTabularData(operationInfo))
                .cell(DESCRIPTION_COLUMN, MoreObjects.firstNonNull(description, ""))
                .cell(RETURN_TYPE_COLUMN, returnType == null ? operationInfo.getReturnType() : returnType.getDisplayName())
                .cell(IMPACT_COLUMN, operationInfo.getImpact())
                .flush();
    }

    @Override
    protected TabularData invoke(final MBeanInfo metadata) throws OpenDataException{
        final TabularDataBuilderRowFill rows = new TabularDataBuilderRowFill(RETURN_TYPE);
        for(final MBeanOperationInfo operationInfo: metadata.getOperations())
            fillRow(operationInfo, rows.newRow());
        return rows.get();
    }
}
