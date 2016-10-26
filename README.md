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

### Other libraries
SNAMP uses the following third-party libraries not placed on any Maven repository:
* IBM WebSphere MQ libraries for Java
* IBM WebSphere MB (Integration Bus) libraries for Java

Copy WebSphere the following libraries to `third-party-libs/binaries/ibm/websphere` folder:
* `com.ibm.mq.pcf.jar`
* `com.ibm.mq.jar`
* `com.ibm.mq.jmqi.jar`
* `com.ibm.mq.headers.jar`

WebSphere MQ classes for Java located in `MQ_INSTALLATION_PATH/java/lib` repository on Linux or
`MQ_INSTALLATION_PATH\java\lib` on Windows.

> See [WebSphere MQ classes for Java](http://www-01.ibm.com/support/knowledgecenter/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q030520_.htm)
for more information about WMQ Java API.

Execute `third-party-libs/binaries/mvn-install.sh` shell script.

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

## Versioning
SNAMP uses [Semantic Versioning](http://semver.org/) for each component separately.

Given a version number _MAJOR.MINOR.PATCH_, increment the:

* MAJOR version when you make incompatible API changes,
* MINOR version when you add functionality in a backwards-compatible manner, and
* PATCH version when you make backwards-compatible bug fixes.

But versioning policy of SNAMP distribution package based the following rules:

* Every MINOR change in the component cause increment of the MINOR version of distribution package
* Every PATCH change in the component cause increment of the PATCH version of distribution package
* Every MAJOR change in the gateway/connector = `MINOR + 2` version of distribution package
* Every MAJOR change in the core bundle cause recursive changes in gateways/connectors. Therefore, MAJOR version of package should be changed
