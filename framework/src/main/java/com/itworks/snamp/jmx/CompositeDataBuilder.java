package com.itworks.snamp.jmx;

import com.google.common.base.Supplier;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Represents builder of {@link javax.management.openmbean.CompositeData} instance.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 * @see javax.management.openmbean.CompositeData
 * @see javax.management.openmbean.CompositeDataSupport
 */
public class CompositeDataBuilder extends LinkedHashMap<String, Object> implements Supplier<CompositeData> {
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
        return itemName instanceof String ? remove((String)itemName) : null;
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

    private CompositeDataBuilder putImpl(final String itemName,
                                         final Object itemValue){
        put(itemName, itemValue);
        return this;
    }

    /**
     * Puts a new item into the composite data.
     * @param itemName The name of the item. Cannot be {@literal null} or empty.
     * @param itemDescription The description of the item. Cannot be {@literal null} or empty.
     * @param itemType The type of the item.
     * @param value The value of the item.
     * @return This builder.
     */
    public CompositeDataBuilder put(final String itemName,
                    final String itemDescription,
                    final OpenType<?> itemType,
                    final Object value){
        typeBuilder.addItem(itemName, itemDescription, itemType);
        return putImpl(itemName, value);
    }

    /**
     * Puts a new item of type {@link javax.management.openmbean.SimpleType#STRING}
     * into the composite data.
     * @param itemName The name of the item. Cannot be {@literal null} or empty.
     * @param itemDescription The description of the item. Cannot be {@literal null} or empty.
     * @param value The value of the item.
     * @return This builder.
     */
    public final CompositeDataBuilder putString(final String itemName,
                              final String itemDescription,
                              final String value){
        typeBuilder.addStringItem(itemName, itemDescription);
        return putImpl(itemName, value);
    }

    /**
     * Puts a new item of type {@link javax.management.openmbean.SimpleType#BYTE}
     * into the composite data.
     * @param itemName The name of the item. Cannot be {@literal null} or empty.
     * @param itemDescription The description of the item. Cannot be {@literal null} or empty.
     * @param value The value of the item.
     * @return This builder.
     */
    public final CompositeDataBuilder putByte(final String itemName,
                          final String itemDescription,
                          final byte value){
        typeBuilder.addByteItem(itemName, itemDescription);
        return putImpl(itemName, value);
    }

    /**
     * Puts a new item of type {@link javax.management.openmbean.SimpleType#CHARACTER}
     * into the composite data.
     * @param itemName The name of the item. Cannot be {@literal null} or empty.
     * @param itemDescription The description of the item. Cannot be {@literal null} or empty.
     * @param value The value of the item.
     * @return This builder.
     */
    public final CompositeDataBuilder putChar(final String itemName,
                        final String itemDescription,
                        final char value){
        typeBuilder.addCharItem(itemName, itemDescription);
        return putImpl(itemName, value);
    }

    /**
     * Puts a new item of type {@link javax.management.openmbean.SimpleType#SHORT}
     * into the composite data.
     * @param itemName The name of the item. Cannot be {@literal null} or empty.
     * @param itemDescription The description of the item. Cannot be {@literal null} or empty.
     * @param value The value of the item.
     * @return This builder.
     */
    public final CompositeDataBuilder putShort(final String itemName,
                        final String itemDescription,
                        final short value){
        typeBuilder.addShortItem(itemName, itemDescription);
        return putImpl(itemName, value);
    }

    /**
     * Puts a new item of type {@link javax.management.openmbean.SimpleType#INTEGER}
     * into the composite data.
     * @param itemName The name of the item. Cannot be {@literal null} or empty.
     * @param itemDescription The description of the item. Cannot be {@literal null} or empty.
     * @param value The value of the item.
     * @return This builder.
     */
    public final CompositeDataBuilder putInt(final String itemName,
                         final String itemDescription,
                         final int value){
        typeBuilder.addIntItem(itemName, itemDescription);
        return putImpl(itemName, value);
    }

    /**
     * Puts a new item of type {@link javax.management.openmbean.SimpleType#LONG}
     * into the composite data.
     * @param itemName The name of the item. Cannot be {@literal null} or empty.
     * @param itemDescription The description of the item. Cannot be {@literal null} or empty.
     * @param value The value of the item.
     * @return This builder.
     */
    public final CompositeDataBuilder putLong(final String itemName,
                       final String itemDescription,
                       final long value){
        typeBuilder.addLongItem(itemName, itemDescription);
        return putImpl(itemName, value);
    }

    /**
     * Puts a new item of type {@link javax.management.openmbean.SimpleType#FLOAT}
     * into the composite data.
     * @param itemName The name of the item. Cannot be {@literal null} or empty.
     * @param itemDescription The description of the item. Cannot be {@literal null} or empty.
     * @param value The value of the item.
     * @return This builder.
     */
    public final CompositeDataBuilder putFloat(final String itemName,
                        final String itemDescription,
                        final float value){
        typeBuilder.addFloatItem(itemName, itemDescription);
        return putImpl(itemName, value);
    }

    /**
     * Puts a new item of type {@link javax.management.openmbean.SimpleType#DOUBLE}
     * into the composite data.
     * @param itemName The name of the item. Cannot be {@literal null} or empty.
     * @param itemDescription The description of the item. Cannot be {@literal null} or empty.
     * @param value The value of the item.
     * @return This builder.
     */
    public final CompositeDataBuilder putDouble(final String itemName,
                         final String itemDescription,
                         final double value){
        typeBuilder.addDoubleItem(itemName, itemDescription);
        return putImpl(itemName, value);
    }

    /**
     * Puts a new item of type {@link javax.management.openmbean.SimpleType#BOOLEAN}
     * into the composite data.
     * @param itemName The name of the item. Cannot be {@literal null} or empty.
     * @param itemDescription The description of the item. Cannot be {@literal null} or empty.
     * @param value The value of the item.
     * @return This builder.
     */
    public final CompositeDataBuilder putBool(final String itemName,
                         final String itemDescription,
                         final boolean value){
        typeBuilder.addBoolItem(itemName, itemDescription);
        return putImpl(itemName, value);
    }

    /**
     * Puts a new item of type {@link javax.management.openmbean.SimpleType#DATE}
     * into the composite data.
     * @param itemName The name of the item. Cannot be {@literal null} or empty.
     * @param itemDescription The description of the item. Cannot be {@literal null} or empty.
     * @param value The value of the item.
     * @return This builder.
     */
    public final CompositeDataBuilder putDate(final String itemName,
                        final String itemDescription,
                        final Date value){
        typeBuilder.addDateItem(itemName, itemDescription);
        return putImpl(itemName, value);
    }

    /**
     * Puts a new item of type {@link javax.management.openmbean.SimpleType#BIGINTEGER}
     * into the composite data.
     * @param itemName The name of the item. Cannot be {@literal null} or empty.
     * @param itemDescription The description of the item. Cannot be {@literal null} or empty.
     * @param value The value of the item.
     * @return This builder.
     */
    public final CompositeDataBuilder putBigInt(final String itemName,
                        final String itemDescription,
                        final BigInteger value){
        typeBuilder.addBigIntItem(itemName, itemDescription);
        return putImpl(itemName, value);
    }

    /**
     * Puts a new item of type {@link javax.management.openmbean.SimpleType#BIGDECIMAL}
     * into the composite data.
     * @param itemName The name of the item. Cannot be {@literal null} or empty.
     * @param itemDescription The description of the item. Cannot be {@literal null} or empty.
     * @param value The value of the item.
     * @return This builder.
     */
    public final CompositeDataBuilder putBigDecimal(final String itemName,
                        final String itemDescription,
                        final BigDecimal value){
        typeBuilder.addBigDecimalItem(itemName, itemDescription);
        return putImpl(itemName, value);
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
