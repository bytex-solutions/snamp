SNAMP
=====

SNAMP acting as a protocol bridge between manageable IT resources with different monitoring&management
protocols and your monitoring&management software. This ability allows to reduce the management cost
of your IT infrastructure.

## Prerequisites
CPU Arch: x86/x64
Runtime: Java SE 7 or higher (Oracle HotSpot or OpenJDK is recommended but not required)
OS: Ubuntu (Server) 10.04 or higher, Windows 7/8, Windows Server 2003/2012, RedHat, OpenSUSE, CentOS
RAM: 2Gb or higher

### For developers
Maven
JDK 7 or higher (Oracle HotSpot or OpenJDK is recommended but not required)

## How to run tests
Not all dependencies located in the Maven Central, therefore, you should download artifacts from the
following repositories:

* [OOSNMP Maven Repository](https://server.oosnmp.net/dist/release). Artifacts: snmp4j-agent, snmp4j

You can do this automatically using `maven` utility or IDE.
Generally, you need to pass
```
-Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Djsse.enableSNIExtension=false
```
to the `maven` process when first importing the project.

If your IDE is IntelliJ IDEA 12/13/14 then use the following instruction:

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