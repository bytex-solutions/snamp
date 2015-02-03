package com.itworks.snamp.jmx;

import com.itworks.snamp.ArrayUtils;

import javax.management.openmbean.*;
import java.util.*;

/**
 * Represents builder of {@link javax.management.openmbean.CompositeType} instances.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 * @see javax.management.openmbean.CompositeType
 */
public final class CompositeTypeBuilder implements OpenTypeBuilder<CompositeType>, Iterable<String> {
    final static int DEFAULT_CAPACITY = 10;

    private final class CompositeTypeItem {
        private final String description;
        private final OpenType<?> type;

        private CompositeTypeItem(final OpenType<?> type,
                          final String description){
            this.description = Objects.requireNonNull(description, "description is null.");
            this.type = Objects.requireNonNull(type, "type is null.");
        }
    }

    private String typeName;
    private String typeDescription;
    private final LinkedHashMap<String, CompositeTypeItem> items;

    CompositeTypeBuilder(final String typeName,
                         final String typeDescription,
                         final int capacity){
        this.items = new LinkedHashMap<>(capacity);
        this.typeDescription = Objects.requireNonNull(typeDescription, "typeDescription is null.");
        this.typeName = Objects.requireNonNull(typeName, "typeName is null.");
    }

    public CompositeTypeBuilder(){
        this("", "");

    }

    public CompositeTypeBuilder(final String typeName,
                                final String typeDescription) {
        this(typeName, typeDescription, DEFAULT_CAPACITY);
    }

    /**
     * Sets composite type name.
     * @param value The name of the composite type. Cannot be {@literal null} or empty.
     * @return This builder.
     */
    public CompositeTypeBuilder setTypeName(final String value){
        this.typeName = Objects.requireNonNull(value, "value is null.");
        return this;
    }

    String getTypeName(){
        return typeName;
    }

    /**
     * Sets description of the composite type.
     * @param value The description of the composite type. Cannot be {@literal null} or empty.
     * @return This builder.
     */
    public final CompositeTypeBuilder setDescription(final String value){
        this.typeDescription = Objects.requireNonNull(value, "value is null.");
        return this;
    }

    String getDescription(){
        return typeDescription;
    }

    /**
     * Adds a new item definition.
     * <p>
     *     Inserting items is order-specific.
     * @param name The name of the item. Cannot be {@literal null} or empty.
     * @param description The description of the item. Cannot be {@literal null} or empty.
     * @param type The type of the dictionary item. Cannot be {@literal null}.
     * @return This builder.
     */
    public CompositeTypeBuilder addItem(final String name,
                                        final String description,
                                        final OpenType<?> type){
        this.items.put(name, new CompositeTypeItem(type, description));
        return this;
    }

    CompositeTypeItem removeItem(final String name){
        return items.remove(name);
    }

    void clear(){
        items.clear();
    }

    /**
     * Adds a new item of type {@link javax.management.openmbean.SimpleType#STRING}.
     * @param name The name of the item. Cannot be {@literal null} or empty.
     * @param description The description of the item. Cannot be {@literal null} or empty.
     * @return This builder.
     */
    public final CompositeTypeBuilder addStringItem(final String name,
                                                    final String description){
        return addItem(name, description, SimpleType.STRING);
    }

    /**
     * Adds a new item of type {@link javax.management.openmbean.SimpleType#BOOLEAN}.
     * @param name The name of the item. Cannot be {@literal null} or empty.
     * @param description The description of the item. Cannot be {@literal null} or empty.
     * @return This builder.
     */
    public final CompositeTypeBuilder addBoolItem(final String name,
                                                  final String description){
        return addItem(name, description, SimpleType.BOOLEAN);
    }

    /**
     * Adds a new item of type {@link javax.management.openmbean.SimpleType#BYTE}.
     * @param name The name of the item. Cannot be {@literal null} or empty.
     * @param description The description of the item. Cannot be {@literal null} or empty.
     * @return This builder.
     */
    public final CompositeTypeBuilder addByteItem(final String name,
                                                    final String description){
        return addItem(name, description, SimpleType.BYTE);
    }

    /**
     * Adds a new item of type {@link javax.management.openmbean.SimpleType#CHARACTER}.
     * @param name The name of the item. Cannot be {@literal null} or empty.
     * @param description The description of the item. Cannot be {@literal null} or empty.
     * @return This builder.
     */
    public final CompositeTypeBuilder addCharItem(final String name,
                                                    final String description){
        return addItem(name, description, SimpleType.CHARACTER);
    }

    /**
     * Adds a new item of type {@link javax.management.openmbean.SimpleType#SHORT}.
     * @param name The name of the item. Cannot be {@literal null} or empty.
     * @param description The description of the item. Cannot be {@literal null} or empty.
     * @return This builder.
     */
    public final CompositeTypeBuilder addShortItem(final String name,
                                                    final String description){
        return addItem(name, description, SimpleType.SHORT);
    }

    /**
     * Adds a new item of type {@link javax.management.openmbean.SimpleType#INTEGER}.
     * @param name The name of the item. Cannot be {@literal null} or empty.
     * @param description The description of the item. Cannot be {@literal null} or empty.
     * @return This builder.
     */
    public final CompositeTypeBuilder addIntItem(final String name,
                                                    final String description){
        return addItem(name, description, SimpleType.INTEGER);
    }

    /**
     * Adds a new item of type {@link javax.management.openmbean.SimpleType#LONG}.
     * @param name The name of the item. Cannot be {@literal null} or empty.
     * @param description The description of the item. Cannot be {@literal null} or empty.
     * @return This builder.
     */
    public final CompositeTypeBuilder addLongItem(final String name,
                                                    final String description){
        return addItem(name, description, SimpleType.LONG);
    }

    /**
     * Adds a new item of type {@link javax.management.openmbean.SimpleType#FLOAT}.
     * @param name The name of the item. Cannot be {@literal null} or empty.
     * @param description The description of the item. Cannot be {@literal null} or empty.
     * @return This builder.
     */
    public final CompositeTypeBuilder addFloatItem(final String name,
                                                    final String description){
        return addItem(name, description, SimpleType.FLOAT);
    }

    /**
     * Adds a new item of type {@link javax.management.openmbean.SimpleType#DOUBLE}.
     * @param name The name of the item. Cannot be {@literal null} or empty.
     * @param description The description of the item. Cannot be {@literal null} or empty.
     * @return This builder.
     */
    public final CompositeTypeBuilder addDoubleItem(final String name,
                                                    final String description){
        return addItem(name, description, SimpleType.DOUBLE);
    }

    /**
     * Adds a new item of type {@link javax.management.openmbean.SimpleType#DATE}.
     * @param name The name of the item. Cannot be {@literal null} or empty.
     * @param description The description of the item. Cannot be {@literal null} or empty.
     * @return This builder.
     */
    public final CompositeTypeBuilder addDateItem(final String name,
                                                  final String description){
        return addItem(name, description, SimpleType.DATE);
    }

    /**
     * Adds a new item of type {@link javax.management.openmbean.SimpleType#BIGINTEGER}.
     * @param name The name of the item. Cannot be {@literal null} or empty.
     * @param description The description of the item. Cannot be {@literal null} or empty.
     * @return This builder.
     */
    public final CompositeTypeBuilder addBigIntItem(final String name,
                                                    final String description){
        return addItem(name, description, SimpleType.BIGINTEGER);
    }

    /**
     * Adds a new item of type {@link javax.management.openmbean.SimpleType#BIGDECIMAL}.
     * @param name The name of the item. Cannot be {@literal null} or empty.
     * @param description The description of the item. Cannot be {@literal null} or empty.
     * @return This builder.
     */
    public final CompositeTypeBuilder addBigDecimalItem(final String name,
                                                    final String description){
        return addItem(name, description, SimpleType.BIGDECIMAL);
    }


    /**
     * Constructs a new instance of the {@link javax.management.openmbean.CompositeType} instance.
     * @return A new instance of the {@link javax.management.openmbean.CompositeType} instance.
     * @throws javax.management.openmbean.OpenDataException Invalid composite type.
     */
    public final CompositeType build() throws OpenDataException {
        return build(typeName,
                typeDescription,
                items);
    }

    static CompositeType build(final String typeName,
                               final String typeDescription,
                               final Map<String, ? extends CompositeTypeItem> items) throws OpenDataException {
        final String[] itemNames = ArrayUtils.toArray(items.keySet(), String.class);
        final String[] itemDescriptions = new String[itemNames.length];
        final OpenType<?>[] itemTypes = new OpenType<?>[itemNames.length];
        for (int i = 0; i < itemNames.length; i++) {
            final CompositeTypeItem item = items.get(itemNames[i]);
            itemDescriptions[i] = item.description;
            itemTypes[i] = item.type;
        }
        return new CompositeType(typeName, typeDescription, itemNames, itemDescriptions, itemTypes);
    }

    /**
     * Constructs a new composite data.
     * @param values
     * @return
     * @throws OpenDataException
     */
    public final CompositeData build(final Map<String, ?> values) throws OpenDataException {
        return new CompositeDataSupport(build(), values);
    }

    final int size(){
        return items.size();
    }

    /**
     * Constructs a new instance of the {@link javax.management.openmbean.CompositeType} instance.
     * @return A new instance of the {@link javax.management.openmbean.CompositeType} instance.
     * @throws java.lang.IllegalStateException Invalid composite type.
     */
    @Override
    public final CompositeType get() throws IllegalStateException{
        try {
            return build();
        } catch (final OpenDataException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns an iterator over an item names.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<String> iterator() {
        return items.keySet().iterator();
    }
}
