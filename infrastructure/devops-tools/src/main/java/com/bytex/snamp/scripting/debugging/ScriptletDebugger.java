package com.bytex.snamp.scripting.debugging;

import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.core.ScriptletCompilationException;
import com.bytex.snamp.core.ScriptletCompiler;
import com.bytex.snamp.shell.SnampShellCommand;
import org.apache.karaf.shell.api.action.Action;

import java.util.Collections;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
abstract class ScriptletDebugger<S> extends SnampShellCommand implements Action {
    private static final class GroovyScriptletConfiguration implements ScriptletConfiguration{
        private String scriptLocation;
        private String language;

        private GroovyScriptletConfiguration(final String language, final String location){
            scriptLocation = location;
            this.language = language;
        }

        @Override
        public String getLanguage() {
            return language;
        }

        @Override
        public void setLanguage(final String value) {
            language = value;
        }

        @Override
        public String getScript() {
            return scriptLocation;
        }

        @Override
        public void setScript(final String value) {
            scriptLocation = value;
        }

        @Override
        public boolean isURL() {
            return true;
        }

        @Override
        public void setURL(final boolean value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, String> getParameters() {
            return Collections.emptyMap();
        }
    }

    abstract ScriptletCompiler<S> createCompiler();

    final S compile(final String language, final String scriptLocation) throws ScriptletCompilationException {
        final ScriptletConfiguration configuration = new GroovyScriptletConfiguration(language, scriptLocation);
        return createCompiler().compile(configuration);
    }
}
