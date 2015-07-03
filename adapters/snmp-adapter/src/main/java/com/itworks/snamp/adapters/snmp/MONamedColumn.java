package com.itworks.snamp.adapters.snmp;

import com.google.common.base.Function;
import com.itworks.snamp.jmx.WellKnownType;
import org.snmp4j.agent.MOAccess;
import org.snmp4j.agent.mo.MOMutableColumn;
import org.snmp4j.smi.Variable;

import javax.management.DescriptorRead;
import javax.management.InvalidAttributeValueException;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularType;

/**
 * Represents named column value.
 */
class MONamedColumn extends MOMutableColumn<Variable> {
    static final String ARRAY_VALUE_COLUMN = "Value";

    /**
     * Represents the name of the column.
     */
    final String name;

    /**
     * Determines whether this column is indexed.
     */
    final boolean isIndexed;

    private final SnmpType columnType;

    private final WellKnownType nativeType;

    private MONamedColumn(final int columnID,
                            final String columnName,
                            final SnmpType columnType,
                            final WellKnownType nativeType,
                            final MOAccess access,
                            final boolean isIndexed){
        super(columnID, columnType.getSyntax(), access);
        this.name = columnName;
        this.isIndexed = isIndexed;
        this.columnType = columnType;
        this.nativeType = nativeType;
    }

    protected MONamedColumn(final int columnID,
                            final String columnName,
                            final WellKnownType columnType,
                            final Function<WellKnownType, SnmpType> mapper,
                            final MOAccess access,
                            final boolean isIndexed){
        this(columnID, columnName, mapper.apply(columnType), columnType, access, isIndexed);
    }

    MONamedColumn(final int columnID,
                  final ArrayType<?> arrayType,
                  final Function<WellKnownType, SnmpType> typeMapper,
                  final MOAccess access) {
        this(columnID, ARRAY_VALUE_COLUMN, WellKnownType.getArrayElementType(arrayType),
                typeMapper,
                access,
                false);
    }

    MONamedColumn(final int columnID,
                  final CompositeType type,
                  final String itemName,
                  final Function<WellKnownType, SnmpType> typeMapper,
                  final MOAccess access){
        this(columnID, itemName, WellKnownType.getItemType(type, itemName), typeMapper, access, false);
    }

    MONamedColumn(final int columnID,
                  final TabularType type,
                  final String columnName,
                  final Function<WellKnownType, SnmpType> typeMapper,
                  final MOAccess access){
        this(columnID,
                columnName,
                WellKnownType.getColumnType(type, columnName),
                typeMapper,
                access,
                type.getIndexNames().contains(columnName));
    }

    /**
     * Determines whether this column is synthetic and doesn't contain any payload.
     * @return {@literal true}, if this column is synthetic and doesn't contain any payload; otherwise, {@literal false}.
     */
    boolean isSynthetic(){
        return false;
    }

    Object parseCellValue(final Variable value, final DescriptorRead conversionOptions) throws InvalidAttributeValueException {
        return columnType.convert(value, nativeType, conversionOptions);
    }

    Variable createCellValue(final Object cell, final DescriptorRead conversionOptions) {
        return columnType.convert(cell, conversionOptions);
    }
}
