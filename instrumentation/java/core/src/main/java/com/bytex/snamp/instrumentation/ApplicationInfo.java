package com.bytex.snamp.instrumentation;

/**
 * Represents information about entire application/service/component.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public abstract class ApplicationInfo {
    /**
     * Represents JVM system property that holds instance of entire application/service/component.
     */
    public static final String INSTANCE_SYSTEM_PROPERTY = ComponentInstanceSource.INSTANCE_SYSTEM_PROPERTY;

    /**
     * Represents JVM system property that holds name of entire application/service/component.
     */
    public static final String NAME_SYSTEM_PROPERTY = ComponentNameSource.NAME_SYSTEM_PROPERTY;

    private static String name;
    private static String instance;

    static {
        name = getDefaultName();
        instance = getDefaultInstance();
    }

    /**
     * This constructor is declared for inheritance purposes only.
     */
    protected ApplicationInfo(){
    }

    /**
     * Gets name of entire application/service/component.
     * @return Name of entire application/service/component.
     */
    public static String getName(){
        return name;
    }

    protected static void setName(final String value){
        if(value == null || value.isEmpty())
            throw new IllegalArgumentException("Cannot be null or empty");
        name = value;
    }

    /**
     * Gets instance of entire application/service/component.
     * @return Instance of entire application/service/component.
     */
    public static String getInstance(){
        return instance;
    }

    protected static void setInstance(final String value){
        if(value == null || value.isEmpty())
            throw new IllegalArgumentException("Cannot be null or empty");
        instance = value;
    }

    /**
     * Tries to resolve instance of the entire application/service/component.
     * @return Instance of the entire application/service/component.
     */
    public static String getDefaultInstance() {
        for (final ComponentInstanceSource source : ComponentInstanceSource.values()) {
            final String name = source.getInstance();
            if (name != null && !name.isEmpty())
                return name;
        }
        return "";
    }

    /**
     * Tries to resolve name of the entire application/service/component.
     * @return Name of the entire application/service/component.
     */
    public static String getDefaultName() {
        for (final ComponentNameSource source : ComponentNameSource.values()) {
            final String name = source.getName();
            if (name != null && !name.isEmpty())
                return name;
        }
        return "";
    }
}
