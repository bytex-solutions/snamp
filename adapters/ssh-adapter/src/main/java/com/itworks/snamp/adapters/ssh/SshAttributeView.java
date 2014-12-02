package com.itworks.snamp.adapters.ssh;

import com.google.common.reflect.TypeToken;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.mapping.Table;
import com.itworks.snamp.mapping.TypeLiterals;
import com.itworks.snamp.connectors.ManagedEntityTabularType;
import com.itworks.snamp.connectors.ManagedEntityType;
import com.itworks.snamp.connectors.ManagedEntityTypeBuilder;
import com.itworks.snamp.connectors.WellKnownTypeSystem;
import com.itworks.snamp.connectors.attributes.AttributeSupportException;

import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static com.itworks.snamp.adapters.AbstractResourceAdapter.AttributeAccessor;

/**
 * Represents form of the attribute suitable for printing via text-based streams.
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
                    final AttributeAccessor input) throws TimeoutException, AttributeSupportException;
    }

    /**
     * Removes element from the array or row from the table by its index.
     */
    static final class DeleteRowTransformation implements ValueTransformation<Integer, Void>{

        private void deleteArrayElement(final Object[] array,
                                           final int index,
                                           final AttributeAccessor setter) throws TimeoutException, AttributeSupportException {
            setter.setValue(ArrayUtils.remove(array, index));
        }

        private void deleteTableRow(final Table<String> table,
                                       final int rowIndex,
                                       final AttributeAccessor setter) throws TimeoutException, AttributeSupportException{
            //modifying the same instance of the table is normal,
            //because input table is a conversion result provided by connector
            table.removeRow(rowIndex);
            setter.setValue(table);
        }

        @Override
        public Void transform(final Integer index, final AttributeAccessor input) throws TimeoutException, AttributeSupportException{
            final Object value = input.getValue(input.getWellKnownType(), null);
            if(TypeLiterals.isInstance(value, TypeLiterals.OBJECT_ARRAY))
                deleteArrayElement(TypeLiterals.cast(value, TypeLiterals.OBJECT_ARRAY), index, input);
            else if(TypeLiterals.isInstance(value, TypeLiterals.STRING_COLUMN_TABLE))
                deleteTableRow(TypeLiterals.cast(value, TypeLiterals.STRING_COLUMN_TABLE), index, input);
            return null;
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
        public final Boolean transform(final Row arg, final AttributeAccessor input) throws TimeoutException, AttributeSupportException {
            return UpdateRowTransformation.update(arg.index, arg.element, true, input);
        }
    }

    static final class UpdateRowTransformation implements ValueTransformation<Row, Boolean>{
        private static boolean updateArrayElement(Object[] array,
                                                  final int index,
                                                  final Object element,
                                                  final ManagedEntityTabularType type,
                                                  final boolean insert,
                                                  final AttributeAccessor output) throws TimeoutException, AttributeSupportException {
            final ManagedEntityType elementType = type.getColumnType(ManagedEntityTypeBuilder.AbstractManagedEntityArrayType.VALUE_COLUMN_NAME);
            final TypeToken<?> elementJavaType = WellKnownTypeSystem.getWellKnownType(elementType);
            if (elementJavaType == null)
                return false;
            else if (insert)
                array = ArrayUtils.add(array, index, elementType.getProjection(elementJavaType).convertFrom(element));
            else array[index] = elementType.getProjection(elementJavaType).convertFrom(element);
            output.setValue(array);
            return true;
        }

        private static boolean updateTableRow(final Table<String> table,
                                              final int index,
                                              final Map<String, Object> row,
                                              final ManagedEntityTabularType type,
                                              final boolean insert,
                                              final AttributeAccessor output) throws TimeoutException, AttributeSupportException{
            for(final String column: row.keySet()) {
                final ManagedEntityType columnType = type.getColumnType(column);
                final TypeToken<?> columnJavaType = WellKnownTypeSystem.getWellKnownType(columnType);
                if(columnJavaType == null) continue;
                row.put(column, columnType.getProjection(columnJavaType).convertFrom(row.get(column)));
            }
            if(insert)
                table.insertRow(index, row);
            else {
                final Map<String, Object> source = table.getRow(index);
                //merge and replace the row
                source.putAll(row);
                table.setRow(index, source);
            }
            output.setValue(table);
            return true;
        }

        private static boolean updateTableRow(final Table<String> table,
                                              final int index,
                                              final Object element,
                                              final ManagedEntityTabularType type,
                                              final boolean insert,
                                              final AttributeAccessor output) throws TimeoutException, AttributeSupportException{
            return TypeLiterals.isInstance(element, TypeLiterals.STRING_MAP) && updateTableRow(table, index, TypeLiterals.cast(element, TypeLiterals.STRING_MAP), type, insert, output);
        }

        static boolean update(final int index,
                              final Object row,
                              final boolean insert,
                              final AttributeAccessor input) throws TimeoutException, AttributeSupportException{
            final Object value = input.getValue(input.getWellKnownType(), null);
            if(TypeLiterals.isInstance(value, TypeLiterals.OBJECT_ARRAY))
                return updateArrayElement(TypeLiterals.cast(value, TypeLiterals.OBJECT_ARRAY), index, row, (ManagedEntityTabularType) input.getType(), insert, input);
            else if(TypeLiterals.isInstance(value, TypeLiterals.STRING_COLUMN_TABLE))
                return updateTableRow(TypeLiterals.cast(value, TypeLiterals.STRING_COLUMN_TABLE), index, row, (ManagedEntityTabularType) input.getType(), insert, input);
            else return false;
        }

        @Override
        public Boolean transform(final Row arg, final AttributeAccessor input) throws TimeoutException, AttributeSupportException {
            return update(arg.index, arg.element, false, input);
        }
    }

    static final class UpdateMapTransformation implements ValueTransformation<Map<String, Object>, Boolean>{

        private static boolean updateMap(final Map<String, Object> from,
                                         final Map<String, Object> to,
                                         final ManagedEntityTabularType type,
                                         final AttributeAccessor output) throws TimeoutException, AttributeSupportException{
            for(final Map.Entry<String, Object> entry: from.entrySet()) {
                final ManagedEntityType keyType = type.getColumnType(entry.getKey());
                final TypeToken<?> keyJavaType = WellKnownTypeSystem.getWellKnownType(keyType);
                if(keyJavaType == null) return false;
                to.put(entry.getKey(), keyType.getProjection(keyJavaType).convertFrom(entry.getValue()));
            }
            output.setValue(to);
            return true;
        }

        @Override
        public Boolean transform(final Map<String, Object> arg, final AttributeAccessor input) throws TimeoutException, AttributeSupportException {
            final TypeToken<?> elementJavaType = input.getWellKnownType();
            if(elementJavaType == null) return false;
            final Object map = input.getValue(elementJavaType, null);
            return TypeLiterals.isInstance(map, TypeLiterals.STRING_MAP) && updateMap(arg, TypeLiterals.cast(map, TypeLiterals.STRING_MAP), (ManagedEntityTabularType) input.getType(), input);
        }
    }

    void printValue(final PrintWriter output) throws TimeoutException;

    <I, O> O applyTransformation(final Class<? extends ValueTransformation<I, O>> transformation, final I arg) throws ReflectiveOperationException, TimeoutException, AttributeSupportException;

    void printOptions(final PrintWriter output);

    String getName();

    boolean canRead();
    boolean canWrite();

    void setValue(final Object value) throws TimeoutException, AttributeSupportException;
}
