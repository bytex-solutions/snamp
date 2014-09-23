package com.itworks.snamp.connectors;

import com.itworks.snamp.AbstractTypeConverterResolver;
import com.itworks.snamp.TypeConverter;
import org.apache.commons.collections4.Factory;

import java.util.*;

/**
 * Represents a base class for building management entity types, such as managementAttributes or notifications.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class ManagedEntityTypeBuilder extends AbstractTypeConverterResolver {

    /**
     * Initializes a new empty entity type builder.
     */
    protected ManagedEntityTypeBuilder(){
    }

    /**
     * Represents a base class for building custom management entity types.
     * <p>
     *     You should derive from this class when you want to expose management entity type
     *     descriptor as a custom interface that extends {@link ManagedEntityType}.
     * </p>
     * @author Roman Sakno
     * @version 1.0
     * @since 1.0
     */
    public static class AbstractManagedEntityType implements ManagedEntityType {
        private final Map<Class<?>, TypeConverter<?>> projections;

        /**
         * Initializes a new management entity type.
         * @param converters Additional converters associated with this management entity type. Can be empty.
         */
        protected AbstractManagedEntityType(final TypeConverter<?>... converters){
            projections = new HashMap<>(5);
            for(final TypeConverter<?> conv: converters)
                projections.put(conv.getType(), conv);
        }

        private static void registerConverter(final AbstractManagedEntityType entityType, final TypeConverter<?> converter){
            entityType.projections.put(converter.getType(), converter);
        }

        /**
         * Returns an array of supported projections.
         * @return An array of supported projections.
         */
        public final Collection<Class<?>> getProjections(){
            return projections.keySet();
        }

        /**
         * Returns the type converter for the specified native Java type mapping.
         *
         * @param projectionType The native Java type to which the entity value can be converted. Cannot be {@literal null}.
         * @param <T>            Type of the projection.
         * @return The type converter for the specified projection type; or {@literal null}, if projection is not supported.
         */
        @SuppressWarnings("unchecked")
        @Override
        public final <T> TypeConverter<T> getProjection(final Class<T> projectionType) {
            return shouldNormalize(projectionType) ?
                    (TypeConverter<T>)getProjection(normalizeClass(projectionType)):
                    (TypeConverter<T>)projections.get(projectionType);
        }

        /**
         * Determines whether the MIB-specific type can be converted into another Java type.
         * @param from THe type of the value to convert.
         * @param to The type of the conversion result.
         * @return {@literal true}, if the MIB-specific type can be converted into another Java type; otherwise, {@literal false}.
         */
        public final boolean canConvert(final Class<?> from, final Class<?> to){
            if(from == null || to == null) return false;
            final TypeConverter<?> converter = projections.get(to);
            return converter != null && converter.canConvertFrom(from);
        }

        /**
         * Converts the specified management entity value into the well-known Java type.
         * @param value A value to convert.
         * @param resultType Well-known Java type.
         * @param <T> Well-known Java type.
         * @return Well-known representation of the management entity value.
         * @throws IllegalArgumentException {@code resultType} is not supported as conversion result.
         */
        public final <T> T convert(final Object value, final Class<T> resultType) throws IllegalArgumentException{
            if(value == null || resultType == null) return null;
            final TypeConverter<?> converter = projections.get(resultType);
            if(converter == null) throw new IllegalArgumentException(String.format("Cannot convert %s to %s", value, resultType));
            return resultType.cast(converter.convertFrom(value));
        }
    }

    /**
     * Represents a simple management entity type without additional methods and properties.
     * This class cannot be inherited.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static final class SimpleManagedEntityType extends AbstractManagedEntityType {
        /**
         * Represents a singleton activator for the simple management entity type.
         */
        public static final Factory<SimpleManagedEntityType> ACTIVATOR = new Factory<SimpleManagedEntityType>() {
            @Override
            public SimpleManagedEntityType create() {
                return new SimpleManagedEntityType();
            }
        };
    }

    /**
     * Represents tabular management entity.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static abstract class AbstractManagedEntityTabularType extends AbstractManagedEntityType implements ManagedEntityTabularType {

    }

    /**
     * Represents an abstract class for constructing custom array types.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static abstract class AbstractManagedEntityArrayType extends AbstractManagedEntityTabularType {
        /**
         * Represents name of the first column.
         */
        public static final String INDEX_COLUMN_NAME = "Index";

        /**
         * Represents name of the second column.
         */
        public static final String VALUE_COLUMN_NAME = "Value";

        /**
         * Represents read-only collection of columns.
         */
        public static final Collection<String> COLUMNS = Collections.unmodifiableList(Arrays.asList(INDEX_COLUMN_NAME, VALUE_COLUMN_NAME));

        /**
         * Gets a set of dictionary keys (items).
         *
         * @return A set of dictionary keys (items).
         */
        @Override
        public final Collection<String> getColumns() {
            return COLUMNS;
        }

        /**
         * Determines whether the specified column is indexed.
         *
         * @param column The name of the column.
         * @return {@literal true}, if the specified column is indexed; otherwise, {@literal false}.
         */
        @Override
        public final boolean isIndexed(final String column) {
            return isIndexedColumn(column);
        }

        /**
         * Determines whether the specified column is indexed.
         *
         * @param column The name of the column.
         * @return {@literal true}, if the specified column is indexed; otherwise, {@literal false}.
         */
        public static boolean isIndexedColumn(final String column){
            return Objects.equals(INDEX_COLUMN_NAME, column);
        }

        /**
         * Returns the type of the array {@link #INDEX_COLUMN_NAME} column.
         * @return The type of the array index column.
         */
        public abstract ManagedEntityType getIndexColumnType();

        /**
         * Returns the type of the array {@link #VALUE_COLUMN_NAME} column.
         * @return The type of the array value column.
         */
        public abstract ManagedEntityType getValueColumnType();

        /**
         * Returns the type of the column.
         * <p>
         *     There is only two available column names:
         *     <ul>
         *         <li>{@link #INDEX_COLUMN_NAME} that represents column containing array index.</li>
         *         <li>{@link #VALUE_COLUMN_NAME} that represents column containing array element.</li>
         *     </ul>
         * </p>
         * @param columnName The name of the column.
         * @return The column type.
         */
        @Override
        public final ManagedEntityType getColumnType(final String columnName) {
            switch (columnName){
                case INDEX_COLUMN_NAME: return getIndexColumnType();
                case VALUE_COLUMN_NAME: return getValueColumnType();
                default: return null;
            }
        }

        /**
         * Returns the number of rows if this information is available.
         *
         * @return The count of rows.
         * @throws UnsupportedOperationException Row count is not supported.
         */
        @Override
        public long getRowCount() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Represents single-dimensional array type as table.
     * <p>
     *     An array type of the attribute should be always represented by this class (or one of its derived classes).
     * </p>
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public class ManagedEntityArrayType extends AbstractManagedEntityArrayType {
        /**
         * Represents array element type.
         */
        protected final ManagedEntityType elementType;

        /**
         * Initializes a new instance of the array type descriptor.
         * @param elementType The type of the array elements.
         * @throws IllegalArgumentException elementType is {@literal null}.
         */
        public ManagedEntityArrayType(final ManagedEntityType elementType){
            if(elementType == null) throw new IllegalArgumentException("elementType is null.");
            this.elementType = elementType;
        }

        /**
         * Returns the type of the array index column.
         * <p>
         *     In the default implementation, this method always returns value
         *     produced by invocation of {@link #createEntitySimpleType(Class[])} with {@code Integer.class}
         *     argument.
         * </p>
         * @return The type of the array index column.
         */
        @Override
        public ManagedEntityType getIndexColumnType(){
            return createEntitySimpleType(Integer.class);
        }

        /**
         * Returns the type of the array
         *
         * @return The type of the array element.
         */
        @Override
        public final ManagedEntityType getValueColumnType() {
            return elementType;
        }
    }


    /**
     * Creates a new management entity type.
     * <p>
     *     An implementation of {@link ManagedEntityType#getProjection(Class)} supplied by SNAMP infrastructure
     *     and you cannot override behavior of this method.
     * </p>
     * @param activator An activator that creates a new instance of {@link ManagedEntityTypeBuilder.AbstractManagedEntityType} with custom methods.
     * @param projections An array of supported projections of the specified management entity type.
     * @return An instance of the management entity type.
     */
    public final <T extends AbstractManagedEntityType> T createEntityType(final Factory<T> activator, final Class<?>... projections) {
        final T entityType = activator.create();
        for(final Class<?> t: projections){
            final TypeConverter<?> converter = getTypeConverter(t);
            if(converter != null) AbstractManagedEntityType.registerConverter(entityType, converter);
        }
        return entityType;
    }

    /**
     * Creates a new simple management entity type.
     * @param projections An array of supported projections of the specified management entity type.
     * @return A new instance of the simple management entity type.
     */
    public final SimpleManagedEntityType createEntitySimpleType(final Class<?>... projections){
        return createEntityType(SimpleManagedEntityType.ACTIVATOR, projections);
    }

    /**
     * Determines whether the management entity supports the specified type projection.
     * @param entityType Type of the management entity.
     * @param t Native Java representation of the management entity type.
     * @return {@literal true}, if the management entity supports the specified type projection; otherwise, {@literal false}.
     */
    public static boolean supportsProjection(final ManagedEntityType entityType, final Class<?> t){
        return entityType != null && entityType.getProjection(t) != null;
    }

    /**
     * Determines whether the specified management type is simple (scalar).
     * <p>
     *     Invocation of this method is equal to {@code entityType instanceof SimpleManagementEntityType}.
     * </p>
     * @param entityType An entity type to check,
     * @return {@literal true}, if the specified management entity type is simple; otherwise, {@literal false}.
     */
    public static boolean isSimpleType(final ManagedEntityType entityType){
        return entityType instanceof SimpleManagedEntityType;
    }

    /**
     * Determines whether the specified management type is table.
     * <p>
     *  Invocation of this method is equal to {@code entityType instanceof ManagementEntityTabularType}.
     * </p>
     * @param entityType An entity type to check,
     * @return {@literal true}, if the specified management entity type is table; otherwise, {@literal false}.
     */
    public static boolean isTable(final ManagedEntityType entityType){
        return entityType instanceof ManagedEntityTabularType;
    }

    private static boolean isArray(final ManagedEntityTabularType entityType){
        final Collection<String> columns = entityType.getColumns();
        return columns.size() == 2 && columns.containsAll(AbstractManagedEntityArrayType.COLUMNS);
    }

    /**
     * Determines whether the specified management type is array.
     * <p>
     *     Implementation of this method is equal to:
     *     <pre><code>
     *         public static boolean isArray(final ManagementEntityTabularType entityType){
     *          final Collection<String> columns = entityType.getColumns();
     *          return columns.size() == 2 && columns.containsAll(AbstractManagementEntityArrayType.COLUMNS);
     *         }
     *     </code></pre>
     * </p>
     * @param entityType An entity type to check,
     * @return {@literal true}, if the specified management entity type is array; otherwise, {@literal false}.
     */
    public static boolean isArray(final ManagedEntityType entityType){
        return entityType instanceof ManagedEntityTabularType && isArray((ManagedEntityTabularType)entityType);

    }

    /**
     * Creates a new simple management entity type that can convert any value to {@link String}.
     * @return A new simple management entity type that can convert any value to {@link String}.
     */
    public final ManagedEntityType createFallbackEntityType(){
        return createEntitySimpleType(String.class);
    }

    /**
     * Determines whether the specified entity represents a map.
     * <p>
     *     Implementation of this method is equal to:
     *     <pre><code>
     *     return entityType instanceof ManagementEntityTabularType &&
     *          entityType.getProjection(Map.class) != null;
     *     </code></pre>
     * </p>
     * @param entityType An entity type to check.
     * @return {@literal true} if the specified entity can be converted into {@link Map}; {@literal false}.
     */
    public static boolean isMap(final ManagedEntityType entityType){
        return entityType instanceof ManagedEntityTabularType &&
                entityType.getProjection(Map.class) != null;
    }
}
