package com.bytex.snamp.management.shell;

import com.bytex.snamp.core.Communicator;
import static com.bytex.snamp.core.DistributedServices.getDistributedObject;
import static com.bytex.snamp.core.ClusterMember.COMMUNICATOR;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class MessageCommand extends SnampShellCommand  {
    final Communicator getCommunicator(){
        return getDistributedObject(getBundleContext(), "SnampShellCommunicator", COMMUNICATOR);
    }
}
