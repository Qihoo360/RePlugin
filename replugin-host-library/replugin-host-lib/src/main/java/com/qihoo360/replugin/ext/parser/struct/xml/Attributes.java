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

package com.qihoo360.replugin.ext.parser.struct.xml;

/**
 * xml node attributes
 *
 * @author dongliu
 */
public class Attributes {

    private final Attribute[] attributes;

    public Attributes(int size) {
        this.attributes = new Attribute[size];
    }

    public void set(int i, Attribute attribute) {
        attributes[i] = attribute;
    }

    public String get(String name) {
        for (Attribute attribute : attributes) {
            if (attribute.getName().equals(name)) {
                return attribute.getValue();
            }
        }
        return null;
    }

    public int size() {
        return attributes.length;
    }

    public boolean getBoolean(String name, boolean b) {
        String value = get(name);
        return value == null ? b : Boolean.parseBoolean(value);
    }

    public Integer getInt(String name) {
        String value = get(name);
        if (value == null) {
            return null;
        }
        if (value.startsWith("0x")) {
            return Integer.valueOf(value.substring(2), 16);
        }
        return Integer.valueOf(value);
    }

    public Long getLong(String name) {
        String value = get(name);
        if (value == null) {
            return null;
        }
        if (value.startsWith("0x")) {
            return Long.valueOf(value.substring(2), 16);
        }
        return Long.valueOf(value);
    }

    public Attribute[] value() {
        return this.attributes;
    }
}

