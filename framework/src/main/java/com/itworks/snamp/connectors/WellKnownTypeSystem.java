package com.itworks.snamp.connectors;

import com.itworks.snamp.Table;
import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.Transformer;

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
 * in the form of static public unary methods annotated with {@link com.itworks.snamp.AbstractTypeConverterProvider.Converter} interface. Typically,
 * each custom SNAMP connector contains its own type system converter, inherited from this class.
 * The following example demonstrates your own type system converter:
 * <pre><code>
 * public final class CustomTypeInfoBuilder extends WellKnownTypeSystem{
 *
 *     public CustomTypeInfoBuilder(){
 *         registerConverter(String.class, Byte[].class,
 *                  new Transformer&lt;String, Byte[]&gt;(){
 *                      public void Byte[] transform(String input){
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
        registerConverter(String.class, Byte.class,
                new Transformer<String, Byte>() {
                    @Override
                    public Byte transform(final String input) {
                        return Byte.valueOf(input);
                    }
                });
        registerConverter(String.class, Short.class,
                new Transformer<String, Short>() {
                    @Override
                    public Short transform(final String input) {
                        return Short.valueOf(input);
                    }
                });
        registerConverter(String.class, Integer.class,
                new Transformer<String, Integer>() {
                    @Override
                    public Integer transform(final String input) {
                        return Integer.valueOf(input);
                    }
                });
        registerConverter(String.class, Long.class,
                new Transformer<String, Long>() {
                    @Override
                    public Long transform(final String input) {
                        return Long.parseLong(input);
                    }
                });
        registerConverter(String.class, BigInteger.class,
                new Transformer<String, BigInteger>() {
                    @Override
                    public BigInteger transform(final String input) {
                        return new BigInteger(input);
                    }
                });
        registerConverter(String.class, Boolean.class,
                new Transformer<String, Boolean>() {
                    @Override
                    public Boolean transform(final String input) {
                        return null;
                    }
                });
        registerConverter(String.class, Float.class,
                new Transformer<String, Float>() {
                    @Override
                    public Float transform(final String input) {
                        return Float.parseFloat(input);
                    }
                });
        registerConverter(String.class, Double.class,
                new Transformer<String, Double>() {
                    @Override
                    public Double transform(final String input) {
                        return Double.parseDouble(input);
                    }
                });
        registerConverter(String.class, BigDecimal.class,
                new Transformer<String, BigDecimal>() {
                    @Override
                    public BigDecimal transform(final String input) {
                        return new BigDecimal(input);
                    }
                });
        registerConverter(Number.class, Byte.class,
                new Transformer<Number, Byte>() {
                    @Override
                    public Byte transform(final Number input) {
                        return input.byteValue();
                    }
                });
        registerConverter(Number.class, Short.class,
                new Transformer<Number, Short>() {
                    @Override
                    public Short transform(final Number input) {
                        return input.shortValue();
                    }
                });
        registerConverter(Number.class, Integer.class,
                new Transformer<Number, Integer>() {
                    @Override
                    public Integer transform(final Number input) {
                        return input.intValue();
                    }
                });
        registerConverter(Number.class, Long.class,
                new Transformer<Number, Long>() {
                    @Override
                    public Long transform(final Number input) {
                        return input.longValue();
                    }
                });
        registerConverter(Number.class, Float.class,
                new Transformer<Number, Float>() {
                    @Override
                    public Float transform(final Number input) {
                        return input.floatValue();
                    }
                });
        registerConverter(Number.class, Double.class,
                new Transformer<Number, Double>() {
                    @Override
                    public Double transform(final Number input) {
                        return input.doubleValue();
                    }
                });
        registerConverter(Number.class, BigInteger.class,
                new Transformer<Number, BigInteger>() {
                    @Override
                    public BigInteger transform(final Number input) {
                        return input instanceof BigInteger ?
                                (BigInteger)input : BigInteger.valueOf(input.longValue());
                    }
                });
        registerConverter(Number.class, BigDecimal.class,
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
        registerConverter(Number.class, Date.class,
                new Transformer<Number, Date>() {
                    @Override
                    public Date transform(final Number input) {
                        return new Date(input.longValue());
                    }
                });
        registerConverter(Number.class, Calendar.class,
                new Transformer<Number, Calendar>() {
                    @Override
                    public Calendar transform(final Number input) {
                        final Calendar now = Calendar.getInstance();
                        now.setTime(new Date(input.longValue()));
                        return now;
                    }
                });
        registerConverter(Number.class, Boolean.class,
                new Transformer<Number, Boolean>() {
                    @Override
                    public Boolean transform(final Number input) {
                        if(input instanceof BigInteger) return !BigInteger.ZERO.equals(input);
                        else if(input instanceof BigDecimal) return !BigDecimal.ZERO.equals(input);
                        else return input.longValue() != 0;
                    }
                });
        registerConverter(Calendar.class, Long.class,
                new Transformer<Calendar, Long>() {
                    @Override
                    public Long transform(final Calendar input) {
                        return input.getTime().getTime();
                    }
                });
        registerConverter(Calendar.class, Date.class,
                new Transformer<Calendar, Date>() {
                    @Override
                    public Date transform(final Calendar input) {
                        return input.getTime();
                    }
                });
        registerConverter(Date.class, Calendar.class,
                new Transformer<Date, Calendar>() {
                    @Override
                    public Calendar transform(final Date input) {
                        final Calendar now = Calendar.getInstance();
                        now.setTime(input);
                        return now;
                    }
                });
        registerConverter(Boolean.class, Byte.class,
                new Transformer<Boolean, Byte>() {
                    @Override
                    public Byte transform(final Boolean input) {
                        return input ? (byte)1 : 0;
                    }
                });
        registerConverter(Boolean.class, Short.class,
                new Transformer<Boolean, Short>() {
                    @Override
                    public Short transform(final Boolean input) {
                        return input ? (short)1 : 0;
                    }
                });
        registerConverter(Boolean.class, Integer.class,
                new Transformer<Boolean, Integer>() {
                    @Override
                    public Integer transform(final Boolean input) {
                        return input ? 1 : 0;
                    }
                });
        registerConverter(Boolean.class, Long.class,
                new Transformer<Boolean, Long>() {
                    @Override
                    public Long transform(final Boolean input) {
                        return input ? 1L : 0L;
                    }
                });
        registerConverter(Boolean.class, BigInteger.class,
                new Transformer<Boolean, BigInteger>() {
                    @Override
                    public BigInteger transform(final Boolean input) {
                        return input ? BigInteger.ONE : BigInteger.ZERO;
                    }
                });
        registerConverter(Boolean.class, BigDecimal.class,
                new Transformer<Boolean, BigDecimal>() {
                    @Override
                    public BigDecimal transform(final Boolean input) {
                        return input ? BigDecimal.ONE : BigDecimal.ZERO;
                    }
                });
        registerConverter(String.class, Character.class,
                new Transformer<String, Character>() {
                    @Override
                    public Character transform(final String input) {
                        return input.isEmpty() ? '\0' : input.charAt(0);
                    }
                });
        registerConverter(Date.class, Long.class,
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
    public final static boolean supportsInt8(final ManagedEntityType entityType){
        return supportsProjection(entityType, Byte.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Byte}.
     * @return A new type converter.
     */
    public final ManagedEntityType createInt8Type(){
        return createEntitySimpleType(Byte.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Short}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Short}.
     */
    public final static boolean supportsInt16(final ManagedEntityType entityType){
        return supportsProjection(entityType, Short.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Short}.
     * @return A new type converter.
     */
    public final ManagedEntityType createInt16Type(){
        return createEntitySimpleType(Short.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Integer}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Integer}.
     */
    public final static boolean supportsInt32(final ManagedEntityType entityType){
        return supportsProjection(entityType, Integer.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Integer}.
     * @return A new type converter.
     */
    public final ManagedEntityType createInt32Type(){
        return createEntitySimpleType(Integer.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Long}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Long}.
     */
    public final static boolean supportsInt64(final ManagedEntityType entityType){
        return supportsProjection(entityType, Long.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Long}.
     * @return A new type converter.
     */
    public final ManagedEntityType createInt64Type(){
        return createEntitySimpleType(Long.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link BigInteger}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link BigInteger}.
     */
    public final static boolean supportsInteger(final ManagedEntityType entityType){
        return supportsProjection(entityType, BigInteger.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link BigInteger}.
     * @return A new type converter.
     */
    public final ManagedEntityType createIntegerType(){
        return createEntitySimpleType(BigInteger.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link BigDecimal}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link BigDecimal}.
     */
    public final static boolean supportsDecimal(final ManagedEntityType entityType){
        return supportsProjection(entityType, BigDecimal.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link BigDecimal}.
     * @return A new type converter.
     */
    public final ManagedEntityType createDecimalType(){
        return createEntitySimpleType(BigDecimal.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Date}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Date}.
     */
    public final static boolean supportsUnixTime(final ManagedEntityType entityType){
        return supportsProjection(entityType, Date.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Date}.
     * @return A new type converter.
     */
    public final ManagedEntityType createUnixTimeType(){
        return createEntitySimpleType(Date.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Boolean}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Boolean}.
     */
    public final static boolean supportsBoolean(final ManagedEntityType entityType){
        return supportsProjection(entityType, Boolean.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Boolean}.
     * @return A new type converter.
     */
    public final ManagedEntityType createBooleanType(){
        return createEntitySimpleType(Boolean.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link String}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link String}.
     */
    public final static boolean supportsString(final ManagedEntityType entityType){
        return supportsProjection(entityType, String.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link String}.
     * @return A new type converter.
     */
    public final ManagedEntityType createStringType(){
        return createEntitySimpleType(String.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Float}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Float}.
     */
    public final static boolean supportsFloat(final ManagedEntityType entityType){
        return supportsProjection(entityType, Float.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Float}.
     * @return A new type converter.
     */
    public final ManagedEntityType createFloatType(){
        return createEntitySimpleType(Float.class);
    }

    /**
     * Determines whether the specified management entity can be converted
     * into {@link Double}.
     * @param entityType The management entity type.
     * @return {@literal true}, if the specified management entity can be converted
     * into {@link Double}.
     */
    public final static boolean supportsDouble(final ManagedEntityType entityType){
        return supportsProjection(entityType, Double.class);
    }

    /**
     * Constructs a new type converter for the management entity which value is represented
     * by {@link Double}.
     * @return A new type converter.
     */
    public final ManagedEntityType createDoubleType(){
        return createEntitySimpleType(Double.class);
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
        }, Object[].class);
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
        }, Table.class);
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
                    public final long getRowCount() {
                        return rowCount;
                    }
                };
            }
        }, Table.class);
    }
}
