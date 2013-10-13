import com.snamp.TimeSpan;
import com.snamp.hosting.Agent;
import com.snamp.hosting.AgentConfiguration;
import com.snamp.hosting.ConfigurationFileFormat;
import junit.framework.TestCase;
import org.junit.Test;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.yaml.snakeyaml.Yaml;

import javax.management.*;
import java.io.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.net.URL;

public class JMXSimpleBeanTest extends TestCase
{
    public static final String checkString = "Some text is here";
    public static final String oidCheckPostfix = "1.1";
    public static final String objectName = "com.snampy.jmx:type=SimpleBean";
    public static final int localHostPort = 1161;
    public static final int localJMXPort = Integer.parseInt(System.getProperties().getProperty("com.sun.management.jmxremote.port"));
    public static final String oidPrefix = "1.1";



    public interface SimpleBeanMBean {

        String getString();
        void setString(String value);

    }
    public class SimpleBean implements SimpleBeanMBean{

        private String chosenString = null;

        public SimpleBean(String chosenString) {
            this.chosenString = chosenString;
        }

        @Override
        public synchronized String getString() {
            return this.chosenString;
        }

        @Override
        public void setString(String value) {
           this.chosenString = value;
        }
    }

    class SNMPManager {

        Snmp snmp = null;
        String address = null;

        /**
         * Constructor
         * @param add
         */
        public SNMPManager(String add)
        {
            address = add;
        }


        /**
         * Start the Snmp session. If you forget the listen() method you will not
         * get any answers because the communication is asynchronous
         * and the listen() method listens for answers.
         * @throws IOException
         */
        private void start() throws IOException {
            TransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
// Do not forget this line!
            transport.listen();
        }

        /**
         * Method which takes a single OID and returns the response from the agent as a String.
         * @param oid
         * @return
         * @throws IOException
         */
        public String getAsString(OID oid) throws IOException {
            ResponseEvent event = get(new OID[] { oid });
            return event.getResponse().get(0).getVariable().toString();
        }

        /**
         * This method is capable of handling multiple OIDs
         * @param oids
         * @return
         * @throws IOException
         */
        public ResponseEvent get(OID oids[]) throws IOException {
            PDU pdu = new PDU();
            for (OID oid : oids) {
                pdu.add(new VariableBinding(oid));
            }
            pdu.setType(PDU.GET);
            ResponseEvent event = snmp.send(pdu, getTarget(), null);
            if(event != null) {
                return event;
            }
            throw new RuntimeException("GET timed out");
        }

        /**
         * This method returns a Target, which contains information about
         * where the data should be fetched and how.
         * @return
         */
        private Target getTarget() {
            Address targetAddress = GenericAddress.parse(address);
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString("public"));
            target.setAddress(targetAddress);
            target.setRetries(2);
            target.setTimeout(1500);
            target.setVersion(SnmpConstants.version2c);
            return target;
        }

    }




    private static AgentConfiguration createTestConfig(){
        return new AgentConfiguration() {
            /**
             * Creates a new default configuration of the management target.
             *
             * @return A new default configuration of the management target.
             */

            @Override
            public HostingConfiguration getAgentHostingConfig() {
              return new HostingConfiguration() {
                  @Override
                  public String getAdapterName() {
                      return "snmp";
                  }

                  @Override
                  public void setAdapterName(final String adapterName) {
                      throw new UnsupportedOperationException();
                  }

                  @Override
                  public Map<String, String> getHostingParams() {
                      return new HashMap<String, String>(){{
                        put("port", "161");
                        put("address", "0.0.0.0");
                      }};
                  }
              };
            }

            @Override
            public Map<String, ManagementTargetConfiguration> getTargets() {
                Map<String, ManagementTargetConfiguration> targets = new HashMap<>();
                targets.put("test_server", new ManagementTargetConfiguration() {
                    /**
                     * Creates a new default attribute configuration.
                     *
                     * @return A new default attribute configuration.
                     */
                    @Override
                    public AttributeConfiguration newAttributeConfiguration() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public String getConnectionString() {
                        return String.format("service:jmx:rmi:///jndi/rmi://localhost:%s/jmxrmi", localJMXPort);
                    }

                    @Override
                    public void setConnectionString(String connectionString) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public String getConnectionType() {
                        return "jmx";
                    }

                    @Override
                    public void setConnectionType(String connectorType) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public String getNamespace() {
                        return oidPrefix;
                    }

                    @Override
                    public void setNamespace(String namespace) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public Map<String, AttributeConfiguration> getAttributes() {
                        return new HashMap<String, AttributeConfiguration>(){{
                            put(oidCheckPostfix,new AttributeConfiguration() {
                                @Override
                                public TimeSpan getReadWriteTimeout() {
                                    return new TimeSpan(1000);
                                }

                                @Override
                                public void setReadWriteTimeout(TimeSpan time) {
                                    throw new UnsupportedOperationException();
                                }

                                @Override
                                public String getAttributeName() {
                                    return "String";
                                }

                                @Override
                                public void setAttributeName(String attributeName) {
                                    throw new UnsupportedOperationException();
                                }

                                @Override
                                public Map<String, String> getAdditionalElements() {
                                    return new HashMap<String, String>(){{
                                        put("objectName",objectName);
                                    }};
                                }
                            });
                        }};
                    }

                    @Override
                    public Map<String, String> getAdditionalElements() {
                        return new HashMap<>();
                    }
                });
                return targets;
            }

            /**
             * Saves the current configuration into the specified stream.
             *
             * @param output
             * @throws UnsupportedOperationException Serialization is not supported.
             */
            @Override
            public void save(final OutputStream output) throws UnsupportedOperationException {
                throw new UnsupportedOperationException();
            }

            /**
             * Reads the file and fills the current instance.
             *
             * @param input
             * @throws UnsupportedOperationException Deserialization is not supported.
             */
            @Override
            public void load(final InputStream input) throws UnsupportedOperationException {
                throw new UnsupportedOperationException();
            }

            @Override
            public ManagementTargetConfiguration newManagementTargetConfiguration() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
    }

    @Test
    public void testGetSimpleBean() throws Exception {
        SimpleBean cache = new SimpleBean(checkString);

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName(objectName);
        mbs.registerMBean(cache, name);

        Thread backward = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(1000);
                    }
                    catch(InterruptedException e) { }
                }
            }
        });
        //Thread.sleep(100000000);

        ClassLoader classLoader = JMXSimpleBeanTest.class.getClassLoader();
        Class aClass = classLoader.loadClass("com.snamp.hosting.AgentHostingSandbox");
        final AgentConfiguration config = createTestConfig();
        try(final Agent hosting = new Agent(config.getAgentHostingConfig())){
            hosting.start(config.getTargets());

         //   Thread.sleep(100000000);

        SNMPManager client = new SNMPManager("udp:127.0.0.1/"+Integer.toString(localHostPort));
        client.start();

        String sysDescr = client.getAsString(new OID(oidPrefix + "." + oidCheckPostfix));

        assertEquals("Something wrong",sysDescr,checkString);

        backward.interrupt();

        }


    }

    @Test
    public void testYaml() throws IOException {

        //Get test file path
        URL inFile = this.getClass().getResource("/in.txt");
        AgentConfiguration config = null;
        //Load the configuration from file
        try(InputStream is = new FileInputStream(inFile.getFile()))
        {
            config = ConfigurationFileFormat.YAML.newAgentConfiguration();
            config.load(is);
        }
        //Check if configuration loaded properly
        assertNotNull(config);

        Map<String, AgentConfiguration.ManagementTargetConfiguration> targets = config.getTargets();
        //Make sure that there are two targets in configuration
        assertEquals(2, targets.size());

        AgentConfiguration.ManagementTargetConfiguration target = targets.get("wso-esb-1");
        //Check connection type
        assertEquals("SOAP", target.getConnectionType());

        //Change connection type and check if it is changed
        target.setConnectionType("HTTP");
        assertEquals("HTTP", target.getConnectionType());

        Map<String, AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration> attrs = target.getAttributes();
        //Check number of attributes
        assertEquals(1, attrs.size());

        AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration attr = attrs.get("1.2.3");
        //Check timeout, should be set to default value
        assertEquals(7000, attr.getReadWriteTimeout().duration);

        //Create and add new target
        AgentConfiguration.ManagementTargetConfiguration newTarget = config.newManagementTargetConfiguration();
        newTarget.setConnectionType("HTTPS");
        newTarget.setConnectionString("https://");
        newTarget.setNamespace("mynamespace");
        newTarget.getAdditionalElements().put("addelem", "value1");
        //Create new attribute
        AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration newAttr = newTarget.newAttributeConfiguration();
        newAttr.setAttributeName("newAttr");
        newAttr.setReadWriteTimeout(new TimeSpan(2890));
        newAttr.getAdditionalElements().put("attraddelem", "value2");
        //Put 3 attributes and two af them with same id
        newTarget.getAttributes().put("1.5.9", newAttr);
        newAttr.setAttributeName("newAttr1");
        newTarget.getAttributes().put("1.5.8", newAttr);
        newAttr.setAttributeName("newAttr3");
        newTarget.getAttributes().put("1.5.9", newAttr);

        //Attributes should be two
        assertEquals(2, newTarget.getAttributes().size());
        //Check that last attribute replases the firts ne
        assertEquals("newAttr3", newTarget.getAttributes().get("1.5.9").getAttributeName());
        //Put just made target into configuration with already existing name
        targets.put("wso-esb-1", newTarget);
        //Check that new target replases the existing one
        assertEquals(2, targets.size());

        URL outFile = this.getClass().getResource("/out.txt");
        try(OutputStream os = new FileOutputStream(outFile.getFile()))
        {
            config.save(os);
        }
        //Load the configuration from file again
        try(InputStream is = new FileInputStream(outFile.getFile()))
        {
            config = ConfigurationFileFormat.YAML.newAgentConfiguration();
            config.load(is);
        }

        AgentConfiguration.ManagementTargetConfiguration outTarget = targets.get("wso-esb-1");
        assertEquals("mynamespace", outTarget.getNamespace());
    }
}