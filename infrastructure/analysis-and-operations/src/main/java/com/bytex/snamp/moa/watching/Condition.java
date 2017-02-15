package com.bytex.snamp.moa.watching;

import javax.management.Attribute;
import java.util.function.Predicate;

/**
 * Represents condition used to evaluate attribute.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface Condition extends Predicate<Attribute> {
    Condition FALSE = a -> false;

    /**
     * Evaluates attribute value.
     * @param attribute Attribute value.
     * @return {@literal true}, if attribute satisfies to this condition; otherwise, {@literal false}.
     */
    @Override
    boolean test(final Attribute attribute);
}
