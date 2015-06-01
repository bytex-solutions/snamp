SNAMP
=====

SNAMP represents a protocol bridge between manageable IT resources with different monitoring&management
protocols and your monitoring&management software. SNAMP can help you to reduce the management costs
for the complex IT infrastructure.

## Prerequisites
* CPU Arch: x86/x64
* Runtime: Java SE 7 or higher (Oracle HotSpot or OpenJDK is recommended but not required)
* OS: Ubuntu (Server) 10.04 or higher, Windows 7/8, Windows Server 2003/2012, RedHat, OpenSUSE, CentOS
* RAM: 2Gb or higher

### For developers
* Maven
* JDK 7 or higher (Oracle HotSpot or OpenJDK is recommended but not required)

## How to build
First, you should build SNAMP using _Development_ profile. After, you can switch to _Release_ profile and build SNAMP Distribution Package.

Not all dependencies located at the Maven Central. Some dependencies are proprietary libraries. Therefore,
it is necessary to prepare your local Maven repository as follows.

### Third-party repositories
SNAMP uses the following third-party repositories:

* [OOSNMP Maven Repository](https://server.oosnmp.net/dist/release). Artifacts: snmp4j-agent, snmp4j

You can do this automatically using `maven` utility or IDE.
Generally, you need to pass
```
-Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Djsse.enableSNIExtension=false
```
to the `maven` process when first importing the project.

If you use IntelliJ IDEA 12/13/14 as IDE - follow the instructions:

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

WebSphere MQ classes for Java located in `MQ_INSTALLATION_PATH/java/lib` repository on Linux and
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
* Every MAJOR change in the adapter/connector = `MINOR + 2` version of distribution package
* Every MAJOR change in the core bundle cause recursive changes in adapters/connectors. Therefore, MAJOR version of package should be changed

## Code Conventions and Optimization Techniques
Optimization techniques used in SNAMP are based on [HotSpot Performance Techniques](https://wikis.oracle.com/display/HotSpotInternals/PerformanceTechniques).
> premature optimization is the root of all evil - Donald Knuth

Code Convention is based on official [Code Conventions for the Java Programming Language](http://www.oracle.com/technetwork/java/javase/documentation/codeconvtoc-136057.html) 
with some modifications.

Generic recommendations:

1. Each public class which is accessible from the declaring bundle should have JavaDoc with short class description, author, `@since` and `@version` elements
1. `final` keyword for parameters and local variables applied as a Code Style that simplifies concurrent programming. There is no optimization reasons for that
1. `final` keyword for fields has two meanings: simplification of concurrent programming and [Value Object](https://wikis.oracle.com/display/HotSpotInternals/Value+Objects) optimization
1. `final` keyword of methods has two menanigs: fix the stable behavior and improve chance of [inlining](https://wikis.oracle.com/display/HotSpotInternals/PerformanceTechniques)
1. All getters and setters must be `final`
1. Use immutable objects whenever possible (to reduce potential concurrent problems and obtain benefits of [Value Objects](https://wikis.oracle.com/display/HotSpotInternals/Value+Objects))
1. Each interface should have default or abstract implementation
1. Use [Template method pattern](http://en.wikipedia.org/wiki/Template_method_pattern) for partial implementation of interface methods within abstract class (abstract class delcares interface method with `final` keyword but provides extensibility point in the form of protected abstract method called from interface method)
1. Declare `serialVersionUUID` for each serializable class
1. Use interface instead of class as a type of parameters and return values
1. Use interface instead of class in generic constraint (`T extends Interface` instead of `T extends Class`)
1. Use weak reference to avoid cyclic references between objects
1. Avoid global escaping of anynymous class instances
1. Avoid global escaping of primitive wrappers (this helps HotSpot escape analysis to allocate wrapper on the thread stack)
1. Reuse `Guava` and standard Java classes wherever possible
1. Avoid usage of Java Proxy in performance-critical code
1. Empty `catch` block is a bad practice

Recommendations for Java Collections:

1. Use collection allocators from _Guava library_ instead of directly collection instantiation, such as `Lists.newArrayListWithExpectedSize`, wherever possible
1. Use `ImmutableMap`, `ImmutableList`, `ImmutableSet` for collections with predefined set of elements

Recommendations for concurrency:

1. Use daemon threads only
1. Avoid exclusive lock (`synchronized` method, monitors) if it can be replaced with read/write lock
1. Avoid exclusive lock (`synchronized` method, monitors) on a whole object if the declared method changes or reads from a single fields in this object. Use more granular locks (see resource separation in `ThreadSafeObject`)
1. Use fine-grained locks and release locks as soon as possible
1. Avoid instantiation and execution of the standalone thread, use thread pool instead.

OSGi-specific recommendations:

1. Avoid direct manipulation with Java `ClassLoader`
1. Release `ServiceReference` to the foreign service as soon as possible