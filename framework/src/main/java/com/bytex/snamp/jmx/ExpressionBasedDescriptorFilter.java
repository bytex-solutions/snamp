package com.bytex.snamp.jmx;

import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;

import javax.management.DescriptorRead;
import java.util.Objects;

/**
 * Represents RFC 1960-based filter for management entities.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class ExpressionBasedDescriptorFilter {
    private final Filter filter;

    /**
     * Initializes a new expression-based filter.
     * <p>
     *     Expression will be compiled and stored in the newly created instance.
     * @param expression RFC 1960-based expression. Cannot be {@literal null} or empty.
     * @throws InvalidSyntaxException Invalid expression.
     */
    public ExpressionBasedDescriptorFilter(final String expression) throws InvalidSyntaxException {
        this.filter = FrameworkUtil.createFilter(expression);
    }

    /**
     * Determines whether the specified management feature matches to the underlying expression.
     * <p>
     *     Matching is case-sensitive.
     * @param metadata The metadata of the management feature. Cannot be {@literal null}.
     * @return {@literal true}, if the specified feature matches to the underlying compiled expression; otherwise, {@literal false}.
     */
    public final boolean match(final DescriptorRead metadata) {
        return filter.matchCase(DescriptorUtils.asDictionary(metadata.getDescriptor()));
    }

    /**
     * Returns underlying expression.
     * @return The filter expression.
     */
    @Override
    public String toString() {
        return filter.toString();
    }

    /**
     * Computes hash code of this expression.
     * @return The hash code of this expression.
     */
    @Override
    public final int hashCode() {
        return filter.hashCode();
    }

    public final boolean equals(final ExpressionBasedDescriptorFilter other){
        return other != null && Objects.equals(this.filter, other.filter);
    }

    @Override
    public final boolean equals(final Object other) {
        return other instanceof ExpressionBasedDescriptorFilter &&
                equals((ExpressionBasedDescriptorFilter)other);
    }
}
