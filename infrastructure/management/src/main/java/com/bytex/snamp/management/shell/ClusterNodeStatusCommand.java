package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.management.jmx.ResignOperation;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "cluster-node",
    description = "Show SNAMP node status in the cluster")
public class ClusterNodeStatusCommand extends OsgiCommandSupport implements SnampShellCommand {
    @Option(name = "-r", aliases = {"--resign"}, required = false, description = "Starts leader election")
    @SpecialUse
    private boolean startElection = false;

    @Override
    protected CharSequence doExecute() {
        final StringBuilder result = new StringBuilder();
        if (startElection) {
            if (ResignOperation.resign(bundleContext))
                IOUtils.appendln(result, "Resigned");
            else
                IOUtils.appendln(result, "This node is not in a cluster member");
        }
        IOUtils.appendln(result, "Is cluster member: %s", DistributedServices.isInCluster(bundleContext));
        IOUtils.appendln(result, "Active Member: %s", DistributedServices.isActiveNode(bundleContext));
        IOUtils.append(result, "Member Name: %s", DistributedServices.getLocalNodeName(bundleContext));
        return result;
    }
}
