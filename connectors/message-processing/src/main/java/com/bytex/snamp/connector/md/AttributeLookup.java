package com.bytex.snamp.connector.md;

import java.util.function.Consumer;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
interface AttributeLookup {
    <A extends MessageDrivenAttribute> void forEachAttribute(final Class<A> attributeType, final Consumer<? super A> handler);
    <A extends MessageDrivenAttribute> boolean acceptAttribute(final String name, final Class<A> attributeType, final Consumer<? super A> handler);
}
