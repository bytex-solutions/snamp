package com.itworks.snamp.adapters.ssh;

import com.google.common.reflect.TypeToken;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.connectors.ManagedEntityTabularType;
import com.itworks.snamp.connectors.ManagedEntityType;
import com.itworks.snamp.connectors.ManagedEntityTypeBuilder;
import com.itworks.snamp.connectors.WellKnownTypeSystem;
import com.itworks.snamp.connectors.attributes.AttributeSupportException;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.mapping.*;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;
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

        private void deleteTableRow(final RowSet<?> table,
                                       final int rowIndex,
                                       final AttributeAccessor setter) throws TimeoutException, AttributeSupportException{
            //modifying the same instance of the table is normal,
            //because input table is a conversion result provided by connector
            setter.setValue(RecordSetUtils.removeRow(table, rowIndex));
        }

        @Override
        public Void transform(final Integer index, final AttributeAccessor input) throws TimeoutException, AttributeSupportException{
            final Object value = input.getValue(input.getWellKnownType(), null);
            if(TypeLiterals.isInstance(value, TypeLiterals.OBJECT_ARRAY))
                deleteArrayElement(TypeLiterals.cast(value, TypeLiterals.OBJECT_ARRAY), index, input);
            else if(TypeLiterals.isInstance(value, TypeLiterals.ROW_SET))
                deleteTableRow(TypeLiterals.cast(value, TypeLiterals.ROW_SET), index, input);
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

        @SuppressWarnings("unchecked")
        private static boolean updateTableRowImpl(RowSet table,
                                              final int index,
                                              final Map<String, Object> row,
                                              final ManagedEntityTabularType type,
                                              final boolean insert,
                                              final AttributeAccessor output) throws TimeoutException, AttributeSupportException{
            if(!Utils.collectionsAreEqual(table.getColumns(), row.keySet()))
                return false;
            for(final String column: row.keySet()) {
                final ManagedEntityType columnType = type.getColumnType(column);
                final TypeToken<?> columnJavaType = WellKnownTypeSystem.getWellKnownType(columnType);
                if(columnJavaType == null) continue;
                row.put(column, columnType.getProjection(columnJavaType).convertFrom(row.get(column)));
            }
            table = insert ?
                    RecordSetUtils.insertRow(table, RecordSetUtils.fromMap(row), index):
                    RecordSetUtils.setRow(table, RecordSetUtils.fromMap(row), index);
            output.setValue(table);
            return true;
        }

        private static boolean updateTableRow(final RowSet<?> table,
                                              final int index,
                                              final Object element,
                                              final ManagedEntityTabularType type,
                                              final boolean insert,
                                              final AttributeAccessor output) throws TimeoutException, AttributeSupportException{
            return TypeLiterals.isInstance(element, SshHelpers.STRING_MAP_TYPE) &&
                    updateTableRowImpl(table, index, TypeLiterals.cast(element, SshHelpers.STRING_MAP_TYPE), type, insert, output);
        }

        static boolean update(final int index,
                              final Object row,
                              final boolean insert,
                              final AttributeAccessor input) throws TimeoutException, AttributeSupportException{
            final Object value = input.getValue(input.getWellKnownType(), null);
            if(TypeLiterals.isInstance(value, TypeLiterals.OBJECT_ARRAY))
                return updateArrayElement(TypeLiterals.cast(value, TypeLiterals.OBJECT_ARRAY), index, row, (ManagedEntityTabularType) input.getType(), insert, input);
            else
                return TypeLiterals.isInstance(value, TypeLiterals.ROW_SET) && updateTableRow(TypeLiterals.cast(value, TypeLiterals.ROW_SET), index, row, (ManagedEntityTabularType) input.getType(), insert, input);
        }

        @Override
        public Boolean transform(final Row arg, final AttributeAccessor input) throws TimeoutException, AttributeSupportException {
            return update(arg.index, arg.element, false, input);
        }
    }

    static final class UpdateMapTransformation implements ValueTransformation<Map<String, Object>, Boolean>{

        private static boolean updateMap(final Map<String, ?> from,
                                         final Map<String, Object> to,
                                         final ManagedEntityTabularType type,
                                         final AttributeAccessor output) throws TimeoutException, AttributeSupportException{
            for(final Map.Entry<String, ?> entry: from.entrySet()) {
                final ManagedEntityType keyType = type.getColumnType(entry.getKey());
                final TypeToken<?> keyJavaType = WellKnownTypeSystem.getWellKnownType(keyType);
                if(keyJavaType == null) return false;
                to.put(entry.getKey(), keyType.getProjection(keyJavaType).convertFrom(entry.getValue()));
            }
            output.setNamedRecordSet(new KeyedRecordSet<String, Object>() {
                @Override
                protected Set<String> getKeys() {
                    return to.keySet();
                }

                @Override
                protected Object getRecord(final String key) {
                    return to.get(key);
                }
            });
            return true;
        }

        @SuppressWarnings("unchecked")
        private static boolean updateMap(final Map<String, Object> from,
                                         final RecordSet<String, ?> to,
                                         final ManagedEntityTabularType type,
                                         final AttributeAccessor output) throws TimeoutException, AttributeSupportException{
            return updateMap(from, RecordSetUtils.toMap((RecordSet<String, Object>)to), type, output);
        }

        @Override
        public Boolean transform(final Map<String, Object> arg, final AttributeAccessor input) throws TimeoutException, AttributeSupportException {
            final TypeToken<?> elementJavaType = input.getWellKnownType();
            if(elementJavaType == null) return false;
            final Object map = input.getValue(elementJavaType, null);
            return TypeLiterals.isInstance(map, TypeLiterals.NAMED_RECORD_SET) && updateMap(arg, TypeLiterals.cast(map, TypeLiterals.NAMED_RECORD_SET), (ManagedEntityTabularType) input.getType(), input);
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
