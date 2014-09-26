package com.itworks.jcommands.impl;

import javax.xml.bind.annotation.*;

/**
 * Represents parsing for the placeholder.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@XmlType(name = "PlaceholderParsingRule", namespace = XmlConstants.NAMESPACE)
@XmlRootElement(name = "skip", namespace = XmlConstants.NAMESPACE)
@XmlAccessorType(XmlAccessType.PROPERTY)
public final class PlaceholderParsingRule extends ParsingRule {
    private String rule;
    /**
     * Initializes a new parsing rule.
     */
    public PlaceholderParsingRule(){
        rule = "";
    }

    @XmlValue
    public String getRule(){
        return rule;
    }

    public void setRule(final String value){
        rule = value != null ? value : "";
    }
}
