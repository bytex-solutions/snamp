import com.snamp.licensing.JmxConnectorLimitations;
import com.snamp.licensing.LicensingException;
import org.junit.Test;

/**
 * Represents SNAMP licensing infrastructure tests.
 * @author roman
 */
public final class LicensingTest extends SnampTestCase {

    @Test
    public void jmxConnectorLicenseLimitations() throws ClassNotFoundException {
        JmxConnectorLimitations.current().verifyMaxAttributeCount(10);
        JmxConnectorLimitations.current().verifyMaxInstanceCount(10);
        //JmxConnectorLimitations.current().verifyPluginVersion((Class<ManagementConnectorFactoryBase>)Class.forName("com.snamp.connectors.jmx.JmxConnectorFactory"));
    }

    @Test(expected = LicensingException.class)
    public void limitationFailures(){
        JmxConnectorLimitations.current().verifyMaxInstanceCount(1000000);
    }
}
