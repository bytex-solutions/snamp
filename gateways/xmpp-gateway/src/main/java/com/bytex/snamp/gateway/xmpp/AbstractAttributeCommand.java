package com.bytex.snamp.gateway.xmpp;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
abstract class AbstractAttributeCommand extends AbstractCommand {
    static final Option NAME_OPTION = new Option("n", "name", true, "Attribute name");

    AbstractAttributeCommand(final Options commandOptions) {
        super(commandOptions);
    }

    AbstractAttributeCommand() {
    }
}
