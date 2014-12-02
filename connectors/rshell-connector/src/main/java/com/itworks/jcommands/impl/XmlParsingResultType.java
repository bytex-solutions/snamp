package com.itworks.jcommands.impl;

import com.itworks.snamp.mapping.Table;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.EnumSet;
import java.util.Map;

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
    BOOLEAN(true, Boolean.class),

    /**
     * Represents string value.
     */
    @XmlEnumValue("string")
    STRING(true, String.class),

    /**
     * Represents an array of bytes.
     * <p>
     *     This type converts hex string into the array of bytes.
     */
    @XmlEnumValue("blob")
    BLOB(false, Byte[].class),

    @XmlEnumValue("8bit")
    BYTE(true, Byte.class),

    @XmlEnumValue("16bit")
    SHORT(true, Short.class),

    @XmlEnumValue("32bit")
    INTEGER(true, Integer.class),

    @XmlEnumValue("64bit")
    LONG(true, Long.class),

    @XmlEnumValue("integer")
    BIG_INTEGER(true, BigInteger.class),

    @XmlEnumValue("decimal")
    BIG_DECIMAL(true, BigDecimal.class),

    @XmlEnumValue("dictionary")
    DICTIONARY(false, Map.class),

    @XmlEnumValue("array")
    ARRAY(false, Object[].class),

    @XmlEnumValue("table")
    TABLE(false, Table.class),

    @XmlEnumValue("date")
    DATE_TIME(true, Date.class),

    @XmlEnumValue("float")
    FLOAT(true, Float.class),

    @XmlEnumValue("double")
    DOUBLE(true, Double.class);

    /**
     * Determines whether this type is scalar.
     */
    public final boolean isScalar;

    /**
     * Represents underying SNAMP-compliant type.
     */
    public final Class<?> underlyingType;

    private XmlParsingResultType(final boolean scalar,
                                 final Class<?> type){
        this.isScalar = scalar;
        this.underlyingType = type;
    }

    /**
     * Gets all scalar types.
     * @return The all scalar types.
     */
    public static EnumSet<XmlParsingResultType> getScalarTypes(){
        return EnumSet.of(BYTE,
                SHORT,
                INTEGER,
                LONG,
                BOOLEAN,
                STRING,
                BIG_INTEGER,
                BIG_DECIMAL,
                DATE_TIME,
                FLOAT,
                DOUBLE);
    }
}
