package com.bytex.snamp.jmx;

import javax.management.openmbean.*;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static com.bytex.snamp.ArrayUtils.emptyArray;

/**
 * Represents builder of {@link javax.management.openmbean.CompositeType} instances.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 * @see javax.management.openmbean.CompositeType
 */
public final class CompositeTypeBuilder implements OpenTypeBuilder<CompositeType>, Iterable<String>, Serializable {
    final static int DEFAULT_CAPACITY = 10;
    private static final long serialVersionUID = 392284311930753280L;

    private String typeName;
    private String typeDescription;
    private final LinkedHashMap<String, CompositeTypeItem> items;

    private CompositeTypeBuilder(final String typeName,
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

    /**
     * Imports items from the specified composite type.
     * @param type A source of import.
     * @return This builder.
     * @since 2.0
     */
    public CompositeTypeBuilder importFrom(final CompositeType type) {
        for (final String itemName : type.keySet())
            addItem(itemName, type.getDescription(itemName), type.getType(itemName));
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
    public CompositeTypeBuilder setDescription(final String value){
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
    @Override
    public CompositeType build() throws OpenDataException {
        return build(typeName,
                typeDescription,
                items);
    }

    private static CompositeType build(final String typeName,
                                       final String typeDescription,
                                       final Map<String, ? extends CompositeTypeItem> items) throws OpenDataException {
        final String[] itemNames = items.keySet().toArray(emptyArray(String[].class));
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
     * @param items The mappings of all the item names to their values
     * @return A new instance of composite data.
     * @throws OpenDataException Invalid items.
     */
    public CompositeData build(final Map<String, ?> items) throws OpenDataException {
        return new CompositeDataSupport(build(), items);
    }

    int size(){
        return items.size();
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
