package com.snamp.licensing;

import java.util.Set;

/**
 * Represents a set of restrictions associated with the licensed part of SNAMP.
 * @author roman
 */
public interface Restrictions extends Set<String> {
    /**
     * Represents validation result.
     */
    public static enum ValidationResult{

        /**
         * The option is allowed.
         */
        SATISFIED,
        /**
         * The option is denied.
         */
        UNSATISFIED,

        /**
         * Validation result is unknown
         */
        UNKNOWN;

    }

    /**
     * Represents restriction validator.
     */
    public static interface Validator{
        /**
         * Validates the restriction.
         * @param restrictionValue The restriction value obtained from the license.
         * @return {@literal true}, if the specified restriction is satisfied; otherwise, {@literal false}.
         */
        public ValidationResult validate(final String restrictionValue);
    }

    /**
     * Validates the restriction.
     * @param restrictionId The name of the restriction.
     * @param validator The restriction checker.
     * @return
     */
    public ValidationResult validate(final String restrictionId, final Validator validator);
}
