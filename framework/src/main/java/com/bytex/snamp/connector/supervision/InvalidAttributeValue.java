package com.bytex.snamp.connector.supervision;

import javax.management.Attribute;
import java.util.Locale;
import java.util.Objects;

/**
 * The root cause of the malfunction is a value of some metric.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class InvalidAttributeValue extends MalfunctionStatus {
    public static final int CODE = 3;
    private static final long serialVersionUID = -84085262684742050L;
    private final Attribute attribute;

    public InvalidAttributeValue(final Attribute attribute, final boolean critical){
        super(CODE, critical);
        this.attribute = Objects.requireNonNull(attribute);
    }

    public Attribute getAttribute(){
        return attribute;
    }

    @Override
    public int hashCode() {
        return attribute.hashCode();
    }

    private boolean equals(final InvalidAttributeValue other){
        return Objects.equals(attribute, other.attribute);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof InvalidAttributeValue && equals((InvalidAttributeValue) other);
    }

    /**
     * Returns the localized description of this object.
     *
     * @param locale The locale of the description. If it is {@literal null} then returns description
     *               in the default locale.
     * @return The localized description of this object.
     */
    @Override
    public String toString(final Locale locale) {
        return "Invalid attribute value: " + attribute;
    }
}
