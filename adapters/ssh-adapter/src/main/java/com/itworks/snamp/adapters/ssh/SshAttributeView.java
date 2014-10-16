package com.itworks.snamp.adapters.ssh;

import com.itworks.snamp.Table;
import com.itworks.snamp.TypeLiterals;
import com.itworks.snamp.connectors.ManagedEntityTabularType;
import com.itworks.snamp.connectors.ManagedEntityType;
import com.itworks.snamp.connectors.ManagedEntityTypeBuilder;
import com.itworks.snamp.connectors.WellKnownTypeSystem;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.Typed;

import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static com.itworks.snamp.adapters.AbstractResourceAdapter.AttributeAccessor;

/**
 * Represents form of the attribute suitable for printing via text-based streams.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface SshAttributeView {
    /**
     * Represents common interface for all transformations that can be applied to
     * attribute value.
     * @param <O> Type of the transformation result.
     */
    static interface ValueTransformation<I, O> {
        O transform(final I arg,
                    final AttributeAccessor input) throws TimeoutException;
    }

    /**
     * Removes element from the array or row from the table by its index.
     */
    static final class DeleteRowTransformation implements ValueTransformation<Integer, Boolean>{

        private boolean deleteArrayElement(final Object[] array,
                                           final int index,
                                           final AttributeAccessor setter) throws TimeoutException{
            return setter.setValue(ArrayUtils.remove(array, index));
        }

        private boolean deleteTableRow(final Table<String> table,
                                       final int rowIndex,
                                       final AttributeAccessor setter) throws TimeoutException{
            //modifying the same instance of the table is normal,
            //because input table is a conversion result provided by connector
            table.removeRow(rowIndex);
            return setter.setValue(table);
        }

        @Override
        public Boolean transform(final Integer index, final AttributeAccessor input) throws TimeoutException{
            final Object value = input.getValue(input.getWellKnownType(), null);
            if(TypeLiterals.isInstance(value, TypeLiterals.OBJECT_ARRAY))
                return deleteArrayElement(TypeLiterals.cast(value, TypeLiterals.OBJECT_ARRAY), index, input);
            else if(TypeLiterals.isInstance(value, TypeLiterals.STRING_COLUMN_TABLE))
                return deleteTableRow(TypeLiterals.cast(value, TypeLiterals.STRING_COLUMN_TABLE), index, input);
            else return false;
        }
    }

    static final class Row{
        private final int index;
        private final Object element;

        public Row(final int index, final Object element){
            this.index = index;
            this.element = element;
        }
    }

    static class InsertRowTransformation implements ValueTransformation<Row, Boolean> {
        @Override
        public final Boolean transform(final Row arg, final AttributeAccessor input) throws TimeoutException {
            return UpdateRowTransformation.update(arg.index, arg.element, true, input);
        }
    }

    static final class UpdateRowTransformation implements ValueTransformation<Row, Boolean>{
        private static boolean updateArrayElement(Object[] array,
                                                  final int index,
                                                  final Object element,
                                                  final ManagedEntityTabularType type,
                                                  final boolean insert,
                                                  final AttributeAccessor output) throws TimeoutException {
            final ManagedEntityType elementType = type.getColumnType(ManagedEntityTypeBuilder.AbstractManagedEntityArrayType.VALUE_COLUMN_NAME);
            final Typed<?> elementJavaType = WellKnownTypeSystem.getWellKnownType(elementType);
            if(elementJavaType == null)
                return false;
            else if(insert)
                array = ArrayUtils.add(array, index, elementType.getProjection(elementJavaType).convertFrom(element));
            else array[index] = elementType.getProjection(elementJavaType).convertFrom(element);
            return output.setValue(array);
        }

        private static boolean insertTableRow(final Table<String> table,
                                              final int index,
                                              final Map<String, Object> row,
                                              final ManagedEntityTabularType type,
                                              final boolean insert,
                                              final AttributeAccessor output) throws TimeoutException{
            for(final String column: type.getColumns()) {
                final ManagedEntityType columnType = type.getColumnType(column);
                final Typed<?> columnJavaType = WellKnownTypeSystem.getWellKnownType(columnType);
                row.put(column, columnType.getProjection(columnJavaType).convertFrom(row.get(column)));
            }
            if(insert)
                table.insertRow(index, row);
            else table.setRow(index, row);
            return output.setValue(table);
        }

        private static boolean updateTableRow(final Table<String> table,
                                              final int index,
                                              final Object element,
                                              final ManagedEntityTabularType type,
                                              final boolean insert,
                                              final AttributeAccessor output) throws TimeoutException{
            return TypeLiterals.isInstance(element, TypeLiterals.STRING_MAP) && insertTableRow(table, index, TypeLiterals.cast(element, TypeLiterals.STRING_MAP), type, insert, output);
        }

        static boolean update(final int index,
                              final Object row,
                              final boolean insert,
                              final AttributeAccessor input) throws TimeoutException{
            final Object value = input.getValue(input.getWellKnownType(), null);
            if(TypeLiterals.isInstance(value, TypeLiterals.OBJECT_ARRAY))
                return updateArrayElement(TypeLiterals.cast(value, TypeLiterals.OBJECT_ARRAY), index, row, (ManagedEntityTabularType) input.getType(), insert, input);
            else if(TypeLiterals.isInstance(value, TypeLiterals.STRING_COLUMN_TABLE))
                return updateTableRow(TypeLiterals.cast(value, TypeLiterals.STRING_COLUMN_TABLE), index, row, (ManagedEntityTabularType) input.getType(), insert, input);
            else return false;
        }

        @Override
        public Boolean transform(final Row arg, final AttributeAccessor input) throws TimeoutException {
            return update(arg.index, arg.element, false, input);
        }
    }

    void printValue(final PrintWriter output) throws TimeoutException;

    <I, O> O applyTransformation(final Class<? extends ValueTransformation<I, O>> transformation, final I arg) throws ReflectiveOperationException, TimeoutException;

    void printOptions(final PrintWriter output);

    String getName();

    boolean canRead();
    boolean canWrite();

    boolean setValue(final Object value) throws TimeoutException;
}
