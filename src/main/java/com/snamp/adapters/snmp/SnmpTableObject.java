package com.snamp.adapters.snmp;

import com.snamp.TimeSpan;
import com.snamp.connectors.*;
import org.snmp4j.agent.mo.*;
import org.snmp4j.smi.*;

import java.lang.ref.*;
import java.util.*;

/**
 * Represents SNMP table.
 * @author roman
 */
final class SnmpTableObject extends DefaultMOTable<MOTableRow<Variable>, MOColumn<Variable>, DefaultMOMutableTableModel<MOTableRow<Variable>>> implements SnmpAttributeMapping{
    private static MOColumn<Variable>[] getDictionaryColumns(){
        return new MOColumn[]{
            new MOMutableColumn<>(0, SMIConstants.SYNTAX_OCTET_STRING, MOAccessImpl.ACCESS_READ_ONLY, new OctetString("key")),
            new MOMutableColumn<>(1, SMIConstants.SYNTAX_OCTET_STRING, MOAccessImpl.ACCESS_READ_ONLY, new OctetString("value"))
        };
    }

    private static MOColumn<Variable>[] getColumns(final AttributeTypeInfo typeInfo){
        if(typeInfo instanceof AttributeDictionaryType)
            return getDictionaryColumns();
        else return new MOColumn[0];
    }

    private final Reference<ManagementConnector> connector;

    private SnmpTableObject(final String oid, final ManagementConnector connector, final TimeSpan timeouts, final AttributeMetadata attribute){
        super(new OID(oid), new MOTableIndex(new MOTableSubIndex[0]), getColumns(attribute.getAttributeType()));
        this.connector = new WeakReference<ManagementConnector>(connector);

    }

    public SnmpTableObject(final String oid, final ManagementConnector connector, final TimeSpan timeouts){
        this(oid, connector, timeouts, connector.getAttributeInfo(oid));
    }

    private final AttributeMetadata getMetadata(final ManagementConnector connector){
        return connector != null ? connector.getAttributeInfo(Objects.toString(getID(), "")) : null;
    }

    /**
     * Returns the metadata of the underlying attribute.
     *
     * @return The metadata of the underlying attribute.
     */
    @Override
    public AttributeMetadata getMetadata() {
        return getMetadata(connector.get());
    }
}
