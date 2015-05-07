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
            itemDescriptions[i] = item.getDescription();
            itemTypes[i] = item.getType();
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
