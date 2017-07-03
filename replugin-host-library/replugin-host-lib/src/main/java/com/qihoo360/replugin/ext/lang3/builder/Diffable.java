/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qihoo360.replugin.ext.lang3.builder;

/**
 * <p>{@code Diffable} classes can be compared with other objects
 * for differences. The {@link DiffResult} object retrieved can be queried
 * for a list of differences or printed using the {@link DiffResult#toString()}.</p>
 *
 * <p>The calculation of the differences is <i>consistent with equals</i> if
 * and only if {@code d1.equals(d2)} implies {@code d1.diff(d2) == ""}.
 * It is strongly recommended that implementations are consistent with equals
 * to avoid confusion. Note that {@code null} is not an instance of any class
 * and {@code d1.diff(null)} should throw a {@code NullPointerException}.</p>
 *
 * <p>
 * {@code Diffable} classes lend themselves well to unit testing, in which a
 * easily readable description of the differences between an anticipated result and
 * an actual result can be retrieved. For example:
 * </p>
 * <pre>
 * Assert.assertEquals(expected.diff(result), expected, result);
 * </pre>
 *
 * @param <T> the type of objects that this object may be differentiated against
 * @since 3.3
 */
public interface Diffable<T> {

    /**
     * <p>Retrieves a list of the differences between
     * this object and the supplied object.</p>
     *
     * @param obj the object to diff against, can be {@code null}
     * @return a list of differences
     * @throws NullPointerException if the specified object is {@code null}
     */
    DiffResult diff(T obj);
}
