package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AbstractOpenAttributeInfo;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.connector.md.notifications.MeasurementNotification;
import com.bytex.snamp.instrumentation.measurements.Measurement;

import javax.management.MBeanException;
import javax.management.Notification;
import javax.management.openmbean.OpenType;
import java.util.Optional;

/**
 * Represents attribute which value depends on the measurement notification received from external component.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 * @see DistributedAttribute
 * @see ProcessingAttribute
 */
public abstract class MessageDrivenAttribute extends AbstractOpenAttributeInfo {
    /**
     * Represents notification processing result.
     */
    public interface NotificationProcessingResult {

        /**
         * Determines whether the notification is processed successfully.
         * @return {@literal true}, if notification was processed; otherwise {@literal false}.
         */
        boolean isProcessed();

        /**
         * Gets error produced during notification processing.
         * @return Optional processing error.
         */
        Optional<Throwable> getProcessingError();

        /**
         * Gets new attribute value produced after processing.
         * @return Optional attribute value.
         */
        Optional<Object> getAttributeValue();
    }

    private static final long serialVersionUID = -2361230399455752656L;
    private static NotificationProcessingResult IGNORED = new NotificationProcessingResult() {
        @Override
        public boolean isProcessed() {
            return false;
        }

        @Override
        public Optional<Throwable> getProcessingError() {
            return Optional.empty();
        }

        @Override
        public Optional<Object> getAttributeValue() {
            return Optional.empty();
        }

        @Override
        public String toString() {
            return "IGNORED";
        }
    };

    MessageDrivenAttribute(final String name,
                           final OpenType<?> type,
                           final String description,
                           final AttributeSpecifier specifier,
                           final AttributeDescriptor descriptor) {
        super(name, type, description, specifier, descriptor);
    }

    /**
     * Indicating that processing was failed.
     * @param e Processing error.
     * @return Notification processing result.
     */
    protected static NotificationProcessingResult processingFailed(final Throwable e){
        return new NotificationProcessingResult() {

            @Override
            public boolean isProcessed() {
                return true;
            }

            @Override
            public Optional<Throwable> getProcessingError() {
                return Optional.of(e);
            }

            @Override
            public Optional<Object> getAttributeValue() {
                return Optional.empty();
            }

            @Override
            public String toString() {
                return e.getMessage();
            }
        };
    }

    /**
     * Indicates whether the notification was ignored.
     * @return Notification processing result.
     */
    protected static NotificationProcessingResult notificationIgnored(){
        return IGNORED;
    }

    /**
     * Indicates whether the modification was processed successfully.
     * @param attributeValue A new attribute value produced as a result of notification processing.
     * @return Notification processing result.
     */
    protected static NotificationProcessingResult notificationProcessed(final Object attributeValue) {
        return new NotificationProcessingResult() {
            @Override
            public boolean isProcessed() {
                return true;
            }

            @Override
            public Optional<Throwable> getProcessingError() {
                return Optional.empty();
            }

            @Override
            public Optional<Object> getAttributeValue() {
                return Optional.ofNullable(attributeValue);
            }

            @Override
            public String toString() {
                return "Success " + attributeValue;
            }
        };
    }

    static MBeanException cannotBeModified(final MessageDrivenAttribute attribute){
        return new MBeanException(new UnsupportedOperationException(String.format("Attribute '%s' cannot be modified", attribute)));
    }

    protected final boolean representsMeasurement(final Measurement measurement){
        final String measurementName = getDescriptor().getName(getName());
        return measurement.getName().equals(measurementName);
    }

    protected final boolean representsMeasurement(final MeasurementNotification<?> measurement){
        return representsMeasurement(measurement.getMeasurement());
    }

    protected abstract NotificationProcessingResult handleNotification(final Notification notification) throws Exception;

    /**
     * Handles notification and return new attribute value.
     * @param notification The notification to handle.
     * @return A new attribute value.
     */
    final NotificationProcessingResult dispatch(final Notification notification) {
        try {
            return handleNotification(notification);
        } catch (final Exception e) {
            return processingFailed(e);
        }
    }
}
