package com.bytex.snamp.scripting;

import com.bytex.snamp.ThreadSafe;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

//based on https://svn.apache.org/repos/asf/felix/trunk/mishell/src/main/java/org/apache/felix/mishell/OSGiScriptEngineManager.java
/**
 * This class acts as a delegate for all the available ScriptEngineManagers. Unluckily, the standard did not
 * define it as an interface, so we need to extend it to allow polymorphism. However, no calls to super are used.
 * It wraps all available ScriptEngineManagers in the OSGi ServicePlatform into a merged ScriptEngineManager.
 * @since 1.0
 * @version 2.0
 * @author Apache Foundation, Roman Sakno
 */
@ThreadSafe(false)
public final class OSGiScriptEngineManager extends ScriptEngineManager {
    private final BundleContext context;

    public OSGiScriptEngineManager(final BundleContext context) {
        super(getClassLoader(Objects.requireNonNull(context).getBundle()));
        this.context = context;
    }

    private static ClassLoader getClassLoader(final Bundle bundle){
        return bundle.adapt(BundleWiring.class).getClassLoader();
    }

    @Override
    public ScriptEngine getEngineByExtension(final String extension) {
        return getEngineFactoriesImpl()
                .filter(factory -> factory.getExtensions().contains(extension))
                .map(ScriptEngineFactory::getScriptEngine)
                .findFirst()
                .orElseGet(() -> super.getEngineByExtension(extension));
    }

    @Override
    public ScriptEngine getEngineByMimeType(final String mimeType) {
        return getEngineFactoriesImpl()
                .filter(factory -> factory.getMimeTypes().contains(mimeType))
                .map(ScriptEngineFactory::getScriptEngine)
                .findFirst()
                .orElseGet(() -> super.getEngineByMimeType(mimeType));
    }

    @Override
    public ScriptEngine getEngineByName(final String shortName) {
        return getEngineFactoriesImpl()
                .filter(factory -> factory.getNames().contains(shortName))
                .map(ScriptEngineFactory::getScriptEngine)
                .findFirst()
                .orElseGet(() -> super.getEngineByName(shortName));
    }

    private static Stream<OSGiScriptEngineFactory> getFactories(final ClassLoader scope) {
        return StreamSupport.stream(ServiceLoader.load(ScriptEngineFactory.class, scope).spliterator(), false).map(engine -> new OSGiScriptEngineFactory(engine, scope));
    }

    private Stream<? extends ScriptEngineFactory> getEngineFactoriesImpl() {
        //find system script engines
        final ServiceLoader<ScriptEngineFactory> engine = ServiceLoader.load(ScriptEngineFactory.class, ClassLoader.getSystemClassLoader());
        final Stream<OSGiScriptEngineFactory> systemFactories = StreamSupport.stream(engine.spliterator(), false)
                .map(factory -> new OSGiScriptEngineFactory(factory, getClassLoader(context.getBundle())));
        //find other script engines
        final Stream<OSGiScriptEngineFactory> userDefinedFactories = Arrays.stream(context.getBundles())
                .filter(bundle -> bundle.getBundleId() != 0L)
                .flatMap(bundle -> getFactories(getClassLoader(bundle)));
        return Stream.concat(systemFactories, userDefinedFactories);
    }

    @Override
    public List<ScriptEngineFactory> getEngineFactories() {
        return getEngineFactoriesImpl().collect(Collectors.toList());
    }
}
