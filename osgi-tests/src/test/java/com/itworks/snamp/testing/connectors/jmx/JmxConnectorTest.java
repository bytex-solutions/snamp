package com.itworks.snamp.testing.connectors.jmx;

import com.itworks.snamp.*;
import org.apache.commons.collections4.*;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeoutException;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@ExamReactorStrategy(PerMethod.class)
public final class JmxConnectorTest extends AbstractJmxConnectorTest<TestManagementBean> {

    public JmxConnectorTest() throws MalformedObjectNameException {
        super(new TestManagementBean(), new ObjectName(TestManagementBean.BEAN_NAME));
    }

    @Override
    protected final void fillAttributes(final Map<String, AttributeConfiguration> attributes, final Factory<AttributeConfiguration> attributeFactory) {
        AttributeConfiguration attribute = attributeFactory.create();
        attribute.setAttributeName("string");
        attribute.getAdditionalElements().put("objectName", TestManagementBean.BEAN_NAME);
        attributes.put("1.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("boolean");
        attribute.getAdditionalElements().put("objectName", TestManagementBean.BEAN_NAME);
        attributes.put("2.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("int32");
        attribute.getAdditionalElements().put("objectName", TestManagementBean.BEAN_NAME);
        attributes.put("3.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("bigint");
        attribute.getAdditionalElements().put("objectName", TestManagementBean.BEAN_NAME);
        attributes.put("4.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("array");
        attribute.getAdditionalElements().put("objectName", TestManagementBean.BEAN_NAME);
        attributes.put("5.1", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("dictionary");
        attribute.getAdditionalElements().put("objectName", TestManagementBean.BEAN_NAME);
        attributes.put("6.1", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("table");
        attribute.getAdditionalElements().put("objectName", TestManagementBean.BEAN_NAME);
        attributes.put("7.1", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("float");
        attribute.getAdditionalElements().put("objectName", TestManagementBean.BEAN_NAME);
        attributes.put("8.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("date");
        attribute.getAdditionalElements().put("objectName", TestManagementBean.BEAN_NAME);
        attributes.put("9.0", attribute);
    }

    @Test
    public final void testForTableProperty() throws TimeoutException{
        final Table<String> table = new SimpleTable<>(new HashMap<String, Class<?>>(3){{
            put("col1", Boolean.class);
            put("col2", Integer.class);
            put("col3", String.class);
        }});
        table.addRow(new HashMap<String, Object>(3){{
            put("col1", true);
            put("col2", 42);
            put("col3", "Frank Underwood");
        }});
        table.addRow(new HashMap<String, Object>(3){{
            put("col1", true);
            put("col2", 43);
            put("col3", "Peter Russo");
        }});
        testAttribute("7.1", "table", Table.class, table, new Equator<Table>() {
            @Override
            public boolean equate(final Table o1, final Table o2) {
                return o1.getRowCount() == o2.getRowCount() &&
                        SetUtils.isEqualSet(o1.getColumns(), o2.getColumns());
            }

            @Override
            public int hash(final Table o) {
                return System.identityHashCode(o);
            }
        });
    }

    @Test
    public final void testForDictionaryProperty() throws TimeoutException{
        final Map<String, Object> dict = new HashMap<>(3);
        dict.put("col1", Boolean.TRUE);
        dict.put("col2", 42);
        dict.put("col3", "Frank Underwood");
        testAttribute("6.1", "dictionary", Map.class, dict, new Equator<Map>() {
            @Override
            public boolean equate(final Map o1, final Map o2) {
                if(o1.size() == o2.size()) {
                    for (final Object key : o1.keySet())
                        if (!o2.containsKey(key)) return false;
                    return true;
                }
                else return false;
            }

            @Override
            public int hash(final Map o) {
                return System.identityHashCode(o);
            }
        });
    }

    @Test
    public final void testForArrayProperty() throws TimeoutException{
        final Object[] array = new Short[]{10, 20, 30, 40, 50};
        testAttribute("5.1", "array", Object[].class, array, new Equator<Object[]>() {
            @Override
            public boolean equate(final Object[] o1, final Object[] o2) {
                return Arrays.equals(o1, o2);
            }

            @Override
            public int hash(final Object[] o) {
                return System.identityHashCode(o);
            }
        });
    }

    @Test
    public final void testForDateProperty() throws TimeoutException{
        testAttribute("9.0", "date", Date.class, new Date());
    }

    @Test
    public final void testForFloatProperty() throws TimeoutException{
        testAttribute("8.0", "float", Float.class, 3.14F);
    }

    @Test
    public final void testForBigIntProperty() throws TimeoutException{
        testAttribute("4.0", "bigint", BigInteger.class, BigInteger.valueOf(100500));
    }

    @Test
    public final void testForInt32Property() throws TimeoutException{
        testAttribute("3.0", "int32", Integer.class, 42);
    }

    @Test
    public final void testForBooleanProperty() throws TimeoutException {
        testAttribute("2.0", "boolean", Boolean.class, Boolean.TRUE);
    }

    @Test
    public final void testForStringProperty() throws TimeoutException {
        testAttribute("1.0", "string", String.class, "Frank Underwood");
    }
}
