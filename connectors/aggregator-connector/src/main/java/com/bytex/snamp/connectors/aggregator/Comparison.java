package com.bytex.snamp.connectors.aggregator;

import java.math.BigDecimal;

/**
 * Number comparison strategy.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
enum Comparison {
    ARE_EQUAL {
        @Override
        boolean compute(final BigDecimal first, final BigDecimal second) {
            return first.equals(second);
        }
    },
    ARE_NOT_EQUAL {
        @Override
        boolean compute(final BigDecimal first, final BigDecimal second) {
            return !first.equals(second);
        }
    },
    GREATER_THAN {
        @Override
        boolean compute(final BigDecimal first, final BigDecimal second) {
            return first.compareTo(second) > 0;
        }
    },
    GREATER_THAN_OR_EQUAL {
        @Override
        boolean compute(final BigDecimal first, final BigDecimal second) {
            return first.compareTo(second) >= 0;
        }
    },
    LESS_THAN {
        @Override
        boolean compute(final BigDecimal first, final BigDecimal second) {
            return first.compareTo(second) < 0;
        }
    },
    LESS_THAN_OR_EQUAL {
        @Override
        boolean compute(final BigDecimal first, final BigDecimal second) {
            return first.compareTo(second) <= 0;
        }
    };

    abstract boolean compute(final BigDecimal first, final BigDecimal second);

    static Comparison parse(final String strategy){
        switch (strategy){
            default:
            case "=":
            case "==": return ARE_EQUAL;
            case "!=":
            case "<>": return ARE_NOT_EQUAL;
            case ">": return GREATER_THAN;
            case ">=": return GREATER_THAN_OR_EQUAL;
            case "<": return LESS_THAN;
            case "<=": return LESS_THAN_OR_EQUAL;
        }
    }
}
