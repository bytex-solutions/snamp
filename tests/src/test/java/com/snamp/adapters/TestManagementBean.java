package com.snamp.adapters;

import javax.management.*;
import javax.management.openmbean.*;
import java.util.Objects;

/**
 * Created with IntelliJ IDEA.
 * User: temni
 * Date: 20.10.13
 * Time: 17:16
 * This class implements com.snamp.adapters.TestManagementBeanInterface interface and needed to test
 * attributes supported by JMX adapter
 */
public final class TestManagementBean implements DynamicMBean {
    private static final MBeanAttributeInfo STRING_PROPERTY = new OpenMBeanAttributeInfoSupport("string",
            "Sample description",
            SimpleType.STRING,
            true,
            true,
            false);

    private static final MBeanInfo BEAN_INFO = new MBeanInfo(TestManagementBean.class.getName(),
            "Test MBean",
            new MBeanAttributeInfo[]{STRING_PROPERTY},
            new MBeanConstructorInfo[0],
            new MBeanOperationInfo[0],
            new MBeanNotificationInfo[0]);

    private String chosenString;

    public TestManagementBean() {
        chosenString = "NO VALUE";
    }

    public final String getString(){
        return chosenString;
    }

    public final void setString(final String value){
        chosenString = value;
    }

    /**
     * Obtain the value of a specific attribute of the Dynamic MBean.
     *
     * @param attribute The name of the attribute to be retrieved
     * @return The value of the attribute retrieved.
     * @throws javax.management.AttributeNotFoundException
     *
     * @throws javax.management.MBeanException
     *          Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's getter.
     * @throws javax.management.ReflectionException
     *          Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the getter.
     * @see #setAttribute
     */
    @Override
    public final Object getAttribute(final String attribute) throws AttributeNotFoundException {
        if(Objects.equals(attribute, STRING_PROPERTY.getName()))
            return chosenString;
        else throw new AttributeNotFoundException();
    }

    /**
     * Set the value of a specific attribute of the Dynamic MBean.
     *
     * @param attribute The identification of the attribute to
     *                  be set and  the value it is to be set to.
     * @throws javax.management.AttributeNotFoundException
     *
     * @throws javax.management.InvalidAttributeValueException
     *
     * @throws javax.management.MBeanException
     *          Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's setter.
     * @throws javax.management.ReflectionException
     *          Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the MBean's setter.
     * @see #getAttribute
     */
    @Override
    public final void setAttribute(final Attribute attribute) throws AttributeNotFoundException{
        if(Objects.equals(attribute, STRING_PROPERTY.getName()))
            chosenString = Objects.toString(attribute.getValue(), "");
        else throw new AttributeNotFoundException();
    }

    /**
     * Get the values of several attributes of the Dynamic MBean.
     *
     * @param attributes A list of the attributes to be retrieved.
     * @return The list of attributes retrieved.
     * @see #setAttributes
     */
    @Override
    public final AttributeList getAttributes(final String[] attributes) {
        final AttributeList result = new AttributeList();
        for(final String aname: attributes)
            try{
                result.add(new Attribute(aname, getAttribute(aname)));
            }
            catch (final AttributeNotFoundException e){

            }
        return result;
    }

    /**
     * Sets the values of several attributes of the Dynamic MBean.
     *
     * @param attributes A list of attributes: The identification of the
     *                   attributes to be set and  the values they are to be set to.
     * @return The list of attributes that were set, with their new values.
     * @see #getAttributes
     */
    @Override
    public final AttributeList setAttributes(final AttributeList attributes) {
        final AttributeList result = new AttributeList();
        for(final Attribute a: attributes.asList())
            try {
                setAttribute(a);
                result.add(new Attribute(a.getName(), getAttribute(a.getName())));
            }
            catch (final AttributeNotFoundException e){

            }
        return result;
    }

    /**
     * Allows an action to be invoked on the Dynamic MBean.
     *
     * @param actionName The name of the action to be invoked.
     * @param params     An array containing the parameters to be set when the action is
     *                   invoked.
     * @param signature  An array containing the signature of the action. The class objects will
     *                   be loaded through the same class loader as the one used for loading the
     *                   MBean on which the action is invoked.
     * @return The object returned by the action, which represents the result of
     *         invoking the action on the MBean specified.
     * @throws javax.management.MBeanException
     *          Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's invoked method.
     * @throws javax.management.ReflectionException
     *          Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the method
     */
    @Override
    public Object invoke(final String actionName, final Object[] params, final String[] signature) throws MBeanException, ReflectionException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Provides the exposed attributes and actions of the Dynamic MBean using an MBeanInfo object.
     *
     * @return An instance of <CODE>MBeanInfo</CODE> allowing all attributes and actions
     *         exposed by this Dynamic MBean to be retrieved.
     */
    @Override
    public final MBeanInfo getMBeanInfo() {
        return BEAN_INFO;
    }
}