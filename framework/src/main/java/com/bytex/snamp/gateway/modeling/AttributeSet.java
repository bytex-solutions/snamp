package com.bytex.snamp.gateway.modeling;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.BooleanBox;
import com.bytex.snamp.BoxFactory;
import com.bytex.snamp.EntryReader;

import java.util.function.Predicate;

/**
 * Represents reader for a set of attributes stored inside of the gateway instance.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface AttributeSet<TAccessor extends AttributeAccessor> {
    /**
     * Reads all attributes sequentially.
     * @param attributeReader An object that accepts attribute and its resource.
     * @param <E> Type of the exception that may be produced by reader.
     * @throws E Unable to process attribute.
     */
    <E extends Throwable> void forEachAttribute(final EntryReader<String, ? super TAccessor, E> attributeReader) throws E;

    default <E extends Throwable> boolean processAttribute(final String resourceName,
                                                                final String attributeName,
                                                                final Acceptor<? super TAccessor, E> processor) throws E {
        return processAttribute(resourceName, accessor -> accessor.getName().equals(attributeName), processor);
    }

    default <E extends Throwable> boolean processAttribute(final String resourceName,
                                                                 final Predicate<? super TAccessor> filter,
                                                                 final Acceptor<? super TAccessor, E> processor) throws E {
        final BooleanBox result = BoxFactory.createForBoolean(false);
        forEachAttribute((resource, accessor) -> {
            if(resource.equals(resourceName) && filter.test(accessor)){
                processor.accept(accessor);
                result.set(true);
                return false;
            }
            else
                return true;
        });
        return result.getAsBoolean();
    }
}
