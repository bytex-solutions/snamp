package com.bytex.snamp.connector.composite.functions;

/**
 * Represents position in stream of tokens.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
interface TokenPosition {
    int get();
    int inc();
}
