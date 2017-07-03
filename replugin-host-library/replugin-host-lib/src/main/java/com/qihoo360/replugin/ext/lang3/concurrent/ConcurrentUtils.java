/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qihoo360.replugin.ext.lang3.concurrent;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.qihoo360.replugin.ext.lang3.Validate;

/**
 * <p>
 * An utility class providing functionality related to the {@code
 * java.util.concurrent} package.
 * </p>
 *
 * @since 3.0
 */
public class ConcurrentUtils {

    /**
     * Private constructor so that no instances can be created. This class
     * contains only static utility methods.
     */
    private ConcurrentUtils() {
    }

    /**
     * Inspects the cause of the specified {@code ExecutionException} and
     * creates a {@code ConcurrentException} with the checked cause if
     * necessary. This method performs the following checks on the cause of the
     * passed in exception:
     * <ul>
     * <li>If the passed in exception is <b>null</b> or the cause is
     * <b>null</b>, this method returns <b>null</b>.</li>
     * <li>If the cause is a runtime exception, it is directly thrown.</li>
     * <li>If the cause is an error, it is directly thrown, too.</li>
     * <li>In any other case the cause is a checked exception. The method then
     * creates a {@link ConcurrentException}, initializes it with the cause, and
     * returns it.</li>
     * </ul>
     *
     * @param ex the exception to be processed
     * @return a {@code ConcurrentException} with the checked cause
     */
    public static ConcurrentException extractCause(final ExecutionException ex) {
        if (ex == null || ex.getCause() == null) {
            return null;
        }

        throwCause(ex);
        return new ConcurrentException(ex.getMessage(), ex.getCause());
    }

    /**
     * Inspects the cause of the specified {@code ExecutionException} and
     * creates a {@code ConcurrentRuntimeException} with the checked cause if
     * necessary. This method works exactly like
     * {@link #extractCause(ExecutionException)}. The only difference is that
     * the cause of the specified {@code ExecutionException} is extracted as a
     * runtime exception. This is an alternative for client code that does not
     * want to deal with checked exceptions.
     *
     * @param ex the exception to be processed
     * @return a {@code ConcurrentRuntimeException} with the checked cause
     */
    public static ConcurrentRuntimeException extractCauseUnchecked(
            final ExecutionException ex) {
        if (ex == null || ex.getCause() == null) {
            return null;
        }

        throwCause(ex);
        return new ConcurrentRuntimeException(ex.getMessage(), ex.getCause());
    }

    /**
     * Handles the specified {@code ExecutionException}. This method calls
     * {@link #extractCause(ExecutionException)} for obtaining the cause of the
     * exception - which might already cause an unchecked exception or an error
     * being thrown. If the cause is a checked exception however, it is wrapped
     * in a {@code ConcurrentException}, which is thrown. If the passed in
     * exception is <b>null</b> or has no cause, the method simply returns
     * without throwing an exception.
     *
     * @param ex the exception to be handled
     * @throws ConcurrentException if the cause of the {@code
     * ExecutionException} is a checked exception
     */
    public static void handleCause(final ExecutionException ex)
            throws ConcurrentException {
        final ConcurrentException cex = extractCause(ex);

        if (cex != null) {
            throw cex;
        }
    }

    /**
     * Handles the specified {@code ExecutionException} and transforms it into a
     * runtime exception. This method works exactly like
     * {@link #handleCause(ExecutionException)}, but instead of a
     * {@link ConcurrentException} it throws a
     * {@link ConcurrentRuntimeException}. This is an alternative for client
     * code that does not want to deal with checked exceptions.
     *
     * @param ex the exception to be handled
     * @throws ConcurrentRuntimeException if the cause of the {@code
     * ExecutionException} is a checked exception; this exception is then
     * wrapped in the thrown runtime exception
     */
    public static void handleCauseUnchecked(final ExecutionException ex) {
        final ConcurrentRuntimeException crex = extractCauseUnchecked(ex);

        if (crex != null) {
            throw crex;
        }
    }

    /**
     * Tests whether the specified {@code Throwable} is a checked exception. If
     * not, an exception is thrown.
     *
     * @param ex the {@code Throwable} to check
     * @return a flag whether the passed in exception is a checked exception
     * @throws IllegalArgumentException if the {@code Throwable} is not a
     * checked exception
     */
    static Throwable checkedException(final Throwable ex) {
        Validate.isTrue(ex != null && !(ex instanceof RuntimeException)
                && !(ex instanceof Error), "Not a checked exception: " + ex);

        return ex;
    }

    /**
     * Tests whether the cause of the specified {@code ExecutionException}
     * should be thrown and does it if necessary.
     *
     * @param ex the exception in question
     */
    private static void throwCause(final ExecutionException ex) {
        if (ex.getCause() instanceof RuntimeException) {
            throw (RuntimeException) ex.getCause();
        }

        if (ex.getCause() instanceof Error) {
            throw (Error) ex.getCause();
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Invokes the specified {@code ConcurrentInitializer} and returns the
     * object produced by the initializer. This method just invokes the {@code
     * get()} method of the given {@code ConcurrentInitializer}. It is
     * <b>null</b>-safe: if the argument is <b>null</b>, result is also
     * <b>null</b>.
     *
     * @param <T> the type of the object produced by the initializer
     * @param initializer the {@code ConcurrentInitializer} to be invoked
     * @return the object managed by the {@code ConcurrentInitializer}
     * @throws ConcurrentException if the {@code ConcurrentInitializer} throws
     * an exception
     */
    public static <T> T initialize(final ConcurrentInitializer<T> initializer)
            throws ConcurrentException {
        return initializer != null ? initializer.get() : null;
    }

    /**
     * Invokes the specified {@code ConcurrentInitializer} and transforms
     * occurring exceptions to runtime exceptions. This method works like
     * {@link #initialize(ConcurrentInitializer)}, but if the {@code
     * ConcurrentInitializer} throws a {@link ConcurrentException}, it is
     * caught, and the cause is wrapped in a {@link ConcurrentRuntimeException}.
     * So client code does not have to deal with checked exceptions.
     *
     * @param <T> the type of the object produced by the initializer
     * @param initializer the {@code ConcurrentInitializer} to be invoked
     * @return the object managed by the {@code ConcurrentInitializer}
     * @throws ConcurrentRuntimeException if the initializer throws an exception
     */
    public static <T> T initializeUnchecked(final ConcurrentInitializer<T> initializer) {
        try {
            return initialize(initializer);
        } catch (final ConcurrentException cex) {
            throw new ConcurrentRuntimeException(cex.getCause());
        }
    }

    //-----------------------------------------------------------------------
    /**
     * <p>
     * Puts a value in the specified {@code ConcurrentMap} if the key is not yet
     * present. This method works similar to the {@code putIfAbsent()} method of
     * the {@code ConcurrentMap} interface, but the value returned is different.
     * Basically, this method is equivalent to the following code fragment:
     * </p>
     *
     * <pre>
     * if (!map.containsKey(key)) {
     *     map.put(key, value);
     *     return value;
     * } else {
     *     return map.get(key);
     * }
     * </pre>
     *
     * <p>
     * except that the action is performed atomically. So this method always
     * returns the value which is stored in the map.
     * </p>
     * <p>
     * This method is <b>null</b>-safe: It accepts a <b>null</b> map as input
     * without throwing an exception. In this case the return value is
     * <b>null</b>, too.
     * </p>
     *
     * @param <K> the type of the keys of the map
     * @param <V> the type of the values of the map
     * @param map the map to be modified
     * @param key the key of the value to be added
     * @param value the value to be added
     * @return the value stored in the map after this operation
     */
    public static <K, V> V putIfAbsent(final ConcurrentMap<K, V> map, final K key, final V value) {
        if (map == null) {
            return null;
        }

        final V result = map.putIfAbsent(key, value);
        return result != null ? result : value;
    }

    /**
     * Checks if a concurrent map contains a key and creates a corresponding
     * value if not. This method first checks the presence of the key in the
     * given map. If it is already contained, its value is returned. Otherwise
     * the {@code get()} method of the passed in {@link ConcurrentInitializer}
     * is called. With the resulting object
     * {@link #putIfAbsent(ConcurrentMap, Object, Object)} is called. This
     * handles the case that in the meantime another thread has added the key to
     * the map. Both the map and the initializer can be <b>null</b>; in this
     * case this method simply returns <b>null</b>.
     *
     * @param <K> the type of the keys of the map
     * @param <V> the type of the values of the map
     * @param map the map to be modified
     * @param key the key of the value to be added
     * @param init the {@link ConcurrentInitializer} for creating the value
     * @return the value stored in the map after this operation; this may or may
     * not be the object created by the {@link ConcurrentInitializer}
     * @throws ConcurrentException if the initializer throws an exception
     */
    public static <K, V> V createIfAbsent(final ConcurrentMap<K, V> map, final K key,
            final ConcurrentInitializer<V> init) throws ConcurrentException {
        if (map == null || init == null) {
            return null;
        }

        final V value = map.get(key);
        if (value == null) {
            return putIfAbsent(map, key, init.get());
        }
        return value;
    }

    /**
     * Checks if a concurrent map contains a key and creates a corresponding
     * value if not, suppressing checked exceptions. This method calls
     * {@code createIfAbsent()}. If a {@link ConcurrentException} is thrown, it
     * is caught and re-thrown as a {@link ConcurrentRuntimeException}.
     *
     * @param <K> the type of the keys of the map
     * @param <V> the type of the values of the map
     * @param map the map to be modified
     * @param key the key of the value to be added
     * @param init the {@link ConcurrentInitializer} for creating the value
     * @return the value stored in the map after this operation; this may or may
     * not be the object created by the {@link ConcurrentInitializer}
     * @throws ConcurrentRuntimeException if the initializer throws an exception
     */
    public static <K, V> V createIfAbsentUnchecked(final ConcurrentMap<K, V> map,
            final K key, final ConcurrentInitializer<V> init) {
        try {
            return createIfAbsent(map, key, init);
        } catch (final ConcurrentException cex) {
            throw new ConcurrentRuntimeException(cex.getCause());
        }
    }

    //-----------------------------------------------------------------------
    /**
     * <p>
     * Gets an implementation of <code>Future</code> that is immediately done
     * and returns the specified constant value.
     * </p>
     * <p>
     * This can be useful to return a simple constant immediately from the
     * concurrent processing, perhaps as part of avoiding nulls.
     * A constant future can also be useful in testing.
     * </p>
     *
     * @param <T> the type of the value used by this {@code Future} object
     * @param value  the constant value to return, may be null
     * @return an instance of Future that will return the value, never null
     */
    public static <T> Future<T> constantFuture(final T value) {
        return new ConstantFuture<>(value);
    }

    /**
     * A specialized {@code Future} implementation which wraps a constant value.
     * @param <T> the type of the value wrapped by this class
     */
    static final class ConstantFuture<T> implements Future<T> {
        /** The constant value. */
        private final T value;

        /**
         * Creates a new instance of {@code ConstantFuture} and initializes it
         * with the constant value.
         *
         * @param value the value (may be <b>null</b>)
         */
        ConstantFuture(final T value) {
            this.value = value;
        }

        /**
         * {@inheritDoc} This implementation always returns <b>true</b> because
         * the constant object managed by this {@code Future} implementation is
         * always available.
         */
        @Override
        public boolean isDone() {
            return true;
        }

        /**
         * {@inheritDoc} This implementation just returns the constant value.
         */
        @Override
        public T get() {
            return value;
        }

        /**
         * {@inheritDoc} This implementation just returns the constant value; it
         * does not block, therefore the timeout has no meaning.
         */
        @Override
        public T get(final long timeout, final TimeUnit unit) {
            return value;
        }

        /**
         * {@inheritDoc} This implementation always returns <b>false</b>; there
         * is no background process which could be cancelled.
         */
        @Override
        public boolean isCancelled() {
            return false;
        }

        /**
         * {@inheritDoc} The cancel operation is not supported. This
         * implementation always returns <b>false</b>.
         */
        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            return false;
        }
    }

}
