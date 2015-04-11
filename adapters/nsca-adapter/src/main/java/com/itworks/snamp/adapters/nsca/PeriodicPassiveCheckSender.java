package com.itworks.snamp.adapters.nsca;

import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.AbstractAttributesModel;
import com.itworks.snamp.adapters.AttributeAccessor;
import com.itworks.snamp.concurrent.Repeater;
import com.itworks.snamp.internal.RecordReader;

import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class PeriodicPassiveCheckSender<TAccessor extends AttributeAccessor> extends Repeater {
    private final AbstractAttributesModel<TAccessor> attributes;

    /**
     * Initializes a new repeater.
     *
     * @param period Time between successive task executions. Cannot be {@literal null}.
     * @throws IllegalArgumentException period is {@literal null}.
     */
    PeriodicPassiveCheckSender(final TimeSpan period,
                               final AbstractAttributesModel<TAccessor> attributes) {
        super(period);
        this.attributes = Objects.requireNonNull(attributes);
    }

    protected abstract void sendCheck(final String resourceName,
                                      final TAccessor accessor);

    /**
     * Provides some periodical action.
     */
    @Override
    protected final void doAction() {
        attributes.forEachAttribute(new RecordReader<String, TAccessor, ExceptionPlaceholder>() {
            @Override
            public void read(final String resourceName, final TAccessor attribute) {
                sendCheck(resourceName, attribute);
            }
        });
    }
}
