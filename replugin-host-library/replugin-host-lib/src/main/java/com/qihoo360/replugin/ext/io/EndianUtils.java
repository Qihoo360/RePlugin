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
package com.qihoo360.replugin.ext.io;

import com.qihoo360.replugin.ext.io.input.SwappedDataInputStream;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility code for dealing with different endian systems.
 * <p>
 * Different computer architectures adopt different conventions for
 * byte ordering. In so-called "Little Endian" architectures (eg Intel),
 * the low-order byte is stored in memory at the lowest address, and
 * subsequent bytes at higher addresses. For "Big Endian" architectures
 * (eg Motorola), the situation is reversed.
 * This class helps you solve this incompatibility.
 * <p>
 * Origin of code: Excalibur
 *
 * @version $Id: EndianUtils.java 1686450 2015-06-19 16:43:48Z krosenvold $
 * @see SwappedDataInputStream
 */
public class EndianUtils {

    /**
     * Instances should NOT be constructed in standard programming.
     */
    public EndianUtils() {
        super();
    }

    // ========================================== Swapping routines

    /**
     * Converts a "short" value between endian systems.
     * @param value value to convert
     * @return the converted value
     */
    public static short swapShort(final short value) {
        return (short) ( ( ( ( value >> 0 ) & 0xff ) << 8 ) +
            ( ( ( value >> 8 ) & 0xff ) << 0 ) );
    }

    /**
     * Converts a "int" value between endian systems.
     * @param value value to convert
     * @return the converted value
     */
    public static int swapInteger(final int value) {
        return
            ( ( ( value >> 0 ) & 0xff ) << 24 ) +
            ( ( ( value >> 8 ) & 0xff ) << 16 ) +
            ( ( ( value >> 16 ) & 0xff ) << 8 ) +
            ( ( ( value >> 24 ) & 0xff ) << 0 );
    }

    /**
     * Converts a "long" value between endian systems.
     * @param value value to convert
     * @return the converted value
     */
    public static long swapLong(final long value) {
        return
            ( ( ( value >> 0 ) & 0xff ) << 56 ) +
            ( ( ( value >> 8 ) & 0xff ) << 48 ) +
            ( ( ( value >> 16 ) & 0xff ) << 40 ) +
            ( ( ( value >> 24 ) & 0xff ) << 32 ) +
            ( ( ( value >> 32 ) & 0xff ) << 24 ) +
            ( ( ( value >> 40 ) & 0xff ) << 16 ) +
            ( ( ( value >> 48 ) & 0xff ) << 8 ) +
            ( ( ( value >> 56 ) & 0xff ) << 0 );
    }

    /**
     * Converts a "float" value between endian systems.
     * @param value value to convert
     * @return the converted value
     */
    public static float swapFloat(final float value) {
        return Float.intBitsToFloat( swapInteger( Float.floatToIntBits( value ) ) );
    }

    /**
     * Converts a "double" value between endian systems.
     * @param value value to convert
     * @return the converted value
     */
    public static double swapDouble(final double value) {
        return Double.longBitsToDouble( swapLong( Double.doubleToLongBits( value ) ) );
    }

    // ========================================== Swapping read/write routines

    /**
     * Writes a "short" value to a byte array at a given offset. The value is
     * converted to the opposed endian system while writing.
     * @param data target byte array
     * @param offset starting offset in the byte array
     * @param value value to write
     */
    public static void writeSwappedShort(final byte[] data, final int offset, final short value) {
        data[ offset + 0 ] = (byte)( ( value >> 0 ) & 0xff );
        data[ offset + 1 ] = (byte)( ( value >> 8 ) & 0xff );
    }

    /**
     * Reads a "short" value from a byte array at a given offset. The value is
     * converted to the opposed endian system while reading.
     * @param data source byte array
     * @param offset starting offset in the byte array
     * @return the value read
     */
    public static short readSwappedShort(final byte[] data, final int offset) {
        return (short)( ( ( data[ offset + 0 ] & 0xff ) << 0 ) +
            ( ( data[ offset + 1 ] & 0xff ) << 8 ) );
    }

    /**
     * Reads an unsigned short (16-bit) value from a byte array at a given
     * offset. The value is converted to the opposed endian system while
     * reading.
     * @param data source byte array
     * @param offset starting offset in the byte array
     * @return the value read
     */
    public static int readSwappedUnsignedShort(final byte[] data, final int offset) {
        return ( ( ( data[ offset + 0 ] & 0xff ) << 0 ) +
            ( ( data[ offset + 1 ] & 0xff ) << 8 ) );
    }

    /**
     * Writes a "int" value to a byte array at a given offset. The value is
     * converted to the opposed endian system while writing.
     * @param data target byte array
     * @param offset starting offset in the byte array
     * @param value value to write
     */
    public static void writeSwappedInteger(final byte[] data, final int offset, final int value) {
        data[ offset + 0 ] = (byte)( ( value >> 0 ) & 0xff );
        data[ offset + 1 ] = (byte)( ( value >> 8 ) & 0xff );
        data[ offset + 2 ] = (byte)( ( value >> 16 ) & 0xff );
        data[ offset + 3 ] = (byte)( ( value >> 24 ) & 0xff );
    }

    /**
     * Reads a "int" value from a byte array at a given offset. The value is
     * converted to the opposed endian system while reading.
     * @param data source byte array
     * @param offset starting offset in the byte array
     * @return the value read
     */
    public static int readSwappedInteger(final byte[] data, final int offset) {
        return ( ( ( data[ offset + 0 ] & 0xff ) << 0 ) +
            ( ( data[ offset + 1 ] & 0xff ) << 8 ) +
            ( ( data[ offset + 2 ] & 0xff ) << 16 ) +
            ( ( data[ offset + 3 ] & 0xff ) << 24 ) );
    }

    /**
     * Reads an unsigned integer (32-bit) value from a byte array at a given
     * offset. The value is converted to the opposed endian system while
     * reading.
     * @param data source byte array
     * @param offset starting offset in the byte array
     * @return the value read
     */
    public static long readSwappedUnsignedInteger(final byte[] data, final int offset) {
        final long low = ( ( ( data[ offset + 0 ] & 0xff ) << 0 ) +
                     ( ( data[ offset + 1 ] & 0xff ) << 8 ) +
                     ( ( data[ offset + 2 ] & 0xff ) << 16 ) );

        final long high = data[ offset + 3 ] & 0xff;

        return (high << 24) + (0xffffffffL & low); 
    }

    /**
     * Writes a "long" value to a byte array at a given offset. The value is
     * converted to the opposed endian system while writing.
     * @param data target byte array
     * @param offset starting offset in the byte array
     * @param value value to write
     */
    public static void writeSwappedLong(final byte[] data, final int offset, final long value) {
        data[ offset + 0 ] = (byte)( ( value >> 0 ) & 0xff );
        data[ offset + 1 ] = (byte)( ( value >> 8 ) & 0xff );
        data[ offset + 2 ] = (byte)( ( value >> 16 ) & 0xff );
        data[ offset + 3 ] = (byte)( ( value >> 24 ) & 0xff );
        data[ offset + 4 ] = (byte)( ( value >> 32 ) & 0xff );
        data[ offset + 5 ] = (byte)( ( value >> 40 ) & 0xff );
        data[ offset + 6 ] = (byte)( ( value >> 48 ) & 0xff );
        data[ offset + 7 ] = (byte)( ( value >> 56 ) & 0xff );
    }

    /**
     * Reads a "long" value from a byte array at a given offset. The value is
     * converted to the opposed endian system while reading.
     * @param data source byte array
     * @param offset starting offset in the byte array
     * @return the value read
     */
    public static long readSwappedLong(final byte[] data, final int offset) {
        final long low = readSwappedInteger(data, offset);
        final long high = readSwappedInteger(data, offset + 4);
        return (high << 32) + (0xffffffffL & low);
    }

    /**
     * Writes a "float" value to a byte array at a given offset. The value is
     * converted to the opposed endian system while writing.
     * @param data target byte array
     * @param offset starting offset in the byte array
     * @param value value to write
     */
    public static void writeSwappedFloat(final byte[] data, final int offset, final float value) {
        writeSwappedInteger( data, offset, Float.floatToIntBits( value ) );
    }

    /**
     * Reads a "float" value from a byte array at a given offset. The value is
     * converted to the opposed endian system while reading.
     * @param data source byte array
     * @param offset starting offset in the byte array
     * @return the value read
     */
    public static float readSwappedFloat(final byte[] data, final int offset) {
        return Float.intBitsToFloat( readSwappedInteger( data, offset ) );
    }

    /**
     * Writes a "double" value to a byte array at a given offset. The value is
     * converted to the opposed endian system while writing.
     * @param data target byte array
     * @param offset starting offset in the byte array
     * @param value value to write
     */
    public static void writeSwappedDouble(final byte[] data, final int offset, final double value) {
        writeSwappedLong( data, offset, Double.doubleToLongBits( value ) );
    }

    /**
     * Reads a "double" value from a byte array at a given offset. The value is
     * converted to the opposed endian system while reading.
     * @param data source byte array
     * @param offset starting offset in the byte array
     * @return the value read
     */
    public static double readSwappedDouble(final byte[] data, final int offset) {
        return Double.longBitsToDouble( readSwappedLong( data, offset ) );
    }

    /**
     * Writes a "short" value to an OutputStream. The value is
     * converted to the opposed endian system while writing.
     * @param output target OutputStream
     * @param value value to write
     * @throws IOException in case of an I/O problem
     */
    public static void writeSwappedShort(final OutputStream output, final short value)
        throws IOException
    {
        output.write( (byte)( ( value >> 0 ) & 0xff ) );
        output.write( (byte)( ( value >> 8 ) & 0xff ) );
    }

    /**
     * Reads a "short" value from an InputStream. The value is
     * converted to the opposed endian system while reading.
     * @param input source InputStream
     * @return the value just read
     * @throws IOException in case of an I/O problem
     */
    public static short readSwappedShort(final InputStream input)
        throws IOException
    {
        return (short)( ( ( read( input ) & 0xff ) << 0 ) +
            ( ( read( input ) & 0xff ) << 8 ) );
    }

    /**
     * Reads a unsigned short (16-bit) from an InputStream. The value is
     * converted to the opposed endian system while reading.
     * @param input source InputStream
     * @return the value just read
     * @throws IOException in case of an I/O problem
     */
    public static int readSwappedUnsignedShort(final InputStream input)
        throws IOException
    {
        final int value1 = read( input );
        final int value2 = read( input );

        return ( ( ( value1 & 0xff ) << 0 ) +
            ( ( value2 & 0xff ) << 8 ) );
    }

    /**
     * Writes a "int" value to an OutputStream. The value is
     * converted to the opposed endian system while writing.
     * @param output target OutputStream
     * @param value value to write
     * @throws IOException in case of an I/O problem
     */
    public static void writeSwappedInteger(final OutputStream output, final int value)
        throws IOException
    {
        output.write( (byte)( ( value >> 0 ) & 0xff ) );
        output.write( (byte)( ( value >> 8 ) & 0xff ) );
        output.write( (byte)( ( value >> 16 ) & 0xff ) );
        output.write( (byte)( ( value >> 24 ) & 0xff ) );
    }

    /**
     * Reads a "int" value from an InputStream. The value is
     * converted to the opposed endian system while reading.
     * @param input source InputStream
     * @return the value just read
     * @throws IOException in case of an I/O problem
     */
    public static int readSwappedInteger(final InputStream input)
        throws IOException
    {
        final int value1 = read( input );
        final int value2 = read( input );
        final int value3 = read( input );
        final int value4 = read( input );

        return ( ( value1 & 0xff ) << 0 ) +
            ( ( value2 & 0xff ) << 8 ) +
            ( ( value3 & 0xff ) << 16 ) +
            ( ( value4 & 0xff ) << 24 );
    }

    /**
     * Reads a unsigned integer (32-bit) from an InputStream. The value is
     * converted to the opposed endian system while reading.
     * @param input source InputStream
     * @return the value just read
     * @throws IOException in case of an I/O problem
     */
    public static long readSwappedUnsignedInteger(final InputStream input)
        throws IOException
    {
        final int value1 = read( input );
        final int value2 = read( input );
        final int value3 = read( input );
        final int value4 = read( input );

        final long low = ( ( ( value1 & 0xff ) << 0 ) +
                     ( ( value2 & 0xff ) << 8 ) +
                     ( ( value3 & 0xff ) << 16 ) );

        final long high = value4 & 0xff;

        return (high << 24) + (0xffffffffL & low); 
    }

    /**
     * Writes a "long" value to an OutputStream. The value is
     * converted to the opposed endian system while writing.
     * @param output target OutputStream
     * @param value value to write
     * @throws IOException in case of an I/O problem
     */
    public static void writeSwappedLong(final OutputStream output, final long value)
        throws IOException
    {
        output.write( (byte)( ( value >> 0 ) & 0xff ) );
        output.write( (byte)( ( value >> 8 ) & 0xff ) );
        output.write( (byte)( ( value >> 16 ) & 0xff ) );
        output.write( (byte)( ( value >> 24 ) & 0xff ) );
        output.write( (byte)( ( value >> 32 ) & 0xff ) );
        output.write( (byte)( ( value >> 40 ) & 0xff ) );
        output.write( (byte)( ( value >> 48 ) & 0xff ) );
        output.write( (byte)( ( value >> 56 ) & 0xff ) );
    }

    /**
     * Reads a "long" value from an InputStream. The value is
     * converted to the opposed endian system while reading.
     * @param input source InputStream
     * @return the value just read
     * @throws IOException in case of an I/O problem
     */
    public static long readSwappedLong(final InputStream input)
        throws IOException
    {
        final byte[] bytes = new byte[8];
        for ( int i=0; i<8; i++ ) {
            bytes[i] = (byte) read( input );
        }
        return readSwappedLong( bytes, 0 );
    }

    /**
     * Writes a "float" value to an OutputStream. The value is
     * converted to the opposed endian system while writing.
     * @param output target OutputStream
     * @param value value to write
     * @throws IOException in case of an I/O problem
     */
    public static void writeSwappedFloat(final OutputStream output, final float value)
        throws IOException
    {
        writeSwappedInteger( output, Float.floatToIntBits( value ) );
    }

    /**
     * Reads a "float" value from an InputStream. The value is
     * converted to the opposed endian system while reading.
     * @param input source InputStream
     * @return the value just read
     * @throws IOException in case of an I/O problem
     */
    public static float readSwappedFloat(final InputStream input)
        throws IOException
    {
        return Float.intBitsToFloat( readSwappedInteger( input ) );
    }

    /**
     * Writes a "double" value to an OutputStream. The value is
     * converted to the opposed endian system while writing.
     * @param output target OutputStream
     * @param value value to write
     * @throws IOException in case of an I/O problem
     */
    public static void writeSwappedDouble(final OutputStream output, final double value)
        throws IOException
    {
        writeSwappedLong( output, Double.doubleToLongBits( value ) );
    }

    /**
     * Reads a "double" value from an InputStream. The value is
     * converted to the opposed endian system while reading.
     * @param input source InputStream
     * @return the value just read
     * @throws IOException in case of an I/O problem
     */
    public static double readSwappedDouble(final InputStream input)
        throws IOException
    {
        return Double.longBitsToDouble( readSwappedLong( input ) );
    }

    /**
     * Reads the next byte from the input stream.
     * @param input  the stream
     * @return the byte
     * @throws IOException if the end of file is reached
     */
    private static int read(final InputStream input)
        throws IOException
    {
        final int value = input.read();

        if( IOUtils.EOF == value ) {
            throw new EOFException( "Unexpected EOF reached" );
        }

        return value;
    }
}
