package com.bytex.snamp.gateway.modeling;

import com.bytex.snamp.ExceptionPlaceholder;
import com.bytex.snamp.concurrent.Repeater;
import com.bytex.snamp.EntryReader;

import java.time.Duration;
import java.util.Objects;

/**
 * Abstract sender of attribute values.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class PeriodicPassiveChecker<TAccessor extends AttributeAccessor> extends Repeater implements EntryReader<String, TAccessor, Exception> {
    private final ModelOfAttributes<TAccessor> attributes;

    /**
     * Initializes a new attribute value sender.
     *
     * @param period Time between successive task executions. Cannot be {@literal null}.
     * @param attributes A collection of attributes. Cannot be {@literal null}.
     * @throws IllegalArgumentException period is {@literal null}.
     */
    protected PeriodicPassiveChecker(final Duration period,
                                     final ModelOfAttributes<TAccessor> attributes) {
        super(period);
        this.attributes = Objects.requireNonNull(attributes);
    }

    /**
     * Processes attribute.
     * @param resourceName The name of the managed resource which provides the specified attribute.
     * @param accessor The attribute of the managed resource.
     * @return {@literal true} to continue processing; otherwise, {@literal false}.
     */
    @Override
    public abstract boolean read(final String resourceName, final TAccessor accessor) throws Exception;

    /**
     * Sends attribute check status.
     */
    @Override
    protected final void doAction() throws Exception {
        attributes.forEachAttribute(this);
    }
}
