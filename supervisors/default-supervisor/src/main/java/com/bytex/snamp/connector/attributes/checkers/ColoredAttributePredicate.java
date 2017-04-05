package com.bytex.snamp.connector.attributes.checkers;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import javax.management.Attribute;
import java.util.function.Predicate;

/**
 * Represents predicate used to detect color of the attribute value.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(ConstantAttributePredicate.class),
        @JsonSubTypes.Type(NumberComparatorPredicate.class),
        @JsonSubTypes.Type(IsInRangePredicate.class)
})
public interface ColoredAttributePredicate extends Predicate<Attribute> {
    @Override
    boolean test(final Attribute attribute);
}
