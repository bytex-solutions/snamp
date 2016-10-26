package com.bytex.snamp.concurrent;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

/**
 * Represents completable {@link Future} in the form of interface.
 * @since 2.0
 * @version 2.0
 */
public interface ComputationPipeline<V> extends Future<V>, CompletionStage<V> {
}
