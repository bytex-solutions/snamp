package com.bytex.snamp.adapters.snmp;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.Consumer;
import com.bytex.snamp.SafeConsumer;
import com.bytex.snamp.connectors.notifications.NotificationDescriptor;
import com.bytex.snamp.connectors.notifications.Severity;
import com.bytex.snamp.jmx.TabularDataUtils;
import com.bytex.snamp.jmx.WellKnownType;
import org.snmp4j.smi.*;

import javax.management.DescriptorRead;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.bytex.snamp.adapters.snmp.SnmpAdapterConfigurationDescriptor.parseDateTimeDisplayFormat;

/**
 * Represents SNMP notification with attachments.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpNotification extends HashMap<OID, Variable> {
    private static final long serialVersionUID = -9060826056942721355L;
    /**
     * Represents identifier of this SNMP notification instance.
     */
    public final OID notificationID;
    private final OID messageId;
    private final OID severityId;
    private final OID sequenceNumberId;
    private final OID timeStampId;
    private final OID categoryId;
    private final OID eventNameId;
    private final OID sourceId;
    private static final int MAX_RESERVED_POSTFIX = 10;

    /**
     * Initializes a new SNMP notification message.
     * @param notificationID Notification identifier. Cannot be {@literal null}.
     */
    SnmpNotification(final OID notificationID, final VariableBinding... bindings){
        super(bindings.length > 0 ? bindings.length : 4);
        if(notificationID == null) throw new IllegalArgumentException("notificationID is null.");
        this.notificationID = notificationID;
        for(final VariableBinding b: bindings)
            put(b);
        messageId = new OID(notificationID).append(1);
        severityId = new OID(notificationID).append(2);
        sequenceNumberId = new OID(notificationID).append(3);
        timeStampId = new OID(notificationID).append(4);
        categoryId = new OID(notificationID).append(5);
        eventNameId = new OID(notificationID).append(6);
        sourceId = new OID(notificationID).append(7);
    }

    SnmpNotification(final OID notificationID,
                     final Notification n,
                     final MBeanNotificationInfo options,
                     final SnmpTypeMapper mapper) {
        this(notificationID);
        put(messageId, SnmpHelpers.toOctetString(n.getMessage()));
        put(severityId, new Integer32(NotificationDescriptor.getSeverity(options).getLevel()));
        put(sequenceNumberId, new Counter64(n.getSequenceNumber()));
        put(categoryId, SnmpHelpers.toOctetString(NotificationDescriptor.getNotificationCategory(options)));
        final DateTimeFormatter formatter = SnmpHelpers.createDateTimeFormatter(parseDateTimeDisplayFormat(options));
        put(timeStampId, new OctetString(formatter.convert(new Date(n.getTimeStamp()))));
        putAttachment(notificationID, n.getUserData(), options, this, mapper);
        put(eventNameId, SnmpHelpers.toOctetString(n.getType()));
        put(sourceId, SnmpHelpers.toOctetString(Objects.toString(n.getSource(), "")));
    }

    private static void putAttachment(final OID notificationID,
                                      final Object attachment,
                                      final DescriptorRead options,
                                      final Map<OID, Variable> output,
                                      final SnmpTypeMapper mapper) {
        if (attachment == null) return;
        final SnmpType type = mapper.apply(WellKnownType.fromValue(attachment));
        assert type != null;
        if (type.isScalar()) {
            final Variable value = type.convert(attachment, options);
            output.put(new OID(notificationID).append(MAX_RESERVED_POSTFIX + 1), value != null ? value : new Null());
        } else if (Objects.equals(type, SnmpType.TABLE))
            forEachVariable(attachment, options, mapper, new SafeConsumer<VariableBinding>() {
                @Override
                public void accept(final VariableBinding binding) {
                    output.put(new OID(notificationID).append(MAX_RESERVED_POSTFIX + 1).append(binding.getOid()), binding.getVariable());
                }
            });
    }

    private static <E extends Exception> void forEachVariable(final CompositeData attachment,
                                                              final DescriptorRead options,
                                                              final SnmpTypeMapper typeMapper,
                                                              final Consumer<VariableBinding, E> handler) throws E {
        int index = 0;
        for(final String itemName: attachment.getCompositeType().keySet()){
            final WellKnownType itemType = WellKnownType.getType(attachment.getCompositeType().getType(itemName));
            if(itemType == null || !itemType.isPrimitive()) continue;
            final SnmpType snmpType = typeMapper.apply(itemType);
            assert snmpType != null;
            handler.accept(new VariableBinding(new OID(new int[]{index++}), snmpType.convert(attachment.get(itemName), options)));
        }
    }

    private static <E extends Exception> void iterateOverArray(final Object array,
                                                               final DescriptorRead options,
                                                               final SnmpTypeMapper typeMapper,
                                                               final Consumer<VariableBinding, E> handler) throws E{
        final WellKnownType elementType = WellKnownType.getType(array.getClass().getComponentType());
        if(elementType == null || !elementType.isPrimitive()) return;
        final SnmpType snmpType = typeMapper.apply(elementType);
        assert snmpType != null;
        for(int i = 0; i < Array.getLength(array); i++) {
            handler.accept(new VariableBinding(new OID(new int[]{i}), snmpType.convert(Array.get(array, i), options)));
        }
    }

    private static <E extends Exception> void forEachVariable(final TabularData attachment,
                                                              final DescriptorRead options,
                                                              final SnmpTypeMapper typeMapper,
                                                              final Consumer<VariableBinding, E> handler) throws E{
        final MutableInteger rowIndex = new MutableInteger(0);
        TabularDataUtils.forEachRow(attachment, new Consumer<CompositeData, E>() {
            @Override
            public void accept(final CompositeData value) throws E {
                int columnIndex = 0;
                for(final String columnName: value.getCompositeType().keySet()){
                    final SnmpType columnType = typeMapper.apply(WellKnownType.getType(value.getCompositeType().getType(columnName)));
                    assert columnType != null;
                    handler.accept(new VariableBinding(new OID(new int[]{columnIndex++, rowIndex.getAndIncrement()}), columnType.convert(value.get(columnName), options)));
                }
            }
        });
    }

    static <E extends Exception> void forEachVariable(final Object attachment,
                                                      final DescriptorRead options,
                                                      final SnmpTypeMapper typeMapper,
                                                      final Consumer<VariableBinding, E> handler) throws E{
        if(attachment instanceof CompositeData)
            forEachVariable((CompositeData)attachment, options, typeMapper, handler);
        else if(ArrayUtils.isArray(attachment))
            iterateOverArray(attachment, options, typeMapper, handler);
        else if(attachment instanceof TabularData)
            forEachVariable((TabularData)attachment, options, typeMapper, handler);
    }

    boolean put(final VariableBinding binding) {
        if (binding == null || containsKey(binding.getOid())) return false;
        put(binding.getOid(), binding.getVariable());
        return true;
    }

    /**
     * Returns an array of variable bindings associated with this message.
     * @return An array of variable bindings associated with this message.
     */
    VariableBinding[] getBindings(){
        final VariableBinding[] result = new VariableBinding[size()];
        int i = 0;
        for(final Map.Entry<OID, Variable> entry: entrySet())
            result[i++] = new VariableBinding(entry.getKey(), entry.getValue());
        return result;
    }

    String getMessage(){
        return containsKey(messageId) ? get(messageId).toString() : null;
    }

    private static Severity getSeverity(final Integer32 value){
        return Severity.resolve(value.toInt());
    }

    Severity getSeverity(){
        return containsKey(severityId) ? getSeverity((Integer32)get(severityId)) : Severity.UNKNOWN;
    }

    long getSequenceNumber(){
        return containsKey(sequenceNumberId) ? get(sequenceNumberId).toLong() : -1L;
    }

    Date getTimeStamp(final DateTimeFormatter formatter) throws ParseException {
        return containsKey(timeStampId) ? formatter.convert(((OctetString)get(timeStampId)).toByteArray()) : null;
    }

    String getCategory() {
        return containsKey(categoryId) ? get(categoryId).toString() : null;
    }

    /**
     * Returns a string representation of this notification.
     * @return A string representation of this notification.
     */
    @Override
    public String toString() {
        return notificationID.toString();
    }
}