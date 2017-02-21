package com.bytex.snamp.health;

import javax.management.JMException;
import java.util.Objects;

/**
 * Indicates that SNAMP cannot obtain access to the managed resource.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ResourceUnavailable extends RootCause {
    private final JMException error;

    public ResourceUnavailable(final JMException e){
        error = Objects.requireNonNull(e);
    }

    public JMException getError(){
        return error;
    }

    @Override
    public int hashCode() {
        return error.hashCode();
    }

    private boolean equals(final ResourceUnavailable other){
        return Objects.equals(error, other.error);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof ResourceUnavailable && equals((ResourceUnavailable) other);
    }

    @Override
    public String toString() {
        return error.toString();
    }
}
