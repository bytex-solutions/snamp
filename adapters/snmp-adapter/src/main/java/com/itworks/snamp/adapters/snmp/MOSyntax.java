package com.itworks.snamp.adapters.snmp;

import java.lang.annotation.*;

/**
 * Represents the syntax of the managed object.
 * @author Roman Sakno
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface MOSyntax {
    /**
     * One of the {@link org.snmp4j.smi.SMIConstants}.
     * @return
     */
    int value();
}
