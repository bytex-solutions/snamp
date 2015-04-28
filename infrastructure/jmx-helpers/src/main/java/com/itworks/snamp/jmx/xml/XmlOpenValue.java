package com.itworks.snamp.jmx.xml;

import javax.management.ObjectName;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Objects;

/**
 * Represents container for JMX Open Value.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 * @see javax.management.openmbean.OpenType
 */
@XmlRootElement(name = "OpenValue", namespace = XmlHelpers.NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlOpenValue {
    @XmlElements({
            @XmlElement(name = "int32", namespace = XmlHelpers.NAMESPACE, type = Integer.class),
            @XmlElement(name = "boolean", namespace = XmlHelpers.NAMESPACE, type = Boolean.class),
            @XmlElement(name = "character", namespace = XmlHelpers.NAMESPACE, type = Character.class),
            @XmlElement(name = "int8", namespace = XmlHelpers.NAMESPACE, type = Byte.class),
            @XmlElement(name = "int16", namespace = XmlHelpers.NAMESPACE, type = Short.class),
            @XmlElement(name = "int64", namespace = XmlHelpers.NAMESPACE, type = Long.class),
            @XmlElement(name = "float32", namespace = XmlHelpers.NAMESPACE, type = Float.class),
            @XmlElement(name = "float64", namespace = XmlHelpers.NAMESPACE, type = Double.class),
            @XmlElement(name = "date", namespace = XmlHelpers.NAMESPACE, type = Date.class),
            @XmlElement(name = "bigint", namespace = XmlHelpers.NAMESPACE, type = BigInteger.class),
            @XmlElement(name = "bigdecimal", namespace = XmlHelpers.NAMESPACE, type = BigDecimal.class),
            @XmlElement(name = "objectname", namespace = XmlHelpers.NAMESPACE, type = ObjectName.class)
    })
    private Object value;

    @SuppressWarnings("unchecked")
    public final <T> T cast(final OpenType<T> openType) throws ClassCastException{
        if(openType.isValue(value))
            return (T)value;
        else throw new ClassCastException(String.format("Unable cast '%s' to '%s'", value, openType));
    }

    public final <T> T cast(final Class<T> openType) throws ClassCastException{
        return openType.cast(value);
    }

    public final OpenType<?> getType(){
        if(value == null)
            return SimpleType.VOID;
        else if(value instanceof Boolean)
            return SimpleType.BOOLEAN;
        else if(value instanceof Short)
            return SimpleType.SHORT;
        else return null;
    }

    /**
     * Returns a string representation of the JMX open value.
     * @return A string representation of the JMX open value.
     */
    @Override
    public String toString() {
        return Objects.toString(value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    private boolean equals(final XmlOpenValue other){
        return other != null && Objects.equals(value, other.value);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof XmlOpenValue && equals((XmlOpenValue)other);
    }
}
