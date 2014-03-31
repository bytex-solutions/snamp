package com.snamp.core.maintenance;

import com.snamp.FutureThread;

import java.lang.annotation.*;
import java.lang.ref.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Represents an abstract class for building your own maintainable object.
 * @param <T> Type of the enum that describes all maintenance actions supported by this class.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 * @see MaintenanceActionInfo
 */
public abstract class AbstractMaintainable<T extends Enum<T> & MaintenanceActionInfo> implements Maintainable {

    /**
     * Indicates that the annotated method is used for action invocation.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Action{
        /**
         * Determines whether the annotated action
         * @return
         */
        boolean localeSpecific() default false;
    }

    /**
     * Represents enum type that describes all maintenance actions.
     */
    protected final Class<T> maintenanceActions;

    /**
     * Initializes a new maintainable object.
     * @param actions An enum that describes all maintenance actions. Cannot be {@literal null}.
     * @throws IllegalArgumentException actions is {@literal null}.
     */
    protected AbstractMaintainable(final Class<T> actions){
        if(actions == null) throw new IllegalArgumentException("action is null.");
        maintenanceActions = actions;
    }

    /**
     * Returns read-only map of maintenance actions.
     *
     * @return Read-only map of maintenance action,
     */
    @Override
    public final Set<String> getActions() {
        return getMaintenanceActions(maintenanceActions);
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
        return getActionDescription(maintenanceActions, actionName, loc);
    }

    private static boolean isPublicInstance(final int modifiers){
        return Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers);
    }

    /**
     * Parses arguments string into an array of method invocation arguments.
     * @param action Action descriptor.
     * @param arguments The string to parse.
     * @param loc The localization of the action arguments.
     * @return An array of parsed arguments.
     */
    protected abstract Object[] parseArguments(final T action, final String arguments, final Locale loc);

    private static Future<String> doAction(final Reference<?> owner, final Method action, final Object[] arguments){
        final Action actionInfo = action.getAnnotation(Action.class);
        final FutureThread<String> thread = new FutureThread<>(new Callable<String>() {
            @Override
            public final String call() throws Exception {
                return Objects.toString(action.invoke(owner.get(), arguments));
            }
        });
        thread.start();
        return thread;
    }

    /**
     * Executes method that implements action logic.
     * <p>
     *     In the default implementation this method executes each
     *     action in separated thread.
     * </p>
     * @param action A method that implements action logic.
     * @param arguments A method invocation arguments.
     * @param loc The culture context of the specified action.
     * @return An object that represents asynchronous execution the action.
     */
    protected Future<String> doAction(final Method action, Object[] arguments, final Locale loc){
        final Action actionInfo = action.getAnnotation(Action.class);
        if(actionInfo.localeSpecific()){
            arguments = Arrays.copyOf(arguments, arguments.length + 1);
            arguments[arguments.length - 1] = loc;
        }
        return doAction(new WeakReference<>(this), action, arguments);
    }

    /**
     * Invokes maintenance action.
     * @param action An action to invoke.
     * @param arguments Action invocation arguments.
     * @param loc The culture context of the action invocation.
     * @return An object that represents asynchronous execution the action.
     */
    public final Future<String> doAction(final T action, final String arguments, final Locale loc){
        //find the action implemented through reflection
        for(final Method m: getClass().getMethods())
            if(isPublicInstance(m.getModifiers()) &&
                    m.isAnnotationPresent(Action.class) &&
                    Objects.equals(m.getName(), action.getName()))
                return doAction(m, parseArguments(action, arguments, loc), loc);
        return null;
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
        final T action = getAction(maintenanceActions, actionName);
        return action != null ? doAction(action, arguments, loc): null;
    }

    /**
     * Returns a collection of maintenance actions described by the specified enum.
     * @param actions An enum that describes maintenance action.
     * @param <T> Type of the enum with maintenance actions.
     * @return A collection of maintenance actions.
     */
    public static <T extends Enum<T> & MaintenanceActionInfo> Set<String> getMaintenanceActions(final Class<T> actions){
        final T[] values = actions.getEnumConstants();
        final Set<String> result = new HashSet<>(values.length);
        for(final T v: values)
            result.add(v.getName());
        return result;
    }

    /**
     * Returns an enum constant that describes the specified maintenance action.
     * @param actions An enum that describes maintenance action.
     * @param actionName The name of the maintenance action.
     * @param <T> Type of the enum that describes maintenance action.
     * @return Enum constant that describes the specified maintenance action.
     */
    public static <T extends Enum<T> & MaintenanceActionInfo> T getAction(final Class<T> actions, final String actionName){
        for(final T value: actions.getEnumConstants())
            if(Objects.equals(value.getName(), actionName)) return value;
        return null;
    }

    /**
     * Returns description of the specified action.
     * @param actions An enum type that describes maintenance actions.
     * @param actionName The name of the maintenance action.
     * @param loc The locale of the description.
     * @param <T> Type of the enum that describes maintenance actions.
     * @return Localized description of the maintenance action.
     */
    public static <T extends Enum<T> & MaintenanceActionInfo> String getActionDescription(final Class<T> actions, final String actionName, final Locale loc){
        final T value = getAction(actions, actionName);
        return value != null ? value.getDescription(loc) : "";
    }
}
