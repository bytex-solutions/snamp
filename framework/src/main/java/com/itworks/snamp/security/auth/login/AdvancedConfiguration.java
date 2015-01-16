package com.itworks.snamp.security.auth.login;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import java.util.Collection;

/**
 * Represents advanced JAAS configuration that gives access to the all entries.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AdvancedConfiguration extends Configuration implements Multimap<String, AppConfigurationEntry> {
    /**
     * Retrieve the AppConfigurationEntries for the specified <i>name</i>
     * from this Configuration.
     * <p/>
     * <p/>
     *
     * @param name the name used to index the Configuration.
     * @return an array of AppConfigurationEntries for the specified <i>name</i>
     * from this Configuration, or null if there are no entries
     * for the specified <i>name</i>
     */
    @Override
    public final AppConfigurationEntry[] getAppConfigurationEntry(final String name) {
        if(containsKey(name)){
            final Collection<AppConfigurationEntry> result = get(name);
            return result.isEmpty() ? null : result.toArray(new AppConfigurationEntry[result.size()]);
        }
        else return null;
    }

    public final void putAll(final String name, final AppConfigurationEntry[] entries) {
        putAll(name, ImmutableList.copyOf(entries));
    }
}
