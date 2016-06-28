package com.bytex.snamp;


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
     * Retrieves the aggregated object.
     * @param objectType Type of the requested object.
     * @param <T> Type of the aggregated object.
     * @return An instance of the aggregated object; or {@literal null} if object is not available.
     */
    <T> T queryObject(final Class<T> objectType);

    static Aggregator empty(){
        return new Aggregator() {
            @Override
            public <T> T queryObject(final Class<T> objectType) {
                return null;
            }
        };
    }

    static Aggregator compose(final Aggregator... values) {
        switch (values.length) {
            case 0:
                return empty();
            case 1:
                return values[0];
            default:
                return new Aggregator() {
                    @Override
                    public <T> T queryObject(final Class<T> objectType) {
                        for (final Aggregator a : values) {
                            final T result = a.queryObject(objectType);
                            if (result != null) return result;
                        }
                        return null;
                    }
                };
        }
    }
}
