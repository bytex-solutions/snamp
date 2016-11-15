package com.bytex.jcommands.impl;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
@XmlType
@XmlEnum(String.class)
public enum ProfileTarget {
    @XmlEnumValue("attribute")
    ATTRIBUTE,
    @XmlEnumValue("command")
    COMMAND
}
