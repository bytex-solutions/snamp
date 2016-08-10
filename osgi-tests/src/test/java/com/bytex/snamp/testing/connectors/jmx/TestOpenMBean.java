package com.bytex.snamp.testing.connectors.jmx;


import com.bytex.snamp.concurrent.Repeater;
import com.google.common.collect.ImmutableMap;

import javax.management.*;
import javax.management.openmbean.*;
import javax.management.timer.TimerNotification;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents simple management bean.
 */
public final class TestOpenMBean extends NotificationBroadcasterSupport implements DynamicMBean {
    public static final String BEAN_NAME = "com.bytex.snamp:type=TestManagementBean";

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
    private static final MBeanAttributeInfo FLOAT_PROPERTY = new OpenMBeanAttributeInfoSupport("float",
            "Sample description",
            SimpleType.FLOAT,
            true,
            true,
            false);

    private static final MBeanAttributeInfo DATE_PROPERTY = new OpenMBeanAttributeInfoSupport("date",
            "Sample description",
            SimpleType.DATE,
            true,
            true,
            false);

    private static final MBeanOperationInfo REVERSE_METHOD = new MBeanOperationInfo("reverse",
            "Reverse byte array",
            new MBeanParameterInfo[]{new OpenMBeanParameterInfoSupport("array", "desc", ArrayType.getPrimitiveArrayType(byte[].class))},
            byte[].class.getName(),
            MBeanOperationInfo.INFO
    );

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

    private static final MBeanNotificationInfo TIMER_EVENT = new MBeanNotificationInfo(
            new String[]{"com.bytex.snamp.connector.tests.impl.testnotif"},
            TimerNotification.class.getName(),
            "Occurs when timer is changed"
    );

    private static final MBeanNotificationInfo PLAIN_EVENT = new MBeanNotificationInfo(
            new String[]{"com.bytex.snamp.connector.tests.impl.plainnotif"},
            Notification.class.getName(),
            "Notification with attachment"
    );

    private static final MBeanInfo BEAN_INFO = new MBeanInfo(TestOpenMBean.class.getName(),
            "Test MBean",
            new MBeanAttributeInfo[]{STRING_PROPERTY,
                    BOOLEAN_PROPERTY,
                    INT32_PROPERTY,
                    BIGINT_PROPERTY,
                    ARRAY_PROPERTY,
                    DICTIONARY_PROPERTY,
                    TABLE_PROPERTY,
                    FLOAT_PROPERTY,
                    DATE_PROPERTY
            },
            new MBeanConstructorInfo[0],
            new MBeanOperationInfo[]{REVERSE_METHOD},
            new MBeanNotificationInfo[]{PROPERTY_CHANGED_EVENT, TIMER_EVENT, PLAIN_EVENT});

    private volatile String chosenString;
    private volatile boolean aBoolean;
    private volatile int anInt;
    private volatile BigInteger aBigInt;
    private volatile short[] array;
    private volatile CompositeData dictionary;
    private volatile TabularData table;
    private final AtomicLong sequenceCounter;
    private volatile float aFloat;
    private volatile Date aDate;

    public TestOpenMBean(final boolean generateNotifs) {
        super(PROPERTY_CHANGED_EVENT, TIMER_EVENT);
        sequenceCounter = new AtomicLong(0);
        chosenString = "NO VALUE";
        aBigInt = BigInteger.ZERO;
        array = new short[]{42,100,43,99};
        aFloat = 0F;
        aDate = new Date();
        try{
            dictionary = new CompositeDataSupport((CompositeType)DICTIONARY_PROPERTY.getOpenType(), ImmutableMap.of(
                "col1", true,
                "col2", 10,
                "col3", "abc"
            ));
            table = new TabularDataSupport((TabularType)TABLE_PROPERTY.getOpenType());
            table.put(new CompositeDataSupport((CompositeType)DICTIONARY_PROPERTY.getOpenType(), ImmutableMap.of(
                "col1", true,
                "col2", 1050,
                "col3", "Hello, world!"
            )));
            table.put(new CompositeDataSupport((CompositeType)DICTIONARY_PROPERTY.getOpenType(), ImmutableMap.of(
                "col1", false,
                "col2", 42,
                "col3", "Ciao, monde!"
            )));
            table.put(new CompositeDataSupport((CompositeType)DICTIONARY_PROPERTY.getOpenType(), ImmutableMap.of(
                "col1", true,
                "col2", 1,
                "col3", "Luke Skywalker"
            )));
        }
        catch (final OpenDataException ignored){

        }
        if(generateNotifs){
            final Repeater generator = new Repeater(Duration.ofSeconds(2)) {
                @Override
                protected void doAction() {
                    propertyChanged("ATTR", STRING_PROPERTY.getType(), "previous", "next");
                }
            };
            generator.run();
        }
    }

    public TestOpenMBean() {
        this(false);
    }

    public short[] getArray(){
        return array;
    }

    public void setArray(final short[] value){
        array = value;
    }

    public BigInteger getBigInt(){
        return aBigInt;
    }

    public void setBigInt(final BigInteger value){
        aBigInt = value;
    }

    public int getInt32(){
        return anInt;
    }

    public void setInt32(final int value){
        anInt = value;
    }

    public String getString(){
        return chosenString;
    }

    public void setString(final String value){
        chosenString = value;
    }

    public boolean getBoolean(){
        return aBoolean;
    }

    public void setBoolean(final boolean value){
        aBoolean = value;
    }

    public float getFloat() {
        return aFloat;
    }

    public void setFloat(final float aFloat) {
        this.aFloat = aFloat;
    }

    public Date getDate() {
        return aDate;
    }

    public void setDate(final Date aDate) {
        this.aDate = aDate;
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
    public Object getAttribute(final String attribute) throws AttributeNotFoundException {
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
        else if(Objects.equals(attribute, FLOAT_PROPERTY.getName()))
            return aFloat;
        else if(Objects.equals(attribute, DATE_PROPERTY.getName()))
            return aDate;
        else throw new AttributeNotFoundException();
    }

    private void propertyChanged(final String attributeName, final String attributeType, final Object oldValue, final Object newValue){
        sendNotification(new AttributeChangeNotification(this,
                sequenceCounter.getAndIncrement(),
                System.currentTimeMillis(),
                String.format("Property %s is changed", attributeName),
                attributeName,
                attributeType,
                oldValue,
                newValue));
        sendNotification(new TimerNotification(TIMER_EVENT.getNotifTypes()[0],
                this,
                sequenceCounter.getAndIncrement(),
                System.currentTimeMillis(),
                "Property changed",
                32));
        final Notification notif = new Notification(PLAIN_EVENT.getNotifTypes()[0],
                this,
                sequenceCounter.getAndIncrement(),
                System.currentTimeMillis(),
                "Message");
        notif.setUserData(table);
        sendNotification(notif);
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
    public void setAttribute(final Attribute attribute) throws AttributeNotFoundException{
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
            newValue = array = (short[])attribute.getValue();
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
        else if(Objects.equals(attribute.getName(), FLOAT_PROPERTY.getName())){
            oldValue = aFloat;
            attributeType = FLOAT_PROPERTY.getType();
            newValue = aFloat = Float.valueOf(Objects.toString(attribute.getValue()));
        }
        else if(Objects.equals(attribute.getName(), DATE_PROPERTY.getName())){
            oldValue = aDate;
            attributeType = DATE_PROPERTY.getType();
            newValue = aDate = (Date) attribute.getValue();
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
    public AttributeList getAttributes(final String[] attributes) {
        final AttributeList result = new AttributeList();
        for(final String aname: attributes)
            try{
                result.add(new Attribute(aname, getAttribute(aname)));
            }
            catch (final AttributeNotFoundException ignored){

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
    public AttributeList setAttributes(final AttributeList attributes) {
        final AttributeList result = new AttributeList();
        for(final Attribute a: attributes.asList())
            try {
                setAttribute(a);
                result.add(new Attribute(a.getName(), getAttribute(a.getName())));
            }
            catch (final AttributeNotFoundException ignored){

            }
        return result;
    }

    private static byte[] reverse(final byte[] array){
        final byte[] result = new byte[array.length];
        for(int i = 0; i < result.length; i++)
            result[result.length - i - 1] = array[i];
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
        if(REVERSE_METHOD.getName().equals(actionName))
            return reverse((byte[])params[0]);
        else return null;
    }

    /**
     * Provides the exposed attributes and actions of the Dynamic MBean using an MBeanInfo object.
     *
     * @return An instance of <CODE>MBeanInfo</CODE> allowing all attributes and actions
     *         exposed by this Dynamic MBean to be retrieved.
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        return BEAN_INFO;
    }
}