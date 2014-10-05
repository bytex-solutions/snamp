package com.itworks.jcommands.impl;

import org.apache.commons.lang3.ArrayUtils;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents format of the BLOB fragment.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@XmlType(name = "BlobFormat", namespace = XmlConstants.NAMESPACE)
@XmlEnum
public enum BLOBFormat implements Converter<Byte[]> {
    /**
     * Represents textual fragment with HEX numbers.
     */
    @XmlEnumValue("hex")
    HEX {

        @Override
        public Byte[] parse(final String input) {
            return ArrayUtils.toObject(DatatypeConverter.parseHexBinary(input));
        }
    },

    /**
     * Represents textual fragment encoding with BASE-64.
     */
    @XmlEnumValue("base64")
    BASE64 {
        @Override
        public Byte[] parse(final String input) {
            return ArrayUtils.toObject(DatatypeConverter.parseBase64Binary(input));
        }
    };



    /**
     * Parses textual fragment into the array of bytes.
     * @param input The fragment to parse.
     * @return An array of bytes that represents the BLOB.
     */
    public abstract Byte[] parse(final String input);

    /**
     * Parses textual fragment into the array of bytes.
     * @param input The fragment to parse.
     * @return An array of bytes that represents the BLOB.
     */
    @Override
    public final Byte[] transform(final String input) {
        return parse(input);
    }
}
