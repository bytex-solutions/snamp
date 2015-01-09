package com.itworks.jcommands.impl;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

/**
 * Represents SNAMP-compliant return type of the command-line tool.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@XmlType(name = "CommandLineToolReturnType", namespace = XmlConstants.NAMESPACE)
@XmlEnum
public enum XmlParsingResultType {

    /**
     * Represents boolean value.
     * <p>
     *     Possible string values are 0, 1, true, false, yes, no.
     */
    @XmlEnumValue("boolean")
    BOOLEAN(true, TypeToken.of(Boolean.class)),

    /**
     * Represents string value.
     */
    @XmlEnumValue("string")
    STRING(true, TypeToken.of(String.class)),

    /**
     * Represents single character.
     */
    @XmlEnumValue("char")
    CHARACTER(true, TypeToken.of(Character.class)),

    /**
     * Represents an array of bytes.
     * <p>
     *     This type converts hex string into the array of bytes.
     */
    @XmlEnumValue("blob")
    BLOB(false, TypeToken.of(Byte[].class)),

    @XmlEnumValue("8bit")
    BYTE(true, TypeToken.of(Byte.class)),

    @XmlEnumValue("16bit")
    SHORT(true, TypeToken.of(Short.class)),

    @XmlEnumValue("32bit")
    INTEGER(true, TypeToken.of(Integer.class)),

    @XmlEnumValue("64bit")
    LONG(true, TypeToken.of(Long.class)),

    @XmlEnumValue("integer")
    BIG_INTEGER(true, TypeToken.of(BigInteger.class)),

    @XmlEnumValue("decimal")
    BIG_DECIMAL(true, TypeToken.of(BigDecimal.class)),

    @XmlEnumValue("dictionary")
    DICTIONARY(false, TypeTokens.DICTIONARY_TYPE_TOKEN),

    @XmlEnumValue("array")
    ARRAY(false, TypeToken.of(Object[].class)),

    @XmlEnumValue("table")
    TABLE(false, TypeTokens.TABLE_TYPE_TOKEN),

    @XmlEnumValue("date")
    DATE_TIME(true, TypeToken.of(Date.class)),

    @XmlEnumValue("float")
    FLOAT(true, TypeToken.of(Float.class)),

    @XmlEnumValue("double")
    DOUBLE(true, TypeToken.of(Double.class));

    /**
     * Determines whether this type is scalar.
     */
    public final boolean isScalar;

    /**
     * Represents underying SNAMP-compliant type.
     */
    public final TypeToken<?> underlyingType;

    private XmlParsingResultType(final boolean scalar,
                                 final TypeToken<?> type){
        this.isScalar = scalar;
        this.underlyingType = type;
    }

    /**
     * Gets all scalar types.
     * @return The all scalar types.
     */
    public static Set<XmlParsingResultType> getScalarTypes() {
        return Sets.filter(EnumSet.allOf(XmlParsingResultType.class), new Predicate<XmlParsingResultType>() {
            @Override
            public boolean apply(final XmlParsingResultType input) {
                return input.isScalar;
            }
        });
    }
}
