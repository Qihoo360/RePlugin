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
package com.qihoo360.replugin.ext.lang3.exception;

import com.qihoo360.replugin.ext.lang3.tuple.Pair;

import java.util.List;
import java.util.Set;

/**
 * Allows the storage and retrieval of contextual information based on label-value
 * pairs for exceptions.
 * <p>
 * Implementations are expected to manage the pairs in a list-style collection
 * that keeps the pairs in the sequence of their addition.
 * </p>
 *
 * @see ContextedException
 * @see ContextedRuntimeException
 * @since 3.0
 */
public interface ExceptionContext {

    /**
     * Adds a contextual label-value pair into this context.
     * <p>
     * The pair will be added to the context, independently of an already
     * existing pair with the same label.
     * </p>
     *
     * @param label  the label of the item to add, {@code null} not recommended
     * @param value  the value of item to add, may be {@code null}
     * @return {@code this}, for method chaining, not {@code null}
     */
    ExceptionContext addContextValue(String label, Object value);

    /**
     * Sets a contextual label-value pair into this context.
     * <p>
     * The pair will be added normally, but any existing label-value pair with
     * the same label is removed from the context.
     * </p>
     *
     * @param label  the label of the item to add, {@code null} not recommended
     * @param value  the value of item to add, may be {@code null}
     * @return {@code this}, for method chaining, not {@code null}
     */
    ExceptionContext setContextValue(String label, Object value);

    /**
     * Retrieves all the contextual data values associated with the label.
     *
     * @param label  the label to get the contextual values for, may be {@code null}
     * @return the contextual values associated with the label, never {@code null}
     */
    List<Object> getContextValues(String label);

    /**
     * Retrieves the first available contextual data value associated with the label.
     *
     * @param label  the label to get the contextual value for, may be {@code null}
     * @return the first contextual value associated with the label, may be {@code null}
     */
    Object getFirstContextValue(String label);

    /**
     * Retrieves the full set of labels defined in the contextual data.
     *
     * @return the set of labels, not {@code null}
     */
    Set<String> getContextLabels();

    /**
     * Retrieves the full list of label-value pairs defined in the contextual data.
     *
     * @return the list of pairs, not {@code null}
     */
    List<Pair<String, Object>> getContextEntries();

    /**
     * Gets the contextualized error message based on a base message.
     * This will add the context label-value pairs to the message.
     *
     * @param baseMessage  the base exception message <b>without</b> context information appended
     * @return the exception message <b>with</b> context information appended, not {@code null}
     */
    String getFormattedExceptionMessage(String baseMessage);

}
