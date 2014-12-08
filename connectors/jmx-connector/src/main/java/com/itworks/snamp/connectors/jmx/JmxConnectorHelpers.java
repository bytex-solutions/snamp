package com.itworks.snamp.connectors.jmx;

import com.itworks.snamp.connectors.AbstractManagedResourceConnector;

import javax.management.InvalidAttributeValueException;
import javax.management.JMException;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxConnectorHelpers {
    /**
     * Represents name of the management connector.
     */
    static final String CONNECTOR_NAME = "jmx";

    private JmxConnectorHelpers(){

    }

    static Logger getLogger(){
        return AbstractManagedResourceConnector.getLogger(CONNECTOR_NAME);
    }

    static InvalidAttributeValueException invalidAttributeValueException(final JMException inner) {
        return new InvalidAttributeValueException(inner.getMessage()){
            @Override
            public JMException getCause() {
                return inner;
            }
        };
    }
}
