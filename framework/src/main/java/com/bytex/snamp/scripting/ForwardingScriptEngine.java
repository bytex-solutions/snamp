package com.bytex.snamp.scripting;

import com.google.common.collect.ForwardingObject;

import javax.script.*;
import java.io.Reader;

abstract class ForwardingScriptEngine extends ForwardingObject implements ScriptEngine{

    @Override
    protected abstract ScriptEngine delegate();

    @Override
    public Bindings createBindings() {
        return delegate().createBindings();
    }

    @Override
    public Object eval(final Reader reader, final Bindings n) throws ScriptException {
        return delegate().eval(reader, n);
    }

    @Override
    public Object eval(final Reader reader, final ScriptContext context) throws ScriptException {
        return delegate().eval(reader, context);
    }

    @Override
    public Object eval(final Reader reader) throws ScriptException {
        return delegate().eval(reader);
    }

    @Override
    public Object eval(final String script, final Bindings n) throws ScriptException {
        return delegate().eval(script, n);
    }

    @Override
    public Object eval(final String script, final ScriptContext context) throws ScriptException {
        return delegate().eval(script, context);
    }

    @Override
    public Object eval(final String script) throws ScriptException {
        return delegate().eval(script);
    }

    @Override
    public Object get(final String key) {
        return delegate().get(key);
    }

    @Override
    public Bindings getBindings(int scope) {
        return delegate().getBindings(scope);
    }

    @Override
    public final ScriptContext getContext() {
        return delegate().getContext();
    }

    @Override
    public ScriptEngineFactory getFactory(){
        return delegate().getFactory();
    }

    @Override
    public void put(final String key, final Object value) {
        delegate().put(key, value);
    }

    @Override
    public void setBindings(final Bindings bindings, final int scope) {
        delegate().setBindings(bindings, scope);
    }

    @Override
    public final void setContext(final ScriptContext context) {
        delegate().setContext(context);
    }

}
