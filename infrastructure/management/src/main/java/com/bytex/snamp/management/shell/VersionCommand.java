package com.bytex.snamp.management.shell;

import com.bytex.snamp.core.PlatformVersion;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "version",
    description = "Show version of SNAMP platform")
@Service
public final class VersionCommand extends SnampShellCommand  {
    @Override
    public CharSequence execute() {
        return PlatformVersion.get().toString();
    }
}
