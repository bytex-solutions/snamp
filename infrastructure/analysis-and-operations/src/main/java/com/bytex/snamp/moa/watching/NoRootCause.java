package com.bytex.snamp.moa.watching;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class NoRootCause extends com.bytex.snamp.health.RootCause {
    public static final com.bytex.snamp.health.NoRootCause INSTANCE = new com.bytex.snamp.health.NoRootCause();

    private NoRootCause(){

    }

    @Override
    public int hashCode() {
        return 0xEBD88320;
    }


    @Override
    public boolean equals(final Object other) {
        return other instanceof com.bytex.snamp.health.NoRootCause;
    }

    @Override
    public String toString() {
        return "No Root Cause";
    }
}
