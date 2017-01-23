package com.bytex.snamp.management.javascript;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.core.PlatformVersion;
import com.bytex.snamp.core.ServiceHolder;
import org.osgi.framework.BundleContext;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.2
 */
public final class SnampScriptAPI {
    public static final String NAME = "snamp";
    private final BundleContext context;

    public SnampScriptAPI(final BundleContext context){
        this.context = Objects.requireNonNull(context);
    }

    /**
     * Gets SNAMP version.
     * @return SNAMP version.
     */
    @SpecialUse(SpecialUse.Case.SCRIPTING)
    public PlatformVersion getVersion(){
        return PlatformVersion.get();
    }

    /**
     * Configure SNAMP.
     * @param handler Configuration procedure.
     * @throws ScriptException Something wrong with calling JavaScript.
     * @throws IOException Can't read SNAMP configuration.
     */
    @SpecialUse(SpecialUse.Case.SCRIPTING)
    public void configure(final ConfigurationManager.ConfigurationProcessor<ScriptException> handler) throws ScriptException, IOException {
        final ServiceHolder<ConfigurationManager> manager = ServiceHolder.tryCreate(context, ConfigurationManager.class);
        if (manager != null)
            try {
                manager.get().processConfiguration(handler);
            } finally {
                manager.release(context);
            }
        else
            throw new ScriptException("SNAMP Configuration Manager is not accessible");
    }
}
