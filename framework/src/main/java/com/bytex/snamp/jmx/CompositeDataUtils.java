package com.bytex.snamp.jmx;

import com.google.common.collect.Maps;

import javax.management.openmbean.*;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Provides helper methods that allows to create and
 * manipulate of {@link javax.management.openmbean.CompositeData} instances.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class CompositeDataUtils {
    private static final class CompositeDataInvocationHandler implements InvocationHandler{
        private final static String GET_METHOD_PREFIX = "get";
        private final static String IS_METHOD_PREFIX = "is";
        private final static String SET_METHOD_PREFIX = "set";

        private CompositeData data;

        private CompositeDataInvocationHandler(final CompositeData data){
            this.data = data;
        }

        public static String decapitalize(final String name) {
            if (name == null) return null;
            switch (name.length()) {
                case 0:
                    return name;
                case 1:
                    return new String(new char[]{Character.toLowerCase(name.charAt(0))});
                default:
                    if (Character.isUpperCase(name.charAt(1)) &&
                            Character.isUpperCase(name.charAt(0)))
                        return name;
                    else {
                        char chars[] = name.toCharArray();
                        chars[0] = Character.toLowerCase(chars[0]);
                        return new String(chars);
                    }
            }
        }

        @Override
        public Object invoke(final Object proxy,
                             final Method method,
                             final Object[] args) throws Throwable {
            final String itemName;
            if(method.getName().startsWith(GET_METHOD_PREFIX))
                itemName = decapitalize(method.getName().replaceFirst(GET_METHOD_PREFIX, ""));
            else if(method.getName().startsWith(IS_METHOD_PREFIX))
                itemName = decapitalize(method.getName().replaceFirst(IS_METHOD_PREFIX, ""));
            else if(method.getName().startsWith(SET_METHOD_PREFIX))
                itemName = decapitalize(method.getName().replaceFirst(SET_METHOD_PREFIX, ""));
            else itemName = method.getName();
            if(args == null || args.length == 0) //getter
                return data.get(itemName);
            else {  //setter
                final Map<String, Object> entries = Maps.newHashMapWithExpectedSize(data.getCompositeType().keySet().size());
                fillMap(data, entries);
                entries.put(itemName, args[0]);
                data = new CompositeDataSupport(data.getCompositeType(), entries);
                return null;
            }
        }
    }

    private CompositeDataUtils() {

    }

    /**
     * Converts {@link CompositeData} into map.
     * @param data Composite object to convert. Cannot be {@literal null}.
     * @return Map with items from input {@link CompositeData}.
     */
    public static Map<String, ?> toMap(final CompositeData data) {
        return data.getCompositeType().keySet().stream()
                .collect(Collectors.toMap(Function.identity(), data::get));
    }

    public static CompositeData create(final String typeName,
                                       final String description,
                                       final Map<String, ?> map,
                                       final Function<? super String, ? extends OpenType<?>> typeProvider,
                                       final Function<String, String> descriptionProvider) throws OpenDataException {
        final String[] itemNames = map.keySet().stream().toArray(String[]::new);
        final String[] itemDescriptions = map.keySet().stream().map(descriptionProvider).toArray(String[]::new);
        final OpenType<?>[] itemTypes = new OpenType<?>[itemNames.length];
        for (int i = 0; i < itemTypes.length; i++)
            itemTypes[i] = typeProvider.apply(itemNames[i]);
        return new CompositeDataSupport(new CompositeType(typeName,
                description,
                itemNames,
                itemDescriptions,
                itemTypes),
                map);
    }

    public static CompositeData create(final String typeName,
                                       final String description,
                                       final Map<String, ?> map,
                                       final Map<String, ? extends OpenType<?>> types,
                                       final Function<String, String> descriptionProvider) throws OpenDataException {
        return create(typeName, description, map, types::get, descriptionProvider);
    }

    public static <V> CompositeData create(final String typeName,
                                           final String description,
                                           final Map<String, V> map,
                                           final OpenType<V> type) throws OpenDataException {
        return create(typeName,
                description,
                map,
                v -> type,
                Function.identity());
    }

    public static <V> CompositeData create(final Map<String, V> map,
                                           final OpenType<V> type) throws OpenDataException {
        return create(map.getClass().getName(),
                map.toString(),
                map,
                type);
    }

    public static <T> T getValue(final CompositeData dict,
                            final String itemName,
                            final Class<T> itemType,
                            final Supplier<T> defval) {
        if (dict.containsKey(itemName)) {
            final Object result = dict.get(itemName);
            if (itemType.isInstance(result)) return itemType.cast(result);
        }
        return defval.get();
    }

    public static <T> T getValue(final CompositeData dict,
                            final String itemName,
                            final Class<T> itemType,
                            final T defval){
        return getValue(dict, itemName, itemType, (Supplier<T>)() -> defval);
    }

    public static <T> T getValue(final CompositeData dict,
                            final String itemName,
                            final Class<T> itemType) {
        return getValue(dict, itemName, itemType, (Supplier<T>) () -> null);
    }

    public static boolean getBoolean(final CompositeData dict,
                                     final String itemName,
                                     final boolean defval){
        return getValue(dict, itemName, Boolean.class, defval);
    }

    public static byte getByte(final CompositeData dict,
                                     final String itemName,
                                     final byte defval){
        return getValue(dict, itemName, Byte.class, defval);
    }

    public static char getCharacter(final CompositeData dict,
                                     final String itemName,
                                     final char defval){
        return getValue(dict, itemName, Character.class, defval);
    }

    public static short getShort(final CompositeData dict,
                                     final String itemName,
                                     final short defval){
        return getValue(dict, itemName, Short.class, defval);
    }

    public static int getInteger(final CompositeData dict,
                                     final String itemName,
                                     final int defval){
        return getValue(dict, itemName, Integer.class, defval);
    }

    public static long getLong(final CompositeData dict,
                                     final String itemName,
                                     final long defval){
        return getValue(dict, itemName, Long.class, defval);
    }

    public static float getFloat(final CompositeData dict,
                                     final String itemName,
                                     final float defval){
        return getValue(dict, itemName, Float.class, defval);
    }

    public static double getDouble(final CompositeData dict,
                                     final String itemName,
                                     final double defval){
        return getValue(dict, itemName, Double.class, defval);
    }

    public static BigInteger getBigInteger(final CompositeData dict,
                                     final String itemName,
                                     final BigInteger defval){
        return getValue(dict, itemName, BigInteger.class, defval);
    }

    public static BigDecimal getBigDecimal(final CompositeData dict,
                                     final String itemName,
                                     final BigDecimal defval){
        return getValue(dict, itemName, BigDecimal.class, defval);
    }

    public static String getString(final CompositeData dict,
                                     final String itemName,
                                     final String defval){
        return getValue(dict, itemName, String.class, defval);
    }

    public static Date getDate(final CompositeData dict,
                                     final String itemName,
                                     final Date defval){
        return getValue(dict, itemName, Date.class, defval);
    }

    public static void fillMap(final CompositeData source, final Map<String, Object> dest){
        source.getCompositeType().keySet().forEach(key -> dest.put(key, source.get(key)));
    }

    /**
     * Convert composite data into the Java Bean.
     * @param beanType The type of the Java Bean. Cannot be {@literal null}.
     * @param data The composite data to convert. Cannot be {@literal null}.
     * @param <B> Java Bean class.
     * @return A new instance of the Java Bean.
     * @throws ReflectiveOperationException Unable to convert composite data into the Java Bean.
     */
    public static  <B extends CompositeDataBean> B convert(final Class<B> beanType,
                                     final CompositeData data) throws ReflectiveOperationException {
        //as proxy
        if(beanType.isInterface())
            return beanType.cast(Proxy.newProxyInstance(beanType.getClassLoader(),
                    new Class<?>[]{beanType}, new CompositeDataInvocationHandler(data)));
        //convert to Java Bean class
        else {
            final B result = beanType.newInstance();
            final BeanInfo metadata;
            try {
                metadata = Introspector.getBeanInfo(beanType, CompositeDataBean.class);
            } catch (IntrospectionException e) {
                throw new ReflectiveOperationException(e);
            }
            for (final PropertyDescriptor descriptor : metadata.getPropertyDescriptors())
                if (data.containsKey(descriptor.getName()))
                    descriptor.getWriteMethod().invoke(result, data.get(descriptor.getName()));
            return result;
        }
    }

    /**
     * Converts Java Bean into the composite data.
     * @param beanInstance The Java Bean instance to be converted.
     * @param type The type of the composite data. May be {@literal null} if the bean instance
     *             was previously created as a proxy.
     * @return The composite data.
     * @throws IllegalArgumentException The bean is a proxy but was not created with {@link #convert(Class, javax.management.openmbean.CompositeData)} method.
     * @throws NullPointerException Bean instance is not a proxy and type is {@literal null}.
     * @throws ReflectiveOperationException Unable to convert bean to composite data.
     * @throws OpenDataException Unable to convert bean to composite data.
     */
    public static CompositeData convert(final CompositeDataBean beanInstance,
                                        final CompositeType type) throws IllegalArgumentException, NullPointerException, ReflectiveOperationException, OpenDataException {
        if(Proxy.isProxyClass(beanInstance.getClass())){
            final InvocationHandler handler = Proxy.getInvocationHandler(beanInstance);
            if(handler instanceof CompositeDataInvocationHandler)
                return ((CompositeDataInvocationHandler)handler).data;
            throw new IllegalArgumentException("beanInstance should be proxy.");
        }
        else if(type == null) throw new NullPointerException("type is null.");
        else {
            final BeanInfo metadata;
            try{
                metadata = Introspector.getBeanInfo(beanInstance.getClass(), Object.class);
            }
            catch (final IntrospectionException e){
                throw new ReflectiveOperationException(e);
            }
            final PropertyDescriptor[] properties = metadata.getPropertyDescriptors();
            final Map<String, Object> entries = Maps.newHashMapWithExpectedSize(properties.length);
            for(final PropertyDescriptor descriptor: properties) {
                if (type.containsKey(descriptor.getDisplayName()))
                    entries.put(descriptor.getName(), descriptor.getReadMethod().invoke(beanInstance));
            }
            return new CompositeDataSupport(type, entries);
        }
    }
}