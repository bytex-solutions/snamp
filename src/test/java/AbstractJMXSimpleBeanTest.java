import com.snamp.TimeSpan;
import com.snamp.hosting.Agent;
import com.snamp.hosting.AgentConfiguration;
import junit.framework.TestCase;
import org.junit.Test;
import org.snmp4j.smi.OID;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class AbstractJMXSimpleBeanTest
{

    protected final String objectName;
    protected final int localHostPort;
    protected final int localJMXPort;
    protected final String oidPrefix;
    protected final Map<String,String> attributes;

    protected AbstractJMXSimpleBeanTest() {
        oidPrefix = "1.1";
        objectName = this.getClass().getPackage().getName() + ":type=" + this.getClass().getName();//"com.snampy.jmx:type=SimpleBean";
        localHostPort = 161; //1161
        localJMXPort = 9010; //Integer.parseInt(System.getProperties().getProperty("com.sun.management.jmxremote.port"));
        attributes = new HashMap<>();
    }

    protected AbstractJMXSimpleBeanTest(String oidPrefix, String objectName, int localHostPort, int localJMXPort, Map<String,String> attributes) {
        this.oidPrefix = oidPrefix;
        this.objectName = objectName;
        this.localHostPort = localHostPort;
        this.localJMXPort = localJMXPort;
        this.attributes = attributes;
    }

    protected AgentConfiguration createTestConfig(){
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
                        put("port", String.valueOf(localHostPort));
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

                            Iterator it = attributes.entrySet().iterator();
                            while (it.hasNext()) {
                                final Map.Entry pairs = (Map.Entry)it.next();
                                put((String) pairs.getKey(),new AttributeConfiguration() {
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
                                        return (String)pairs.getValue();
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
                                it.remove(); // avoids a ConcurrentModificationException
                            }
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
                return null;
            }
        };
    }


}