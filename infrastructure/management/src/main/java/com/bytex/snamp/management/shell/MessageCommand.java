package com.bytex.snamp.management.shell;

import com.bytex.snamp.core.Communicator;

import static com.bytex.snamp.core.SharedObjectType.COMMUNICATOR;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class MessageCommand extends ClusterMemberCommand  {

    final Communicator getCommunicator(){
        return clusterMember.getService("SnampShellCommunicator", COMMUNICATOR).orElseThrow(AssertionError::new);
    }
}
