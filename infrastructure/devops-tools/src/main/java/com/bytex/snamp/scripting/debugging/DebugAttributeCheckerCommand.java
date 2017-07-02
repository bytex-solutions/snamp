package com.bytex.snamp.scripting.debugging;

import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.connector.attributes.checkers.AttributeChecker;
import com.bytex.snamp.connector.attributes.checkers.AttributeCheckerFactory;
import com.bytex.snamp.core.ScriptletCompilationException;
import com.bytex.snamp.internal.Utils;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import javax.management.Attribute;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Command(scope = Utils.SHELL_COMMAND_SCOPE,
        description = "Executes attribute checker for debugging purposes",
        name = "debug-attribute-checker")
@Service
public final class DebugAttributeCheckerCommand extends ScriptletDebugger<AttributeChecker> {
    @Argument(index = 0, required = true, description = "URL-formatted location of attribute checker written in scripting language")
    private String scriptLocation;

    @Argument(index = 1, required = false, description = "Script language")
    private String language = ScriptletConfiguration.GROOVY_LANGUAGE;

    @Argument(index = 2, required = true, description = "Name of the attribute to check")
    private String attributeName;

    @Argument(index = 3, required = true, description = "Value of the attribute to check")
    private double value;

    @Override
    AttributeCheckerFactory createCompiler() {
        return new AttributeCheckerFactory();
    }

    @Override
    public Object execute() throws ScriptletCompilationException {
        return compile(language, scriptLocation).getStatus(new Attribute(attributeName, value));
    }
}
