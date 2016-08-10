package com.bytex.snamp.gateway.xmpp;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.jivesoftware.smack.packet.Message;

/**
 * Prints connected managed resources.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class ListOfResourcesCommand extends AbstractCommand {
    static final String NAME = "resources";
    static final Options COMMAND_OPTIONS = new Options();

    static final String COMMAND_DESC = "Display list of connected managed resources";

    private final AttributeReader reader;

    ListOfResourcesCommand(final AttributeReader reader) {
        super(COMMAND_OPTIONS);
        this.reader = reader;
    }

    @Override
    protected Message doCommand(final CommandLine command) {
        final StringBuilder resources = new StringBuilder(64);
        reader.getHostedResources().forEach(resources::append);
        final Message msg = new Message();
        msg.setSubject("List of managed resources");
        msg.setBody(resources.toString());
        return msg;
    }
}
