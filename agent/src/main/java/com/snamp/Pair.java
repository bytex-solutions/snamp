package com.snamp;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;

import static java.util.Map.Entry;

/**
 * Represents a pair of some objects. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class Pair<T1, T2> implements Serializable, Cloneable {


    /**
     * Represents the first element in the pair
     */
    public final T1 first;

    /**
     * Represents the second element in the pair.
     */
    public final T2 second;

    /**
     * Initializes a new pair of objects from key/value pair.
     * @param pair Key/value pair. Cannot be {@literal null}.
     * @throws IllegalArgumentException pair is {@literal null}.
     */
    public Pair(final Entry<? extends T1, ? extends T2> pair){
        if(pair == null) throw new IllegalArgumentException("pair is null.");
        first = pair.getKey();
        second = pair.getValue();
    }

    /**
     * Initializes a new pair of objects from another pair.
     * @param pair A pair to copy. Cannot be {@literal null}.
     * @throws IllegalArgumentException pair is {@literal null}.
     */
    public Pair(final Pair<? extends T1, ? extends T2> pair){
        if(pair == null) throw new IllegalArgumentException("pair is null.");
        first = pair.first;
        second = pair.second;
    }

    /**
     * Initializes a new pair of objects.
     * @param f The first object in the pair.
     * @param s The second object in the pair.
     */
    public Pair(final T1 f, final T2 s){
        this.first = f;
        this.second = s;
    }

    /**
     * Creates a new pair with modified first element.
     * @param first
     * @param <T3>
     * @return
     */
    public final <T3> Pair<T3, T2> setFirst(final T3 first){
        return new Pair<T3, T2>(first, this.second);
    }

    /**
     * Converts this pair into typed array.
     * @param pair A pair to convert.
     * @param componentType Type of the elements in this array.
     * @param <T> Type of the elements in this array.
     * @param <T1> Type of the first element in the pair.
     * @param <T2> Type of the second element in the pair.
     * @return A new array that contains first and second elements from the pair.
     */
    public static  <T, T1 extends T, T2 extends T> T[] asArray(final Pair<T1, T2> pair, final Class<T> componentType){
        if(pair == null) return null;
        final Object result = Array.newInstance(componentType, 2);
        Array.set(result, 0, pair.first);
        Array.set(result, 1, pair.second);
        return (T[])result;
    }

    /**
     * Converts the specified pair into list.
     * @param pair The pair to convert.
     * @param <T> Type of the list element.
     * @param <T1> Type of the first element in the pair.
     * @param <T2> Type of the second element in the pair.
     * @return A new {@link ArrayList} with two elements from the pair.
     */
    public static <T, T1 extends T, T2 extends T> List<T> asList(final Pair<T1, T2> pair){
        if(pair == null) return null;
        final List<T> result = new ArrayList<>(2);
        result.add(pair.first);
        result.add(pair.second);
        return result;
    }

    /**
     * Returns a new copy of this pair.
     * @return A new copy of this pair.
     */
    @Override
    public final Pair<T1, T2> clone() {
        return new Pair<>(first, second);
    }

    /**
     * Creates a new key/value pair based on this class.
     * <p>
     *     {@link #first} will be interpreted as key. {@link #second} will be interpreted as value.
     * </p>
     * @return A new instance of the key/value pair.
     */
    public final Entry<T1, T2> asKeyValuePair(){
        return new Entry<T1, T2>() {
            private T2 value = second;
            @Override
            public T1 getKey() {
                return first;
            }

            @Override
            public T2 getValue() {
                return value;
            }

            @Override
            public T2 setValue(final T2 value) {
                final T2 previous = value;
                this.value = value;
                return previous;
            }
        };
    }

    /**
     * Puts key/value pairs into the specified map.
     * @param target A map to fill. Cannot be {@literal null}.
     * @param pairs A key/value pairs to put into map.
     * @param <K> Type of the map keys.
     * @param <V> Type of the map values.
     * @throws IllegalArgumentException target is {@literal null}.
     */
    public static <K, V> void fillMap(final Map<K, V> target, final Pair<? extends K, ? extends V>... pairs){
        if(target == null) throw new IllegalArgumentException("target is null.");
        for(final Pair<? extends K, ? extends V> kv: pairs)
            target.put(kv.first, kv.second);
    }

    /**
     * Creates a new map for the set of key/value pairs.
     * @param pairs A set of key/value pairs to be added into map.
     * @param <K> Type of the map keys.
     * @param <V> Type of the map values.
     * @return A new instance of the map containing the specified pairs.
     */
    public static <K, V> Map<K, V> toMap(final Pair<? extends K, ? extends V>... pairs){
        final Map<K, V> result = new HashMap<>(pairs.length);
        fillMap(result, pairs);
        return result;
    }

    /**
     * Creates a new pair of objects.
     * <p>
     *     This method just create a new pair using {@link #Pair(Object, Object)} constructor.
     * </p>
     * @param first The first element in the pair.
     * @param second The second element in the pair.
     * @param <T1> Type of the first element in the pair.
     * @param <T2> Type of the second element in the pair.
     * @return A new pair of objects.
     */
    public static <T1, T2> Pair<T1, T2> pair(final T1 first, final T2 second){
        return new Pair(first, second);
    }

    /**
     * Returns a string representation of this pair.
     * @return The string representation of this pair.
     */
    @Override
    public final String toString() {
        return String.format("[%s, %s]", first, second);
    }
}
