package com.snamp.restmanager;

import com.snamp.hosting.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author Alexey Grishin
 * @version 1.0
 * @since 1.0
 */
public class AgentConfigurationBean extends AbstractAgentConfiguration{

    @Override
    public AbstractAgentConfiguration clone() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public HostingConfiguration getAgentHostingConfig() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<String, ManagementTargetConfiguration> getTargets() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ManagementTargetConfiguration newManagementTargetConfiguration() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void save(OutputStream output) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void load(InputStream input) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}
