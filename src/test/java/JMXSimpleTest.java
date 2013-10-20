/**
 * Created with IntelliJ IDEA.
 * User: temni
 * Date: 20.10.13
 * Time: 17:26
 * To change this template use File | Settings | File Templates.
 */

import com.snamp.hosting.Agent;
import com.snamp.hosting.AgentConfiguration;
import org.junit.Test;
import org.snmp4j.smi.OID;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

public class JMXSimpleTest extends AbstractJMXSimpleBeanTest {

    // JMX String checking
    final String checkString = "String attribute";

    private static final String oidPrefix = "1.1";
    private static final String objectName = "com.snampy.jmx:type=SimpleBean";
    private static final int localHostPort = 1161;
    private static final int localJMXPort = Integer.parseInt(System.getProperties().getProperty("com.sun.management.jmxremote.port"));
    private static final Map<String, String> attributes = new HashMap<String,String>(){{
        put("1.1","String");
    }};

    public JMXSimpleTest() {
        super(oidPrefix,objectName, localHostPort, localJMXPort, attributes);
    }

    @Test
    public void testGetSimpleBean() throws Exception {
        final SimpleBean cache = new SimpleBean(checkString);
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        final ObjectName name = new ObjectName(objectName);
        mbs.registerMBean(cache, name);

   /*     final Thread backward = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(1000);
                    }
                    catch(InterruptedException e) { }
                }
            }
        });   */

        final AgentConfiguration config = createTestConfig();
        try(final Agent hosting = new Agent(config.getAgentHostingConfig())){
            hosting.start(config.getTargets());

            final SNMPManager client = new SNMPManager("udp:127.0.0.1/"+Integer.toString(localHostPort));
            client.start();

            final String sysDescr = client.getAsString(new OID(oidPrefix + "." + "1.1"));

            assertEquals("Checking String attribute failed",sysDescr,checkString);

          //  backward.interrupt();

        }
    }

}
