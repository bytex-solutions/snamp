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
