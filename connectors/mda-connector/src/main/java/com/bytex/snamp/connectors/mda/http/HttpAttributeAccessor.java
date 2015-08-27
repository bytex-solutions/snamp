package com.bytex.snamp.connectors.mda.http;

import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.attributes.AttributeSpecifier;
import com.bytex.snamp.connectors.mda.MdaAttributeAccessor;
import com.google.common.cache.Cache;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import javax.management.InvalidAttributeValueException;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents attribute value storage.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class HttpAttributeAccessor extends MdaAttributeAccessor {
    private final HttpValueParser storageManager;

    HttpAttributeAccessor(final String name,
                          final OpenType<?> type,
                          final AttributeDescriptor descriptor,
                          final HttpValueParser storage) {
        super(name, type, AttributeSpecifier.READ_WRITE, descriptor);
        this.storageManager = Objects.requireNonNull(storage);
    }

    /**
     * Returns the default value for this parameter, if it has one, or
     * <tt>null</tt> otherwise.
     *
     * @return the default value.
     */
    @Override
    public Object getDefaultValue() {
        return storageManager.getDefaultValue();
    }

    static JsonElement setValue(final String storageName,
                           final HttpValueParser parser,
                          final JsonElement value,
                          final Gson formatter,
                          final ConcurrentMap<String, Object> storage) throws OpenDataException, InvalidAttributeValueException {
        return parser.serialize(storage.put(storageName, parser.deserialize(value, formatter)), formatter);
    }

    static JsonElement getValue(final String storageName,
                          final HttpValueParser parser,
                          final Gson formatter,
                          final ConcurrentMap<String, ?> storage) {
        return parser.serialize(storage.get(storageName), formatter);
    }

    static void saveParser(final HttpValueParser parser,
                           final AttributeDescriptor descriptor,
                           final Cache<String, HttpValueParser> parsers) {
        parsers.put(getStorageName(descriptor), parser);
    }
}
