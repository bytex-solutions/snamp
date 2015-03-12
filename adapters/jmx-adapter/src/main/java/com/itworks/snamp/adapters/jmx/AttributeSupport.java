package com.itworks.snamp.adapters.jmx;

import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.openmbean.OpenMBeanAttributeInfo;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface AttributeSupport {
    /**
     * Obtain the value of a specific attribute of the Dynamic MBean.
     *
     * @param attribute The name of the attribute to be retrieved
     *
     * @return  The value of the attribute retrieved.
     *
     * @exception AttributeNotFoundException
     * @exception MBeanException  Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's getter.
     * @exception ReflectionException  Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the getter.
     *
     * @see #setAttribute
     */
    Object getAttribute(final String attribute) throws AttributeNotFoundException,
            MBeanException, ReflectionException;

    /**
     * Set the value of a specific attribute of the Dynamic MBean.
     *
     * @param attributeName The identification of the attribute to
     * be set and  the value it is to be set to.
     * @param value The value of the attribute.
     *
     * @exception AttributeNotFoundException
     * @exception javax.management.InvalidAttributeValueException
     * @exception MBeanException Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's setter.
     * @exception ReflectionException Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the MBean's setter.
     *
     * @see #getAttribute
     */
    void setAttribute(final String attributeName, final Object value) throws AttributeNotFoundException,
            InvalidAttributeValueException, MBeanException, ReflectionException ;

    OpenMBeanAttributeInfo[] getAttributeInfo();
}
