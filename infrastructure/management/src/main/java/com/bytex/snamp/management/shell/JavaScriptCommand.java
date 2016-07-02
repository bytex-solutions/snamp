package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.management.javascript.SnampScriptAPI;
import com.bytex.snamp.scripting.OSGiScriptEngineManager;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.osgi.framework.BundleContext;

import javax.script.*;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * The command that ables to execute JavaScript from command line
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
name = "script",
description = "Run JavaScript from command line")
public final class JavaScriptCommand extends OsgiCommandSupport implements SnampShellCommand {
    @Argument(index = 0, name = "script-or-file-path", description = "JavaScript code or file name with JavaScript", required = true)
    @SpecialUse
    private String scriptOrFilePath;

    @Option(name = "-f", aliases = {"--file", "--jsfile"}, description = "Path to JavaScript file")
    @SpecialUse
    private boolean useFilePath;

    public JavaScriptCommand() {
    }

    private static Object doExecute(final ScriptEngine javaScriptEngine, final Reader scriptToExecute, final BundleContext context) throws ScriptException {
        final Bindings bindings = javaScriptEngine.createBindings();
        bindings.put(SnampScriptAPI.NAME, new SnampScriptAPI(context));
        return javaScriptEngine.eval(scriptToExecute, bindings);
    }

    @Override
    protected Object doExecute() throws ScriptException, IOException {
        final ScriptEngineManager engineManager = new OSGiScriptEngineManager(bundleContext);
        final ScriptEngine javaScriptEngine = engineManager.getEngineByName("JavaScript");
        if(javaScriptEngine == null)
            throw new ScriptException("JavaScript engine is not available");
        try(final Reader scriptReader = useFilePath ? new FileReader(scriptOrFilePath) : new StringReader(scriptOrFilePath)){
            return doExecute(javaScriptEngine, scriptReader, bundleContext);
        }
    }
}
