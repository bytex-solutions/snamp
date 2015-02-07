package com.itworks.snamp.connectors.attributes;

import com.itworks.snamp.TimeSpan;

import javax.management.*;
import javax.management.openmbean.CompositeData;

import static com.itworks.snamp.configuration.AgentConfiguration.ConfigurationEntity;

/**
 * Represents support for management attributes.
 * <p>
 *     The type of each managed resource attribute is limited to the following classes:
 *     <ul>
 *         <li>All JMX Open Types are valid attribute types. For more information, see {@link javax.management.openmbean.OpenType}</li>
 *         <li>{@link java.nio.ByteBuffer}</li>
 *         <li>{@link java.nio.CharBuffer}</li>
 *         <li>{@link java.nio.ShortBuffer}</li>
 *         <li>{@link java.nio.IntBuffer}</li>
 *         <li>{@link java.nio.LongBuffer}</li>
 *         <li>{@link java.nio.FloatBuffer}</li>
 *         <li>{@link java.nio.DoubleBuffer}</li>
 *     </ul>
 * </p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface AttributeSupport {
    /**
     * The name of field in {@link javax.management.Descriptor} which contains
     * the name of the attribute.
     */
    String ATTRIBUTE_NAME_FIELD = "attributeName";

    /**
     * The name of the field in {@link javax.management.Descriptor} which
     * contains {@link com.itworks.snamp.TimeSpan} value.
     */
    String READ_WRITE_TIMEOUT_FIELD = "readWriteTimeout";

    /**
     * The name of the field in {@link javax.management.Descriptor}
     * which contains attribute description.
     */
    String DESCRIPTION_FIELD = ConfigurationEntity.DESCRIPTION_KEY;

    /**
     * Connects to the specified attribute.
     * @param id A key string that is used to invoke attribute from this connector.
     * @param attributeName The name of the attribute.
     * @param readWriteTimeout A read/write timeout using for attribute read/write operation.
     * @param options The attribute discovery options.
     * @return The description of the attribute.
     * @throws javax.management.AttributeNotFoundException The managed resource doesn't provide the attribute with the specified name.
     * @throws javax.management.JMException Internal connector error.
     */
    MBeanAttributeInfo connectAttribute(final String id,
                                       final String attributeName,
                                       final TimeSpan readWriteTimeout,
                                       final CompositeData options) throws JMException;

    /**
     * Obtain the value of a specific attribute of the managed resource.
     *
     * @param attribute The name of the attribute to be retrieved
     *
     * @return  The value of the attribute retrieved.
     *
     * @exception javax.management.AttributeNotFoundException
     * @exception javax.management.MBeanException  Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's getter.
     * @exception javax.management.ReflectionException  Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the getter.
     *
     * @see #setAttribute
     */
    Object getAttribute(String attribute) throws AttributeNotFoundException,
            MBeanException, ReflectionException;

    /**
     * Set the value of a specific attribute of the managed resource.
     *
     * @param attribute The identification of the attribute to
     * be set and  the value it is to be set to.
     *
     * @exception AttributeNotFoundException
     * @exception InvalidAttributeValueException
     * @exception MBeanException Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's setter.
     * @exception ReflectionException Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the MBean's setter.
     *
     * @see #getAttribute
     */
    void setAttribute(Attribute attribute) throws AttributeNotFoundException,
            InvalidAttributeValueException, MBeanException, ReflectionException ;

    /**
     * Get the values of several attributes of the managed resource.
     *
     * @param attributes A list of the attributes to be retrieved.
     *
     * @return  The list of attributes retrieved.
     *
     * @see #setAttributes
     */
    AttributeList getAttributes(String[] attributes);

    /**
     * Sets the values of several attributes of the managed resource.
     *
     * @param attributes A list of attributes: The identification of the
     * attributes to be set and  the values they are to be set to.
     *
     * @return  The list of attributes that were set, with their new values.
     *
     * @see #getAttributes
     */
    AttributeList setAttributes(AttributeList attributes);

    /**
     * Gets an array of connected attributes.
     * @return An array of connected attributes.
     */
    MBeanAttributeInfo[] getAttributeInfo();

    /**
     * Removes the attribute from the connector.
     * @param id The unique identifier of the attribute.
     * @return {@literal true}, if the attribute successfully disconnected; otherwise, {@literal false}.
     */
    boolean disconnectAttribute(final String id);
}
