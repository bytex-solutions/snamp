package com.itworks.snamp.adapters;

import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.concurrent.Repeater;
import com.itworks.snamp.internal.RecordReader;

import java.util.Objects;

/**
 * Abstract sender of attribute values.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class PeriodicPassiveChecker<TAccessor extends AttributeAccessor> extends Repeater {
    private final AbstractAttributesModel<TAccessor> attributes;

    /**
     * Initializes a new attribute value sender.
     *
     * @param period Time between successive task executions. Cannot be {@literal null}.
     * @param attributes A collection of attributes. Cannot be {@literal null}.
     * @throws IllegalArgumentException period is {@literal null}.
     */
    protected PeriodicPassiveChecker(final TimeSpan period,
                                     final AbstractAttributesModel<TAccessor> attributes) {
        super(period);
        this.attributes = Objects.requireNonNull(attributes);
    }

    /**
     * Processes attribute.
     * @param resourceName The name of the managed resource which provides the specified attribute.
     * @param accessor The attribute of the managed resource.
     */
    protected abstract void processAttribute(final String resourceName,
                                             final TAccessor accessor);

    /**
     * Sends attribute check status.
     */
    @Override
    protected final void doAction() {
        attributes.forEachAttribute(new RecordReader<String, TAccessor, ExceptionPlaceholder>() {
            @Override
            public boolean read(final String resourceName, final TAccessor attribute) {
                processAttribute(resourceName, attribute);
                return true;
            }
        });
    }
}
