package com.bytex.snamp;


import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

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
        public <T> T queryObject(@Nonnull final Class<T> objectType) {
            return null;
        }

        @Override
        public Aggregator compose(@Nonnull final Aggregator other) {
            return other;
        }
    };

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the requested object.
     * @param <T>        Type of the aggregated object.
     * @return An instance of the aggregated object; or {@literal null} if object is not available.
     */
    <T> T queryObject(@Nonnull final Class<T> objectType);

    default Aggregator compose(@Nonnull final Aggregator other) {
        final class AggregatorComposition implements Aggregator {
            @Override
            public <T> T queryObject(@Nonnull final Class<T> objectType) {
                final T obj = Aggregator.this.queryObject(objectType);
                return obj == null ? other.queryObject(objectType) : obj;
            }
        }

        return new AggregatorComposition();
    }

    static <I, O> Optional<O> queryAndApply(final Aggregator a, final Class<I> type, final Function<? super I, ? extends O> processing) {
        final I obj = a.queryObject(type);
        return obj == null ? Optional.empty() : Optional.of(processing.apply(obj));
    }

    static <I, O> O queryAndApply(final Aggregator a, final Class<I> type, final Function<? super I, ? extends O> processing, final Supplier<? extends O> fallback) {
        return queryAndApply(a, type, processing).orElseGet(fallback);
    }

    static <I, E extends Throwable> boolean queryAndAccept(final Aggregator a, final Class<I> type, final Acceptor<? super I, E> processing) throws E {
        final I obj = a.queryObject(type);
        if (obj == null)
            return false;
        else {
            processing.accept(obj);
            return true;
        }
    }
}
