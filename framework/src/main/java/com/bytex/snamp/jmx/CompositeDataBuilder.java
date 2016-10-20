package com.bytex.snamp.jmx;

import javax.management.ObjectName;
import javax.management.openmbean.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.function.Supplier;
import static com.bytex.snamp.internal.Utils.convertTo;

/**
 * Represents builder of {@link javax.management.openmbean.CompositeData} instance.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 * @see javax.management.openmbean.CompositeData
 * @see javax.management.openmbean.CompositeDataSupport
 */
public class CompositeDataBuilder extends LinkedHashMap<String, Object> implements Supplier<CompositeData> {
    private static final long serialVersionUID = 4339347653114240740L;
    private final CompositeTypeBuilder typeBuilder;

    /**
     * Initializes a new empty builder with the specified composite type name and description.
     * @param typeName The name of the composite type. Cannot be {@literal null}.
     * @param typeDescription The description of the composite type. Cannot be {@literal null}.
     */
    public CompositeDataBuilder(final String typeName, final String typeDescription){
        super(CompositeTypeBuilder.DEFAULT_CAPACITY);
        typeBuilder = new CompositeTypeBuilder(typeName, typeDescription);
    }

    /**
     * Initializes a new empty builder.
     */
    public CompositeDataBuilder(){
        this("", "");
    }

    /**
     * Gets name of the composite type.
     * @return The name of the composite type.
     */
    public final String getTypeName(){
        return typeBuilder.getTypeName();
    }

    /**
     * Sets composite type name.
     * @param value The name of the composite type. Cannot be {@literal null} or empty.
     * @return This builder.
     */
    public CompositeDataBuilder setTypeName(final String value){
        typeBuilder.setTypeName(value);
        return this;
    }

    /**
     * Gets description of the composite type.
     * @return The description of the composite type.
     */
    public final String getTypeDescription(){
        return typeBuilder.getDescription();
    }

    /**
     * Sets description of the composite type.
     * @param value The description of the composite type. Cannot be {@literal null} or empty.
     * @return This builder.
     */
    public CompositeDataBuilder setTypeDescription(final String value){
        typeBuilder.setDescription(value);
        return this;
    }


    /**
     * Removes an item from this builder.
     * @param itemName The name of the item to remove.
     * @return The item value.
     */
    @Override
    public Object remove(final Object itemName) {
        return convertTo(itemName, String.class, this::remove);
    }

    /**
     * Removes an item from this builder.
     * @param itemName The name of the item to remove.
     * @return The item value.
     */
    public Object remove(final String itemName){
        typeBuilder.removeItem(itemName);
        return super.remove(itemName);
    }

    /**
     * Removes all items from this builder.
     */
    @Override
    public void clear() {
        typeBuilder.clear();
        super.clear();
    }

    /**
     * Puts a new item into the composite data.
     * @param itemName The name of the item. Cannot be {@literal null} or empty.
     * @param itemDescription The description of the item. Cannot be {@literal null} or empty.
     * @param itemType The type of the item.
     * @param value The value of the item.
     * @param <T> The type of the item.
     * @return This builder.
     */
    public <T> CompositeDataBuilder put(final String itemName,
                    final String itemDescription,
                    final OpenType<T> itemType,
                    final T value){
        typeBuilder.addItem(itemName, itemDescription, itemType);
        put(itemName, value);
        return this;
    }

    /**
     * Puts a new item of type {@link javax.management.openmbean.SimpleType#STRING}
     * into the composite data.
     * @param itemName The name of the item. Cannot be {@literal null} or empty.
     * @param itemDescription The description of the item. Cannot be {@literal null} or empty.
     * @param value The value of the item.
     * @return This builder.
     */
    public final CompositeDataBuilder put(final String itemName,
                              final String itemDescription,
                              final String value){
        return put(itemName, itemDescription, SimpleType.STRING, value);
    }

    /**
     * Puts a new item of type {@link javax.management.openmbean.SimpleType#BYTE}
     * into the composite data.
     * @param itemName The name of the item. Cannot be {@literal null} or empty.
     * @param itemDescription The description of the item. Cannot be {@literal null} or empty.
     * @param value The value of the item.
     * @return This builder.
     */
    public final CompositeDataBuilder put(final String itemName,
                          final String itemDescription,
                          final byte value){
        return put(itemName, itemDescription, SimpleType.BYTE, value);
    }

    /**
     * Puts a new item of type {@link javax.management.openmbean.SimpleType#CHARACTER}
     * into the composite data.
     * @param itemName The name of the item. Cannot be {@literal null} or empty.
     * @param itemDescription The description of the item. Cannot be {@literal null} or empty.
     * @param value The value of the item.
     * @return This builder.
     */
    public final CompositeDataBuilder put(final String itemName,
                        final String itemDescription,
                        final char value){
        return put(itemName, itemDescription, SimpleType.CHARACTER, value);
    }

    /**
     * Puts a new item of type {@link javax.management.openmbean.SimpleType#SHORT}
     * into the composite data.
     * @param itemName The name of the item. Cannot be {@literal null} or empty.
     * @param itemDescription The description of the item. Cannot be {@literal null} or empty.
     * @param value The value of the item.
     * @return This builder.
     */
    public final CompositeDataBuilder put(final String itemName,
                        final String itemDescription,
                        final short value){
        return put(itemName, itemDescription, SimpleType.SHORT, value);
    }

    /**
     * Puts a new item of type {@link javax.management.openmbean.SimpleType#INTEGER}
     * into the composite data.
     * @param itemName The name of the item. Cannot be {@literal null} or empty.
     * @param itemDescription The description of the item. Cannot be {@literal null} or empty.
     * @param value The value of the item.
     * @return This builder.
     */
    public final CompositeDataBuilder put(final String itemName,
                         final String itemDescription,
                         final int value){
        return put(itemName, itemDescription, SimpleType.INTEGER, value);
    }

    /**
     * Puts a new item of type {@link javax.management.openmbean.SimpleType#LONG}
     * into the composite data.
     * @param itemName The name of the item. Cannot be {@literal null} or empty.
     * @param itemDescription The description of the item. Cannot be {@literal null} or empty.
     * @param value The value of the item.
     * @return This builder.
     */
    public final CompositeDataBuilder put(final String itemName,
                       final String itemDescription,
                       final long value){
        return put(itemName, itemDescription, SimpleType.LONG, value);
    }

    /**
     * Puts a new item of type {@link javax.management.openmbean.SimpleType#FLOAT}
     * into the composite data.
     * @param itemName The name of the item. Cannot be {@literal null} or empty.
     * @param itemDescription The description of the item. Cannot be {@literal null} or empty.
     * @param value The value of the item.
     * @return This builder.
     */
    public final CompositeDataBuilder put(final String itemName,
                        final String itemDescription,
                        final float value){
        return put(itemName, itemDescription, SimpleType.FLOAT, value);
    }

    /**
     * Puts a new item of type {@link javax.management.openmbean.SimpleType#DOUBLE}
     * into the composite data.
     * @param itemName The name of the item. Cannot be {@literal null} or empty.
     * @param itemDescription The description of the item. Cannot be {@literal null} or empty.
     * @param value The value of the item.
     * @return This builder.
     */
    public final CompositeDataBuilder put(final String itemName,
                         final String itemDescription,
                         final double value){
        return put(itemName, itemDescription, SimpleType.DOUBLE, value);
    }

    /**
     * Puts a new item of type {@link javax.management.openmbean.SimpleType#BOOLEAN}
     * into the composite data.
     * @param itemName The name of the item. Cannot be {@literal null} or empty.
     * @param itemDescription The description of the item. Cannot be {@literal null} or empty.
     * @param value The value of the item.
     * @return This builder.
     */
    public final CompositeDataBuilder put(final String itemName,
                         final String itemDescription,
                         final boolean value){
        return put(itemName, itemDescription, SimpleType.BOOLEAN, value);
    }

    /**
     * Puts a new item of type {@link javax.management.openmbean.SimpleType#DATE}
     * into the composite data.
     * @param itemName The name of the item. Cannot be {@literal null} or empty.
     * @param itemDescription The description of the item. Cannot be {@literal null} or empty.
     * @param value The value of the item.
     * @return This builder.
     */
    public final CompositeDataBuilder put(final String itemName,
                        final String itemDescription,
                        final Date value){
        return put(itemName, itemDescription, SimpleType.DATE, value);
    }

    /**
     * Puts a new item of type {@link javax.management.openmbean.SimpleType#BIGINTEGER}
     * into the composite data.
     * @param itemName The name of the item. Cannot be {@literal null} or empty.
     * @param itemDescription The description of the item. Cannot be {@literal null} or empty.
     * @param value The value of the item.
     * @return This builder.
     */
    public final CompositeDataBuilder put(final String itemName,
                        final String itemDescription,
                        final BigInteger value){
        return put(itemName, itemDescription, SimpleType.BIGINTEGER, value);
    }

    /**
     * Puts a new item of type {@link javax.management.openmbean.SimpleType#BIGDECIMAL}
     * into the composite data.
     * @param itemName The name of the item. Cannot be {@literal null} or empty.
     * @param itemDescription The description of the item. Cannot be {@literal null} or empty.
     * @param value The value of the item.
     * @return This builder.
     */
    public final CompositeDataBuilder put(final String itemName,
                        final String itemDescription,
                        final BigDecimal value){
        return put(itemName, itemDescription, SimpleType.BIGDECIMAL, value);
    }

    public final CompositeDataBuilder put(final String itemName,
                                                       final String itemDescription,
                                                       final CompositeData dict) {
        return put(itemName, itemDescription, dict.getCompositeType(), dict);
    }

    public final CompositeDataBuilder put(final String itemName,
                                          final String itemDescription,
                                          final ObjectName value){
        return put(itemName, itemDescription, SimpleType.OBJECTNAME, value);
    }

    public final CompositeDataBuilder put(final String itemName,
                                          final String itemDescription,
                                          final TabularData table){
        return put(itemName, itemDescription, table.getTabularType(), table);
    }

    /**
     * Constructs a new JMX Composite Data.
     * @return A new JMX Composite Data.
     * @throws javax.management.openmbean.OpenDataException Unable to construct composite data.
     */
    public final CompositeData build() throws OpenDataException{
        return typeBuilder.build(this);
    }

    /**
     * Constructs a new JMX Composite Data.
     * @return A new JMX Composite Data.
     * @throws java.lang.IllegalStateException Unable to construct composite data.
     */
    @Override
    public final CompositeData get() throws IllegalStateException{
        try {
            return build();
        } catch (final OpenDataException e) {
            throw new IllegalStateException(e);
        }
    }
}
