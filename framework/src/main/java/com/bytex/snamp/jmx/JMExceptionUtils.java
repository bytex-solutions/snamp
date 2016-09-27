package com.bytex.snamp.jmx;

import javax.management.AttributeNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.NotificationListener;
import java.util.concurrent.Callable;
import static com.bytex.snamp.internal.Utils.callAndWrapException;

/**
 * Provides various methods for working with JMX exceptions.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class JMExceptionUtils {
    private JMExceptionUtils(){
        throw new InstantiationError();
    }

    public static AttributeNotFoundException attributeNotFound(final String attributeName){
        return new AttributeNotFoundException(String.format("Attribute %s doesn't exist.", attributeName));
    }

    public static ListenerNotFoundException listenerNotFound(final NotificationListener listener) {
        return new ListenerNotFoundException(String.format("Listener %s doesn't exist.", listener));
    }

    static <V> V assertCall(final Callable<V> task) {
        return callAndWrapException(task, e -> new AssertionError("Unexpected exception", e));
    }
}
