package com.snamp.connectors;

import java.beans.*;
import java.lang.reflect.Proxy;
import java.rmi.*;
import java.rmi.registry.*;
import java.util.logging.Logger;

/**
 * Represents RMI connector.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class RmiConnector extends ManagementConnectorBean {
    /**
     * Represents name of this connector.
     */
    public final static String NAME = "rmi";

    private final Logger logger;

    private static final class RemoteBeanIntrospector implements BeanIntrospector<Remote> {
        @Override
        public BeanInfo getBeanInfo(final Remote beanInstance) throws IntrospectionException {
            return Introspector.getBeanInfo(beanInstance.getClass(), Proxy.class);
        }
    }

    public RmiConnector(final String host, final int port, final String objectName, final Logger logger) throws RemoteException, NotBoundException, IntrospectionException {
        super(LocateRegistry.getRegistry(host, port).lookup(objectName), new RemoteBeanIntrospector(), new WellKnownTypeSystem());
        this.logger = logger;
    }
}
