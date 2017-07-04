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
package com.qihoo360.replugin.ext.lang3.math;

import com.qihoo360.replugin.ext.lang3.Validate;

/**
 * <p>Provides IEEE-754r variants of NumberUtils methods. </p>
 *
 * <p>See: <a href="http://en.wikipedia.org/wiki/IEEE_754r">http://en.wikipedia.org/wiki/IEEE_754r</a></p>
 *
 * @since 2.4
 */
public class IEEE754rUtils {

     /**
     * <p>Returns the minimum value in an array.</p>
     *
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
      * @since 3.4 Changed signature from min(double[]) to min(double...)
     */
    public static double min(final double... array) {
        Validate.isTrue(array != null, "The Array must not be null");
        Validate.isTrue(array.length != 0, "Array cannot be empty.");

        // Finds and returns min
        double min = array[0];
        for (int i = 1; i < array.length; i++) {
            min = min(array[i], min);
        }

        return min;
    }

    /**
     * <p>Returns the minimum value in an array.</p>
     *
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     * @since 3.4 Changed signature from min(float[]) to min(float...)
     */
    public static float min(final float... array) {
        Validate.isTrue(array != null, "The Array must not be null");
        Validate.isTrue(array.length != 0, "Array cannot be empty.");

        // Finds and returns min
        float min = array[0];
        for (int i = 1; i < array.length; i++) {
            min = min(array[i], min);
        }

        return min;
    }

    /**
     * <p>Gets the minimum of three <code>double</code> values.</p>
     *
     * <p>NaN is only returned if all numbers are NaN as per IEEE-754r. </p>
     *
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the smallest of the values
     */
    public static double min(final double a, final double b, final double c) {
        return min(min(a, b), c);
    }

    /**
     * <p>Gets the minimum of two <code>double</code> values.</p>
     *
     * <p>NaN is only returned if all numbers are NaN as per IEEE-754r. </p>
     *
     * @param a  value 1
     * @param b  value 2
     * @return  the smallest of the values
     */
    public static double min(final double a, final double b) {
        if(Double.isNaN(a)) {
            return b;
        } else
        if(Double.isNaN(b)) {
            return a;
        } else {
            return Math.min(a, b);
        }
    }

    /**
     * <p>Gets the minimum of three <code>float</code> values.</p>
     *
     * <p>NaN is only returned if all numbers are NaN as per IEEE-754r. </p>
     *
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the smallest of the values
     */
    public static float min(final float a, final float b, final float c) {
        return min(min(a, b), c);
    }

    /**
     * <p>Gets the minimum of two <code>float</code> values.</p>
     *
     * <p>NaN is only returned if all numbers are NaN as per IEEE-754r. </p>
     *
     * @param a  value 1
     * @param b  value 2
     * @return  the smallest of the values
     */
    public static float min(final float a, final float b) {
        if(Float.isNaN(a)) {
            return b;
        } else
        if(Float.isNaN(b)) {
            return a;
        } else {
            return Math.min(a, b);
        }
    }

    /**
     * <p>Returns the maximum value in an array.</p>
     *
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     * @since 3.4 Changed signature from max(double[]) to max(double...)
     */
    public static double max(final double... array) {
        Validate.isTrue(array != null, "The Array must not be null");
        Validate.isTrue(array.length != 0, "Array cannot be empty.");

        // Finds and returns max
        double max = array[0];
        for (int j = 1; j < array.length; j++) {
            max = max(array[j], max);
        }

        return max;
    }

    /**
     * <p>Returns the maximum value in an array.</p>
     *
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     * @since 3.4 Changed signature from max(float[]) to max(float...)
     */
    public static float max(final float... array) {
        Validate.isTrue(array != null, "The Array must not be null");
        Validate.isTrue(array.length != 0, "Array cannot be empty.");

        // Finds and returns max
        float max = array[0];
        for (int j = 1; j < array.length; j++) {
            max = max(array[j], max);
        }

        return max;
    }

    /**
     * <p>Gets the maximum of three <code>double</code> values.</p>
     *
     * <p>NaN is only returned if all numbers are NaN as per IEEE-754r. </p>
     *
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the largest of the values
     */
    public static double max(final double a, final double b, final double c) {
        return max(max(a, b), c);
    }

    /**
     * <p>Gets the maximum of two <code>double</code> values.</p>
     *
     * <p>NaN is only returned if all numbers are NaN as per IEEE-754r. </p>
     *
     * @param a  value 1
     * @param b  value 2
     * @return  the largest of the values
     */
    public static double max(final double a, final double b) {
        if(Double.isNaN(a)) {
            return b;
        } else
        if(Double.isNaN(b)) {
            return a;
        } else {
            return Math.max(a, b);
        }
    }

    /**
     * <p>Gets the maximum of three <code>float</code> values.</p>
     *
     * <p>NaN is only returned if all numbers are NaN as per IEEE-754r. </p>
     *
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the largest of the values
     */
    public static float max(final float a, final float b, final float c) {
        return max(max(a, b), c);
    }

    /**
     * <p>Gets the maximum of two <code>float</code> values.</p>
     *
     * <p>NaN is only returned if all numbers are NaN as per IEEE-754r. </p>
     *
     * @param a  value 1
     * @param b  value 2
     * @return  the largest of the values
     */
    public static float max(final float a, final float b) {
        if(Float.isNaN(a)) {
            return b;
        } else
        if(Float.isNaN(b)) {
            return a;
        } else {
            return Math.max(a, b);
        }
    }

}
