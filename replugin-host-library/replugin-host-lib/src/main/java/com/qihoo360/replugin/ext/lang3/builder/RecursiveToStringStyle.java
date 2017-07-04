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
package com.qihoo360.replugin.ext.lang3.builder;

import java.util.Collection;

import com.qihoo360.replugin.ext.lang3.ClassUtils;

/**
 * <p>Works with {@link ToStringBuilder} to create a "deep" <code>toString</code>.</p>
 *
 * <p>To use this class write code as follows:</p>
 *
 * <pre>
 * public class Job {
 *   String title;
 *   ...
 * }
 *
 * public class Person {
 *   String name;
 *   int age;
 *   boolean smoker;
 *   Job job;
 *
 *   ...
 *
 *   public String toString() {
 *     return new ReflectionToStringBuilder(this, new RecursiveToStringStyle()).toString();
 *   }
 * }
 * </pre>
 *
 * <p>This will produce a toString of the format:
 * <code>Person@7f54[name=Stephen,age=29,smoker=false,job=Job@43cd2[title=Manager]]</code></p>
 *
 * @since 3.2
 */
public class RecursiveToStringStyle extends ToStringStyle {

    /**
     * Required for serialization support.
     *
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    /**
     * <p>Constructor.</p>
     */
    public RecursiveToStringStyle() {
        super();
    }

    @Override
    public void appendDetail(final StringBuffer buffer, final String fieldName, final Object value) {
        if (!ClassUtils.isPrimitiveWrapper(value.getClass()) &&
            !String.class.equals(value.getClass()) &&
            accept(value.getClass())) {
            buffer.append(ReflectionToStringBuilder.toString(value, this));
        } else {
            super.appendDetail(buffer, fieldName, value);
        }
    }

    @Override
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final Collection<?> coll) {
        appendClassName(buffer, coll);
        appendIdentityHashCode(buffer, coll);
        appendDetail(buffer, fieldName, coll.toArray());
    }

    /**
     * Returns whether or not to recursively format the given <code>Class</code>.
     * By default, this method always returns {@code true}, but may be overwritten by
     * sub-classes to filter specific classes.
     *
     * @param clazz
     *            The class to test.
     * @return Whether or not to recursively format the given <code>Class</code>.
     */
    protected boolean accept(final Class<?> clazz) {
        return true;
    }
}
