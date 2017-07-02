package com.bytex.jcommands.impl;

import javax.xml.bind.annotation.*;

/**
 * Represents a constant that will be returned by the template without other parsing.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@XmlRootElement(name = "const", namespace = XmlConstants.NAMESPACE)
@XmlType(name = "Constant", namespace = XmlConstants.NAMESPACE)
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ConstantParsingRule extends ParsingRule {
    private String value;

    public ConstantParsingRule(){
        super(XmlParsingResultType.getScalarTypes());
        value = "";
    }

    @XmlValue
    public String getValue(){
        return value;
    }

    public void setValue(final String value){
        this.value = value != null ? value : "";
    }
}
