package com.bytex.snamp.adapters.snmp;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import java.util.Hashtable;

/**
 * Represents local factory for JNDI context.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
interface DirContextFactory {
    /**
     * Creates a new JNDI context.
     * @param env Environment parameters used to create JNDI context.
     * @return A new instance of JNDI context.
     * @throws NamingException Unable to create JNDI context.
     */
    DirContext create(final Hashtable<String, ?> env) throws NamingException;
}
