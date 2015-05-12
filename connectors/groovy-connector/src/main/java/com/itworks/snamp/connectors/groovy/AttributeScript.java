package com.itworks.snamp.connectors.groovy;

import com.itworks.snamp.connectors.attributes.AttributeSpecifier;
import com.itworks.snamp.internal.annotations.SpecialUse;

import javax.management.ObjectName;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/**
 * Represents an abstract class for attribute handling script
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AttributeScript extends ResourceFeatureScript {
    private static final String GET_VALUE_METHOD = "getValue";
    private static final String SET_VALUE_METHOD = "setValue";
    @SpecialUse
    protected static final SimpleType<Byte> INT8 = SimpleType.BYTE;
    @SpecialUse
    protected static final SimpleType<Short> INT16 = SimpleType.SHORT;
    @SpecialUse
    protected static final SimpleType<Integer> INT32 = SimpleType.INTEGER;
    @SpecialUse
    protected static final SimpleType<Long> INT64 = SimpleType.LONG;
    @SpecialUse
    protected static final SimpleType<Float> FLOAT32 = SimpleType.FLOAT;
    @SpecialUse
    protected static final SimpleType<Double> FLOAT64 = SimpleType.DOUBLE;
    @SpecialUse
    protected static final SimpleType<String> STRING = SimpleType.STRING;
    @SpecialUse
    protected static final SimpleType<Boolean> BOOL = SimpleType.BOOLEAN;
    @SpecialUse
    protected static final SimpleType<ObjectName> OBJECTNAME = SimpleType.OBJECTNAME;
    @SpecialUse
    protected static final SimpleType<BigInteger> BIGINT = SimpleType.BIGINTEGER;
    @SpecialUse
    protected static final SimpleType<BigDecimal> BIGDECIMAL = SimpleType.BIGDECIMAL;
    @SpecialUse
    protected static final SimpleType<Character> CHAR = SimpleType.CHARACTER;

    private OpenType<?> openType = STRING;

    //<editor-fold desc="Script helpers">

    /**
     * Sets type of this attribute.
     * @param value The type of this attribute
     */
    @SpecialUse
    protected final void type(final OpenType<?> value){
        this.openType = Objects.requireNonNull(value);
    }

    /**
     * Gets value of this attribute.
     * @return The value of this attribute.
     * @throws Exception Unable to get attribute value.
     */
    @SpecialUse
    public Object getValue() throws Exception{
        throw new UnsupportedOperationException();
    }

    /**
     * Sets value of this attribute.
     * @param value The value of this attribute.
     * @return A new attribute value.
     * @throws Exception Unable to set attribute value.
     */
    @SpecialUse
    public Object setValue(final Object value) throws Exception{
        throw new UnsupportedOperationException();
    }

    /**
     * Determines whether this attribute is readable.
     * @return {@literal true}, if this method is readable.
     */
    @SpecialUse
    public final boolean isReadable(){
        try {
            final Method getter = getClass().getMethod(GET_VALUE_METHOD);
            return Objects.equals(getter.getDeclaringClass(), getClass());
        }
        catch (final NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Determines whether this attribute is writable.
     * @return {@literal true}, if this method is writable.
     */
    @SpecialUse
    public final boolean isWritable(){
        try {
            final Method getter = getClass().getMethod(SET_VALUE_METHOD, Object.class);
            return Objects.equals(getter.getDeclaringClass(), getClass());
        }
        catch (final ReflectiveOperationException e) {
            return false;
        }
    }

    /**
     * Releases all resources associated with this attribute.
     * @throws Exception Releases all resources associated with this attribute.
     */
    @Override
    @SpecialUse
    public void close() throws Exception {
        openType = null;
    }

    //</editor-fold>

    //<editor-fold desc="Internal operations">

    /**
     * Gets type of this attribute.
     * @return The type of this attribute.
     */
    public final OpenType<?> type(){
        return openType;
    }

    public final AttributeSpecifier specifier() {
        return AttributeSpecifier
                .NOT_ACCESSIBLE
                .writable(isWritable())
                .readable(isReadable());
    }

    //</editor-fold>
}
