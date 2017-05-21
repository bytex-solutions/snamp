@Grab(group = 'com.bytex.snamp.supervisors', module = 'default', version = '2.0.0', transitive = true)
import com.bytex.snamp.supervision.health.triggers.HealthStatusTrigger
import com.bytex.snamp.supervision.health.ResourceGroupHealthStatus
import com.bytex.snamp.connector.health.HealthStatus

/*
 * Code inside of this class represents body of health status trigger.
 * This example script can be used to debug triggers before putting it into SNAMP configuration
 */
class MyHealthStatusTrigger implements HealthStatusTrigger{
    @Override
    public void statusChanged(final ResourceGroupHealthStatus previousStatus, final ResourceGroupHealthStatus newStatus){
        println "Previous: ${previousStatus}"
        println "Next: ${newStatus}"
    }
}

class ResourceGroupHealthStatusImpl extends HashMap<String, HealthStatus> implements ResourceGroupHealthStatus{

}

final previousStatus = new ResourceGroupHealthStatusImpl()
final newStatus = new ResourceGroupHealthStatusImpl()
final HealthStatusTrigger trigger = new MyHealthStatusTrigger()

trigger.statusChanged(previousStatus, newStatus)