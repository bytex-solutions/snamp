import com.itworks.snamp.connectors.NotificationSupport;

notification.setSequenceNumber(mqmessage.getInt8Property('seqnum'));
notification.setTimeStamp(mqmessage.getObjectProperty('timestamp'));
notification.setMessage(mqmessage.readStringOfByteLength(mqmessage.getDataLength()));
notification.addAttachment('attachment', mqmessage.getObjectProperty('attachment'));
notification.setSeverity(mqmessage.getObjectProperty('severity'));

return notification.build();