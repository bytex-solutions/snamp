package com.bytex.snamp.scripting.groovy;

import com.bytex.snamp.ArrayUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ObjectArrays;
import groovy.lang.Binding;
import groovy.lang.Script;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

/**
 * Represents file-based script engine that can extract Groovy search path from string delimited with semicolon.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public class FileBasedGroovyScriptEngine<B extends Script> extends OSGiGroovyScriptEngine<B> {
    private static final char PATH_DELIMITER = ';';
    private static final Splitter PATH_SPLITTER = Splitter.on(PATH_DELIMITER).trimResults().omitEmptyStrings();
    private static final Joiner PATH_JOINER = Joiner.on(PATH_DELIMITER).skipNulls();
    private final String scriptFileName;

    public FileBasedGroovyScriptEngine(final ClassLoader rootClassLoader, final Properties properties, final Class<B> baseScriptClass, final List<String> paths) throws IOException{
        super(rootClassLoader, properties, baseScriptClass, getScriptPaths(paths));
        if(paths.isEmpty())
            throw new IllegalArgumentException("Path must contain a list of file paths separated with semicolon");
        scriptFileName = paths.get(0);
    }

    public FileBasedGroovyScriptEngine(final ClassLoader rootClassLoader, final Properties properties, final Class<B> baseScriptClass, final String paths) throws IOException {
        this(rootClassLoader, properties, baseScriptClass, PATH_SPLITTER.splitToList(paths));
    }

    private static URL[] getScriptPaths(final List<String> paths) throws IOException {
        switch (paths.size()) {
            case 0:
            case 1:
                return ArrayUtils.emptyArray(URL[].class);
            default:
                final URL[] path = new URL[paths.size() - 1];
                for (int i = 1; i < paths.size(); i++)
                    path[i - 1] = new URL(paths.get(i));
                return path;
        }
    }

    public final String getScriptName(){
        return scriptFileName;
    }

    public final B createScript(final Binding binding) throws ResourceException, ScriptException {
        return createScript(getScriptName(), binding);
    }

    public synchronized final <C extends B> C createScript(final Binding binding, final Class<C> baseScriptClass) throws ResourceException, ScriptException {
        return createScript(getScriptName(), binding, baseScriptClass);
    }

    public static String createPath(final String scriptName, final String... path){
        return PATH_JOINER.join(ObjectArrays.concat(scriptName, path));
    }
}
