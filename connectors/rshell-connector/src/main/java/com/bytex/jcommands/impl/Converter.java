package com.bytex.jcommands.impl;

import java.util.function.Function;

/**
* @author Roman Sakno
* @version 2.1
* @since 1.0
*/
@FunctionalInterface
interface Converter<T> extends Function<String, T> {

}
