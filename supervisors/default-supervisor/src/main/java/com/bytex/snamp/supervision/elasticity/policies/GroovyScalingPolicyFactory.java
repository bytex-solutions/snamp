package com.bytex.snamp.supervision.elasticity.policies;

import com.bytex.snamp.scripting.groovy.OSGiGroovyScriptEngine;

import java.util.Properties;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class GroovyScalingPolicyFactory extends OSGiGroovyScriptEngine<GroovyScalingPolicy> {

    GroovyScalingPolicyFactory(final ClassLoader rootClassLoader) {
        super(rootClassLoader, new Properties(), GroovyScalingPolicy.class);
    }

    @Override
    protected void interceptCreate(final GroovyScalingPolicy script) {
        script.setBundleContext(getBundleContext());
    }

    public GroovyScalingPolicy create(final String text) {
        return parseScript(text, getGlobalVariables());
    }
}
