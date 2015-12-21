package com.bytex.snamp.management.shell;

import org.apache.karaf.shell.commands.Action;

/**
 * Represents Karaf shell command for manipulating by SNAMP components.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface SnampShellCommand extends Action {
    /**
     * Namespace of commands.
     */
    String SCOPE = "snamp";
}
