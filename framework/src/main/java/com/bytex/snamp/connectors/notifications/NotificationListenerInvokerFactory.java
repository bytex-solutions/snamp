package com.bytex.snamp.connectors.notifications;

import javax.management.NotificationListener;
import java.util.concurrent.Executor;

/**
 * Represents commonly used listener invocation strategies.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class NotificationListenerInvokerFactory {

    private NotificationListenerInvokerFactory(){
        throw new InstantiationError();
    }

    /**
     * Creates an invoker that invokes a list of listeners sequentially (one-by-one) in the caller thread.
     * <p>
     *     If one of the listeners throws an exception then it can be handled by the caller code.
     * </p>
     * @return A new instance of the invoker.
     */
    public static NotificationListenerSequentialInvoker createSequentialInvoker(){
        return (n, handback, listeners) -> listeners.forEach(listener -> listener.handleNotification(n, handback));
    }

    /**
     * Represents notification listener exception handler.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
     */
    @FunctionalInterface
    public interface ExceptionHandler{
        /**
         * Handles the exception raised by notification listener.
         * @param e An exception to handle.
         * @param source A listener which throws an exception.
         */
        void handle(final Throwable e, final NotificationListener source);
    }

    /**
     * Creates an invoker that invokes each listener using {@link Executor#execute(Runnable)} method.
     * @param executor An executor that is used to apply each listener.
     * @return A new instance of the listener invoker that uses {@link Executor} for listener invocation.
     */
    public static NotificationListenerInvoker createParallelInvoker(final Executor executor) {
        return (n, handback, listeners) -> listeners.forEach(listener -> executor.execute(() -> listener.handleNotification(n, handback)));
    }

    public static NotificationListenerInvoker createParallelExceptionResistantInvoker(final Executor executor, final ExceptionHandler handler){
        return (n, handback, listeners) -> listeners.forEach(listener -> executor.execute(() -> {
            try{
                listener.handleNotification(n, handback);
            }
            catch (final Throwable e){
                handler.handle(e, listener);
            }
        }));
    }
}
