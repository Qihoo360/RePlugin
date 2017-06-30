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

package com.qihoo360.replugin.ext.parser.struct;

/**
 * String pool chunk header.
 *
 * @author dongliu
 */
public class StringPoolHeader extends ChunkHeader {
    // If set, the string index is sorted by the string values (based on strcmp16()).
    public static final int SORTED_FLAG = 1;
    // String pool is encoded in UTF-8
    public static final int UTF8_FLAG = 1 << 8;
    // Number of style span arrays in the pool (number of uint32_t indices
    // follow the string indices).
    private long stringCount;
    // Number of style span arrays in the pool (number of uint32_t indices
    // follow the string indices).
    private long styleCount;
    private long flags;
    // Index from header of the string data.
    private long stringsStart;
    // Index from header of the style data.
    private long stylesStart;
    public StringPoolHeader(int chunkType, int headerSize, long chunkSize) {
        super(chunkType, headerSize, chunkSize);
    }

    public long getStringCount() {
        return stringCount;
    }

    public void setStringCount(long stringCount) {
        this.stringCount = stringCount;
    }

    public long getStyleCount() {
        return styleCount;
    }

    public void setStyleCount(long styleCount) {
        this.styleCount = styleCount;
    }

    public long getFlags() {
        return flags;
    }

    public void setFlags(long flags) {
        this.flags = flags;
    }

    public long getStringsStart() {
        return stringsStart;
    }

    public void setStringsStart(long stringsStart) {
        this.stringsStart = stringsStart;
    }

    public long getStylesStart() {
        return stylesStart;
    }

    public void setStylesStart(long stylesStart) {
        this.stylesStart = stylesStart;
    }
}
