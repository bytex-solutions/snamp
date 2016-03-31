package com.bytex.jcommands.impl;

import javax.xml.bind.annotation.*;

/**
 * Represents parsing rule that detects line termination for recursive data structures,
 * such as dictionaries and tables. This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.2
 */
@XmlRootElement(name = "line-terminator", namespace = XmlConstants.NAMESPACE)
@XmlType(name = "LineTerminationParsingRule", namespace = XmlConstants.NAMESPACE)
@XmlAccessorType(XmlAccessType.PROPERTY)
public final class LineTerminationParsingRule extends ParsingRule{
    private String terminationRule;

    public LineTerminationParsingRule(){
        super(XmlParsingResultType.ARRAY, XmlParsingResultType.TABLE);
        terminationRule = "";
    }

    @XmlValue
    public String getTerminationRule(){
        return terminationRule;
    }

    public void setTerminationRule(final String value){
        terminationRule = value != null ? value : null;
    }
}
