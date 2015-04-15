package com.itworks.snamp.adapters.xmpp;

import org.apache.commons.cli.*;
import org.jivesoftware.smack.packet.Message;

import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractCommand extends BasicParser implements Command {
    private final Options options;
    protected static final Option RESOURCE_OPTION = new Option("r",
            "resource",
            true,
            "Name of the managed resource");

    protected AbstractCommand(final Options commandOptions){
        this.options = Objects.requireNonNull(commandOptions);
    }

    protected AbstractCommand(){
        this(new Options());
    }

    protected abstract Message doCommand(final CommandLine command) throws CommandException;

    @Override
    public final Message doCommand(final String[] arguments) throws CommandException {
        final CommandLine command;
        try {
            command = parse(options, arguments);
        } catch (final ParseException e) {
            throw new CommandException(e);
        }
        return doCommand(command);
    }
}
