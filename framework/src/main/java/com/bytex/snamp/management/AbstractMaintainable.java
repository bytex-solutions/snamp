package com.bytex.snamp.management;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.concurrent.FutureThread;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.ObjectArrays;
import com.google.common.util.concurrent.Futures;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Represents an abstract class for building your own maintainable object.
 * <p>
 *     Derived class should not be inner private.
 * @param <T> Type of the enum that describes all maintenance actions supported by this class.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 * @see MaintenanceActionInfo
 */
public abstract class AbstractMaintainable<T extends Enum<T> & MaintenanceActionInfo> extends AbstractAggregator implements Maintainable {

    /**
     * Indicates that the annotated method is used for action invocation.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Action{
        /**
         * Represents default value of {@link #localeSpecific()} parameter.
         */
        boolean DEFAULT_LOCALE_SPECIFIC = false;

        /**
         * Determines whether the annotated action behavior is locale-specific.
         * <p>
         *  If action is locale-specific then the last parameter of the action
         *  should have {@link java.util.Locale} type.
         * @return {@literal true}, if action is locale-specific; otherwise, {@literal false}.
         */
        boolean localeSpecific() default DEFAULT_LOCALE_SPECIFIC;
    }

    /**
     * Represents action implementation. This class cannot be inherited or instantiated directly
     * from your code.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static final class ActionHandle{
        private final MethodHandle handle;
        /**
         * Indicates that the action behavior is locale-specific
         */
        public final boolean localeSpecific;

        private ActionHandle(final Maintainable owner,
                             final Method actionImpl,
                             final MethodHandles.Lookup resolver) throws IllegalAccessException {
            handle = isStatic(actionImpl.getModifiers()) ?
                    resolver.unreflect(actionImpl) :
                    resolver.unreflect(actionImpl).bindTo(owner);
            final Action actionInfo = actionImpl.getAnnotation(Action.class);
            localeSpecific = actionInfo != null ? actionInfo.localeSpecific() : Action.DEFAULT_LOCALE_SPECIFIC;
        }

        /**
         * Executes an action.
         * @param args Action invocation arguments.
         * @param loc The locale of the action behavior.
         * @return Action invocation result.
         * @throws Exception Some troubles
         */
        public String doAction(Object[] args, final Locale loc) throws Exception {
            if (localeSpecific)
                args = ObjectArrays.concat(args, loc);
            try {
                return Objects.toString(handle.invokeWithArguments(args));
            } catch (final Exception | Error e) {
                throw e;
            } catch (final Throwable e) {
                throw new UndeclaredThrowableException(e);
            }
        }

        @Override
        public int hashCode() {
            return localeSpecific ? handle.hashCode() ^ 1 : handle.hashCode();
        }

        private boolean equals(final ActionHandle other){
            return handle.equals(other.handle);
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof ActionHandle ? equals((ActionHandle)other) : handle.equals(other);
        }

        @Override
        public String toString() {
            return handle.toString();
        }
    }

    private final Cache<T, ActionHandle> actionCache;
    private final EnumSet<T> allActions;
    private final MethodHandles.Lookup actionResolver;

    /**
     * Initializes a new maintainable object.
     * @param actions A set of supported actions. Cannot be {@literal null}.
     * @throws IllegalArgumentException actions is {@literal null}.
     */
    protected AbstractMaintainable(final EnumSet<T> actions) {
        this.allActions = Objects.requireNonNull(actions, "actions is null.");
        this.actionCache = CacheBuilder
                .<T, MethodHandle>newBuilder()
                .maximumSize(actions.size())
                .build();
        this.actionResolver = MethodHandles.lookup().in(getClass());
    }

    /**
     * Initializes a new maintainable object.
     * @param actions An enum that describes all maintenance actions. Cannot be {@literal null}.
     * @throws IllegalArgumentException actions is {@literal null}.
     */
    protected AbstractMaintainable(final Class<T> actions){
        this(EnumSet.allOf(actions));
    }

    /**
     * Returns read-only map of maintenance actions.
     *
     * @return Read-only map of maintenance action,
     */
    @Override
    public final Set<String> getActions() {
        return getMaintenanceActions(allActions);
    }

    /**
     * Returns human-readable description of the specified maintenance action that
     * includes description of the arguments string.
     *
     * @param actionName The name of the maintenance action.
     * @param loc        Target locale of the action description.
     * @return Localized description of the action.
     */
    @Override
    public final String getActionDescription(final String actionName, final Locale loc) {
        return getActionDescription(allActions, actionName, loc);
    }

    private static boolean isPublic(final int modifiers){
        return Modifier.isPublic(modifiers);
    }

    private static boolean isStatic(final int modifiers){
        return Modifier.isStatic(modifiers);
    }

    /**
     * Parses arguments string into an array of method invocation arguments.
     * @param action Action descriptor.
     * @param arguments The string to parse.
     * @param loc The localization of the action arguments.
     * @return An array of parsed arguments.
     * @throws java.text.ParseException Unable to parse arguments.
     */
    protected abstract Object[] parseArguments(final T action, final String arguments, final Locale loc) throws ParseException;

    /**
     * Executes method that implements action logic.
     * <p>
     *     In the default implementation this method executes each
     *     action in separated thread.
     * @param action Bounded method handle that references the action implementation logic.
     * @param arguments A method invocation arguments.
     * @param loc The locale of the action behavior.
     * @return An object that represents asynchronous execution the action.
     */
    protected Future<String> doAction(final ActionHandle action, final Object[] arguments, final Locale loc) {
        return FutureThread.start(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return action.doAction(arguments, loc);
            }

            @Override
            public String toString() {
                return action.toString();
            }
        });
    }

    private static ActionHandle findActionImplementation(final Maintainable maintainable,
                                                   final MaintenanceActionInfo action,
                                                   final MethodHandles.Lookup resolver) throws IllegalAccessException, NoSuchElementException {
        for (final Method m : maintainable.getClass().getDeclaredMethods())
            if (isPublic(m.getModifiers()) &&
                    m.isAnnotationPresent(Action.class) &&
                    Objects.equals(m.getName(), action.getName()))
                return new ActionHandle(maintainable, m, resolver);
        throw new NoSuchElementException(String.format("An implementation of %s action doesn't exist", action.getName()));
    }

    /**
     * Invokes maintenance action.
     * @param action An action to invoke.
     * @param arguments Action invocation arguments.
     * @param loc The culture context of the action invocation.
     * @return An object that represents asynchronous execution the action.
     */
    public final Future<String> doAction(final T action, final String arguments, final Locale loc) {
        //find the action implemented through reflection
        try {
            final ActionHandle actionImpl = actionCache.get(action, new Callable<ActionHandle>() {
                @Override
                public ActionHandle call() throws IllegalAccessException {
                    return findActionImplementation(AbstractMaintainable.this, action, actionResolver);
                }
            });
            if (actionImpl == null)
                throw new NoSuchElementException(String.format("Action %s doesn't exist", action.getName()));
            else return doAction(actionImpl, parseArguments(action, arguments, loc), loc);
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof Error)
                throw (Error) cause;
            else if (cause instanceof Exception)
                return Futures.immediateFailedCheckedFuture((Exception) cause);
            else return Futures.immediateFailedCheckedFuture(e);
        } catch (final NoSuchElementException | ParseException e) {
            return Futures.immediateFailedCheckedFuture(e);
        }
    }

    /**
     * Invokes maintenance action.
     *
     * @param actionName The name of the action to invoke.
     * @param arguments  The action invocation command line. May be {@literal null} or empty for parameterless
     *                   action.
     * @param loc        Localization of the action arguments string and invocation result.
     * @return The localized result of the action invocation.
     */
    @Override
    public final Future<String> doAction(final String actionName, final String arguments, final Locale loc) {
        final T action = getAction(allActions, actionName);
        return action != null ? doAction(action, arguments, loc): null;
    }

    private static <T extends Enum<T> & MaintenanceActionInfo> Set<String> getMaintenanceActions(final Iterable<T> actions) {
        return ImmutableSet.copyOf(Iterables.transform(actions, new Function<T, String>() {
            @Override
            public String apply(final T input) {
                return input.getName();
            }
        }));
    }

    /**
     * Returns a collection of maintenance actions described by the specified enum.
     * @param actions An enum that describes maintenance action.
     * @param <T> Type of the enum with maintenance actions.
     * @return A collection of maintenance actions.
     */
    public static <T extends Enum<T> & MaintenanceActionInfo> Set<String> getMaintenanceActions(final Class<T> actions){
        return getMaintenanceActions(EnumSet.allOf(actions));
    }

    /**
     * Returns an enum constant that describes the specified maintenance action.
     * @param actions An enum that describes maintenance action.
     * @param actionName The name of the maintenance action.
     * @param <T> Type of the enum that describes maintenance action.
     * @return Enum constant that describes the specified maintenance action.
     * @throws java.util.NoSuchElementException Action with the specified name doesn't exist.
     */
    public static <T extends Enum<T> & MaintenanceActionInfo> T getAction(final Set<T> actions, final String actionName) {
        return Iterables.find(actions, new Predicate<T>() {
            @Override
            public boolean apply(final T input) {
                return Objects.equals(input.getName(), actionName);
            }
        });
    }

    /**
     * Returns an enum constant that describes the specified maintenance action.
     * @param actions An enum that describes maintenance action.
     * @param actionName The name of the maintenance action.
     * @param <T> Type of the enum that describes maintenance action.
     * @return Enum constant that describes the specified maintenance action.
     * @throws java.util.NoSuchElementException Action with the specified name doesn't exist.
     */
    public static <T extends Enum<T> & MaintenanceActionInfo> T getAction(final Class<T> actions, final String actionName){
        return getAction(EnumSet.allOf(actions), actionName);
    }

    /**
     * Returns description of the specified action.
     * @param actions A set of maintenance actions.
     * @param actionName The name of the maintenance action.
     * @param loc The locale of the description.
     * @param <T> Type of the enum that describes maintenance actions.
     * @return Localized description of the maintenance action.
     * @throws java.util.NoSuchElementException Action with the specified name doesn't exist.
     */
    public static <T extends Enum<T> & MaintenanceActionInfo> String getActionDescription(final Set<T> actions, final String actionName, final Locale loc){
        return getAction(actions, actionName).getDescription(loc);
    }

    /**
     * Returns description of the specified action.
     * @param actions An enum type that describes maintenance actions.
     * @param actionName The name of the maintenance action.
     * @param loc The locale of the description.
     * @param <T> Type of the enum that describes maintenance actions.
     * @return Localized description of the maintenance action.
     * @throws java.util.NoSuchElementException Action with the specified name doesn't exist.
     */
    public static <T extends Enum<T> & MaintenanceActionInfo> String getActionDescription(final Class<T> actions, final String actionName, final Locale loc){
        return getActionDescription(EnumSet.allOf(actions), actionName, loc);
    }
}
