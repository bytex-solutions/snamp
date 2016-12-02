package com.bytex.snamp.instrumentation;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.TreeSet;

/**
 * Provides different sources of component name.
 */
enum  ComponentInstanceSource { //WARNING: order of this enum is significant for callers
    /**
     * Use network interface address as instance name.
     */
    MACHINE_ADDRESS {
        @Override
        String getInstance() {
            final String LOCALHOST = "127.0.0.1";
            final Enumeration<NetworkInterface> ifaces;
            try {
                ifaces = NetworkInterface.getNetworkInterfaces();
            } catch (final SocketException e) {
                return LOCALHOST;
            }
            final TreeSet<String> siteLocalCandidates = new TreeSet<String>();
            final TreeSet<String> candidates = new TreeSet<String>();
            for (NetworkInterface iface; ifaces.hasMoreElements(); ) {
                iface = ifaces.nextElement();
                for (final Enumeration<InetAddress> addrs = iface.getInetAddresses(); addrs.hasMoreElements(); ) {
                    final InetAddress addr = addrs.nextElement();
                    if (!addr.isLoopbackAddress())
                        (addr.isSiteLocalAddress() ? siteLocalCandidates : candidates).add(addr.getHostAddress());
                }
            }
            if (siteLocalCandidates.isEmpty())
                return candidates.isEmpty() ? LOCALHOST : candidates.first();
            else
                return siteLocalCandidates.first();
        }
    };

    /**
     * Gets instance using the specified source.
     * @return Instance of the component.
     */
    abstract String getInstance();
}
