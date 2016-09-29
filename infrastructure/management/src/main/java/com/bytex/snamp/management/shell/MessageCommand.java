package com.bytex.snamp.management.shell;

import com.bytex.snamp.core.Communicator;
import com.bytex.snamp.core.DistributedServices;
import org.apache.karaf.shell.console.OsgiCommandSupport;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class MessageCommand extends OsgiCommandSupport implements SnampShellCommand {
    final Communicator getCommunicator(){
        return DistributedServices.getDistributedCommunicator(bundleContext, "SnampShellCommunicator");
    }
}
