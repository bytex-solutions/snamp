package com.bytex.snamp;

/**
 * Represents stub of exception that is used to suppress requirement to handle
 * exception declared in {@code throws} section if this exception is declared
 * as Java Generic. This class cannot be inherited or instantiated directly from your code.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class ExceptionPlaceholder extends RuntimeException {
    private static final long serialVersionUID = -3199291164997328632L;

    private ExceptionPlaceholder(){
        throw new InstantiationError("Exception placeholder cannot be instantiated");
    }
}
