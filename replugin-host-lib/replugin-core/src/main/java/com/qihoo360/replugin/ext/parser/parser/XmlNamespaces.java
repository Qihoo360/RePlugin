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

package com.qihoo360.replugin.ext.parser.parser;

import com.qihoo360.replugin.ext.parser.struct.xml.XmlNamespaceEndTag;
import com.qihoo360.replugin.ext.parser.struct.xml.XmlNamespaceStartTag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * the xml file's namespaces.
 *
 * @author dongliu
 */
class XmlNamespaces {

    private List<XmlNamespace> namespaces;

    private List<XmlNamespace> newNamespaces;

    public XmlNamespaces() {
        this.namespaces = new ArrayList<>();
        this.newNamespaces = new ArrayList<>();
    }

    public void addNamespace(XmlNamespaceStartTag tag) {
        XmlNamespace namespace = new XmlNamespace(tag.getPrefix(), tag.getUri());
        namespaces.add(namespace);
        newNamespaces.add(namespace);
    }

    public void removeNamespace(XmlNamespaceEndTag tag) {
        XmlNamespace namespace = new XmlNamespace(tag.getPrefix(), tag.getUri());
        namespaces.remove(namespace);
        newNamespaces.remove(namespace);
    }

    public String getPrefixViaUri(String uri) {
        if (uri == null) {
            return null;
        }
        for (XmlNamespace namespace : namespaces) {
            if (namespace.uri.equals(uri)) {
                return namespace.prefix;
            }
        }
        return null;
    }

    public List<XmlNamespace> consumeNameSpaces() {
        if (!newNamespaces.isEmpty()) {
            List<XmlNamespace> xmlNamespaces = new ArrayList<>();
            xmlNamespaces.addAll(newNamespaces);
            newNamespaces.clear();
            return xmlNamespaces;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * one namespace
     */
    public static class XmlNamespace {
        private String prefix;
        private String uri;

        private XmlNamespace(String prefix, String uri) {
            this.prefix = prefix;
            this.uri = uri;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getUri() {
            return uri;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            XmlNamespace namespace = (XmlNamespace) o;

            if (prefix == null && namespace.prefix != null) {
                return false;
            }
            if (uri == null && namespace.uri != null) {
                return false;
            }
            if (prefix != null && !prefix.equals(namespace.prefix)) {
                return false;
            }
            if (uri != null && !uri.equals(namespace.uri)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = prefix.hashCode();
            result = 31 * result + uri.hashCode();
            return result;
        }
    }
}
