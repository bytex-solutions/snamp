package com.bytex.snamp.connector.health;

import javax.annotation.Nonnull;
import javax.management.Attribute;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

/**
 * The root cause of the malfunction is a value of some metric.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class InvalidAttributeValue extends ResourceMalfunctionStatus {
    private static final long serialVersionUID = -84085262684742050L;
    private final Attribute attribute;
    private final boolean important;

    public InvalidAttributeValue(final String resourceName,
                                 final Attribute attribute,
                                 final boolean important){
        super(Instant.now());
        this.important = important;
        this.attribute = Objects.requireNonNull(attribute);
    }

    @Override
    @Nonnull
    public Level getLevel() {
        return important ? Level.MODERATE : Level.LOW;
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

    private boolean like(final InvalidAttributeValue status){
        return super.like(status) && status.attribute.getName().equals(attribute.getName());
    }

    @Override
    public boolean like(final HealthStatus status) {
        return status instanceof InvalidAttributeValue && like((InvalidAttributeValue) status);
    }
}
