package com.bytex.snamp.jmx;

import javax.management.openmbean.OpenDataException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class UncheckedOpenDataException extends RuntimeException {
    private static final long serialVersionUID = 2510803576354654470L;

    UncheckedOpenDataException(final OpenDataException cause){
        super(cause);
    }
}
