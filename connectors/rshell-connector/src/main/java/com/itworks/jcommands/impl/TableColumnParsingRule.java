package com.itworks.jcommands.impl;

import javax.xml.bind.annotation.*;

/**
* @author Roman Sakno
* @version 1.0
* @since 1.0
*/
@XmlRootElement(name = "column", namespace = XmlConstants.NAMESPACE)
@XmlType(name = "TableColumnParsingRule")
@XmlAccessorType(XmlAccessType.PROPERTY)
public final class TableColumnParsingRule extends ParsingRule{
    private String columnName;
    private String columnValueParsingRule;
    private XmlParsingResultType columnType;
    private boolean indexed;

    public TableColumnParsingRule(){
        super(XmlParsingResultType.TABLE);
        columnName = columnValueParsingRule = "";
        columnType = XmlParsingResultType.STRING;
        indexed = false;
    }

    @XmlAttribute(name = "indexed", namespace = XmlConstants.NAMESPACE, required = false)
    public void setIndexed(final boolean value){
        indexed = value;
    }

    public boolean isIndexed(){
        return indexed;
    }

    @XmlAttribute(name = "name", namespace = XmlConstants.NAMESPACE, required = true)
    public String getColumnName(){
        return columnName;
    }

    public void setColumnName(final String value){
        columnName = value != null ? value : "";
    }

    @XmlValue
    public String getColumnValueParsingRule(){
        return columnValueParsingRule;
    }

    public void setColumnValueParsingRule(final String value){
        columnValueParsingRule = value != null ? value : "";
    }

    @XmlAttribute(name = "type", namespace = XmlConstants.NAMESPACE, required = true)
    public XmlParsingResultType getColumnType(){
        return columnType;
    }

    public void setColumnType(final XmlParsingResultType value){
        if(value == null || !value.isScalar())
            throw new IllegalArgumentException(String.format("Expecting scalar type but found %s", value));
        else columnType = value;
    }
}
