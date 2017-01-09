package com.bytex.snamp.json;

import org.codehaus.jackson.node.JsonNodeFactory;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ThreadLocalJsonFactory extends ThreadLocal<JsonNodeFactory> {
    private static final ThreadLocalJsonFactory INSTANCE = new ThreadLocalJsonFactory();

    private ThreadLocalJsonFactory(){

    }

    public static JsonNodeFactory getFactory(){
        return INSTANCE.get();
    }

    @Override
    protected JsonNodeFactory initialValue() {
        return JsonNodeFactory.instance;
    }
}
