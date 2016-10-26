package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.management.jmx.ResignOperation;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import static com.bytex.snamp.management.shell.Utils.append;
import static com.bytex.snamp.management.shell.Utils.appendln;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "cluster-member",
    description = "Show status of the local cluster member")
public class ClusterMemberStatusCommand extends OsgiCommandSupport implements SnampShellCommand {
    @Option(name = "-r", aliases = {"--resign"}, required = false, description = "Starts leader election")
    @SpecialUse
    private boolean startElection = false;

    @Override
    protected CharSequence doExecute() {
        final StringBuilder result = new StringBuilder();
        if (startElection) {
            if (ResignOperation.resign(bundleContext))
                appendln(result, "Resigned");
            else
                appendln(result, "This node is not in a cluster member");
        }
        appendln(result, "Is cluster member: %s", DistributedServices.isInCluster(bundleContext));
        appendln(result, "Active Member: %s", DistributedServices.isActiveNode(bundleContext));
        append(result, "Member Name: %s", DistributedServices.getLocalMemberName(bundleContext));
        return result;
    }
}
