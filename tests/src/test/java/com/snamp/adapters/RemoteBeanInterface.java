package com.snamp.adapters;

import java.math.BigInteger;
import java.rmi.*;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface RemoteBeanInterface extends Remote {
    public BigInteger getBigint() throws RemoteException;
    public void setBigint(final BigInteger value) throws RemoteException;
}
