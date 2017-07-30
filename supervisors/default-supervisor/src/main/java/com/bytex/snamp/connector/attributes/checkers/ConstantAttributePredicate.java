package com.bytex.snamp.connector.attributes.checkers;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import javax.management.Attribute;
import java.util.function.BooleanSupplier;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
@JsonTypeName("constant")
public final class ConstantAttributePredicate implements ColoredAttributePredicate, BooleanSupplier {
    private static final String VALUE_PROPERTY = "value";
    private final boolean value;

    @JsonCreator
    public ConstantAttributePredicate(@JsonProperty(VALUE_PROPERTY) final boolean value){
        this.value = value;
    }

    @Override
    public boolean test(final Attribute attribute) {
        return value;
    }

    @Override
    @JsonProperty(VALUE_PROPERTY)
    public boolean getAsBoolean() {
        return value;
    }
}
