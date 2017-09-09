package com.bytex.snamp.cluster;

import com.google.common.collect.ImmutableSortedSet;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.index.OCompositeKey;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

import javax.annotation.concurrent.Immutable;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.bytex.snamp.Convert.TypeConverter;

/**
 * Represents key definition in key/value database.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.1
 */
@Immutable
final class RecordKey implements Serializable, Supplier<Comparable<?>> {
    private static final long serialVersionUID = 5779949763828705959L;

    @FunctionalInterface
    private interface RecordKeyFactory<I extends Comparable<I>> extends Function<I, RecordKey> {
        @Override
        RecordKey apply(final I value);
    }

    private static final class RecordKeyConverter extends TypeConverter<RecordKeyFactory>{

        <I extends Comparable<I>> RecordKeyConverter addConverter(final Class<I> indexType,
                                                                  final RecordKeyFactory<? super I> converter){
            super.addConverter(indexType, converter);
            return this;
        }
    }

    private enum KeyField{
        N_KEY(OType.DECIMAL, "Decimal key"),
        S_KEY(OType.STRING, "String key"),
        D_KEY(OType.DATETIME, "Date/time key");

        private final OType fieldType;
        private final String description;

        KeyField(final OType type, final String descr){
            fieldType = type;
            description = descr;
        }

        final void defineField(final OClass documentClass){
            documentClass.createProperty(name(), fieldType)
                .setNotNull(true)
                .setDescription(description);
        }

        final <T> T getField(final ODocument document){
            return document.field(name());
        }

        static final ImmutableSortedSet<KeyField> ALL = ImmutableSortedSet.copyOf(values());
    }

    private static final RecordKeyConverter CONVERTER = new RecordKeyConverter()
            .addConverter(BigDecimal.class, RecordKey::new)
            .addConverter(BigInteger.class, RecordKey::new)
            .addConverter(Byte.class, value -> new RecordKey(value.longValue()))
            .addConverter(Short.class, value -> new RecordKey(value.longValue()))
            .addConverter(Integer.class, value -> new RecordKey(value.longValue()))
            .addConverter(Long.class, value -> new RecordKey(value.longValue()))
            .addConverter(Float.class, value -> new RecordKey(value.doubleValue()))
            .addConverter(Double.class, value -> new RecordKey(value.doubleValue()))
            .addConverter(String.class, RecordKey::new)
            .addConverter(Date.class, RecordKey::new)
            .addConverter(Instant.class, RecordKey::new)
            .addConverter(UUID.class, RecordKey::new)
            .addConverter(Character.class, RecordKey::new);

    private final Comparable<?> keyValue;
    private final KeyField keyName;

    RecordKey(final ODocument document) {
        Comparable<?> keyValue = null;
        KeyField keyName = null;
        for (final KeyField field : KeyField.ALL) {
            keyValue = field.getField(document);
            if (keyValue != null) {
                keyName = field;
                break;
            }
        }
        if (keyName == null)
            throw new IllegalArgumentException(String.format("Document %s has no keys", document));
        else {
            this.keyName = keyName;
            this.keyValue = keyValue;
        }
    }

    private RecordKey(final BigDecimal key){
        keyName = KeyField.N_KEY;
        keyValue = Objects.requireNonNull(key);
    }

    private RecordKey(final BigInteger key){
        this(new BigDecimal(key));
    }

    private RecordKey(final long key){
        this(new BigDecimal(key));
    }

    private RecordKey(final double key){
        this(new BigDecimal(key));
    }

    private RecordKey(final char key) {
        this((long) key);
    }

    private RecordKey(final String key){
        keyName = KeyField.S_KEY;
        keyValue = Objects.requireNonNull(key);
    }

    private RecordKey(final UUID key){
        this(key.toString());
    }

    private RecordKey(final Date key){
        keyName = KeyField.D_KEY;
        keyValue = key;
    }

    private RecordKey(final Instant key) {
        this(Date.from(key));
    }

    @SuppressWarnings("unchecked")
    static RecordKey create(final Comparable<?> key) {
        final RecordKeyFactory factory = CONVERTER.getConverter(key);
        if (factory == null)
            throw new IllegalArgumentException(String.format("Key %s has unsupported type %s", key, key.getClass()));
        else
            return factory.apply(key);
    }

    void setKey(final ODocument document) {
        document.field(keyName.name(), keyValue);
    }

    /**
     * Gets underlying key.
     *
     * @return Key value.
     */
    @Override
    public Comparable<?> get() {
        return keyValue;
    }

    OIdentifiable getRecordFromIndex(final OIndex<?> index) {
        final OCompositeKey key = new OCompositeKey();
        for (final KeyField field : KeyField.ALL)
            key.addKey(keyName.equals(field) ? keyValue : null);
        return (OIdentifiable) index.get(key);
    }

    static OIndex<?> defineIndex(final OClass documentClass, final String indexName) {
        //order of fields is significant!!!
        final String[] names = KeyField.ALL.stream().map(Enum::name).toArray(String[]::new);
        final OIndex<?> index = documentClass
                .createIndex(indexName, OClass.INDEX_TYPE.UNIQUE_HASH_INDEX, names);
        index.flush();
        return index;
    }

    static void defineFields(final OClass documentClass) {
        for (final KeyField field : KeyField.ALL)
            if (!documentClass.existsProperty(field.name()))
                field.defineField(documentClass);
    }

    @Override
    public String toString() {
        return keyName.name() + '=' + keyValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyName, keyValue);
    }

    private boolean equals(final RecordKey other) {
        return Objects.equals(keyName, other.keyName) &&
                Objects.equals(keyValue, other.keyValue);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof RecordKey && equals((RecordKey) other);
    }
}
