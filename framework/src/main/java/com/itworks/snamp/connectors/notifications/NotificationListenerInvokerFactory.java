package com.itworks.snamp.connectors.notifications;

import javax.management.Notification;
import javax.management.NotificationListener;
import java.util.concurrent.ExecutorService;

/**
 * Represents commonly used listener invocation strategies.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class NotificationListenerInvokerFactory {

    private NotificationListenerInvokerFactory(){

    }

    /**
     * Creates an invoker that invokes a list of listeners sequentially (one-by-one) in the caller thread.
     * <p>
     *     If one of the listeners throws an exception then it can be handled by the caller code.
     * </p>
     * @return A new instance of the invoker.
     */
    public static NotificationListenerInvoker createSequentialInvoker(){
        return new NotificationListenerInvoker() {
            @Override
            public void invoke(final Notification n, final Object handback, final Iterable<? extends NotificationListener> listeners) {
                for(final NotificationListener listener: listeners)
                    listener.handleNotification(n, handback);
            }
        };
    }

    /**
     * Represents notification listener exception handler.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static interface ExceptionHandler{
        /**
         * Handles the exception raised by notification listener.
         * @param e An exception to handle.
         * @param source A listener which throws an exception.
         */
        void handle(final Throwable e, final NotificationListener source);
    }

    /**
     * Creates a new invoker that invokes each notification listener sequentially in the caller thread
     * in exception-safe manner.
     * @param handler An exception handler that captures exception raised by the notification listener.
     * @return A new instance of the invoker.
     */
    public static NotificationListenerInvoker createExceptionResistantInvoker(final ExceptionHandler handler){
        return new NotificationListenerInvoker() {
            @Override
            public void invoke(final Notification n, final Object handback, final Iterable<? extends NotificationListener> listeners) {
                for(final NotificationListener listener: listeners)
                    try{
                        listener.handleNotification(n, handback);
                    }
                    catch (final Throwable e){
                        handler.handle(e, listener);
                    }
            }
        };
    }

    /**
     * Creates an invoker that invokes each listener using {@link ExecutorService#execute(Runnable)} method.
     * @param executor An executor that is used to apply each listener.
     * @return A new instance of the listener invoker that uses {@link ExecutorService} for listener invocation.
     */
    public static NotificationListenerInvoker createParallelInvoker(final ExecutorService executor){
        return new NotificationListenerInvoker() {
            @Override
            public void invoke(final Notification n, final Object handback, final Iterable<? extends NotificationListener> listeners) {
                for(final NotificationListener listener: listeners)
                    executor.execute(new Runnable() {
                        @Override
                        public final void run() {
                            listener.handleNotification(n, handback);
                        }
                    });
            }
        };
    }

    public static NotificationListenerInvoker createParallelExceptionResistantInvoker(final ExecutorService executor, final ExceptionHandler handler){
        return new NotificationListenerInvoker() {
            @Override
            public void invoke(final Notification n, final Object handback, final Iterable<? extends NotificationListener> listeners) {
                for(final NotificationListener listener: listeners)
                    executor.execute(new Runnable() {
                        @Override
                        public final void run() {
                            try{
                                listener.handleNotification(n, handback);
                            }
                            catch (final Throwable e){
                                handler.handle(e, listener);
                            }
                        }
                    });
            }
        };
    }
}
