/*
 * Copyright (c) 2016, Liu Dong
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.qihoo360.replugin.ext.lang3.mutable;

import java.io.Serializable;

/**
 * A mutable <code>Object</code> wrapper.
 *
 * @param <T> the type to set and get
 * @since 2.1
 */
public class MutableObject<T> implements Mutable<T>, Serializable {

    /**
     * Required for serialization support.
     *
     * @see Serializable
     */
    private static final long serialVersionUID = 86241875189L;

    /** The mutable value. */
    private T value;

    /**
     * Constructs a new MutableObject with the default value of <code>null</code>.
     */
    public MutableObject() {
        super();
    }

    /**
     * Constructs a new MutableObject with the specified value.
     *
     * @param value  the initial value to store
     */
    public MutableObject(final T value) {
        super();
        this.value = value;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the value.
     *
     * @return the value, may be null
     */
    @Override
    public T getValue() {
        return this.value;
    }

    /**
     * Sets the value.
     *
     * @param value  the value to set
     */
    @Override
    public void setValue(final T value) {
        this.value = value;
    }

    //-----------------------------------------------------------------------
    /**
     * <p>
     * Compares this object against the specified object. The result is <code>true</code> if and only if the argument
     * is not <code>null</code> and is a <code>MutableObject</code> object that contains the same <code>T</code>
     * value as this object.
     * </p>
     *
     * @param obj  the object to compare with, <code>null</code> returns <code>false</code>
     * @return  <code>true</code> if the objects are the same;
     *          <code>true</code> if the objects have equivalent <code>value</code> fields;
     *          <code>false</code> otherwise.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (this.getClass() == obj.getClass()) {
            final MutableObject<?> that = (MutableObject<?>) obj;
            return this.value.equals(that.value);
        }
        return false;
    }

    /**
     * Returns the value's hash code or <code>0</code> if the value is <code>null</code>.
     *
     * @return the value's hash code or <code>0</code> if the value is <code>null</code>.
     */
    @Override
    public int hashCode() {
        return value == null ? 0 : value.hashCode();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns the String value of this mutable.
     *
     * @return the mutable value as a string
     */
    @Override
    public String toString() {
        return value == null ? "null" : value.toString();
    }

}
