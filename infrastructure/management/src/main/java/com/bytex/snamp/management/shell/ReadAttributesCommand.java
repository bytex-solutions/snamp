package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connectors.ManagedResourceConnectorClient;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import javax.management.DynamicMBean;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import java.io.PrintStream;

/**
 * Read attribute value.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "read-attributes")
public final class ReadAttributesCommand extends OsgiCommandSupport implements SnampShellCommand {
    @Argument(index = 0, name = "resource", required = true, description = "Name of the resource to read")
    @SpecialUse
    private String resourceName = "";

    @Option(name = "-n", aliases = "--name", multiValued = true, required = false, description = "Name of the attribute to read")
    @SpecialUse
    private String[] attributes = ArrayUtils.emptyArray(String[].class);

    @Option(name = "-t", aliases = "--period", multiValued = false, required = false, description = "Period for reading attributes, in millis")
    @SpecialUse
    private int readPeriodMillis = 0;

    private static void readAttributes(final DynamicMBean bean, final String[] attributes, final PrintStream output) throws JMException {
        for(final String name: attributes)
            output.println(String.format("%s = %s", name, bean.getAttribute(name)));
    }

    private static String[] getNames(final MBeanAttributeInfo[] attributes){
        final String[] result = new String[attributes.length];
        for(int i = 0; i < attributes.length; i++)
            result[i] = attributes[i].getName();
        return result;
    }

    @Override
    protected Void doExecute() throws JMException, InterruptedException {
        final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(bundleContext, resourceName);
        try {
            String[] attributes = this.attributes;
            if(ArrayUtils.isNullOrEmpty(attributes))
                attributes = getNames(client.getMBeanInfo().getAttributes());
            //read attributes infinitely
            if (readPeriodMillis > 0) {
                session.getConsole().println("Press CTRL+C to stop reading attributes");
                while (true) {
                    readAttributes(client, attributes, session.getConsole());
                    Thread.sleep(readPeriodMillis);//InterruptedException when CTRL+C was pressed
                }
            }
            //read attributes and exit
            else readAttributes(client, attributes, session.getConsole());
            return null;
        } finally {
            client.release(bundleContext);
        }
    }
}
