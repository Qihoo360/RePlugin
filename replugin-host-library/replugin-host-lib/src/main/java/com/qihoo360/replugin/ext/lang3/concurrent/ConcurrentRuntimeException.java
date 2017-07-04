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

/**
 * <p>
 * An exception class used for reporting runtime error conditions related to
 * accessing data of background tasks.
 * </p>
 * <p>
 * This class is an analogue of the {@link ConcurrentException} exception class.
 * However, it is a runtime exception and thus does not need explicit catch
 * clauses. Some methods of {@link ConcurrentUtils} throw {@code
 * ConcurrentRuntimeException} exceptions rather than
 * {@link ConcurrentException} exceptions. They can be used by client code that
 * does not want to be bothered with checked exceptions.
 * </p>
 *
 * @since 3.0
 */
public class ConcurrentRuntimeException extends RuntimeException {
    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = -6582182735562919670L;

    /**
     * Creates a new, uninitialized instance of {@code
     * ConcurrentRuntimeException}.
     */
    protected ConcurrentRuntimeException() {
        super();
    }

    /**
     * Creates a new instance of {@code ConcurrentRuntimeException} and
     * initializes it with the given cause.
     *
     * @param cause the cause of this exception
     * @throws IllegalArgumentException if the cause is not a checked exception
     */
    public ConcurrentRuntimeException(final Throwable cause) {
        super(ConcurrentUtils.checkedException(cause));
    }

    /**
     * Creates a new instance of {@code ConcurrentRuntimeException} and
     * initializes it with the given message and cause.
     *
     * @param msg the error message
     * @param cause the cause of this exception
     * @throws IllegalArgumentException if the cause is not a checked exception
     */
    public ConcurrentRuntimeException(final String msg, final Throwable cause) {
        super(msg, ConcurrentUtils.checkedException(cause));
    }
}
