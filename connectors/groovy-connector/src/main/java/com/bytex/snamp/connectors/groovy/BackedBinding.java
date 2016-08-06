package com.bytex.snamp.connectors.groovy;

import groovy.lang.Binding;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the variable bindings with fallback parent bindings.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class BackedBinding extends Binding {
    private final Binding parent;

    private BackedBinding(final Binding parent, final Map<String, ?> environment) {
        super(environment);
        this.parent = Objects.requireNonNull(parent);
    }

    /**
     * @param name the name of the variable to lookup
     * @return the variable value
     */
    @Override
    public Object getVariable(final String name) {
        return super.hasVariable(name) ? super.getVariable(name) : parent.getVariable(name);
    }

    /**
     * Simple check for whether the binding contains a particular variable or not.
     *
     * @param name the name of the variable to check for
     */
    @Override
    public boolean hasVariable(final String name) {
        return super.hasVariable(name) && parent.hasVariable(name);
    }

    /**
     * Sets the value of the given variable
     *
     * @param name  the name of the variable to set
     * @param value the new value for the given variable
     */
    @Override
    public void setVariable(final String name, final Object value) {
        if(super.hasVariable(name))
            super.setVariable(name, value);
        else
            parent.setVariable(name, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map getVariables() {
        final Map thisVars = super.getVariables();
        final Map parentVars = parent.getVariables();
        final LinkedHashMap result = new LinkedHashMap(thisVars.size() + parentVars.size());
        result.putAll(thisVars);
        result.putAll(parentVars);
        return result;
    }

    static Binding create(final Binding parent, final Map<String, ?> environment) {
        return environment.isEmpty() ? parent : new BackedBinding(parent, environment);
    }
}
