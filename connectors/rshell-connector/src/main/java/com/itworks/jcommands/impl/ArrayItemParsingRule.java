package com.itworks.jcommands.impl;

import javax.xml.bind.annotation.*;
import java.lang.reflect.Array;
import java.util.List;

/**
* @author Roman Sakno
* @version 1.0
* @since 1.0
*/
@XmlRootElement(name = "item", namespace = XmlConstants.NAMESPACE)
@XmlType(name = "ArrayItemParsingRule", namespace = XmlConstants.NAMESPACE)
@XmlAccessorType(XmlAccessType.PROPERTY)
public final class ArrayItemParsingRule extends ParsingRule{
    private String itemParsingRule;
    private XmlParsingResultType elementType;

    public ArrayItemParsingRule(){
        super(XmlParsingResultType.ARRAY);
        itemParsingRule = "";
        elementType = XmlParsingResultType.STRING;
    }

    public XmlParsingResultType getElementType(){
        return elementType;
    }

    @XmlAttribute(name = "elementType", namespace = XmlConstants.NAMESPACE, required = true)
    public void setElementType(final XmlParsingResultType value){
        if(value == null || !value.isScalar)
            throw new IllegalArgumentException(String.format("Expected scalar type but found %s", value));
        else elementType = value;
    }

    @XmlValue
    public String getItemParsingRule(){
        return itemParsingRule;
    }

    public void setItemParsingRule(final String value){
        itemParsingRule = value != null ? value : "";
    }

    private static Class<?> getNativeType(final List parsingTemplate) {
        final ArrayItemParsingRule rule = findRule(parsingTemplate, ArrayItemParsingRule.class);
        return rule != null ? Array.newInstance(rule.getElementType().underlyingType, 0).getClass() : null;
    }
}
