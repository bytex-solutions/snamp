package com.itworks.snamp.scripting;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.itworks.snamp.Consumer;
import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.ExceptionalCallable;
import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.internal.annotations.Internal;
import com.itworks.snamp.internal.annotations.ThreadSafe;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import javax.script.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

//https://svn.apache.org/repos/asf/felix/trunk/mishell/src/main/java/org/apache/felix/mishell/OSGiScriptEngineManager.java
/**
 * This class acts as a delegate for all the available ScriptEngineManagers. Unluckily, the standard did not
 * define it as an interface, so we need to extend it to allow polymorphism. However, no calls to super are used.
 * It wraps all available ScriptEngineManagers in the OSGi ServicePlatform into a merged ScriptEngineManager.
 *
 * Internally, what this class does is creating ScriptEngineManagers for each bundle 
 * that contains a ScriptEngineFactory and includes a META-INF/services/javax.script.ScriptEngineFactory file. 
 * It assumes that the file contains a list of @link ScriptEngineFactory classes. For each bundle, it creates a
 * ScriptEngineManager, then merges them. @link ScriptEngineFactory objects are wrapped
 * into @link OSGiScriptEngineFactory objects to deal with problems of context class loader:
 * Those scripting engines that rely on the ContextClassloader for finding resources need to use this wrapper
 * and the @link OSGiScriptFactory. Mainly, jruby does.
 *
 * Note that even if no context classloader issues arose, it would still be needed to search manually for the 
 * factories and either use them directly (losing the mimeType/extension/shortName mechanisms for finding engines
 * or manually registering them) or still use this class, which would be smarter. In the latter case, 
 * it would only be needed to remove the hack that temporarily sets the context classloader to the appropriate, 
 * bundle-related, class loader.
 *
 * Caveats:
 * <ul><li>
 * All factories are wrapped with an {@link OSGiScriptEngineFactory}. As Engines are not wrapped,
 * calls like 
 * <code>
 * ScriptEngineManager osgiManager=new OSGiScriptEngineManager(context);<br>
 * ScriptEngine engine=osgiManager.getEngineByName("ruby");
 * ScriptEngineFactory factory=engine.getFactory() //this does not return the OSGiFactory wrapper
 * factory.getScriptEngine(); //this might fail, as it does not use OSGiScriptEngineFactory wrapper
 * </code>
 * might result in unexpected errors. Future versions may wrap the ScriptEngine with a OSGiScriptEngine to solve this
 * issue, but for the moment it is not needed.
 * </li>
 * @since 1.0
 * @version 1.0
 * @author Apache Foundation, Roman Sakno
 */
@ThreadSafe(false)
public final class OSGiScriptEngineManager extends ScriptEngineManager{

    private static final String META_INF_SERVICES = "META-INF/services";
    private Bindings bindings;
    private final WeakHashMap<ClassLoader, ScriptEngineManager> classLoaders;
    private final Collection<ScriptEngineFactory> systemFactories;
    private final BundleContext context;

    public OSGiScriptEngineManager(final BundleContext context) throws IOException, ReflectiveOperationException {
        this.classLoaders = findThirdPartyManagers(this.context = context,
                bindings = new SimpleBindings());
        this.systemFactories = findSystemFactories(context);
    }

    /**
     * This method is the only one that is visible and not part of the ScriptEngineManager class.
     * Its purpose is to find new managers that weren't available before, but keeping the globalScope bindings
     * set.
     * If you want to clean the bindings you can either get a fresh instance of OSGiScriptManager or
     * setting up a new bindings object.
     * This can be done with:
     * <code>
     * ScriptEngineManager manager=new OSGiScriptEngineManager(context);
     * (...)//do stuff
     * osgiManager=(OSGiScriptEngineManager)manager;//cast to ease reading
     * osgiManager.reloadManagers();
     *
     * manager.setBindings(new OSGiBindings());//or you can use your own bindings implementation
     *
     * </code>
     *
     */
    public void reloadManagers() throws IOException, ReflectiveOperationException {
        classLoaders.clear();
        systemFactories.clear();
        classLoaders.putAll(findThirdPartyManagers(context, bindings));
        systemFactories.addAll(findSystemFactories(context));
    }

    @Override
    public Object get(final String key) {
        return bindings.get(key);
    }

    @Override
    public Bindings getBindings() {
        return bindings;
    }

    private static ForwardingScriptEngine createOsgiEngine(final ScriptEngine underlyingEngine, final ClassLoader loader){
        return new ForwardingScriptEngine() {
            private final OSGiScriptEngineFactory factory = new OSGiScriptEngineFactory(underlyingEngine.getFactory(), loader);

            @Override
            protected ScriptEngine delegate () {
                return underlyingEngine;
            }

            @Override
            public ScriptEngineFactory getFactory () {
                return factory;
            }
        };
    }

    private ForwardingScriptEngine getProxyEngine(final Function<ScriptEngineManager, ScriptEngine> provider) {
        for (final ClassLoader loader : classLoaders.keySet()) {
            final ScriptEngine engine = Utils.withContextClassLoader(loader, new ExceptionalCallable<ScriptEngine, ExceptionPlaceholder>() {
                private final ScriptEngineManager manager = classLoaders.get(loader);

                @Override
                public ScriptEngine call() {
                    return manager != null ? provider.apply(manager) : null;
                }
            });
            if (engine != null) return createOsgiEngine(engine, loader);
        }
        return null;
    }

    private ScriptEngine getSystemEngine(final Predicate<ScriptEngineFactory> factoryFilter) {
        final ScriptEngineFactory factory = Iterables.getFirst(Iterables.filter(systemFactories, factoryFilter),
                null);
        return factory != null ? factory.getScriptEngine() : null;
    }

    private ScriptEngine getEngine(final Predicate<ScriptEngineFactory> systemFactoryFilter,
                                   final Function<ScriptEngineManager, ScriptEngine> proxyEngineResolver){
        ScriptEngine result = getSystemEngine(systemFactoryFilter);
        if(result == null)
            result = getProxyEngine(proxyEngineResolver);
        if(result != null)
            result.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
        return result;
    }

    @Override
    public ScriptEngine getEngineByExtension(final String extension) {
        return getEngine(new Predicate<ScriptEngineFactory>() {
                             @Override
                             public boolean apply(final ScriptEngineFactory factory) {
                                 return factory.getExtensions().contains(extension);
                             }
                         },
                new Function<ScriptEngineManager, ScriptEngine>() {
                    @Override
                    public ScriptEngine apply(final ScriptEngineManager manager) {
                        return manager.getEngineByExtension(extension);
                    }
                }
        );
    }

    @Override
    public ScriptEngine getEngineByMimeType(final String mimeType) {
        return getEngine(new Predicate<ScriptEngineFactory>() {
                             @Override
                             public boolean apply(final ScriptEngineFactory factory) {
                                 return factory.getMimeTypes().contains(mimeType);
                             }
                         },
                new Function<ScriptEngineManager, ScriptEngine>() {
                    @Override
                    public ScriptEngine apply(final ScriptEngineManager manager) {
                        return manager.getEngineByMimeType(mimeType);
                    }
                }
        );
    }

    @Override
    public ScriptEngine getEngineByName(final String shortName) {
        return getEngine(new Predicate<ScriptEngineFactory>() {
                             @Override
                             public boolean apply(final ScriptEngineFactory factory) {
                                 return factory.getNames().contains(shortName);
                             }
                         },
                new Function<ScriptEngineManager, ScriptEngine>() {
                    @Override
                    public ScriptEngine apply(final ScriptEngineManager manager) {
                        return manager.getEngineByName(shortName);
                    }
                }
        );
    }

    @Override
    public List<ScriptEngineFactory> getEngineFactories() {
        final List<ScriptEngineFactory> osgiFactories = new ArrayList<>(classLoaders.size() * 2);
        for (final Map.Entry<ClassLoader, ScriptEngineManager> entry : classLoaders.entrySet())
            for (final ScriptEngineFactory factory : entry.getValue().getEngineFactories())
                osgiFactories.add(new OSGiScriptEngineFactory(factory, entry.getKey()));
        osgiFactories.addAll(systemFactories);
        return osgiFactories;
    }

    @Override
    public void put(final String key, final Object value) {
        bindings.put(key, value);
    }

    @Override
    public void registerEngineExtension(final String extension, final ScriptEngineFactory factory) {
        for (final ScriptEngineManager engineManager : classLoaders.values())
            engineManager.registerEngineExtension(extension, factory);
    }

    @Override
    public void registerEngineMimeType(final String type, final ScriptEngineFactory factory) {
        for (final ScriptEngineManager engineManager : classLoaders.values())
            engineManager.registerEngineMimeType(type, factory);
    }

    @Override
    public void registerEngineName(final String name, final ScriptEngineFactory factory) {
        for (final ScriptEngineManager engineManager : classLoaders.values())
            engineManager.registerEngineName(name, factory);
    }

    /**
     * Follows the same behavior of @link javax.script.ScriptEngineManager#setBindings(Bindings)
     * This means that the same bindings are applied to all the underlying managers.
     * @param bindings
     */
    @Override
    public void setBindings(final Bindings bindings) {
        this.bindings = bindings;
        for (final ScriptEngineManager manager : classLoaders.values())
            manager.setBindings(bindings);
    }

    private static Collection<ScriptEngineFactory> findSystemFactories(final Bundle rootLoader) throws IOException, ReflectiveOperationException {
        final Set<String> factoryClasses = getSystemScriptEngineFactories();
        final Collection<ScriptEngineFactory> result = Lists.newArrayListWithExpectedSize(factoryClasses.size());
        for (final String factoryClassName : factoryClasses) {
            final Class<?> factoryClass = rootLoader.loadClass(factoryClassName);
            if (ScriptEngineFactory.class.isAssignableFrom(factoryClass))
                result.add((ScriptEngineFactory) factoryClass.newInstance());
        }
        return result;
    }

    private static Collection<ScriptEngineFactory> findSystemFactories(final BundleContext context) throws IOException, ReflectiveOperationException {
        return findSystemFactories(context.getBundle());
    }

    private static WeakHashMap<ClassLoader, ScriptEngineManager> findThirdPartyManagers(final BundleContext context,
                                                                                        final Bindings bindings) throws IOException, ReflectiveOperationException{
        final WeakHashMap<ClassLoader, ScriptEngineManager> managers = new WeakHashMap<>();
        //load bundlized engines
        findFactoryCandidates(context,
                new SafeConsumer<Class<? extends ScriptEngineFactory>>() {
                    @Override
                    public void accept(final Class<? extends ScriptEngineFactory> factoryClass) {
                        //We do not really need the class, but we need the classloader
                        final ClassLoader factoryLoader = factoryClass.getClassLoader();
                        if(managers.containsKey(factoryLoader)) return;
                        final ScriptEngineManager manager = new ScriptEngineManager(factoryLoader);
                        manager.setBindings(bindings);
                        managers.put(factoryLoader, manager);
                    }
                });
        return managers;
    }

    @SuppressWarnings("unchecked")
    private static <E extends Exception> void findFactoryCandidates(final BundleContext context,
                                              final Consumer<Class<? extends ScriptEngineFactory>, E> handler) throws IOException, ReflectiveOperationException, E {
        final Bundle[] bundles = context.getBundles();
        //find engines in resolved third-party OSGi bundles
        for (final Bundle bundle : bundles) {
            //ignore system bundle
            if (bundle.getBundleId() == 0L) continue;
            final Enumeration urls = bundle.findEntries(META_INF_SERVICES,
                    ScriptEngineFactory.class.getName(), false);
            if (urls == null)
                continue;
            while (urls.hasMoreElements()) {
                final URL u = (URL) urls.nextElement();
                try (final BufferedReader reader = new BufferedReader(new InputStreamReader(u.openStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (checkScriptEngineFactoryClassName(line)) {
                            final Class<?> candidate = bundle.loadClass(line);
                            if (ScriptEngineFactory.class.isAssignableFrom(candidate))
                                handler.accept((Class<? extends ScriptEngineFactory>) candidate);
                        }
                    }
                }
            }
        }
    }

    private static boolean checkScriptEngineFactoryClassName(final String className) {
        if (!className.isEmpty()) {
            int cp;
            if ((className.indexOf(' ') >= 0) || (className.indexOf('\t') >= 0))
                return false;
            else if (Character.isJavaIdentifierStart(cp = className.codePointAt(0)))
                for (int i = Character.charCount(cp); i < className.length(); i += Character.charCount(cp)) {
                    cp = className.codePointAt(i);
                    if (!Character.isJavaIdentifierPart(cp) && (cp != '.'))
                        return false;
                }
            else return false;
        } else return false;
        return true;
    }

    /**
     * Returns a set of script engine factories provided by JDK itself.
     * @return A set of script engine factories.
     * @throws IOException Unable to load system factories.
     */
    @Internal
    public static Set<String> getSystemScriptEngineFactories() throws IOException {
        final Set<String> factories = new HashSet<>(10);
        final Enumeration<URL> resoures = ClassLoader.getSystemResources(META_INF_SERVICES + "/" + ScriptEngineFactory.class.getName());
        while (resoures.hasMoreElements()) {
            final URL url = resoures.nextElement();
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    final int ci = line.indexOf('#');
                    if (ci >= 0) line = line.substring(0, ci);
                    line = line.trim();
                    if (checkScriptEngineFactoryClassName(line))
                        factories.add(line);
                }
            }
        }
        return factories;
    }
}
