package com.itworks.snamp.security.auth.login;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.gson.Gson;

import javax.security.auth.login.AppConfigurationEntry;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class JsonConfiguration extends AdvancedConfiguration {
    private static final Charset CONFIG_ENCODING = Charset.forName("UTF-8");
    public static final String TYPE = "JsonLoginConfig";

    private final Multimap<String, AppConfigurationEntry> entries;

    public JsonConfiguration(){
        this.entries = LinkedListMultimap.create(5);
    }

    public JsonConfiguration(final Multimap<String, AppConfigurationEntry> entries){
        this.entries = LinkedListMultimap.create(entries);
    }

    public JsonConfiguration(final int expectedSize){
        this(LinkedListMultimap.<String, AppConfigurationEntry>create(expectedSize));
    }

    /**
     * Return the type of this Configuration.
     * <p/>
     * <p> This Configuration instance will only have a type if it
     * was obtained via a call to <code>Configuration.getInstance</code>.
     * Otherwise this method returns null.
     *
     * @return the type of this Configuration, or null.
     * @since 1.6
     */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Returns the number of key-value pairs in this multimap.
     * <p/>
     * <p><b>Note:</b> this method does not return the number of <i>distinct
     * keys</i> in the multimap, which is given by {@code keySet().size()} or
     * {@code asMap().size()}. See the opening section of the {@link com.google.common.collect.Multimap}
     * class documentation for clarification.
     */
    @Override
    public int size() {
        return entries.size();
    }

    /**
     * Returns {@code true} if this multimap contains no key-value pairs.
     * Equivalent to {@code size() == 0}, but can in some cases be more efficient.
     */
    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    /**
     * Returns {@code true} if this multimap contains at least one key-value pair
     * with the key {@code key}.
     *
     * @param key
     */
    @Override
    public boolean containsKey(final Object key) {
        return entries.containsKey(key);
    }

    /**
     * Returns {@code true} if this multimap contains at least one key-value pair
     * with the value {@code value}.
     *
     * @param value
     */
    @Override
    public boolean containsValue(final Object value) {
        return entries.containsValue(value);
    }

    /**
     * Returns {@code true} if this multimap contains at least one key-value pair
     * with the key {@code key} and the value {@code value}.
     *
     * @param key
     * @param value
     */
    @Override
    public boolean containsEntry(final Object key, final Object value) {
        return entries.containsEntry(key, value);
    }

    /**
     * Stores a key-value pair in this multimap.
     * <p/>
     * <p>Some multimap implementations allow duplicate key-value pairs, in which
     * case {@code put} always adds a new key-value pair and increases the
     * multimap size by 1. Other implementations prohibit duplicates, and storing
     * a key-value pair that's already in the multimap has no effect.
     *
     * @param key
     * @param value
     * @return {@code true} if the method increased the size of the multimap, or
     * {@code false} if the multimap already contained the key-value pair and
     * doesn't allow duplicates
     */
    @Override
    public boolean put(final String key, final AppConfigurationEntry value) {
        return entries.put(key, value);
    }

    /**
     * Removes a single key-value pair with the key {@code key} and the value
     * {@code value} from this multimap, if such exists. If multiple key-value
     * pairs in the multimap fit this description, which one is removed is
     * unspecified.
     *
     * @param key
     * @param value
     * @return {@code true} if the multimap changed
     */
    @Override
    public boolean remove(final Object key, final Object value) {
        return entries.remove(key, value);
    }

    /**
     * Stores a key-value pair in this multimap for each of {@code values}, all
     * using the same key, {@code key}. Equivalent to (but expected to be more
     * efficient than): <pre>   {@code
     * <p/>
     *   for (V value : values) {
     *     put(key, value);
     *   }}</pre>
     * <p/>
     * <p>In particular, this is a no-op if {@code values} is empty.
     *
     * @param key
     * @param values
     * @return {@code true} if the multimap changed
     */
    @Override
    public boolean putAll(final String key, final Iterable<? extends AppConfigurationEntry> values) {
        return entries.putAll(key, values);
    }

    /**
     * Stores all key-value pairs of {@code multimap} in this multimap, in the
     * order returned by {@code multimap.entries()}.
     *
     * @param multimap
     * @return {@code true} if the multimap changed
     */
    @Override
    public boolean putAll(final Multimap<? extends String, ? extends AppConfigurationEntry> multimap) {
        return entries.putAll(multimap);
    }

    /**
     * Stores a collection of values with the same key, replacing any existing
     * values for that key.
     * <p/>
     * <p>If {@code values} is empty, this is equivalent to
     * {@link #removeAll(Object) removeAll(key)}.
     *
     * @param key
     * @param values
     * @return the collection of replaced values, or an empty collection if no
     * values were previously associated with the key. The collection
     * <i>may</i> be modifiable, but updating it will have no effect on the
     * multimap.
     */
    @Override
    public Collection<AppConfigurationEntry> replaceValues(final String key, final Iterable<? extends AppConfigurationEntry> values) {
        return entries.replaceValues(key, values);
    }

    /**
     * Removes all values associated with the key {@code key}.
     * <p/>
     * <p>Once this method returns, {@code key} will not be mapped to any values,
     * so it will not appear in {@link #keySet()}, {@link #asMap()}, or any other
     * views.
     *
     * @param key
     * @return the values that were removed (possibly empty). The returned
     * collection <i>may</i> be modifiable, but updating it will have no
     * effect on the multimap.
     */
    @Override
    public Collection<AppConfigurationEntry> removeAll(final Object key) {
        return entries.removeAll(key);
    }

    /**
     * Removes all key-value pairs from the multimap, leaving it {@linkplain
     * #isEmpty empty}.
     */
    @Override
    public void clear() {
        entries.clear();
    }

    /**
     * Returns a view collection of the values associated with {@code key} in this
     * multimap, if any. Note that when {@code containsKey(key)} is false, this
     * returns an empty collection, not {@code null}.
     * <p/>
     * <p>Changes to the returned collection will update the underlying multimap,
     * and vice versa.
     *
     * @param key
     */
    @Override
    public Collection<AppConfigurationEntry> get(final String key) {
        return entries.get(key);
    }

    /**
     * Returns a view collection of all <i>distinct</i> keys contained in this
     * multimap. Note that the key set contains a key if and only if this multimap
     * maps that key to at least one value.
     * <p/>
     * <p>Changes to the returned set will update the underlying multimap, and
     * vice versa. However, <i>adding</i> to the returned set is not possible.
     */
    @Override
    public Set<String> keySet() {
        return entries.keySet();
    }

    /**
     * Returns a view collection containing the key from each key-value pair in
     * this multimap, <i>without</i> collapsing duplicates. This collection has
     * the same size as this multimap, and {@code keys().count(k) ==
     * get(k).size()} for all {@code k}.
     * <p/>
     * <p>Changes to the returned multiset will update the underlying multimap,
     * and vice versa. However, <i>adding</i> to the returned collection is not
     * possible.
     */
    @Override
    public Multiset<String> keys() {
        return entries.keys();
    }

    /**
     * Returns a view collection containing the <i>value</i> from each key-value
     * pair contained in this multimap, without collapsing duplicates (so {@code
     * values().size() == size()}).
     * <p/>
     * <p>Changes to the returned collection will update the underlying multimap,
     * and vice versa. However, <i>adding</i> to the returned collection is not
     * possible.
     */
    @Override
    public Collection<AppConfigurationEntry> values() {
        return entries.values();
    }

    /**
     * Returns a view collection of all key-value pairs contained in this
     * multimap, as {@link java.util.Map.Entry} instances.
     * <p/>
     * <p>Changes to the returned collection or the entries it contains will
     * update the underlying multimap, and vice versa. However, <i>adding</i> to
     * the returned collection is not possible.
     */
    @Override
    public Collection<Map.Entry<String, AppConfigurationEntry>> entries() {
        return entries.entries();
    }

    /**
     * Returns a view of this multimap as a {@code Map} from each distinct key
     * to the nonempty collection of that key's associated values. Note that
     * {@code this.asMap().get(k)} is equivalent to {@code this.get(k)} only when
     * {@code k} is a key contained in the multimap; otherwise it returns {@code
     * null} as opposed to an empty collection.
     * <p/>
     * <p>Changes to the returned map or the collections that serve as its values
     * will update the underlying multimap, and vice versa. The map does not
     * support {@code put} or {@code putAll}, nor do its entries support {@link
     * java.util.Map.Entry#setValue setValue}.
     */
    @Override
    public Map<String, Collection<AppConfigurationEntry>> asMap() {
        return entries.asMap();
    }

    @Override
    public String toString() {
        return entries.toString();
    }

    public static JsonConfiguration deserialize(final Gson formatter, final InputStream stream) throws IOException {
        try(final Reader reader = new InputStreamReader(stream, CONFIG_ENCODING)){
            return formatter.fromJson(reader, JsonConfiguration.class);
        }
    }

    public static JsonConfiguration deserialize(final Gson formatter, final URL location) throws IOException{
        try(final InputStream stream = location.openStream()){
            return deserialize(formatter, stream);
        }
    }

    public static JsonConfiguration deserialize(final Gson formatter, final File configFile) throws IOException{
        return deserialize(formatter, configFile.toURI().toURL());
    }

    public void serialize(final Gson formatter, final OutputStream out) throws IOException {
        try(final Writer writer = new OutputStreamWriter(out, CONFIG_ENCODING)){
            writer.write(formatter.toJson(this, JsonConfiguration.class));
        }
    }

    public void serialize(final Gson formatter, final File configFile) throws IOException{
        try(final OutputStream out = new FileOutputStream(configFile)){
            serialize(formatter, out);
        }
    }
}
