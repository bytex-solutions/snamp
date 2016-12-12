package com.bytex.snamp.management.shell;

import com.bytex.snamp.core.Communicator;
import com.bytex.snamp.core.DistributedServices;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class MessageCommand extends SnampShellCommand  {
    final Communicator getCommunicator(){
        return DistributedServices.getDistributedCommunicator(getBundleContext(), "SnampShellCommunicator");
    }
}
