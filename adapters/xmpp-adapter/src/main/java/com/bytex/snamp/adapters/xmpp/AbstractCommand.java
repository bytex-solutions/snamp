package com.bytex.snamp.adapters.xmpp;

import org.apache.commons.cli.*;
import org.jivesoftware.smack.packet.Message;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
abstract class AbstractCommand extends BasicParser implements Command {
    protected static final Option RESOURCE_OPTION = new Option("r",
            "resource",
            true,
            "Name of the managed resource");

    protected AbstractCommand(final Options commandOptions){
        setOptions(commandOptions);
    }

    protected AbstractCommand(){
        this(new Options());
    }

    protected abstract Message doCommand(final CommandLine command) throws CommandException;

    @Override
    public final Message doCommand(final String[] arguments) throws CommandException {
        final CommandLine command;
        try {
            command = parse(getOptions(), arguments);
        } catch (final ParseException e) {
            throw new CommandException(e);
        }
        return doCommand(command);
    }
}
