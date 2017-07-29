package com.bytex.snamp.connector.dataStream;

/**
 * Provides a chain of parsers.
 * @since 2.0
 * @version 2.1
 */
public interface NotificationParserChain extends NotificationParser {
    /**
     * Sets fallback notification parser.
     * @param parser Fallback notification parser.
     */
    void setFallbackParser(final NotificationParser parser);
}
