SNAMP
=====

SNAMP acting as a protocol bridge between manageable IT resources with different monitoring&management
protocols and your monitoring&management software. This ability allows to reduce the management cost
of your IT infrastructure.

## Prerequisites
* CPU Arch: x86/x64
* Runtime: Java SE 7 or higher (Oracle HotSpot or OpenJDK is recommended but not required)
* OS: Ubuntu (Server) 10.04 or higher, Windows 7/8, Windows Server 2003/2012, RedHat, OpenSUSE, CentOS
* RAM: 2Gb or higher

### For developers
* Maven
* JDK 7 or higher (Oracle HotSpot or OpenJDK is recommended but not required)

## How to build
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

## Code Conventions and Optimization Techniques
Optimization techniques used in SNAMP are based on [HotSpot Perofmance Techniques](https://wikis.oracle.com/display/HotSpotInternals/PerformanceTechniques).
> premature optimization is the root of all evil - Donald Knuth
Code Convention is based on official [Code Conventions for the Java Programming Language](http://www.oracle.com/technetwork/java/javase/documentation/codeconvtoc-136057.html) 
with some modifications.

Generic recommendations:

1. Each public class which is accessible from the declaring bundle should have JavaDoc with short class description, author, `@since` and `@version` elements
1. `final` keyword for parameters and local variables applied as a Code Style that simplifies concurrent programming. There is no optimization reasons for that
1. `final` keyword for fields has two meanings: simplification of concurrent programming and [Value Object](https://wikis.oracle.com/display/HotSpotInternals/Value+Objects) optimization
1. `final` keyword of methods has two menanigs: fix the stable behavior and improve chance of [inlining](https://wikis.oracle.com/display/HotSpotInternals/PerformanceTechniques)
1. All getters and setters must be `final`
1. Use immutable objects whenever possible (to reduce potential concurrenct problems and obtain benefits of [Value Objects](https://wikis.oracle.com/display/HotSpotInternals/Value+Objects))
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