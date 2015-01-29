package com.itworks.snamp.scripting;

import javax.script.*;
import java.io.Reader;

public abstract class ForwardingScriptEngine implements ScriptEngine{

    protected abstract ScriptEngine delegate();

    @Override
    public Bindings createBindings() {
        return delegate().createBindings();
    }

    @Override
    public Object eval(Reader reader, Bindings n) throws ScriptException {
        return delegate().eval(reader, n);
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        return delegate().eval(reader, context);
    }

    @Override
    public Object eval(Reader reader) throws ScriptException {
        return delegate().eval(reader);
    }

    @Override
    public Object eval(String script, Bindings n) throws ScriptException {
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
    public abstract ScriptEngineFactory getFactory();

    @Override
    public void put(String key, Object value) {
        delegate().put(key, value);
    }

    @Override
    public void setBindings(Bindings bindings, int scope) {
        delegate().setBindings(bindings, scope);
    }

    @Override
    public final void setContext(final ScriptContext context) {
        delegate().setContext(context);
    }

}
