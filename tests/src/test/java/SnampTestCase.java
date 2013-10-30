import com.snamp.licensing.LicenseReader;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 * Represents an abstract class for building SNAMP tests.
 * @author roman
 */
public abstract class SnampTestCase extends Assert {
    public static final String TEST_LICENCE_FILE = "unlimited.lic";

    static {
        System.setProperty(LicenseReader.LICENSE_FILE_PROPERTY, TEST_LICENCE_FILE);
        LicenseReader.reloadCurrentLicense();
    }
}
