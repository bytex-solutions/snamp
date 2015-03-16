package com.itworks.snamp.testing;

import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.concurrent.ConditionWait;

import java.util.Collection;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class CollectionSizeAwaitor extends ConditionWait<ExceptionPlaceholder> {
    private final Collection<?> collection;
    private final int expectedSize;

    public CollectionSizeAwaitor(final Collection<?> col,
                                 final int expectedSize){
        this.collection = col;
        this.expectedSize = expectedSize;
    }

    @Override
    protected boolean checkCondition() {
        return expectedSize >= collection.size();
    }
}
