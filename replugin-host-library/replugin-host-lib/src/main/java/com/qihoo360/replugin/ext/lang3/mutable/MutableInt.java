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
package com.qihoo360.replugin.ext.lang3.mutable;

import com.qihoo360.replugin.ext.lang3.math.NumberUtils;

/**
 * A mutable <code>int</code> wrapper.
 * <p>
 * Note that as MutableInt does not extend Integer, it is not treated by String.format as an Integer parameter.
 *
 * @see Integer
 * @since 2.1
 */
public class MutableInt extends Number implements Comparable<MutableInt>, Mutable<Number> {

    /**
     * Required for serialization support.
     *
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 512176391864L;

    /** The mutable value. */
    private int value;

    /**
     * Constructs a new MutableInt with the default value of zero.
     */
    public MutableInt() {
        super();
    }

    /**
     * Constructs a new MutableInt with the specified value.
     *
     * @param value  the initial value to store
     */
    public MutableInt(final int value) {
        super();
        this.value = value;
    }

    /**
     * Constructs a new MutableInt with the specified value.
     *
     * @param value  the initial value to store, not null
     * @throws NullPointerException if the object is null
     */
    public MutableInt(final Number value) {
        super();
        this.value = value.intValue();
    }

    /**
     * Constructs a new MutableInt parsing the given string.
     *
     * @param value  the string to parse, not null
     * @throws NumberFormatException if the string cannot be parsed into an int
     * @since 2.5
     */
    public MutableInt(final String value) throws NumberFormatException {
        super();
        this.value = Integer.parseInt(value);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the value as a Integer instance.
     *
     * @return the value as a Integer, never null
     */
    @Override
    public Integer getValue() {
        return Integer.valueOf(this.value);
    }

    /**
     * Sets the value.
     *
     * @param value  the value to set
     */
    public void setValue(final int value) {
        this.value = value;
    }

    /**
     * Sets the value from any Number instance.
     *
     * @param value  the value to set, not null
     * @throws NullPointerException if the object is null
     */
    @Override
    public void setValue(final Number value) {
        this.value = value.intValue();
    }

    //-----------------------------------------------------------------------
    /**
     * Increments the value.
     *
     * @since Commons Lang 2.2
     */
    public void increment() {
        value++;
    }

    /**
     * Increments this instance's value by 1; this method returns the value associated with the instance
     * immediately prior to the increment operation. This method is not thread safe.
     *
     * @return the value associated with the instance before it was incremented
     * @since 3.5
     */
    public int getAndIncrement() {
        final int last = value;
        value++;
        return last;
    }

    /**
     * Increments this instance's value by 1; this method returns the value associated with the instance
     * immediately after the increment operation. This method is not thread safe.
     *
     * @return the value associated with the instance after it is incremented
     * @since 3.5
     */
    public int incrementAndGet() {
        value++;
        return value;
    }

    /**
     * Decrements the value.
     *
     * @since Commons Lang 2.2
     */
    public void decrement() {
        value--;
    }

    /**
     * Decrements this instance's value by 1; this method returns the value associated with the instance
     * immediately prior to the decrement operation. This method is not thread safe.
     *
     * @return the value associated with the instance before it was decremented
     * @since 3.5
     */
    public int getAndDecrement() {
        final int last = value;
        value--;
        return last;
    }

    /**
     * Decrements this instance's value by 1; this method returns the value associated with the instance
     * immediately after the decrement operation. This method is not thread safe.
     *
     * @return the value associated with the instance after it is decremented
     * @since 3.5
     */
    public int decrementAndGet() {
        value--;
        return value;
    }

    //-----------------------------------------------------------------------
    /**
     * Adds a value to the value of this instance.
     *
     * @param operand  the value to add, not null
     * @since Commons Lang 2.2
     */
    public void add(final int operand) {
        this.value += operand;
    }

    /**
     * Adds a value to the value of this instance.
     *
     * @param operand  the value to add, not null
     * @throws NullPointerException if the object is null
     * @since Commons Lang 2.2
     */
    public void add(final Number operand) {
        this.value += operand.intValue();
    }

    /**
     * Subtracts a value from the value of this instance.
     *
     * @param operand  the value to subtract, not null
     * @since Commons Lang 2.2
     */
    public void subtract(final int operand) {
        this.value -= operand;
    }

    /**
     * Subtracts a value from the value of this instance.
     *
     * @param operand  the value to subtract, not null
     * @throws NullPointerException if the object is null
     * @since Commons Lang 2.2
     */
    public void subtract(final Number operand) {
        this.value -= operand.intValue();
    }

    /**
     * Increments this instance's value by {@code operand}; this method returns the value associated with the instance
     * immediately after the addition operation. This method is not thread safe.
     *
     * @param operand the quantity to add, not null
     * @return the value associated with this instance after adding the operand
     * @since 3.5
     */
    public int addAndGet(final int operand) {
        this.value += operand;
        return value;
    }

    /**
     * Increments this instance's value by {@code operand}; this method returns the value associated with the instance
     * immediately after the addition operation. This method is not thread safe.
     *
     * @param operand the quantity to add, not null
     * @throws NullPointerException if {@code operand} is null
     * @return the value associated with this instance after adding the operand
     * @since 3.5
     */
    public int addAndGet(final Number operand) {
        this.value += operand.intValue();
        return value;
    }

    /**
     * Increments this instance's value by {@code operand}; this method returns the value associated with the instance
     * immediately prior to the addition operation. This method is not thread safe.
     *
     * @param operand the quantity to add, not null
     * @return the value associated with this instance immediately before the operand was added
     * @since 3.5
     */
    public int getAndAdd(final int operand) {
        final int last = value;
        this.value += operand;
        return last;
    }

    /**
     * Increments this instance's value by {@code operand}; this method returns the value associated with the instance
     * immediately prior to the addition operation. This method is not thread safe.
     *
     * @param operand the quantity to add, not null
     * @throws NullPointerException if {@code operand} is null
     * @return the value associated with this instance immediately before the operand was added
     * @since 3.5
     */
    public int getAndAdd(final Number operand) {
        final int last = value;
        this.value += operand.intValue();
        return last;
    }

    //-----------------------------------------------------------------------
    // shortValue and byteValue rely on Number implementation
    /**
     * Returns the value of this MutableInt as an int.
     *
     * @return the numeric value represented by this object after conversion to type int.
     */
    @Override
    public int intValue() {
        return value;
    }

    /**
     * Returns the value of this MutableInt as a long.
     *
     * @return the numeric value represented by this object after conversion to type long.
     */
    @Override
    public long longValue() {
        return value;
    }

    /**
     * Returns the value of this MutableInt as a float.
     *
     * @return the numeric value represented by this object after conversion to type float.
     */
    @Override
    public float floatValue() {
        return value;
    }

    /**
     * Returns the value of this MutableInt as a double.
     *
     * @return the numeric value represented by this object after conversion to type double.
     */
    @Override
    public double doubleValue() {
        return value;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets this mutable as an instance of Integer.
     *
     * @return a Integer instance containing the value from this mutable, never null
     */
    public Integer toInteger() {
        return Integer.valueOf(intValue());
    }

    //-----------------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <code>true</code> if and only if the argument is
     * not <code>null</code> and is a <code>MutableInt</code> object that contains the same <code>int</code> value
     * as this object.
     *
     * @param obj  the object to compare with, null returns false
     * @return <code>true</code> if the objects are the same; <code>false</code> otherwise.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof MutableInt) {
            return value == ((MutableInt) obj).intValue();
        }
        return false;
    }

    /**
     * Returns a suitable hash code for this mutable.
     *
     * @return a suitable hash code
     */
    @Override
    public int hashCode() {
        return value;
    }

    //-----------------------------------------------------------------------
    /**
     * Compares this mutable to another in ascending order.
     *
     * @param other  the other mutable to compare to, not null
     * @return negative if this is less, zero if equal, positive if greater
     */
    @Override
    public int compareTo(final MutableInt other) {
        return NumberUtils.compare(this.value, other.value);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns the String value of this mutable.
     *
     * @return the mutable value as a string
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
