package com.bytex.snamp.connector.health;

import javax.management.Attribute;
import java.util.Objects;

/**
 * The root cause of the malfunction is a value of some metric.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class CausedByAttribute extends RootCause {
    private final Attribute attribute;

    public CausedByAttribute(final Attribute attribute){
        this.attribute = Objects.requireNonNull(attribute);
    }

    public Attribute getAttribute(){
        return attribute;
    }

    @Override
    public int hashCode() {
        return attribute.hashCode();
    }

    private boolean equals(final CausedByAttribute other){
        return Objects.equals(attribute, other.attribute);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof CausedByAttribute && equals((CausedByAttribute) other);
    }

    @Override
    public String toString() {
        return attribute.toString();
    }
}
