import com.snamp.TimeSpan;
import com.snamp.hosting.AgentConfiguration;
import com.snamp.hosting.ConfigurationFileFormat;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.util.Map;

/**
 * Test env for YAML configuration object (including r/w from/to files, assertations etc)
 * User: temni
 * Date: 20.10.13
 * Time: 17:02
 */
public class YAMLTest extends SnampTestCase{

    @Test
    public void testYaml() throws IOException, ClassNotFoundException {

        //Get test file path
        final URL inFile = this.getClass().getResource("/in.txt");
        AgentConfiguration config = null;
        //Load the configuration from file
        try(final InputStream is = new FileInputStream(inFile.getFile()))
        {
            config = ConfigurationFileFormat.YAML.newAgentConfiguration();
            config.load(is);
        }
        //Check if configuration loaded properly
        assertNotNull(config);

        final Map<String, AgentConfiguration.ManagementTargetConfiguration> targets = config.getTargets();
        //Make sure that there are two targets in configuration
        assertEquals(2, targets.size());

        final AgentConfiguration.ManagementTargetConfiguration target = targets.get("wso-esb-1");
        //Check connection type
        assertEquals("SOAP", target.getConnectionType());

        //Change connection type and check if it is changed
        target.setConnectionType("HTTP");
        assertEquals("HTTP", target.getConnectionType());

        final Map<String, AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration> attrs = target.getAttributes();
        //Check number of attributes
        assertEquals(1, attrs.size());

        AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration attr = attrs.get("1.2.3");
        //Check timeout, should be set to default value
        assertEquals(7000, attr.getReadWriteTimeout().duration);

        //Create and add new target
        final AgentConfiguration.ManagementTargetConfiguration newTarget = config.newManagementTargetConfiguration();
        newTarget.setConnectionType("HTTPS");
        newTarget.setConnectionString("https://");
        newTarget.setNamespace("mynamespace");
        newTarget.getAdditionalElements().put("addelem", "value1");
        //Create new attribute
        final AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration newAttr = newTarget.newAttributeConfiguration();
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

        //HostingConfiguration test
        AgentConfiguration.HostingConfiguration hostConf= config.getAgentHostingConfig();
        assertEquals("SNMPAdapter", hostConf.getAdapterName());
        assertEquals(2, hostConf.getHostingParams().size());
        final Map<String, String> hostParams = hostConf.getHostingParams();
        hostParams.put("addelem", "changedValue");
        //Size still should be same
        assertEquals(2, hostConf.getHostingParams().size());
        //Value should be changed
        assertEquals("changedValue", hostConf.getHostingParams().get("addelem"));
        final URL outFile = this.getClass().getResource("/out.txt");
        try(final OutputStream os = new FileOutputStream(outFile.getFile()))
        {
            config.save(os);
        }
        //Load the configuration from file again
        try(final InputStream is = new FileInputStream(outFile.getFile()))
        {
            config = ConfigurationFileFormat.YAML.newAgentConfiguration();
            config.load(is);
        }

        final AgentConfiguration.ManagementTargetConfiguration outTarget = targets.get("wso-esb-1");
        assertEquals("mynamespace", outTarget.getNamespace());
    }

    @Test
    public void testCrashYamlConfig() throws IOException {
        final URL inFile = this.getClass().getResource("/err.txt");
        AgentConfiguration config = null;
        //Load the configuration from file
        try(final InputStream is = new FileInputStream(inFile.getFile()))
        {
            config = ConfigurationFileFormat.YAML.newAgentConfiguration();
            config.load(is);
        }

        assertNotNull(config.getTargets());

        assertEquals(0, config.getTargets().size());
    }
}
