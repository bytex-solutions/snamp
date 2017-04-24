SNAMP
=====

SNAMP represents a protocol bridge between manageable IT resources with different monitoring&management
protocols and your monitoring&management software. SNAMP can help you to reduce the management costs
for the complex IT infrastructure.

## Prerequisites
* CPU Arch: x86/x64
* Runtime: Java SE 8 or higher (Oracle HotSpot or OpenJDK is recommended but not required)
* OS: Ubuntu (Server) 12.04 or higher, Windows 7/8/10, Windows Server 2003/2012, RedHat, OpenSUSE, CentOS
* RAM: 2Gb or higher

### For developers
* Maven
* JDK 8 or higher (Oracle HotSpot or OpenJDK is recommended but not required)

## How to build
First, you should build SNAMP using _Development_ profile. After, you can switch to _Release_ profile and build SNAMP Distribution Package.

Not all dependencies located at the Maven Central. Some dependencies are proprietary libraries. Therefore,
it is necessary to prepare your local Maven repository as follows.

### Third-party repositories
SNAMP uses the following third-party repositories:

* [ServiceMix Repository](http://svn.apache.org/repos/asf/servicemix/m2-repo/).

You can do this automatically using `maven` utility or IDE.
Generally, you need to pass
```
-Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Djsse.enableSNIExtension=false
```
to the `maven` process when first importing the project.

If you use IntelliJ IDEA 15/2016 as IDE - follow the instructions:

1. Open _File_ -> _Settings_ menu
1. Find _Build, Execution, Deployment_ -> _Build Tools_ -> _Maven_ -> _Runner_
1. Paste JVM args described above into _VM Options_ text box
1. Go to _Maven Projects_ tab
1. Select _snamp_ module
1. Execute _clean_ action
1. Execute _validate_ action

> Note: Please verify that IntelliJ IDEA correctly recognized Maven Home (M2_HOME environment variable)

Also, you can do this without IDE with the following command:

```sh
cd <snamp-project-dir>/third-party-libs/bundlized/snmp4j
export MAVEN_OPTS="-Xmx512m -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Djsse.enableSNIExtension=false"
mvn clean package
```

## Running tests
SNAMP project contains two category of tests:

* Unit tests located in their bundles and running at `test` phase
* Integration tests located in `osgi-tests` project

It is necessary to install all OSGi bundles into local Maven repository before running integration tests.
Therefore, integration tests can be executed at `site` build phase of `osgi-tests` project

## Profiles
SNAM project provides the following Maven profiles:

* `Development` profile disables all unit and integrations tests in the project
* `Release` profile enables to assembly final SNAMP Distribution package on top of Apache Karaf container
* `Remote Debug` enables breakpoints for debug session in integration tests

## OpenStack
To enable integration tests with OpenStack you should install DevStack on virtual machine:

* Install [DevStack](https://docs.openstack.org/developer/devstack/)
* `su - stack`
* `cd devstack`
* `source openrc`
* `cd ..`
* Create key pair in OpenStack Horizon (Compute tab)
* Test profile for Senlin (in file `cirros-cluster-profile.yaml`):

```yaml
type: os.nova.server
version: 1.0
properties:
  name: cirros_server
  flavor: 1
  image: "cirros-0.3.5-x86_64-disk"
  key_name: snamp-key
  networks:
   - network: private
  metadata:
    test_key: test_value
  user_data: |
    #!/bin/sh
    echo 'hello, world' > /tmp/test_file
```

* `openstack cluster profile create --spec-file cirros-cluster-profile.yaml ProfileForTests`