package com.bytex.snamp.management.shell;

import com.bytex.snamp.core.Communicator;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
abstract class MessageCommand extends ClusterMemberCommand  {

    final Communicator getCommunicator(){
        return clusterMember.getService(Communicator.ofName("SnampShellCommunicator")).orElseThrow(AssertionError::new);
    }
}
