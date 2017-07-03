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
package com.qihoo360.replugin.ext.io;

import com.qihoo360.replugin.ext.io.input.BOMInputStream;

import java.io.Serializable;

/**
 * Byte Order Mark (BOM) representation - see {@link BOMInputStream}.
 *
 * @see BOMInputStream
 * @see <a href="http://en.wikipedia.org/wiki/Byte_order_mark">Wikipedia: Byte Order Mark</a>
 * @see <a href="http://www.w3.org/TR/2006/REC-xml-20060816/#sec-guessing">W3C: Autodetection of Character Encodings
 *      (Non-Normative)</a>
 * @version $Id: ByteOrderMark.java 1586504 2014-04-10 23:34:37Z ggregory $
 * @since 2.0
 */
public class ByteOrderMark implements Serializable {

    private static final long serialVersionUID = 1L;

    /** UTF-8 BOM */
    public static final ByteOrderMark UTF_8    = new ByteOrderMark("UTF-8",    0xEF, 0xBB, 0xBF);

    /** UTF-16BE BOM (Big-Endian) */
    public static final ByteOrderMark UTF_16BE = new ByteOrderMark("UTF-16BE", 0xFE, 0xFF);

    /** UTF-16LE BOM (Little-Endian) */
    public static final ByteOrderMark UTF_16LE = new ByteOrderMark("UTF-16LE", 0xFF, 0xFE);

    /**
     * UTF-32BE BOM (Big-Endian)
     * @since 2.2
     */
    public static final ByteOrderMark UTF_32BE = new ByteOrderMark("UTF-32BE", 0x00, 0x00, 0xFE, 0xFF);

    /**
     * UTF-32LE BOM (Little-Endian)
     * @since 2.2
     */
    public static final ByteOrderMark UTF_32LE = new ByteOrderMark("UTF-32LE", 0xFF, 0xFE, 0x00, 0x00);

    /**
     * Unicode BOM character; external form depends on the encoding.
     * @see <a href="http://unicode.org/faq/utf_bom.html#BOM">Byte Order Mark (BOM) FAQ</a>
     * @since 2.5
     */
    public static final char UTF_BOM = '\uFEFF';

    private final String charsetName;
    private final int[] bytes;

    /**
     * Construct a new BOM.
     *
     * @param charsetName The name of the charset the BOM represents
     * @param bytes The BOM's bytes
     * @throws IllegalArgumentException if the charsetName is null or
     * zero length
     * @throws IllegalArgumentException if the bytes are null or zero
     * length
     */
    public ByteOrderMark(final String charsetName, final int... bytes) {
        if (charsetName == null || charsetName.isEmpty()) {
            throw new IllegalArgumentException("No charsetName specified");
        }
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("No bytes specified");
        }
        this.charsetName = charsetName;
        this.bytes = new int[bytes.length];
        System.arraycopy(bytes, 0, this.bytes, 0, bytes.length);
    }

    /**
     * Return the name of the {@link java.nio.charset.Charset} the BOM represents.
     *
     * @return the character set name
     */
    public String getCharsetName() {
        return charsetName;
    }

    /**
     * Return the length of the BOM's bytes.
     *
     * @return the length of the BOM's bytes
     */
    public int length() {
        return bytes.length;
    }

    /**
     * The byte at the specified position.
     *
     * @param pos The position
     * @return The specified byte
     */
    public int get(final int pos) {
        return bytes[pos];
    }

    /**
     * Return a copy of the BOM's bytes.
     *
     * @return a copy of the BOM's bytes
     */
    public byte[] getBytes() {
        final byte[] copy = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            copy[i] = (byte)bytes[i];
        }
        return copy;
    }

    /**
     * Indicates if this BOM's bytes equals another.
     *
     * @param obj The object to compare to
     * @return true if the bom's bytes are equal, otherwise
     * false
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ByteOrderMark)) {
            return false;
        }
        final ByteOrderMark bom = (ByteOrderMark)obj;
        if (bytes.length != bom.length()) {
            return false;
        }
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] != bom.get(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return the hashcode for this BOM.
     *
     * @return the hashcode for this BOM.
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hashCode = getClass().hashCode();
        for (final int b : bytes) {
            hashCode += b;
        }
        return hashCode;
    }

    /**
     * Provide a String representation of the BOM.
     *
     * @return the length of the BOM's bytes
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append('[');
        builder.append(charsetName);
        builder.append(": ");
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append("0x");
            builder.append(Integer.toHexString(0xFF & bytes[i]).toUpperCase());
        }
        builder.append(']');
        return builder.toString();
    }

}
