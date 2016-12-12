package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.management.javascript.SnampScriptAPI;
import com.bytex.snamp.scripting.OSGiScriptEngineManager;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.BundleContext;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
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
@Service
public final class JavaScriptCommand extends SnampShellCommand  {
    @Argument(index = 0, name = "script-or-file-path", description = "JavaScript code or file name with JavaScript", required = true)
    @SpecialUse
    private String scriptOrFilePath;

    @Option(name = "-f", aliases = {"--file", "--jsfile"}, description = "Interpret command argument as a path to file with script")
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
    public Object execute() throws ScriptException, IOException {
        final ScriptEngineManager engineManager = new OSGiScriptEngineManager(getBundleContext());
        final ScriptEngine javaScriptEngine = engineManager.getEngineByName("JavaScript");
        if(javaScriptEngine == null)
            throw new ScriptException("JavaScript engine is not available");
        try(final Reader scriptReader = useFilePath ? new FileReader(scriptOrFilePath) : new StringReader(scriptOrFilePath)){
            return doExecute(javaScriptEngine, scriptReader, getBundleContext());
        }
    }
}
