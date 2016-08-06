package com.bytex.jcommands.impl;

import javax.xml.bind.annotation.*;

/**
 * Describes parsing of the key/value pair for the diction1ary.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.2
 */
@XmlType(name = "DictionaryEntryParsingRule", namespace = XmlConstants.NAMESPACE)
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlRootElement(name = "entry", namespace = XmlConstants.NAMESPACE)
public final class DictionaryEntryParsingRule extends ParsingRule{
    private String key;
    private String value;
    private XmlParsingResultType valueType;

    /**
     * Initializes a new dictionary entry with default settings.
     */
    public DictionaryEntryParsingRule(){
        super(XmlParsingResultType.DICTIONARY);
        key = value = "";
        valueType = XmlParsingResultType.STRING;
    }

    /**
     * Returns name of the dictionary key.
     * @return The name of the dictionary key.
     */
    @XmlAttribute(name = "key", namespace = XmlConstants.NAMESPACE, required = true)
    public String getKeyName() {
        return key;
    }

    /**
     * Sets the name of the dictionary key.
     * @param value The name of the dictionary key.
     */
    public void setKeyName(final String value){
        this.key = value != null ? value : "";
    }

    /**
     * Gets parsing rule for the value of the dictionary entry.
     * @return The parsing rule.
     */
    @XmlValue
    public String getValueParsingRule() {
        return value;
    }

    /**
     * Sets parsing rule for the value of the dictionary entry.
     * @param value The parsing rule.
     */
    public void setValueParsingRule(final String value){
        this.value = value != null ? value : "";
    }

    @XmlAttribute(name = "type", namespace = XmlConstants.NAMESPACE, required = true)
    public XmlParsingResultType getValueType(){
        return valueType;
    }

    /**
     * Sets the type of the entry value.
     * @param value The type of the entry value. Only scalar types are allowed.
     */
    public void setValueType(final XmlParsingResultType value){
        if(value == null || !value.isScalar())
            throw new IllegalArgumentException(String.format("Expecting scalar type but %s found", value));
        else this.valueType = value;
    }

}
