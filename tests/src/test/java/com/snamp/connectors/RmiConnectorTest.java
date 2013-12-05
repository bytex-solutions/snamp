package com.snamp.connectors;

import com.snamp.hosting.*;

import java.lang.management.ManagementFactory;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class RmiConnectorTest<RemoteBean extends Remote> extends HostingTest {
    protected static final int RMI_OBJ_PORT = 9887;
    protected static final int RMI_REG_PORT = 9888;
    private final RemoteBean bean;
    private final String beanName;

    protected RmiConnectorTest(final String adapterName, final Map<String, String> adapterParams, final RemoteBean bean, final String remoteName){
        super(adapterName, adapterParams);
        this.bean = bean;
        this.beanName = remoteName;
    }

    @Override
    protected final void beforeAgentStart(final Agent agent) throws RemoteException {
        final Remote stub = UnicastRemoteObject.exportObject(bean, RMI_OBJ_PORT);
        LocateRegistry.createRegistry(RMI_REG_PORT).rebind(beanName, stub);

    }

    @Override
    protected final void afterAgentStop(final Agent agent) throws RemoteException, NotBoundException {
        LocateRegistry.getRegistry(RMI_REG_PORT).unbind(beanName);
        UnicastRemoteObject.unexportObject(bean, true);
    }

    protected String getAttributesNamespace(){
        return "";
    }

    protected abstract void fillAttributes(final Map<String, ManagementTargetConfiguration.AttributeConfiguration> attributes);


    /**
     * Represents management targets.
     *
     * @return The dictionary of management targets (management back-ends).
     */
    @Override
    public final Map<String, ManagementTargetConfiguration> getTargets() {
        return new HashMap<String, ManagementTargetConfiguration>(1){{
            final ManagementTargetConfiguration targetConfig = new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration();
            targetConfig.setConnectionString(String.format("rmi://127.0.0.1:%s/%s", RMI_REG_PORT, beanName));
            targetConfig.setConnectionType("rmi");
            targetConfig.setNamespace(getAttributesNamespace());
            fillAttributes(targetConfig.getAttributes());
            put("test-rmi", targetConfig);
        }};
    }
}
