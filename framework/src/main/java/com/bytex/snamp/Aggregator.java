package com.bytex.snamp;


import java.util.Objects;

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
 * @version 1.2
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
        public <T> T queryObject(final Class<T> objectType) {
            return null;
        }

        @Override
        public Aggregator compose(final Aggregator other) {
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
    <T> T queryObject(final Class<T> objectType);

    default Aggregator compose(final Aggregator other) {
        final class AggregatorComposition implements Aggregator {
            private final Aggregator other;

            private AggregatorComposition(final Aggregator other) {
                this.other = Objects.requireNonNull(other);
            }

            @Override
            public <T> T queryObject(final Class<T> objectType) {
                final T obj = Aggregator.this.queryObject(objectType);
                return obj == null ? other.queryObject(objectType) : obj;
            }
        }

        return new AggregatorComposition(other);
    }
}
