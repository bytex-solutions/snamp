package com.bytex.snamp.scripting.groovy;

import com.google.common.collect.ImmutableMap;
import groovy.lang.Binding;

import java.util.Map;
import java.util.Objects;

/**
 * Represents the variable bindings with fallback parent bindings.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class ForwardingBinding extends Binding {
    private final Binding first;
    private final Binding second;

    private ForwardingBinding(final Binding first, final Binding second) {
        this.first = Objects.requireNonNull(first);
        this.second = Objects.requireNonNull(second);
    }

    /**
     * @param name the name of the variable to lookup
     * @return the variable value
     */
    @Override
    public Object getVariable(final String name) {
        return first.hasVariable(name) ? first.getVariable(name) : second.getVariable(name);
    }

    /**
     * Simple check for whether the binding contains a particular variable or not.
     *
     * @param name the name of the variable to check for
     */
    @Override
    public boolean hasVariable(final String name) {
        return first.hasVariable(name) && second.hasVariable(name);
    }

    /**
     * Sets the value of the given variable
     *
     * @param name  the name of the variable to set
     * @param value the new value for the given variable
     */
    @Override
    public void setVariable(final String name, final Object value) {
        if(first.hasVariable(name))
            first.setVariable(name, value);
        else
            second.setVariable(name, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map getVariables() {
        return ImmutableMap.builder()
                .putAll(first.getVariables())
                .putAll(second.getVariables())
                .build();
    }

    static Binding create(final Binding initial, final Binding... other){
        Binding first = initial;
        for(final Binding second: other)
            first = new ForwardingBinding(first, second);
        return first;
    }
}