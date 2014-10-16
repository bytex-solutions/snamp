package com.itworks.snamp.connectors;

import com.itworks.snamp.Table;
import com.itworks.snamp.TypeLiterals;
import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.reflect.Typed;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Represents a set of well known management entity types.
 * <p>
 *     You should use this builder for all management entities which
 *     values are implicitly compatible with well-known Java types:
 *     <ul>
 *         <li>Int8 - wrapper for {@link Byte} class.</li>
 *         <li>Int16 - wrapper for {@link Short} class.</li>
 *         <li>Int32 - wrapper for {@link Integer} class.</li>
 *         <li>Int64 - wrapper for {@link Long} class.</li>
 *         <li>String - wrapper for {@link String} class.</li>
 *         <li>Integer - wrapper for {@link BigInteger} class.</li>
 *         <li>Decimal - wrapper for {@link BigDecimal} class.</li>
 *         <li>Boolean - wrapper for {@link Boolean} class.</li>
 *         <li>Date - wrapper for {@link Date} and {@link Calendar} classes both.</li>
 *     </ul>
 * </p>
 * <p>
 *     For non-scalar data types such as arrays, dictionaries and tables it is recommended to use the
 *     following mapping:
 *     <ul>
 *         <li>Tabular data should be convertible to {@link com.itworks.snamp.Table} type. An implementation
 *         for this interface is provided by {@link com.itworks.snamp.SimpleTable} class. Entity data type
 *         should implements {@link ManagedEntityTabularType} interface.</li>
 *         <li>Array data should be convertible to Java array and {@link com.itworks.snamp.Table} type. Attribute
 *         data type should inherits from {@link ManagedEntityTypeBuilder.ManagedEntityArrayType} class.</li>
 *         <li>Map data should be convertible to {@link com.itworks.snamp.Table} type, and, optionally,
 *         to {@link Map} type. Map is a special case of tabular data when table has single row
 *         and multiple columns, where each column represents map key.</li>
 *     </ul>
 * </p>
 * <p>
 * Management entity type system is a set of converters that provides conversion between MIB-specific
 * data types and universal data types. This class provides set of converters between these data types
 * in the form {@link org.apache.commons.collections4.Transformer} interface implementation. Typically,
 * each custom SNAMP connector contains its own type system converter, inherited from this class.
 * The following example demonstrates your own type system converter:
 * <pre><code>
 * public final class CustomTypeInfoBuilder extends WellKnownTypeSystem{
 *
 *     public CustomTypeInfoBuilder(){
 *         registerConverter(TypeLiterals.STRING, TypeLiterals.BYTE_ARRAY,
 *                  new Transformer&lt;String, Byte[]&gt;(){
 *                      public void Byte[] transform(final String input){
 *                          return ArrayUtils.toObject(input.getBytes("UTF-8"));
 *                      }
 *                  });
 *     }
 *
 *     public final ManagementEntityType createByteArrayType(){
 *       return createArrayType(createInt8Type());
 *     }
 * }
 *
 * final CustomTypeInfoBuilder builder = new CustomTypeInfoBuilder();
 * final AttributeTypeInfo arrayType = builder.createByteArrayType();
 * final String result = builder.convert(Byte[].class, "Hello, world!");
 * </code></pre>
 * </p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class WellKnownTypeSystem extends ManagedEntityTypeBuilder {
    /**
     * Initializes a new type system.
     */
    public WellKnownTypeSystem(){
        registerConverter(TypeLiterals.STRING, TypeLiterals.BYTE,
                new Transformer<String, Byte>() {
                    @Override
                    public Byte transform(final String input) {
                        return Byte.valueOf(input);
                    }
                });
        registerConverter(TypeLiterals.STRING, TypeLiterals.SHORT,
                new Transformer<String, Short>() {
                    @Override
                    public Short transform(final String input) {
                        return Short.valueOf(input);
                    }
                });
        registerConverter(TypeLiterals.STRING, TypeLiterals.INTEGER,
                new Transformer<String, Integer>() {
                    @Override
                    public Integer transform(final String input) {
                        return Integer.valueOf(input);
                    }
                });
        registerConverter(TypeLiterals.STRING, TypeLiterals.LONG,
                new Transformer<String, Long>() {
                    @Override
                    public Long transform(final String input) {
                        return Long.parseLong(input);
                    }
                });
        registerConverter(TypeLiterals.STRING, TypeLiterals.BIG_INTEGER,
                new Transformer<String, BigInteger>() {
                    @Override
                    public BigInteger transform(final String input) {
                        return new BigInteger(input);
                    }
                });
        registerConverter(TypeLiterals.STRING, TypeLiterals.BOOLEAN,
                new Transformer<String, Boolean>() {
                    @Override
                    public Boolean transform(final String input) {
                        return Boolean.parseBoolean(input);
                    }
                });
        registerConverter(TypeLiterals.STRING, TypeLiterals.FLOAT,
                new Transformer<String, Float>() {
                    @Override
                    public Float transform(final String input) {
                        return Float.parseFloat(input);
                    }
                });
        registerConverter(TypeLiterals.STRING, TypeLiterals.DOUBLE,
                new Transformer<String, Double>() {
                    @Override
                    public Double transform(final String input) {
                        return Double.parseDouble(input);
                    }
                });
        registerConverter(TypeLiterals.STRING, TypeLiterals.BIG_DECIMAL,
                new Transformer<String, BigDecimal>() {
                    @Override
                    public BigDecimal transform(final String input) {
                        return new BigDecimal(input);
                    }
                });
        registerConverter(TypeLiterals.NUMBER, TypeLiterals.BYTE,
                new Transformer<Number, Byte>() {
                    @Override
                    public Byte transform(final Number input) {
                        return input.byteValue();
                    }
                });
        registerConverter(TypeLiterals.NUMBER, TypeLiterals.SHORT,
                new Transformer<Number, Short>() {
                    @Override
                    public Short transform(final Number input) {
                        return input.shortValue();
                    }
                });
        registerConverter(TypeLiterals.NUMBER, TypeLiterals.INTEGER,
                new Transformer<Number, Integer>() {
                    @Override
                    public Integer transform(final Number input) {
                        return input.intValue();
                    }
                });
        registerConverter(TypeLiterals.NUMBER, TypeLiterals.LONG,
                new Transformer<Number, Long>() {
                    @Override
                    public Long transform(final Number input) {
                        return input.longValue();
                    }
                });
        registerConverter(TypeLiterals.NUMBER, TypeLiterals.FLOAT,
                new Transformer<Number, Float>() {
                    @Override
                    public Float transform(final Number input) {
                        return input.floatValue();
                    }
                });
        registerConverter(TypeLiterals.NUMBER, TypeLiterals.DOUBLE,
                new Transformer<Number, Double>() {
                    @Override
                    public Double transform(final Number input) {
                        return input.doubleValue();
                    }
                });
        registerConverter(TypeLiterals.NUMBER, TypeLiterals.BIG_INTEGER,
                new Transformer<Number, BigInteger>() {
                    @Override
                    public BigInteger transform(final Number input) {
                        return input instanceof BigInteger ?
                                (BigInteger)input : BigInteger.valueOf(input.longValue());
                    }
                });
        registerConverter(TypeLiterals.NUMBER, TypeLiterals.BIG_DECIMAL,
                new Transformer<Number, BigDecimal>() {
                    @Override
                    public BigDecimal transform(final Number input) {
                        if(input instanceof BigDecimal)
                            return (BigDecimal)input;
                        else if(input instanceof BigInteger)
                            return new BigDecimal((BigInteger)input);
                        else if(input instanceof Double)
                            return BigDecimal.valueOf(input.doubleValue());
                        else if(input instanceof Float)
                            return BigDecimal.valueOf(input.floatValue());
                        else return BigDecimal.valueOf(input.longValue());
                    }
                });
        registerConverter(TypeLiterals.NUMBER, TypeLiterals.DATE,
                new Transformer<Number, Date>() {
                    @Override
                    public Date transform(final Number input) {
                        return new Date(input.longValue());
                    }
                });
        registerConverter(TypeLiterals.NUMBER, TypeLiterals.CALENDAR,
                new Transformer<Number, Calendar>() {
                    @Override
                    public Calendar transform(final Number input) {
                        final Calendar now = Calendar.getInstance();
                        now.setTime(new Date(input.longValue()));
                        return now;
                    }
                });
        registerConverter(TypeLiterals.NUMBER, TypeLiterals.BOOLEAN,
                new Transformer<Number, Boolean>() {
                    @Override
                    public Boolean transform(final Number input) {
                        if(input instanceof BigInteger) return !BigInteger.ZERO.equals(input);
                        else if(input instanceof BigDecimal) return !BigDecimal.ZERO.equals(input);
                        else return input.longValue() != 0;
                    }
                });
        registerConverter(TypeLiterals.CALENDAR, TypeLiterals.LONG,
                new Transformer<Calendar, Long>() {
                    @Override
                    public Long transform(final Calendar input) {
                        return input.getTime().getTime();
                    }
                });
        registerConverter(TypeLiterals.CALENDAR, TypeLiterals.DATE,
                new Transformer<Calendar, Date>() {
                    @Override
                    public Date transform(final Calendar input) {
                        return input.getTime();
                    }
                });
        registerConverter(TypeLiterals.DATE, TypeLiterals.CALENDAR,
                new Transformer<Date, Calendar>() {
                    @Override
                    public Calendar transform(final Date input) {
                        final Calendar now = Calendar.getInstance();
                        now.setTime(input);
                        return now;
                    }
                });
        registerConverter(TypeLiterals.BOOLEAN, TypeLiterals.BYTE,
                new Transformer<Boolean, Byte>() {
                    @Override
                    public Byte transform(final Boolean input) {
                        return input ? (byte)1 : 0;
                    }
                });
        registerConverter(TypeLiterals.BOOLEAN, TypeLiterals.SHORT,
                new Transformer<Boolean, Short>() {
                    @Override
                    public Short transform(final Boolean input) {
                        return input ? (short)1 : 0;
                    }
                });
        registerConverter(TypeLiterals.BOOLEAN, TypeLiterals.INTEGER,
                new Transformer<Boolean, Integer>() {
                    @Override
                    public Integer transform(final Boolean input) {
                        return input ? 1 : 0;
                    }
                });
        registerConverter(TypeLiterals.BOOLEAN, TypeLiterals.LONG,
                new Transformer<Boolean, Long>() {
                    @Override
                    public Long transform(final Boolean input) {
                        return input ? 1L : 0L;
                    }
                });
        registerConverter(TypeLiterals.BOOLEAN, TypeLiterals.BIG_INTEGER,
                new Transformer<Boolean, BigInteger>() {
                    @Override
                    public BigInteger transform(final Boolean input) {
                        return input ? BigInteger.ONE : BigInteger.ZERO;
                    }
                });
        registerConverter(TypeLiterals.BOOLEAN, TypeLiterals.BIG_DECIMAL,
                new Transformer<Boolean, BigDecimal>() {
                    @Override
                    public BigDecimal transform(final Boolean input) {
                        return input ? BigDecimal.ONE : BigDecimal.ZERO;
                    }
                });
        registerConverter(TypeLiterals.STRING, TypeLiterals.CHAR,
                new Transformer<String, Character>() {
                    @Override
                    public Character transform(final String input) {
                        return input.isEmpty() ? '\0' : input.charAt(0);
                    }
                });
        registerConverter(TypeLiterals.DATE, TypeLiterals.LONG,
                new Transformer<Date, Long>() {
                    @Override
                    public Long transform(final Date input) {
                        return input.getTime();
                    }
                });
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Byte}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Byte}.
     */
    public static boolean supportsInt8(final ManagedEntityType entityType){
        return supportsProjection(entityType, TypeLiterals.BYTE);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Byte}.
     * @return A new type converter.
     */
    public final ManagedEntityType createInt8Type(){
        return createEntitySimpleType(TypeLiterals.BYTE);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Short}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Short}.
     */
    public static boolean supportsInt16(final ManagedEntityType entityType){
        return supportsProjection(entityType, TypeLiterals.SHORT);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Short}.
     * @return A new type converter.
     */
    public final ManagedEntityType createInt16Type(){
        return createEntitySimpleType(TypeLiterals.SHORT);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Integer}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Integer}.
     */
    public static boolean supportsInt32(final ManagedEntityType entityType){
        return supportsProjection(entityType, TypeLiterals.INTEGER);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Integer}.
     * @return A new type converter.
     */
    public final ManagedEntityType createInt32Type(){
        return createEntitySimpleType(TypeLiterals.INTEGER);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Long}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Long}.
     */
    public static boolean supportsInt64(final ManagedEntityType entityType){
        return supportsProjection(entityType, TypeLiterals.LONG);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Long}.
     * @return A new type converter.
     */
    public final ManagedEntityType createInt64Type(){
        return createEntitySimpleType(TypeLiterals.LONG);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link BigInteger}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link BigInteger}.
     */
    public static boolean supportsInteger(final ManagedEntityType entityType){
        return supportsProjection(entityType, TypeLiterals.BIG_INTEGER);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link BigInteger}.
     * @return A new type converter.
     */
    public final ManagedEntityType createIntegerType(){
        return createEntitySimpleType(TypeLiterals.BIG_INTEGER);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link BigDecimal}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link BigDecimal}.
     */
    public static boolean supportsDecimal(final ManagedEntityType entityType){
        return supportsProjection(entityType, TypeLiterals.BIG_DECIMAL);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link BigDecimal}.
     * @return A new type converter.
     */
    public final ManagedEntityType createDecimalType(){
        return createEntitySimpleType(TypeLiterals.BIG_DECIMAL);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Date}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Date}.
     */
    public static boolean supportsUnixTime(final ManagedEntityType entityType){
        return supportsProjection(entityType, TypeLiterals.DATE);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Date}.
     * @return A new type converter.
     */
    public final ManagedEntityType createUnixTimeType(){
        return createEntitySimpleType(TypeLiterals.DATE);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Boolean}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Boolean}.
     */
    public static boolean supportsBoolean(final ManagedEntityType entityType){
        return supportsProjection(entityType, TypeLiterals.BOOLEAN);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Boolean}.
     * @return A new type converter.
     */
    public final ManagedEntityType createBooleanType(){
        return createEntitySimpleType(TypeLiterals.BOOLEAN);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link String}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link String}.
     */
    public static boolean supportsString(final ManagedEntityType entityType){
        return supportsProjection(entityType, TypeLiterals.STRING);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link String}.
     * @return A new type converter.
     */
    public final ManagedEntityType createStringType(){
        return createEntitySimpleType(TypeLiterals.STRING);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Float}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Float}.
     */
    public static boolean supportsFloat(final ManagedEntityType entityType){
        return supportsProjection(entityType, TypeLiterals.FLOAT);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Float}.
     * @return A new type converter.
     */
    public final ManagedEntityType createFloatType(){
        return createEntitySimpleType(TypeLiterals.FLOAT);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Double}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Double}.
     */
    public static boolean supportsDouble(final ManagedEntityType entityType){
        return supportsProjection(entityType, TypeLiterals.DOUBLE);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Double}.
     * @return A new type converter.
     */
    public final ManagedEntityType createDoubleType(){
        return createEntitySimpleType(TypeLiterals.DOUBLE);
    }

    /**
     * Creates a new array type that can be converted into {@link Object[]}.
     * @param elementType An element type of the array.
     * @return A new array type that can be converted into {@link Object[]}.
     */
    public final AbstractManagedEntityType createEntityArrayType(final ManagedEntityType elementType){
        return createEntityType(new Factory<ManagedEntityArrayType>(){

            /**
             * Creates a new instance of the specified type.
             *
             * @return A new instance of the specified type.
             */
            @Override
            public ManagedEntityArrayType create() {
                return new ManagedEntityArrayType(elementType);
            }
        }, TypeLiterals.OBJECT_ARRAY);
    }

    /**
     * Creates a new tabular type that can be converted into {@link Table}.
     * <p>
     *  The returned {@link ManagedEntityTabularType} instance throws {@link UnsupportedOperationException}
     *  exception when {@link ManagedEntityTabularType#getRowCount()} is invoked.
     * </p>
     * @param columns A collection of columns.
     * @param index An array of indexed columns.
     * @return A new instance of the tabular type.
     */
    public final ManagedEntityType createEntityTabularType(final Map<String, ManagedEntityType> columns, final String... index){
        final Map<String, ManagedEntityType> readonlyColumns = Collections.unmodifiableMap(columns);
        final Collection<String> readonlyIndex = Collections.unmodifiableCollection(Arrays.asList(index));
        return createEntityType(new Factory<AbstractManagedEntityTabularType>(){

            /**
             * Creates a new instance of the specified type.
             *
             * @return A new instance of the specified type.
             */
            @Override
            public AbstractManagedEntityTabularType create() {
                return new AbstractManagedEntityTabularType() {
                    @Override
                    public final Collection<String> getColumns() {
                        return readonlyColumns.keySet();
                    }

                    /**
                     * Determines whether the specified column is indexed.
                     *
                     * @param column The name of the column.
                     * @return {@literal true}, if the specified column is indexed; otherwise, {@literal false}.
                     */
                    @Override
                    public final boolean isIndexed(final String column) {
                        return readonlyIndex.contains(column);
                    }

                    @Override
                    public final ManagedEntityType getColumnType(final String column) {
                        return readonlyColumns.get(column);
                    }

                    @Override
                    public final long getRowCount() throws UnsupportedOperationException {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        }, TypeLiterals.STRING_COLUMN_TABLE);
    }

    public final ManagedEntityType createEntityDictionaryType(final Map<String, ManagedEntityType> keys) {
        return createEntityType(new Factory<AbstractManagedEntityType>() {
            private final Map<String, ManagedEntityType> readonlyColumns = Collections.unmodifiableMap(keys);

            @Override
            public AbstractManagedEntityTabularType create() {
                return new AbstractManagedEntityTabularType() {

                    /**
                     * Returns a set of column names.
                     *
                     * @return The set of column names.
                     */
                    @Override
                    public Collection<String> getColumns() {
                        return readonlyColumns.keySet();
                    }

                    /**
                     * Determines whether the specified column is indexed.
                     *
                     * @param column The name of the column.
                     * @return {@literal true}, if the specified column is indexed; otherwise, {@literal false}.
                     */
                    @Override
                    public boolean isIndexed(final String column) {
                        return false;
                    }

                    /**
                     * Returns the column type.
                     *
                     * @param column The name of the column.
                     * @return The type of the column; or {@literal null} if the specified column doesn't exist.
                     */
                    @Override
                    public ManagedEntityType getColumnType(final String column) {
                        return readonlyColumns.get(column);
                    }

                    /**
                     * Returns the number of rows if this information is available.
                     *
                     * @return The count of rows.
                     */
                    @Override
                    public long getRowCount() {
                        return 1L;
                    }
                };
            }
        }, TypeLiterals.STRING_COLUMN_TABLE, TypeLiterals.STRING_MAP);
    }

    /**
     * Creates a new tabular type that can be converted into {@link Table}.
     * <p>
     *  The returned {@link ManagedEntityTabularType} returns {@code rowCount} from
     *  {@link ManagedEntityTabularType#getRowCount()} method.
     * </p>
     * @param columns A collection of columns.
     * @param rowCount A row count in the table.
     * @param index An array of indexed columns.
     * @return A new instance of the tabular type.
     */
    public final ManagedEntityType createEntityTabularType(final Map<String, ManagedEntityType> columns, final int rowCount, final String... index){
        return createEntityType(new Factory<AbstractManagedEntityTabularType>(){
            private final Map<String, ManagedEntityType> readonlyColumns = Collections.unmodifiableMap(columns);
            private final Collection<String> readonlyIndex = Collections.unmodifiableCollection(Arrays.asList(index));

            /**
             * Creates a new instance of the specified type.
             *
             * @return A new instance of the specified type.
             */
            @Override
            public AbstractManagedEntityTabularType create() {
                return new AbstractManagedEntityTabularType() {
                    @Override
                    public final Collection<String> getColumns() {
                        return readonlyColumns.keySet();
                    }

                    /**
                     * Determines whether the specified column is indexed.
                     *
                     * @param column The name of the column.
                     * @return {@literal true}, if the specified column is indexed; otherwise, {@literal false}.
                     */
                    @Override
                    public final boolean isIndexed(final String column) {
                        return readonlyIndex.contains(column);
                    }

                    @Override
                    public final ManagedEntityType getColumnType(final String column) {
                        return readonlyColumns.get(column);
                    }

                    @Override
                    public final long getRowCount() {
                        return rowCount;
                    }
                };
            }
        }, TypeLiterals.STRING_COLUMN_TABLE);
    }

    /**
     * Gets system type associated with the specified managed entity type descriptor.
     * @param type Type of the managed entity.
     * @return System type; or {@literal null}, if the specified managed entity is not a part
     * of well-known type system.
     */
    public static Typed<?> getWellKnownType(final ManagedEntityType type){
        if(type == null)
            return null;
        else if(supportsBoolean(type))
            return TypeLiterals.BOOLEAN;
        else if(supportsInt8(type))
            return TypeLiterals.BYTE;
        else if(supportsInt16(type))
            return TypeLiterals.SHORT;
        else if(supportsInt32(type))
            return TypeLiterals.INTEGER;
        else if(supportsInt64(type))
            return TypeLiterals.LONG;
        else if(supportsFloat(type))
            return TypeLiterals.FLOAT;
        else if(supportsDouble(type))
            return TypeLiterals.DOUBLE;
        else if(supportsInteger(type))
            return TypeLiterals.BIG_INTEGER;
        else if(supportsDecimal(type))
            return TypeLiterals.BIG_DECIMAL;
        else if(supportsUnixTime(type))
            return TypeLiterals.DATE;
        else if(supportsString(type))
            return TypeLiterals.STRING;
        else if(isArray(type))
            return TypeLiterals.OBJECT_ARRAY;
        else if(isMap(type))
            return TypeLiterals.STRING_MAP;
        else if(isTable(type))
            return TypeLiterals.STRING_COLUMN_TABLE;
        else return null;
    }
}
