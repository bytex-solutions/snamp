package com.bytex.snamp.instrumentation.measurements;

import java.lang.management.ManagementFactory;
import java.util.regex.Pattern;

/**
 * Provides different sources of component name.
 */
enum ComponentNameSource {  //WARNING: order of this enum is significant for callers
    /**
     * Extracts component name using Spring configuration property.
     */
    SPRING {
        @Override
        String getName() {
            //https://github.com/spring-projects/spring-boot/blob/master/spring-boot/src/main/java/org/springframework/boot/context/ContextIdApplicationContextInitializer.java
            return Utils.getSystemProperty("spring.application.name", "vcap.application.name", "spring.config.name");
        }
    },

    /**
     * Extracts component name from command-line string of this process.
     */
    COMMAND_LINE{
        private final Pattern CLASS_NAME_SPLITTER = Pattern.compile("\\s+");
        private final Pattern DOT_SPLITTER = Pattern.compile("\\.");

        @Override
        String getName() {
            String appName = System.getProperty("sun.java.command");
            if(appName != null && !appName.isEmpty()) {
                final String[] classParts = DOT_SPLITTER.split(CLASS_NAME_SPLITTER.split(appName)[0]);
                appName = classParts.length > 0 ? classParts[classParts.length - 1] : "";
            }
            return appName;
        }
    },

    /**
     * Extracts component name from JVM.
     */
    JVM{
        @Override
        String getName() {
            return ManagementFactory.getRuntimeMXBean().getName();
        }
    };

    /**
     * Gets component name using the specified source.
     * @return Component name.
     */
    abstract String getName();
}
