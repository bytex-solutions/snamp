/**
 * Created with IntelliJ IDEA.
 * User: temni
 * Date: 20.10.13
 * Time: 17:16
 * This class implements SimpleBeanMBean interface and needed to test
 * attributes supported by JMX adapter
 */
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