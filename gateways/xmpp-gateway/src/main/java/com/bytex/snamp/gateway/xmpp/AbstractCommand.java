package com.bytex.snamp.gateway.xmpp;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.jivesoftware.smack.packet.Message;

import static com.bytex.snamp.internal.Utils.callAndWrapException;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
abstract class AbstractCommand extends BasicParser implements Command {
    static final Option RESOURCE_OPTION = new Option("r",
            "resource",
            true,
            "Name of the managed resource");

    AbstractCommand(final Options commandOptions){
        setOptions(commandOptions);
    }

    AbstractCommand(){
        this(new Options());
    }

    protected abstract Message doCommand(final CommandLine command) throws CommandException;

    @Override
    public final Message doCommand(final String[] arguments) throws CommandException {
        final CommandLine command = callAndWrapException(() -> parse(getOptions(), arguments), CommandException::new);
        return doCommand(command);
    }
}
