package com.bytex.snamp.gateway.groovy;

import com.bytex.snamp.gateway.modeling.FeatureAccessor;
import com.bytex.snamp.jmx.ExpressionBasedDescriptorFilter;
import org.osgi.framework.InvalidSyntaxException;

import javax.management.MBeanFeatureInfo;
import java.util.function.Supplier;

/**
 * Represents a root interface for resource features analyzer.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public interface ResourceFeaturesAnalyzer {
    /**
     * A root interface for all supported statements.
     */
    interface FeatureStatement{

    }

    /**
     * An abstract class for selection statement.
     */
    abstract class AbstractSelectStatement extends ExpressionBasedDescriptorFilter implements FeatureStatement{
        protected AbstractSelectStatement(final String expression) throws InvalidSyntaxException {
            super(expression);
        }

        /**
         * Determines whether the specified feature accessor matches to this filter.
         * @param accessor An accessor to check. Cannot be {@literal null}.
         * @return {@literal true}, if the specified feature accessor matches to this filter; otherwise, {@literal false}.
         * @see FeatureAccessor
         */
        public final boolean match(final Supplier<? extends MBeanFeatureInfo> accessor){
            return match(accessor.get());
        }
    }

    /**
     * Creates a new selection statement using RFC 1960-based filter that will be applied to feature metadata (and configuration properties).
     * @param expression An expression used to select resource features. Cannot be {@literal null} or empty.
     * @return Selection statement.
     * @throws InvalidSyntaxException Incorrect expression.
     */
    AbstractSelectStatement select(final String expression) throws InvalidSyntaxException;
}
