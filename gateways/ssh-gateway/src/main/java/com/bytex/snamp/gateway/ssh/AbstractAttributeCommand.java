package com.bytex.snamp.gateway.ssh;

import org.apache.commons.cli.Option;

/**
 * An abstract class for set/get attribute command.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
abstract class AbstractAttributeCommand extends AbstractManagementShellCommand {
    static final Option NAME_OPTION = new Option("n", "name", true, "Attribute name");
    AbstractAttributeCommand(final CommandExecutionContext context) {
        super(context);
    }
}
