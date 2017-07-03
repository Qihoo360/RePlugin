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
package com.qihoo360.replugin.ext.lang3.reflect;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import com.qihoo360.replugin.ext.lang3.Validate;

/**
 * <p>Type literal comparable to {@code javax.enterprise.util.TypeLiteral},
 * made generally available outside the JEE context. Allows the passing around of
 * a "token" that represents a type in a typesafe manner, as opposed to
 * passing the (non-parameterized) {@link Type} object itself. Consider:</p>
 * <p>
 * You might see such a typesafe API as:
 * <pre>
 * class Typesafe {
 *   &lt;T&gt; T obtain(Class&lt;T&gt; type, ...);
 * }
 * </pre>
 * Consumed in the manner of:
 * <pre>
 * Foo foo = typesafe.obtain(Foo.class, ...);
 * </pre>
 * Yet, you run into problems when you want to do this with a parameterized type:
 * <pre>
 * List&lt;String&gt; listOfString = typesafe.obtain(List.class, ...); // could only give us a raw List
 * </pre>
 * {@code java.lang.reflect.Type} might provide some value:
 * <pre>
 * Type listOfStringType = ...; // firstly, how to obtain this? Doable, but not straightforward.
 * List&lt;String&gt; listOfString = (List&lt;String&gt;) typesafe.obtain(listOfStringType, ...); // nongeneric Type would necessitate a cast
 * </pre>
 * The "type literal" concept was introduced to provide an alternative, i.e.:
 * <pre>
 * class Typesafe {
 *   &lt;T&gt; T obtain(TypeLiteral&lt;T&gt; type, ...);
 * }
 * </pre>
 * Consuming code looks like:
 * <pre>
 * List&lt;String&gt; listOfString = typesafe.obtain(new TypeLiteral&lt;List&lt;String&gt;&gt;() {}, ...);
 * </pre>
 * <p>
 * This has the effect of "jumping up" a level to tie a {@code java.lang.reflect.Type}
 * to a type variable while simultaneously making it short work to obtain a
 * {@code Type} instance for any given type, inline.
 * </p>
 * <p>Additionally {@link TypeLiteral} implements the {@link Typed} interface which
 * is a generalization of this concept, and which may be implemented in custom classes.
 * It is suggested that APIs be defined in terms of the interface, in the following manner:
 * </p>
 * <pre>
 *   &lt;T&gt; T obtain(Typed&lt;T&gt; typed, ...);
 * </pre>
 *
 * @since 3.2
 */
public abstract class TypeLiteral<T> implements Typed<T> {

    @SuppressWarnings("rawtypes")
    private static final TypeVariable<Class<TypeLiteral>> T = TypeLiteral.class.getTypeParameters()[0];

    /**
     * Represented type.
     */
    public final Type value;

    private final String toString;

    /**
     * The default constructor.
     */
    protected TypeLiteral() {
        this.value =
            Validate.notNull(TypeUtils.getTypeArguments(getClass(), TypeLiteral.class).get(T),
                "%s does not assign type parameter %s", getClass(), TypeUtils.toLongString(T));

        this.toString = String.format("%s<%s>", TypeLiteral.class.getSimpleName(), TypeUtils.toString(value));
    }

    @Override
    public final boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof TypeLiteral == false) {
            return false;
        }
        final TypeLiteral<?> other = (TypeLiteral<?>) obj;
        return TypeUtils.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return 37 << 4 | value.hashCode();
    }

    @Override
    public String toString() {
        return toString;
    }

    @Override
    public Type getType() {
        return value;
    }
}
