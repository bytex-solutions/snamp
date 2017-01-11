/**
 * Provides a necessary classes for instrumenting application/service/component and makes in measurable for SNAMP.
 * <p />
 * Starting guide about instrumenting application code:
 * <ul>
 *     <li>
 *         Derive from {@link com.bytex.snamp.instrumentation.ApplicationInfo} and specify application name and instance:
 *         <pre><code>
 *             public final class MyApplicationInfo extends ApplicationInfo {
 *                 static {
 *                     setName("FinancialService");
 *                     setInstance("node1");
 *                 }
 *
 *                 private MyApplicationInfo(){ }
 *             }
 *         </code></pre>
 *         Another approach is to specify name and instance through JVM system properties (or OSGi global properties):
 *         {@link com.bytex.snamp.instrumentation.ApplicationInfo#INSTANCE_SYSTEM_PROPERTY},
 *         {@link com.bytex.snamp.instrumentation.ApplicationInfo#NAME_SYSTEM_PROPERTY}
 *         or Spring configuration properties.
 *     </li>
 *     <li>
 *         Include JAR with one of the appropriate implementations of {@link com.bytex.snamp.instrumentation.reporters.Reporter}.
 *         By default, SNAMP provides asynchronous HTTP reporter (see http-reporter artifact in Maven).
 *         The reporter acts as a transport for all measurements produced by application to SNAMP.
 *     </li>
 *     <li>
 *         Create a new instance of {@link com.bytex.snamp.instrumentation.MetricRegistry} and make it accessible
 *         globally to all code in the application. The recommended way is to derive from it and implement Singleton pattern:
 *         <pre><code>
 *             public final class MyMetricRegistry extends MetricRegistry {
 *                  private static final MyMetricRegistry INSTANCE = new MyMetricRegistry();
 *
 *                  private MyMetricRegistry(){
 *
 *                  }
 *
 *                  public static MyMetricRegistry getInstance(){
 *                      return INSTANCE;
 *                  }
 *             }
 *         </code></pre>
 *
 *         Another approach is just to save it into static final field:
 *         <pre><code>
 *             public final class MyApplicationInfo extends ApplicationInfo{
 *                  private static final MetricRegistry registry;
 *
 *                  static {
 *                     setName("FinancialService");
 *                     setInstance("node1");
 *                     registry = new MetricRegistry();
 *                 }
 *
 *                 public static MetricRegistry getRegistry(){
 *                     return registry;
 *                 }
 *
 *                 private MyApplicationInfo(){ }
 *             }
 *         </code></pre>
 *     </li>
 *     <li>
 *         Instrument classes with measurement reporters:
 *         <code><pre>
 *             public class MyClass{
 *                 private static final IntegerMeasurementReporter numberOfCalls = MyApplicationInfo.getRegistry().integer("numberOfCalls");
 *
 *                 public void flush(){
 *                     numberOfCalls.report(1L, ChangeType.SUM);
 *                     try(final MeasurementScope timer = MyApplicationInfo.getRegistry().timer("flushCallTime")){
 *                         //do something
 *                     }
 *                 }
 *             }
 *         </pre></code>
 *     </li>
 * </ul>
 *
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 * @see com.bytex.snamp.instrumentation.ApplicationInfo
 * @see com.bytex.snamp.instrumentation.MetricRegistry
 */
package com.bytex.snamp.instrumentation;