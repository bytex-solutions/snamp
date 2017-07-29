package org.snmp4j.jmx;

import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

import javax.management.Notification;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents SNMP trap.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public class SnmpNotification extends Notification {
    private static final long serialVersionUID = 6746575967189120410L;
    private final Map<OID, Variable> variables;

    public SnmpNotification(final String type,
                            final Object source,
                            final long sequenceNumber,
                            final String message,
                            final VariableBinding... bindings) {
        super(type, source, sequenceNumber, message);
        variables = new HashMap<>(bindings.length + 3);
        for (final VariableBinding binding : bindings)
            variables.put(binding.getOid(), binding.getVariable());
    }

    public SnmpNotification(final String type,
                            final Object source,
                            final long sequenceNumber,
                            final String message,
                            final Map<OID, Variable> bindings) {
        super(type, source, sequenceNumber, message);
        variables = Objects.requireNonNull(bindings);
    }

    /**
     * Returns an array of variable bindings associated with this message.
     *
     * @return An array of variable bindings associated with this message.
     */
    public final VariableBinding[] getBindings() {
        return variables.entrySet().stream()
                .map(entry -> new VariableBinding(entry.getKey(), entry.getValue()))
                .toArray(VariableBinding[]::new);
    }
}
