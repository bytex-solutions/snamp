package com.snamp.adapters;

import com.snamp.SnampClassTestSet;
import com.snamp.TimeSpan;
import com.snamp.connectors.*;
import static com.snamp.hosting.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration;
import org.junit.Test;

import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author roman
 */
public final class EmbeddedAdapterTest extends SnampClassTestSet<EmbeddedAdapter> {
    public static final class TestAdapter extends EmbeddedAdapter{
        public TestAdapter(){
            super("Test");
        }

        public final int getProperty(){
            return getAttribute("", "0.1", int.class, 0);
        }

        public final boolean setProperty(final int value){
            return setAttribute("", "0.1", value);
        }
    }

    @Test
    public final void embeddingTest() throws IntrospectionException {
        final EmbeddedManagementConnector connector = EmbeddedManagementConnector.wrap(new Object(){
            private int value;

            public final int getProperty(){
                return value;
            }

            public final void setProperty(final int val){
                value = val;
            }
        }, new AttributePrimitiveTypeBuilder());
        final TestAdapter adapter = new TestAdapter();
        adapter.exposeAttributes(connector, "", new HashMap<String, AttributeConfiguration>(){{
            put("0.1", new AttributeConfiguration() {
                @Override
                public TimeSpan getReadWriteTimeout() {
                    return TimeSpan.INFINITE;
                }

                @Override
                public void setReadWriteTimeout(final TimeSpan time) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public String getAttributeName() {
                    return "property";
                }

                @Override
                public void setAttributeName(final String attributeName) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Map<String, String> getAdditionalElements() {
                    return new HashMap<>();
                }
            });
        }});
        assertTrue(adapter.setProperty(20));
        assertEquals(20, adapter.getProperty());
    }
}
