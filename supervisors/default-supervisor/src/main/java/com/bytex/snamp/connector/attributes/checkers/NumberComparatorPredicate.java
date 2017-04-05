package com.bytex.snamp.connector.attributes.checkers;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import javax.annotation.Nonnull;
import javax.management.Attribute;

import static com.bytex.snamp.Convert.toDouble;

/**
 * Represents attribute predicate based on binary operator.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("comparator")
public final class NumberComparatorPredicate implements ColoredAttributePredicate {
    public enum Operator{
        GREATER_THAN {
            @Override
            boolean test(final double actual, final double expected) {
                return Double.compare(actual, expected) > 0;
            }
        },
        GREATER_THAN_OR_EQUAL {
            @Override
            boolean test(final double actual, final double expected) {
                return Double.compare(actual, expected) >= 0;
            }
        },
        LESS_THAN {
            @Override
            boolean test(final double actual, final double expected) {
                return Double.compare(actual, expected) < 0;
            }
        },
        LESS_THAN_OR_EQUAL {
            @Override
            boolean test(final double actual, final double expected) {
                return Double.compare(actual, expected) <= 0;
            }
        },
        EQUAL {
            @Override
            boolean test(final double actual, final double expected) {
                return Double.compare(actual, expected) == 0;
            }
        },
        NOT_EQUAL {
            @Override
            boolean test(final double actual, final double expected) {
                return Double.compare(actual, expected) != 0;
            }
        };

        abstract boolean test(final double actual, final double expected);

        final boolean test(final Attribute attribute, final double value){
            final double actual = toDouble(attribute.getValue());
            return test(actual, value);
        }
    }

    private Operator operator;
    private double value;

    public NumberComparatorPredicate(@Nonnull final Operator operator, final double value){
        this.operator = operator;
        this.value = value;
    }

    public NumberComparatorPredicate(){
        this(Operator.EQUAL, Double.NaN);
    }

    @Override
    public boolean test(final Attribute attribute) {
        return operator.test(attribute, value);
    }

    @JsonProperty("operator")
    public Operator getOperator(){
        return operator;
    }

    public void setOperator(@Nonnull final Operator value){
        operator = value;
    }

    @JsonProperty("value")
    public double getValue(){
        return value;
    }

    public void setValue(final double value) {
        this.value = value;
    }
}