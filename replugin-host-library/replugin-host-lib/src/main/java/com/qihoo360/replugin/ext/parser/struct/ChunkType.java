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
 * Resource type
 * see https://github.com/android/platform_frameworks_base/blob/master/include/androidfw/ResourceTypes.h
 *
 * @author dongliu
 */
public class ChunkType {
    public static final int NULL = 0x0000;
    public static final int STRING_POOL = 0x0001;
    public static final int TABLE = 0x0002;
    public static final int XML = 0x0003;

    // Chunk types in XML
    public static final int XML_FIRST_CHUNK = 0x0100;
    public static final int XML_START_NAMESPACE = 0x0100;
    public static final int XML_END_NAMESPACE = 0x0101;
    public static final int XML_START_ELEMENT = 0x0102;
    public static final int XML_END_ELEMENT = 0x0103;
    public static final int XML_CDATA = 0x0104;
    public static final int XML_LAST_CHUNK = 0x017f;
    // This contains a uint32_t array mapping strings in the string
    // pool back to resource identifiers.  It is optional.
    public static final int XML_RESOURCE_MAP = 0x0180;

    // Chunk types in RES_TABLE_TYPE
    public static final int TABLE_PACKAGE = 0x0200;
    public static final int TABLE_TYPE = 0x0201;
    public static final int TABLE_TYPE_SPEC = 0x0202;
    // android5.0+
    // DynamicRefTable
    public static final int TABLE_LIBRARY = 0x0203;
}
