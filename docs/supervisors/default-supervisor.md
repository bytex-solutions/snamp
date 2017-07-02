Default Supervisor
====
Default Supervisor is used by default for all resource groups in SNAMP. It supports the following features:

Feature | Description
---- | ----
Group health status | Yes
Elasticity management | Partially

Resource Discovery is not supported.

## Configuration parameters
Default Supervisor recognizes the following configuration parameters:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
groovyElasticityManager | List of URLs | No | Semicolon-separated list of URLs with directories containing Groovy script. First element in the list should be name of Groovy script with implementation of elasticity manager | `GroovyElasticityManager.groovy;file:/opt/snamp/scripts`

## Elasticity management
Elasticity management can be implemented using handwritten Groovy script only. Without implementation of this script Default Supervisor doesn't provide elasticity management functionality.

Default Supervisor provides DSL for writing scaling operations:
```groovy
import com.bytex.snamp.Aggregator

public void scaleIn(final Aggregator provisioningContext){
  //remove cluster node and managed resource from group
}

public void scaleOut(final Aggregator provisioningContext) {
  //add cluster node and managed resource into group
}

public void cooldown(final Aggregator provisioningContext){
  //cooldown happens. Optional method that can be ignored by developer
}

public void outOfSpace(final Aggregator provisioningContext){
  //cannot enlarge cluster because maximum cluster size is reached
}
```

Full functionality of Groovy language is supported including Groovy Grapes. Example of Groovy script with test implementation of Elasticity Manager you can find [here](https://bitbucket.org/bytex-solutions/snamp/src/master/sample-groovy-scripts/GroovyElasticityManager.groovy).
