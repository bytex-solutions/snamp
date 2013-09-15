package com.snamp.hosting;

import net.xeoh.plugins.base.*;
import net.xeoh.plugins.base.impl.PluginManagerFactory;

/**
 * Represents SNAMP extension loader.
 * @author roman
 */
public final class Extensions {
    private static final PluginManager manager;

    private Extensions(){

    }

    static {
        manager = PluginManagerFactory.createPluginManager();
    }


}
