package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.notifications.measurement.Measurement;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.scripting.OSGiScriptEngineManager;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ScriptNotificationParser implements NotificationParser {
    private final ScriptEngine engine;
    private final File scriptFile;

    ScriptNotificationParser(final String language, final String scriptFile){
        final ScriptEngineManager manager = new OSGiScriptEngineManager(Utils.getBundleContextOfObject(this));
        engine = manager.getEngineByName(language);
        this.scriptFile = new File(scriptFile);
    }

    @Override
    public Measurement parse(final Map<String, ?> headers, final Object body) throws IOException, ScriptException {
        Object result;
        try (final FileReader reader = new FileReader(scriptFile)) {
            result = engine.eval(reader);
        }
        return (Measurement) result;
    }
}
