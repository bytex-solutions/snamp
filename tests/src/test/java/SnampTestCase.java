import com.snamp.licensing.LicenseReader;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 * Represents an abstract class for building SNAMP tests.
 * @author roman
 */
public abstract class SnampTestCase extends Assert {

    static {
        System.setProperty(LicenseReader.LICENSE_FILE_PROPERTY, "unlimited.lic");
        LicenseReader.reloadCurrentLicense();
    }
}
