package com.bytex.jcommands.impl;

import java.util.function.Function;

/**
* @author Roman Sakno
* @version 1.2
* @since 1.0
*/
@FunctionalInterface
interface Converter<T> extends Function<String, T> {

}
