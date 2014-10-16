package com.itworks.snamp;

/**
 * Represents exception processor.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ExceptionProcessor<E extends Exception, O> {
    /**
     * Processes the exception.
     * @param error The exception to process.
     * @return The processing result.
     */
    O process(final E error);

}
