package com.bytex.snamp;


import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Represents an object that aggregates another objects.<br/>
 * <p>
 *     <b>Example:</b><br/>
 *     <pre>{@code
 *         final class SimpleAggregator implements Aggregator{
 *           private final File someFile;
 *
 *           public SimpleAggregator(final String path){
 *             someFile = new File(path);
 *           }
 *
 *           public <T> T queryObject(final Class<T> ot){
 *             if(File.class.equals(ot)) return ot.cast(someFile);
 *             else return null;
 *           }
 *         }
 *     }</pre>
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 * @see AbstractAggregator
 */
public interface Aggregator {
    /**
     * This object doesn't aggregate any other object.
     * @since 1.2
     */
    Aggregator EMPTY = new Aggregator() {
        @Override
        public <T> Optional<T> queryObject(@Nonnull final Class<T> objectType) {
            return Optional.empty();
        }

        @Override
        @Nonnull
        public Aggregator compose(@Nonnull final Aggregator other) {
            return other;
        }
    };

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the requested object.
     * @param <T>        Type of the aggregated object.
     * @return An instance of the aggregated object.
     */
    <T> Optional<T> queryObject(@Nonnull final Class<T> objectType);

    @Nonnull
    default Aggregator compose(@Nonnull final Aggregator other) {
        final class AggregatorComposition implements Aggregator {
            @Override
            public <T> Optional<T> queryObject(@Nonnull final Class<T> objectType) {
                final Optional<T> obj = Aggregator.this.queryObject(objectType);
                return obj.isPresent() ? obj : other.queryObject(objectType);
            }
        }

        return new AggregatorComposition();
    }

    static <I, E extends Throwable> boolean queryAndAccept(final Aggregator a, final Class<I> type, final Acceptor<? super I, E> processing) throws E {
        final Optional<I> obj = a.queryObject(type);
        if (obj.isPresent())
            processing.accept(obj.get());
        return obj.isPresent();
    }
}
