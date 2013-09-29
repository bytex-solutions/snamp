import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.apache.log4j.jmx.AbstractDynamicMBean;
import org.junit.Test;
import junit.framework.TestCase;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class JMXSimpleBeanTest extends TestCase
{
    public static final String checkString = "Some text is here";

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

    @Test
    public void testGetSimpleBean() throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, InterruptedException {
        SimpleBean cache = new SimpleBean(checkString);
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("com.snampy.jmx:type=SimpleBean");
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


        backward.interrupt();


    }
}