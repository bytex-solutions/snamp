package com.itworks.snamp.adapters.rest;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class RestAdapterHelpers {
    private RestAdapterHelpers(){

    }

    public static String makeAttributeID(final String prefix, final String postfix){
        return String.format("%s/%s", prefix, postfix);
    }

    public static String makeEventID(final String prefix, final String postfix){
        return String.format("%s/%s", prefix, postfix);
    }
}
