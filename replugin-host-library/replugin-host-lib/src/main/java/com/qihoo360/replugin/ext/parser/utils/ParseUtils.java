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

package com.qihoo360.replugin.ext.parser.utils;

import com.qihoo360.replugin.ext.parser.parser.StringPoolEntry;
import com.qihoo360.replugin.ext.parser.struct.ResourceValue;
import com.qihoo360.replugin.ext.parser.struct.StringPool;
import com.qihoo360.replugin.ext.parser.struct.StringPoolHeader;
import com.qihoo360.replugin.ext.parser.exception.ParserException;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author dongliu
 */
public class ParseUtils {

    public static Charset charsetUTF8 = Charset.forName("UTF-8");

    /**
     * read string from input buffer. if get EOF before read enough data, throw IOException.
     */
    public static String readString(ByteBuffer buffer, boolean utf8) {
        if (utf8) {
            //  The lengths are encoded in the same way as for the 16-bit format
            // but using 8-bit rather than 16-bit integers.
            int strLen = readLen(buffer);
            int bytesLen = readLen(buffer);
            byte[] bytes = Buffers.readBytes(buffer, bytesLen);
            String str = new String(bytes, charsetUTF8);
            // zero
            int trailling = Buffers.readUByte(buffer);
            return str;
        } else {
            // The length is encoded as either one or two 16-bit integers as per the commentRef...
            int strLen = readLen16(buffer);
            String str = Buffers.readString(buffer, strLen);
            // zero
            int trailling = Buffers.readUShort(buffer);
            return str;
        }
    }

    /**
     * read encoding len.
     * see StringPool.cpp ENCODE_LENGTH
     */
    private static int readLen(ByteBuffer buffer) {
        int len = 0;
        int i = Buffers.readUByte(buffer);
        if ((i & 0x80) != 0) {
            //read one more byte.
            len |= (i & 0x7f) << 7;
            len += Buffers.readUByte(buffer);
        } else {
            len = i;
        }
        return len;
    }

    /**
     * read encoding len.
     * see Stringpool.cpp ENCODE_LENGTH
     */
    private static int readLen16(ByteBuffer buffer) {
        int len = 0;
        int i = Buffers.readUShort(buffer);
        if ((i & 0x8000) != 0) {
            len |= (i & 0x7fff) << 15;
            len += Buffers.readUShort(buffer);
        } else {
            len = i;
        }
        return len;
    }


    /**
     * read String pool, for apk binary xml file and resource table.
     */
    public static StringPool readStringPool(ByteBuffer buffer, StringPoolHeader stringPoolHeader) {

        long beginPos = buffer.position();
        long[] offsets = new long[(int) stringPoolHeader.getStringCount()];
        // read strings offset
        if (stringPoolHeader.getStringCount() > 0) {
            for (int idx = 0; idx < stringPoolHeader.getStringCount(); idx++) {
                offsets[idx] = Buffers.readUInt(buffer);
            }
        }
        // read flag
        // the string index is sorted by the string values if true
        boolean sorted = (stringPoolHeader.getFlags() & StringPoolHeader.SORTED_FLAG) != 0;
        // string use utf-8 format if true, otherwise utf-16
        boolean utf8 = (stringPoolHeader.getFlags() & StringPoolHeader.UTF8_FLAG) != 0;

        // read strings. the head and metas have 28 bytes
        long stringPos = beginPos + stringPoolHeader.getStringsStart() - stringPoolHeader.getHeaderSize();
        buffer.position((int) stringPos);

        StringPoolEntry[] entries = new StringPoolEntry[offsets.length];
        for (int i = 0; i < offsets.length; i++) {
            entries[i] = new StringPoolEntry(i, stringPos + offsets[i]);
        }

        String lastStr = null;
        long lastOffset = -1;
        StringPool stringPool = new StringPool((int) stringPoolHeader.getStringCount());
        for (StringPoolEntry entry : entries) {
            if (entry.getOffset() == lastOffset) {
                stringPool.set(entry.getIdx(), lastStr);
                continue;
            }

            buffer.position((int) entry.getOffset());
            lastOffset = entry.getOffset();
            String str = ParseUtils.readString(buffer, utf8);
            lastStr = str;
            stringPool.set(entry.getIdx(), str);
        }

        // read styles
        if (stringPoolHeader.getStyleCount() > 0) {
            // now we just skip it
        }

        buffer.position((int) (beginPos + stringPoolHeader.getBodySize()));

        return stringPool;
    }

    /**
     * read res value, convert from different types to string.
     */
    public static ResourceValue readResValue(ByteBuffer buffer, StringPool stringPool) {
//        ResValue resValue = new ResValue();
        int size = Buffers.readUShort(buffer);
        short res0 = Buffers.readUByte(buffer);
        short dataType = Buffers.readUByte(buffer);
        return ResourceValue.raw(buffer.getInt(), dataType);
    }

    public static void checkChunkType(int expected, int real) {
        if (expected != real) {
            throw new ParserException("Expect chunk type:" + Integer.toHexString(expected)
                    + ", but got:" + Integer.toHexString(real));
        }
    }
}
