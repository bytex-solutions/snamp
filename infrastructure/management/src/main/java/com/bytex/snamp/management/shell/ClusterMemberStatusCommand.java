package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.management.jmx.ResignOperation;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.io.PrintWriter;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "cluster-member",
    description = "Show status of the local cluster member")
@Service
public class ClusterMemberStatusCommand extends ClusterMemberCommand {
    @Option(name = "-r", aliases = {"--resign"}, required = false, description = "Starts leader election")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private boolean startElection = false;

    @Override
    public void execute(final PrintWriter output) {
        final StringBuilder result = new StringBuilder();
        if (startElection) {
            if (ResignOperation.resign(getBundleContext()))
                output.println("Resigned");
            else
                output.println("This node is not in a cluster member");
        }
        output.format("Is cluster member: %s", ClusterMember.isInCluster(getBundleContext())).println();
        output.format("Active Member: %s", clusterMember.isActive()).println();
        output.format("Member Name: %s", clusterMember.getName());
    }
}
