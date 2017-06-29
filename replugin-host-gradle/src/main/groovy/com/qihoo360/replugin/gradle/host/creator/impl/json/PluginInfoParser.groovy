/*
 * Copyright (C) 2005-2017 Qihoo 360 Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed To in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.qihoo360.replugin.gradle.host.creator.impl.json

import net.dongliu.apk.parser.ApkFile
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler

import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory

/**
 * 从manifest的xml中抽取PluginInfo信息
 * @author RePlugin Team
 */
public class PluginInfoParser extends DefaultHandler {

    private final String ANDROID_NAME = "android:name"
    private final String ANDROID_VALUE = "android:value"

    private final String TAG_NAME = "com.qihoo360.plugin.name"
    private final String TAG_VERSION_LOW = "com.qihoo360.plugin.version.low"
    private final String TAG_VERSION_HIGH = "com.qihoo360.plugin.version.high"
    private final String TAG_VERSION_VER = "com.qihoo360.plugin.version.ver"
    private final String TAG_FRAMEWORK_VER = "com.qihoo360.framework.ver"

    private PluginInfo pluginInfo


    public PluginInfoParser(File pluginFile, def config) {

        pluginInfo = new PluginInfo()

        ApkFile apkFile = new ApkFile(pluginFile)

        String manifestXmlStr = apkFile.getManifestXml()
        ByteArrayInputStream inputStream = new ByteArrayInputStream(manifestXmlStr.getBytes("UTF-8"))

        SAXParserFactory factory = SAXParserFactory.newInstance()
        SAXParser parser = factory.newSAXParser()
        parser.parse(inputStream, this)

        String fullName = pluginFile.name
        pluginInfo.path = config.pluginDir + "/" + fullName

        String postfix = config.pluginFilePostfix
        pluginInfo.name = fullName.substring(0, fullName.length() - postfix.length())
    }


    public PluginInfo getPluginInfo() {
        return pluginInfo;
    }


    @Override
    public void startDocument() throws SAXException {
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        if ("meta-data" == qName) {
            switch (attributes.getValue(ANDROID_NAME)) {
                case TAG_NAME:
                    pluginInfo.name = attributes.getValue(ANDROID_VALUE)
                    break;
                case TAG_VERSION_LOW:
                    pluginInfo.low = new Long(attributes.getValue(ANDROID_VALUE))
                    break;
                case TAG_VERSION_HIGH:
                    pluginInfo.high = new Long(attributes.getValue(ANDROID_VALUE))
                    break;
                case TAG_VERSION_VER:
                    pluginInfo.ver = new Long(attributes.getValue(ANDROID_VALUE))
                    break
                case TAG_FRAMEWORK_VER:
                    pluginInfo.frm = new Long(attributes.getValue(ANDROID_VALUE))
                    break
                default:
                    break
            }
        } else if ("manifest" == qName) {
            pluginInfo.pkg = attributes.getValue("package")
            pluginInfo.ver = new Long(attributes.getValue("android:versionCode"))
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
    }

}
