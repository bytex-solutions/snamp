package com.bytex.snamp.supervision.elasticity.policies;

import com.bytex.snamp.scripting.groovy.Scriptlet;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

/**
 * Represents Groovy-based voter for scaling process.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class GroovyScalingPolicy extends Scriptlet implements ScalingPolicy {
    private final ThreadLocal<ScalingPolicyEvaluationContext> context = new ThreadLocal<>();

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {

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
}
