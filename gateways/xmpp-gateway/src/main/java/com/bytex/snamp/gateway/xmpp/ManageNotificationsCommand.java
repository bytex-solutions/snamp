package com.bytex.snamp.gateway.xmpp;

import com.bytex.snamp.jmx.ExpressionBasedDescriptorFilter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.jivesoftware.smack.packet.Message;

import java.util.concurrent.atomic.AtomicReference;

import static com.bytex.snamp.internal.Utils.callAndWrapException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class ManageNotificationsCommand extends AbstractCommand {
    static final String NAME = "notifs";
    static final String COMMAND_DESC = "Enables/disables notifications";
    static final String COMMAND_USAGE = String.format(XMPPNotificationAccessor.LISTEN_COMMAND_PATTERN, "[-f filter-expression]");
    private static final Option FILTER_OPT = new Option("f", "filter", true, "RFC 1960 filter for notifications");
    static final Options COMMAND_OPTIONS = new Options()
            .addOption(FILTER_OPT);

    private final AtomicReference<ExpressionBasedDescriptorFilter> filter;

    ManageNotificationsCommand(final AtomicReference<ExpressionBasedDescriptorFilter> filter){
        super(COMMAND_OPTIONS);
        this.filter = filter;
    }

    @Override
    protected Message doCommand(final CommandLine command) throws CommandException {
        if (command.hasOption(FILTER_OPT.getOpt()))
            callAndWrapException(() -> {
                final String expr = command.getOptionValue(FILTER_OPT.getOpt());
                filter.set(new ExpressionBasedDescriptorFilter(expr));
                return null;
            }, CommandException::new);
        else
            filter.set(null);
        return null;
    }
}
