package com.bytex.snamp.connector.attributes.checkers;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import javax.management.Attribute;
import java.util.function.BooleanSupplier;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@JsonTypeName("constant")
public final class ConstantAttributePredicate implements ColoredAttributePredicate, BooleanSupplier {
    private final boolean value;

    public ConstantAttributePredicate(final boolean value){
        this.value = value;
    }

    @Override
    public boolean test(final Attribute attribute) {
        return value;
    }

    @Override
    @JsonProperty("value")
    public boolean getAsBoolean() {
        return value;
    }
}
