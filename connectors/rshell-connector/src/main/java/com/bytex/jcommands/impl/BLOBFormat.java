package com.bytex.jcommands.impl;


import com.google.common.io.BaseEncoding;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import static com.bytex.snamp.ArrayUtils.emptyByteArray;

/**
 * Represents format of the BLOB fragment.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
@XmlType(name = "BlobFormat", namespace = XmlConstants.NAMESPACE)
@XmlEnum
public enum BLOBFormat implements Converter<byte[]> {
    /**
     * Represents textual fragment with HEX numbers.
     */
    @XmlEnumValue("hex")
    HEX(BaseEncoding.base16()),

    /**
     * Represents textual fragment encoding with BASE-32.
     */
    @XmlEnumValue("base32")
    BASE32(BaseEncoding.base32()),

    /**
     * Represents textual fragment encoding with BASE-64.
     */
    @XmlEnumValue("base64")
    BASE64(BaseEncoding.base64());

    private final BaseEncoding encoding;

    BLOBFormat(final BaseEncoding enc){
        this.encoding = enc;
    }

    /**
     * Parses textual fragment into the array of bytes.
     * @param input The fragment to parse.
     * @return An array of bytes that represents the BLOB.
     */
    @Override
    public final byte[] apply(final String input) {
        return input != null ? encoding.decode(input) : emptyByteArray();
    }
}
