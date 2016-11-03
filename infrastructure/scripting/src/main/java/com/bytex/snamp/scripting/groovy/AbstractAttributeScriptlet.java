package com.bytex.snamp.scripting.groovy;

import com.bytex.snamp.SpecialUse;

import javax.management.openmbean.*;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Represents base implementation of {@link AttributeScriptlet}.
 */
public abstract class AbstractAttributeScriptlet extends Scriptlet implements AttributeScriptlet {
    private static final String GET_VALUE_METHOD = "getValue";
    private static final String SET_VALUE_METHOD = "setValue";

    protected OpenType<?> openType = STRING;

    /**
     * Sets type of this attribute.
     *
     * @param value The type of this attribute
     */
    @SpecialUse
    public final void type(final OpenType<?> value) {
        this.openType = Objects.requireNonNull(value);
    }

    @Override
    public final CompositeData asDictionary(final Map<String, ?> items) throws OpenDataException {
        if (openType instanceof CompositeType)
            return AttributeScriptlet.asDictionary((CompositeType) openType, items);
        else throw new OpenDataException(String.format("Expected dictionary type but '%s' found", openType));
    }

    @Override
    public final TabularData asTable(final Collection<Map<String, ?>> rows) throws OpenDataException {
        if (openType instanceof TabularType)
            return AttributeScriptlet.asTable((TabularType) openType, rows);
        else throw new OpenDataException(String.format("Expected dictionary type but '%s' found", openType));
    }

    /**
     * Gets value of this attribute.
     *
     * @return The value of this attribute.
     * @throws Exception Unable to get attribute value.
     */
    @SpecialUse
    public Object getValue() throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets value of this attribute.
     *
     * @param value The value of this attribute.
     * @return A new attribute value.
     * @throws Exception Unable to set attribute value.
     */
    @SpecialUse
    public Object setValue(final Object value) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Determines whether this attribute is readable.
     *
     * @return {@literal true}, if this method is readable.
     */
    @SpecialUse
    public final boolean isReadable() {
        try {
            final Method getter = getClass().getMethod(GET_VALUE_METHOD);
            return Objects.equals(getter.getDeclaringClass(), getClass());
        } catch (final NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Determines whether this attribute is writable.
     *
     * @return {@literal true}, if this method is writable.
     */
    @SpecialUse
    public final boolean isWritable() {
        try {
            final Method getter = getClass().getMethod(SET_VALUE_METHOD, Object.class);
            return Objects.equals(getter.getDeclaringClass(), getClass());
        } catch (final ReflectiveOperationException e) {
            return false;
        }
    }

    /**
     * Gets type of this attribute.
     *
     * @return The type of this attribute.
     */
    @Override
    public final OpenType<?> type() {
        return openType;
    }
}
