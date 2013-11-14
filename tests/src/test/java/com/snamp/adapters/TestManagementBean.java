package com.snamp.adapters;

import com.sun.jmx.interceptor.DefaultMBeanServerInterceptor;

import javax.management.*;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.openmbean.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: temni
 * Date: 20.10.13
 * Time: 17:16
 * This class implements com.snamp.adapters.TestManagementBeanInterface interface and needed to test
 * attributes supported by JMX adapter
 */
public final class TestManagementBean extends NotificationBroadcasterSupport implements DynamicMBean {
    public static final String BEAN_NAME = "com.snampy.jmx:type=com.snamp.adapters.TestManagementBean";

    private static final MBeanAttributeInfo STRING_PROPERTY = new OpenMBeanAttributeInfoSupport("string",
            "Sample description",
            SimpleType.STRING,
            true,
            true,
            false);
    private static final MBeanAttributeInfo BOOLEAN_PROPERTY = new OpenMBeanAttributeInfoSupport("boolean",
            "Sample description",
            SimpleType.BOOLEAN,
            true,
            true,
            false);
    private static final MBeanAttributeInfo INT32_PROPERTY = new OpenMBeanAttributeInfoSupport("int32",
            "Sample description",
            SimpleType.INTEGER,
            true,
            true,
            false);
    private static final MBeanAttributeInfo BIGINT_PROPERTY = new OpenMBeanAttributeInfoSupport("bigint",
            "Sample description",
            SimpleType.BIGINTEGER,
            true,
            true,
            false);
    private static final MBeanAttributeInfo ARRAY_PROPERTY = new OpenMBeanAttributeInfoSupport("array",
            "Sample description",
            ArrayType.getPrimitiveArrayType(short[].class),
            true,
            true,
            false);

    private static CompositeType createCompositeType(){
        try {
            return new CompositeType("dictionary", "Test dictionary",
                    new String[]{"col1", "col2", "col3"},
                    new String[]{"descr1", "descr2", "descr3"},
                    new OpenType[]{SimpleType.BOOLEAN, SimpleType.INTEGER, SimpleType.STRING});
        } catch (OpenDataException e) {
            return null;
        }
    }
    private static final OpenMBeanAttributeInfoSupport DICTIONARY_PROPERTY = new OpenMBeanAttributeInfoSupport("dictionary",
            "Composite data property",
            createCompositeType(),
            true,
            true,
            false
            );

    private static TabularType createTabularType(){
        try {
            return new TabularType("SimpleTable", "Example of simple table", createCompositeType(), new String[]{"col3"});
        } catch (OpenDataException e) {
            return null;
        }
    }

    private static final OpenMBeanAttributeInfoSupport TABLE_PROPERTY = new OpenMBeanAttributeInfoSupport("table",
            "Table data property",
            createTabularType(),
            true,
            true,
            false);

    private static final MBeanNotificationInfo PROPERTY_CHANGED_EVENT = new MBeanNotificationInfo(
            new String[]{AttributeChangeNotification.ATTRIBUTE_CHANGE},
            AttributeChangeNotification.class.getName(),
            "Occurs when property is changed"
    );

    private static final MBeanInfo BEAN_INFO = new MBeanInfo(TestManagementBean.class.getName(),
            "Test MBean",
            new MBeanAttributeInfo[]{STRING_PROPERTY,
                    BOOLEAN_PROPERTY,
                    INT32_PROPERTY,
                    BIGINT_PROPERTY,
                    ARRAY_PROPERTY,
                    DICTIONARY_PROPERTY,
                    TABLE_PROPERTY},
            new MBeanConstructorInfo[0],
            new MBeanOperationInfo[0],
            new MBeanNotificationInfo[]{PROPERTY_CHANGED_EVENT});

    private String chosenString;
    private boolean aBoolean;
    private int anInt;
    private BigInteger aBigInt;
    private Short[] array;
    private CompositeData dictionary;
    private TabularData table;
    private final AtomicLong sequenceCounter;

    public TestManagementBean() {
        super(PROPERTY_CHANGED_EVENT);
        sequenceCounter = new AtomicLong(0);
        chosenString = "NO VALUE";
        aBigInt = BigInteger.ZERO;
        array = new Short[0];
        try{
            dictionary = new CompositeDataSupport((CompositeType)DICTIONARY_PROPERTY.getOpenType(), new HashMap<String, Object>() {{
                put("col1", true);
                put("col2", 10);
                put("col3", "abc");
            }});
            table = new TabularDataSupport((TabularType)TABLE_PROPERTY.getOpenType());
            table.put(new CompositeDataSupport((CompositeType)DICTIONARY_PROPERTY.getOpenType(), new HashMap<String, Object>() {{
                put("col1", true);
                put("col2", 1050);
                put("col3", "Hello, world!");
            }}));
            table.put(new CompositeDataSupport((CompositeType)DICTIONARY_PROPERTY.getOpenType(), new HashMap<String, Object>() {{
                put("col1", false);
                put("col2", 42);
                put("col3", "Ciao, monde!");
            }}));
            table.put(new CompositeDataSupport((CompositeType)DICTIONARY_PROPERTY.getOpenType(), new HashMap<String, Object>() {{
                put("col1", true);
                put("col2", 1);
                put("col3", "Luke Skywalker");
            }}));
        }
        catch (final OpenDataException e){

        }
    }

    public final Short[] getArray(){
        return array;
    }

    public final void setArray(final Short[] value){
        array = value;
    }

    public final BigInteger getBigInt(){
        return aBigInt;
    }

    public final void setBigInt(final BigInteger value){
        aBigInt = value;
    }

    public final int getInt32(){
        return anInt;
    }

    public final void setInt32(final int value){
        anInt = value;
    }

    public final String getString(){
        return chosenString;
    }

    public final void setString(final String value){
        chosenString = value;
    }

    public final boolean getBoolean(){
        return aBoolean;
    }

    public final void setBoolean(final boolean value){
        aBoolean = value;
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
        else if(Objects.equals(attribute, BOOLEAN_PROPERTY.getName()))
            return aBoolean;
        else if(Objects.equals(attribute, INT32_PROPERTY.getName()))
            return anInt;
        else if(Objects.equals(attribute, BIGINT_PROPERTY.getName()))
            return aBigInt;
        else if(Objects.equals(attribute, ARRAY_PROPERTY.getName()))
            return array;
        else if(Objects.equals(attribute, DICTIONARY_PROPERTY.getName()))
            return dictionary;
        else if(Objects.equals(attribute, TABLE_PROPERTY.getName()))
            return table;
        else throw new AttributeNotFoundException();
    }

    private final void propertyChanged(final String attributeName, final String attributeType, final Object oldValue, final Object newValue){
        sendNotification(new AttributeChangeNotification(this,
                sequenceCounter.getAndIncrement(),
                System.currentTimeMillis(),
                String.format("Property %s is changed", attributeName),
                attributeName,
                attributeType,
                oldValue,
                newValue));
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
        final Object oldValue;
        final Object newValue;
        final String attributeType;
        if(Objects.equals(attribute.getName(), STRING_PROPERTY.getName())){
            oldValue = chosenString;
            newValue = chosenString = Objects.toString(attribute.getValue(), "");
            attributeType = STRING_PROPERTY.getType();
        }
        else if(Objects.equals(attribute.getName(), BOOLEAN_PROPERTY.getName())){
            oldValue = aBoolean;
            newValue = aBoolean = Boolean.valueOf(Objects.toString(attribute.getValue()));
            attributeType = BOOLEAN_PROPERTY.getType();
        }
        else if(Objects.equals(attribute.getName(), INT32_PROPERTY.getName())){
            oldValue = anInt;
            newValue = anInt = Integer.valueOf(Objects.toString(attribute.getValue()));
            attributeType = INT32_PROPERTY.getType();
        }
        else if(Objects.equals(attribute.getName(), BIGINT_PROPERTY.getName())){
            oldValue = aBigInt;
            attributeType = BIGINT_PROPERTY.getType();
            newValue = aBigInt = new BigInteger(Objects.toString(attribute.getValue()));
        }
        else if(Objects.equals(attribute.getName(), ARRAY_PROPERTY.getName())){
            oldValue = array;
            newValue = array = (Short[])attribute.getValue();
            attributeType = ARRAY_PROPERTY.getType();
        }
        else if(Objects.equals(attribute.getName(), DICTIONARY_PROPERTY.getName())){
            oldValue = dictionary;
            newValue = dictionary = (CompositeData)attribute.getValue();
            attributeType = DICTIONARY_PROPERTY.getType();
        }
        else if(Objects.equals(attribute.getName(), TABLE_PROPERTY.getName())){
            oldValue = table;
            newValue = table = (TabularData)attribute.getValue();
            attributeType = TABLE_PROPERTY.getType();
        }
        else throw new AttributeNotFoundException();
        propertyChanged(attribute.getName(), attributeType, oldValue, newValue);
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