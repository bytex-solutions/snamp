package com.bytex.snamp.connector.health;

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

    public InvalidAttributeValue(final String resourceName,
                                 final Attribute attribute,
                                 final boolean critical){
        super(resourceName, CODE, critical);
        this.attribute = Objects.requireNonNull(attribute);
    }

    public Attribute getAttribute(){
        return attribute;
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
