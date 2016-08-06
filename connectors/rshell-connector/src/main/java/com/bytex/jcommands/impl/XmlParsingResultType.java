package com.bytex.jcommands.impl;

import com.google.common.collect.Sets;

import javax.management.openmbean.*;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

/**
 * Represents SNAMP-compliant return type of the command-line tool.
 * @author Roman Sakno
 * @version 2.0
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
    @XmlEnumValue("bool")
    BOOLEAN(SimpleType.BOOLEAN) {
        @Override
        public boolean[] newArray(final int length) {
            return new boolean[length];
        }
    },

    /**
     * Represents string value.
     */
    @XmlEnumValue("string")
    STRING(SimpleType.STRING){
        @Override
        public String[] newArray(final int length) {
            return new String[length];
        }
    },

    /**
     * Represents single character.
     */
    @XmlEnumValue("char")
    CHARACTER(SimpleType.CHARACTER) {
        @Override
        public char[] newArray(final int length) {
            return new char[length];
        }
    },

    /**
     * Represents an array of bytes.
     * <p>
     *     This type converts hex string into the array of bytes.
     */
    @XmlEnumValue("blob")
    BLOB(ArrayType.getPrimitiveArrayType(byte[].class)) {
        @Override
        public byte[][] newArray(final int length) {
            return new byte[length][];
        }
    },

    @XmlEnumValue("int8")
    BYTE(SimpleType.BYTE) {
        @Override
        public byte[] newArray(final int length) {
            return new byte[length];
        }
    },

    @XmlEnumValue("int16")
    SHORT(SimpleType.SHORT) {
        @Override
        public short[] newArray(final int length) {
            return new short[length];
        }
    },

    @XmlEnumValue("int32")
    INTEGER(SimpleType.INTEGER) {
        @Override
        public int[] newArray(final int length) {
            return new int[length];
        }
    },

    @XmlEnumValue("int64")
    LONG(SimpleType.LONG) {
        @Override
        public long[] newArray(final int length) {
            return new long[length];
        }
    },

    @XmlEnumValue("bigint")
    BIG_INTEGER(SimpleType.BIGINTEGER) {
        @Override
        public BigInteger[] newArray(final int length) {
            final BigInteger[] result = new BigInteger[length];
            Arrays.fill(result, BigInteger.ZERO);
            return result;
        }
    },

    @XmlEnumValue("bigdecimal")
    BIG_DECIMAL(SimpleType.BIGDECIMAL) {
        @Override
        public BigDecimal[] newArray(final int length) {
            final BigDecimal[] result = new BigDecimal[length];
            Arrays.fill(result, BigDecimal.ZERO);
            return result;
        }
    },

    @XmlEnumValue("dictionary")
    DICTIONARY {
        @Override
        public CompositeData[] newArray(final int length) {
            return new CompositeData[length];
        }
    },

    @XmlEnumValue("array")
    ARRAY {
        @Override
        public Object newArray(final int length) {
            return new Object[length];
        }
    },

    @XmlEnumValue("table")
    TABLE {
        @Override
        public TabularData[] newArray(final int length) {
            return new TabularData[length];
        }
    },

    @XmlEnumValue("date")
    DATE_TIME(SimpleType.DATE) {
        @Override
        public Date[] newArray(final int length) {
            return new Date[length];
        }
    },

    @XmlEnumValue("float32")
    FLOAT(SimpleType.FLOAT) {
        @Override
        public float[] newArray(final int length) {
            return new float[length];
        }
    },

    @XmlEnumValue("float64")
    DOUBLE(SimpleType.DOUBLE) {
        @Override
        public double[] newArray(final int length) {
            return new double[length];
        }
    };

    private final OpenType<?> openType;

    XmlParsingResultType(final SimpleType<?> type){
        this.openType = type;
    }

    XmlParsingResultType(final ArrayType<?> type){
        this.openType = type;
    }

    XmlParsingResultType(){
        this.openType = null;
    }

    public OpenType<?> getOpenType(){
        return openType;
    }

    public boolean isScalar(){
        return openType != null;
    }

    /**
     * Gets all scalar types.
     * @return The all scalar types.
     */
    public static Set<XmlParsingResultType> getScalarTypes() {
        return Sets.filter(EnumSet.allOf(XmlParsingResultType.class), XmlParsingResultType::isScalar);
    }

    public abstract Object newArray(final int length);
}
