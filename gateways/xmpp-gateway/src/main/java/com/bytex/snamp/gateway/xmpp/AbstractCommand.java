package com.bytex.snamp.gateway.xmpp;

import org.apache.commons.cli.*;
import org.jivesoftware.smack.packet.Message;
import static com.bytex.snamp.internal.Utils.callAndWrapException;

/**
 * @author Roman Sakno
 * @version 2.0
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
        final CommandLine command = callAndWrapException(() -> parse(getOptions(), arguments), CommandException::new);
        return doCommand(command);
    }
}
