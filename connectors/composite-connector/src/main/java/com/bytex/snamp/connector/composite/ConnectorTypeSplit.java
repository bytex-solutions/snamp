package com.bytex.snamp.connector.composite;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
interface ConnectorTypeSplit {
    Pattern PREFIXED_FRAGMENT_PATTERN = Pattern.compile("(?<connectorType>[a-z]+)\\s*:\\s*(?<fragment>.+)", Pattern.CASE_INSENSITIVE);

    static boolean split(final CharSequence input, final BiConsumer<String, String> acceptor){
        final Matcher result = PREFIXED_FRAGMENT_PATTERN.matcher(input);
        final boolean matches;
        if(matches = result.matches())
            acceptor.accept(result.group("connectorType"), result.group("fragment"));
        return matches;
    }

    static boolean split(final CharSequence input, final Consumer<String> connectorTypeConsumer, final Consumer<String> fragmentConsumer){
        return split(input, (connectorType, fragment) -> {
            connectorTypeConsumer.accept(connectorType);
            fragmentConsumer.accept(fragment);
        });
    }
}
