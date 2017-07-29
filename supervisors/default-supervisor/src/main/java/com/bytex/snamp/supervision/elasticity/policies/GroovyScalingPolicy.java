package com.bytex.snamp.supervision.elasticity.policies;

import com.bytex.snamp.Aggregator;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.scripting.groovy.Scriptlet;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Represents Groovy-based voter for scaling process.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public abstract class GroovyScalingPolicy extends Scriptlet implements ScalingPolicy, Aggregator {
    private final ThreadLocal<ScalingPolicyEvaluationContext> context = new ThreadLocal<>();

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {

    }

    @Override
    public final Set<String> getResources() {
        final ScalingPolicyEvaluationContext context = this.context.get();
        return context == null ? Collections.emptySet() : context.getResources();
    }

    @Override
    public final Object getProperty(final String property) {
        final ScalingPolicyEvaluationContext context = this.context.get();
        Object result = context == null ? null : context.getConfiguration().get(property);
        return result == null ? super.getProperty(property) : result;
    }

    @SpecialUse(SpecialUse.Case.SCRIPTING)
    protected final ScalingPolicyEvaluationContext getContext(){
        return context.get();
    }

    @Override
    public final double evaluate(final ScalingPolicyEvaluationContext context) {
        this.context.set(context);
        try{
            return DefaultTypeTransformation.doubleUnbox(run());
        } finally {
            this.context.remove();
        }
    }

    @Override
    public final <T> Optional<T> queryObject(@Nonnull final Class<T> objectType) {
        final ScalingPolicyEvaluationContext context = this.context.get();
        return context == null ? Optional.empty() : context.queryObject(objectType);
    }

    @Nonnull
    @Override
    public final Aggregator compose(@Nonnull final Aggregator other) {
        final ScalingPolicyEvaluationContext context = this.context.get();
        return context == null ? other : context.compose(other);
    }
}
