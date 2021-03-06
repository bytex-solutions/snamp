package com.bytex.snamp.connector.attributes;

import com.bytex.snamp.configuration.EntityConfiguration;
import com.bytex.snamp.connector.ManagedResourceAggregatedService;

import javax.management.*;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
 * @version 2.0
 * @since 1.0
 */
public interface AttributeSupport extends ManagedResourceAggregatedService {
    /**
     * The name of field in {@link javax.management.Descriptor} which contains
     * the name of the attribute.
     */
    String ATTRIBUTE_NAME_FIELD = "attributeName";

    /**
     * The name of the field in {@link javax.management.Descriptor} which
     * contains {@link java.time.Duration} value.
     */
    String READ_WRITE_TIMEOUT_FIELD = "readWriteTimeout";

    /**
     * The name of the field in {@link javax.management.Descriptor}
     * which contains attribute description.
     */
    String DESCRIPTION_FIELD = EntityConfiguration.DESCRIPTION_KEY;

    /**
     * The name of the field of {@link javax.management.openmbean.OpenType} in {@link javax.management.Descriptor}
     * which describes the attribute type.
     */
    String OPEN_TYPE = JMX.OPEN_TYPE_FIELD;

    /**
     * Registers a new attribute in the managed resource connector.
     * @param attributeName The name of the attribute in the managed resource.
     * @param descriptor Descriptor of created attribute.
     * @return Metadata of created attribute.
     * @since 2.0
     */
    Optional<? extends MBeanAttributeInfo> addAttribute(final String attributeName, final AttributeDescriptor descriptor);

    /**
     * Removes attribute from the managed resource.
     * @param attributeName Name of the attribute to remove.
     * @return An instance of removed attribute; or {@link Optional#empty()}, if attribute with the specified name doesn't exist.
     * @since 2.0
     */
    Optional<? extends MBeanAttributeInfo> removeAttribute(final String attributeName);

    /**
     * Removes all attributes except specified in the collection.
     * @param attributes A set of attributes which should not be deleted.
     * @since 2.0
     */
    void retainAttributes(final Set<String> attributes);

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
    Object getAttribute(final String attribute) throws AttributeNotFoundException,
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
    void setAttribute(final Attribute attribute) throws AttributeNotFoundException,
            InvalidAttributeValueException, MBeanException, ReflectionException;

    /**
     * Get the values of several attributes of the managed resource.
     *
     * @param attributes A list of the attributes to be retrieved.
     *
     * @return  The list of attributes retrieved.
     *
     * @see #setAttributes
     */
    AttributeList getAttributes(final String[] attributes);

    /**
     * Gets the values of all attributes.
     * @return The values of all attributes
     */
    AttributeList getAttributes() throws MBeanException, ReflectionException;

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
    AttributeList setAttributes(final AttributeList attributes);

    /**
     * Gets an array of connected attributes.
     * @return An array of connected attributes.
     */
    MBeanAttributeInfo[] getAttributeInfo();

    /**
     * Gets attribute metadata.
     * @param attributeName The name of the attribute.
     * @return The attribute metadata; or {@link Optional#empty()}, if attribute doesn't exist.
     */
    Optional<? extends MBeanAttributeInfo> getAttributeInfo(final String attributeName);

    /**
     * Discover attributes.
     *
     * @return A map of discovered attributed that can be added using method {@link #addAttribute(String, AttributeDescriptor)}.
     * @since 2.0
     */
    default Map<String, AttributeDescriptor> discoverAttributes(){
        return Collections.emptyMap();
    }
}
