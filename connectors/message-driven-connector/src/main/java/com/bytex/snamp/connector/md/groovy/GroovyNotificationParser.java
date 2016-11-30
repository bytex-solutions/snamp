package com.bytex.snamp.connector.md.groovy;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.md.NotificationParser;
import com.bytex.snamp.connector.md.NotificationParserChain;
import com.bytex.snamp.connector.md.notifications.MeasurementNotification;
import com.bytex.snamp.connector.md.notifications.SpanNotification;
import com.bytex.snamp.connector.md.notifications.TimeMeasurementNotification;
import com.bytex.snamp.connector.md.notifications.ValueMeasurementNotification;
import com.bytex.snamp.connector.notifications.NotificationBuilder;
import com.bytex.snamp.instrumentation.*;
import com.bytex.snamp.scripting.groovy.Scriptlet;

import javax.management.Notification;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Represents notification parser written in Groovy language.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class GroovyNotificationParser extends Scriptlet implements NotificationParserChain {
    private static final String COMPONENT_NAME = "componentName";
    private static final String INSTANCE_NAME = "componentInstance";

    /**
     * Represents notification supplier.
     */
    @FunctionalInterface
    private interface NotificationFactory extends Supplier<Notification> {
    }

    /**
     * Represents DSL starter.
     * @param <T> Type of object used in DSL pipeline.
     */
    @FunctionalInterface
    protected interface DSLStarter<T>{
        /**
         * Starts DSL pipeline.
         * @return DSL pipeline.
         */
        T start();
    }

    @FunctionalInterface
    protected interface DSLFinalizer<T>{
        T terminate();
    }

    @FunctionalInterface
    protected interface MeasurementFinalizer<M extends Measurement> extends DSLFinalizer<M>{
        @Override
        M terminate();
    }

    private final class NotificationBuilderStarter implements DSLStarter<NotificationBuilder>{
        @Override
        public NotificationBuilder start() {
            final NotificationBuilder builder = new NotificationBuilder();
            builder.setSource(GroovyNotificationParser.this);
            getNotifications().add(builder::get);
            return builder;
        }
    }

    private final class MeasurementFinalizerImpl<M extends Measurement> implements MeasurementFinalizer<M>{
        private final Function<Object, ? extends MeasurementNotification<M>> notificationFactory;

        private MeasurementFinalizerImpl(final Function<Object, ? extends MeasurementNotification<M>> notificationFactory){
            this.notificationFactory = notificationFactory;
        }

        @Override
        public M terminate() {
            final MeasurementNotification<M> notification = notificationFactory.apply(GroovyNotificationParser.this);
            getNotifications().add(() -> notification);
            return notification.getMeasurement();
        }
    }

    protected final class MeasurementPipeline implements DSLStarter<MeasurementPipeline> {
        private MeasurementPipeline(){

        }

        @Override
        public MeasurementPipeline start() {
            return this;
        }

        @SpecialUse
        public <M extends Measurement> M of(final MeasurementFinalizer<M> selector){
            return selector.terminate();
        }
    }

    //DSL keywords
    @SpecialUse
    protected final DSLStarter<NotificationBuilder> notification = new NotificationBuilderStarter();
    @SpecialUse
    protected final MeasurementFinalizer<TimeMeasurement> span = new MeasurementFinalizerImpl<>(SpanNotification::new);
    @SpecialUse
    protected final MeasurementFinalizer<TimeMeasurement> time = new MeasurementFinalizerImpl<>(TimeMeasurementNotification::new);
    @SpecialUse
    protected final MeasurementPipeline measurement = new MeasurementPipeline();
    //measurement types
    @SpecialUse
    protected final MeasurementFinalizer<ValueMeasurement> bool = new MeasurementFinalizerImpl<ValueMeasurement>(ValueMeasurementNotification::ofBool);
    @SpecialUse
    protected final MeasurementFinalizer<ValueMeasurement> integer = new MeasurementFinalizerImpl<ValueMeasurement>(ValueMeasurementNotification::ofInt);
    @SpecialUse
    protected final MeasurementFinalizer<ValueMeasurement> fp = new MeasurementFinalizerImpl<ValueMeasurement>(ValueMeasurementNotification::ofFP);
    @SpecialUse
    protected final MeasurementFinalizer<ValueMeasurement> str = new MeasurementFinalizerImpl<ValueMeasurement>(ValueMeasurementNotification::ofString);

    private final ThreadLocal<Collection<NotificationFactory>> notifications = ThreadLocal.withInitial(LinkedList::new);
    private NotificationParser nextParser;

    /*
        DSL starter. Examples:
        define notification => define(notification)
        define measurement as bool => define(measurement).as(bool)
     */
    @SpecialUse
    protected static <S> S define(final DSLStarter<S> starter){
        return starter.start();
    }

    private void syntaxController(){     //this method is declared just for compile time control of DSL expressions
        define(measurement).of(bool);
        define(notification).setType("test").setSource(this);
    }

    @Override
    public final void setFallbackParser(final NotificationParser value){
        nextParser = value;
    }

    private Collection<NotificationFactory> getNotifications(){
        return notifications.get();
    }

    @SpecialUse
    protected final void addMeasurement(final BooleanMeasurement measurement){
        getNotifications().add(() -> new ValueMeasurementNotification(this, measurement));
    }

    @SpecialUse
    protected final void addMeasurement(final IntegerMeasurement measurement){
        getNotifications().add(() -> new ValueMeasurementNotification(this, measurement));
    }

    @SpecialUse
    protected final void addMeasurement(final StringMeasurement measurement){
        getNotifications().add(() -> new ValueMeasurementNotification(this, measurement));
    }

    @SpecialUse
    protected final void addMeasurement(final FloatingPointMeasurement measurement){
        getNotifications().add(() -> new ValueMeasurementNotification(this, measurement));
    }

    @SpecialUse
    protected final void addMeasurement(final TimeMeasurement measurement){
        getNotifications().add(() -> new TimeMeasurementNotification(this, measurement));
    }

    @SpecialUse
    protected final void value(final Span measurement){
        getNotifications().add(() -> new SpanNotification(this, measurement));
    }

    /**
     * This method should be overridden in Groovy script.
     * @param headers Headers to parse.
     * @param body Body to parse.
     * @return Notification; or measurement; or {@literal null}, if notification should be ignored.
     * @throws Exception Unable to parse notifications.
     */
    @SpecialUse
    protected abstract Object parse(final Object headers, final Object body) throws Exception;

    @SuppressWarnings("unchecked")
    @Override
    public final Stream<Notification> parse(final Map<String, ?> headers, final Object body) throws Exception {
        final Object result;
        final Collection<NotificationFactory> submittedNotifs = getNotifications();
        try {
            result = parse((Object) headers, body);
        } finally {
            this.notifications.remove();
        }
        if (result instanceof NotificationFactory)
            submittedNotifs.add((NotificationFactory) result);
        else if (result instanceof Notification)
            submittedNotifs.add(() -> (Notification) result);
        Stream<Notification> stream = submittedNotifs.stream().map(NotificationFactory::get);
        if (result instanceof Stream<?>)
            stream = Stream.concat(stream, (Stream<Notification>) result);
        return stream;
    }

    /**
     * Delegates parsing to the fallback parser.
     * @param headers Headers to parse.
     * @param body Message body to parse.
     * @return Parsing result.
     * @throws Exception
     */
    @SpecialUse
    protected final Stream<Notification> delegateParsing(final Map<String, ?> headers, final Object body) throws Exception{
        return nextParser == null ? Stream.empty() : nextParser.parse(headers, body);
    }

    public final void setComponentName(final String value){
        setProperty(COMPONENT_NAME, value);
    }

    public final void setInstanceName(final String value){
        setProperty(INSTANCE_NAME, value);
    }
}
