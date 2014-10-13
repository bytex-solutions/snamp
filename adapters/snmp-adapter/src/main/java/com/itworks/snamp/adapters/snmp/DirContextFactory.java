package com.itworks.snamp.adapters.snmp;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import java.util.Hashtable;

/**
 * Represents local factory for JNDI context.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface DirContextFactory {
    DirContext create(final Hashtable<?, ?> env) throws NamingException;
}
