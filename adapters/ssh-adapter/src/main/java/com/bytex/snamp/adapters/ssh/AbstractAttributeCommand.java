package com.bytex.snamp.adapters.ssh;

import org.apache.commons.cli.Option;

/**
 * An abstract class for set/get attribute command.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractAttributeCommand extends AbstractManagementShellCommand {
    protected static final Option NAME_OPTION = new Option("n", "name", true, "Attribute name");

    protected AbstractAttributeCommand(final CommandExecutionContext context) {
        super(context);
    }
}
