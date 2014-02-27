import com.snamp.connectors.NotificationSupport;

notification.setMessage(mqmessage.toString());
notification.addAttachment('prop', 'custom value');
notification.setSeverity(NotificationSupport.Notification.Severity.DEBUG);
notification.setSequenceNumber(10);

return notification.build();