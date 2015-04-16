package com.itworks.snamp.adapters.xmpp;

import com.itworks.snamp.StringAppender;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.jivesoftware.smack.packet.Message;

import java.util.Collections;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ManageNotificationsCommand extends AbstractCommand {
    static final String NAME = "notif";
    static final String COMMAND_DESC = "Enables/disables notifications";
    static final String COMMAND_USAGE = "notif [-e resource-name...] | [-d resource-name...] | -a | -n | -l";
    private static final Option ENABLE_OPT = new Option("e", "enable", true, "Enables notifications of the managed resource");
    private static final Option DISABLE_OPT = new Option("d", "disable", true, "Disables notifications of the managed resource");
    private static final Option ALL_OPT = new Option("a", "all", false, "Enables all notifications");
    private static final Option NONE_OPT = new Option("n", "none", false, "Disables all notifications");
    static final Options COMMAND_OPTIONS = new Options()
            .addOption(ENABLE_OPT)
            .addOption(DISABLE_OPT)
            .addOption(ALL_OPT)
            .addOption(NONE_OPT);

    private final AllowedNotifications filter;

    ManageNotificationsCommand(final AllowedNotifications filter){
        super(COMMAND_OPTIONS);
        this.filter = filter;

    }

    private Message getEnabledNotifs(){
        final Message result = new Message();
        if(filter.isAny()) result.setBody("ANY");
        else if(filter.isEmpty()) result.setBody("NONE");
        else try(final StringAppender appender = new StringAppender(64)){
            for(final String resourceName: filter)
                appender.append(resourceName).newLine();
            result.setBody(appender.toString());
        }
        return result;
    }

    @Override
    protected Message doCommand(final CommandLine command) {
        if(command.getArgs().length == 0)
            return getEnabledNotifs();
        final String[] enabled = command.getOptionValues(ENABLE_OPT.getOpt());
        final String[] disabled = command.getOptionValues(DISABLE_OPT.getOpt());
        final boolean all = command.hasOption(ALL_OPT.getOpt());
        final boolean none = command.hasOption(NONE_OPT.getOpt());

        if(all) filter.allowAll();
        else if(enabled != null) Collections.addAll(filter, enabled);

        if(none) filter.clear();
        else if(disabled != null)
            for(final String dis: disabled)
                filter.remove(dis);

        return getEnabledNotifs();
    }
}
