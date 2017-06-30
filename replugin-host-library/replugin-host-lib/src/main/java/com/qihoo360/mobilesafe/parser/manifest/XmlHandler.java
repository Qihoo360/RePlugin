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

package com.qihoo360.mobilesafe.parser.manifest;

import android.content.IntentFilter;
import android.text.TextUtils;

import com.qihoo360.mobilesafe.parser.manifest.bean.ComponentBean;
import com.qihoo360.mobilesafe.parser.manifest.bean.DataBean;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author RePlugin Team
 */
class XmlHandler extends DefaultHandler {

    private ArrayList<ComponentBean> activities;
    private ArrayList<ComponentBean> services;
    private ArrayList<ComponentBean> receivers;

    private String pkg;
    private ComponentBean curComponent;
    private IntentFilter curFilter;
    private List<IntentFilter> filters;
    private List<String> curActions;
    private List<String> curCategories;
    private List<DataBean> curDataBeans;

    public List<ComponentBean> getActivities() {
        return activities;
    }

    public List<ComponentBean> getServices() {
        return services;
    }

    public List<ComponentBean> getReceivers() {
        return receivers;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        switch (qName) {
            case "manifest":
                pkg = attributes.getValue("package");
                break;

            case "activity":
                if (activities == null) {
                    activities = new ArrayList<>();
                }
                curComponent = new ComponentBean();
                filters = new ArrayList<>();
                curComponent.intentFilters = filters;
                curComponent.name = repairAttrName(attributes.getValue("android:name"));
                break;

            case "service":
                if (services == null) {
                    services = new ArrayList<>();
                }
                curComponent = new ComponentBean();
                filters = new ArrayList<>();
                curComponent.intentFilters = filters;
                curComponent.name = repairAttrName(attributes.getValue("android:name"));
                break;

            case "receiver":
                if (receivers == null) {
                    receivers = new ArrayList<>();
                }
                curComponent = new ComponentBean();
                filters = new ArrayList<>();
                curComponent.intentFilters = filters;
                curComponent.name = repairAttrName(attributes.getValue("android:name"));
                break;

            case "intent-filter":
                curFilter = new IntentFilter();
                filters.add(curFilter);
                break;

            case "action":
                if (curActions == null) {
                    curActions = new ArrayList<>();
                }
                curActions.add(attributes.getValue("android:name"));
                break;

            case "category":
                if (curCategories == null) {
                    curCategories = new ArrayList<>();
                }
                curCategories.add(attributes.getValue("android:name"));
                break;

            case "data":
                if (curDataBeans == null) {
                    curDataBeans = new ArrayList<>();
                }
                DataBean bean = new DataBean();
                bean.scheme = attributes.getValue("android:scheme");
                bean.mimeType = attributes.getValue("android:mimeType");
                bean.host = attributes.getValue("android:host");
                bean.port = attributes.getValue("android:port");
                bean.path = attributes.getValue("android:path");
                bean.pathPattern = attributes.getValue("android:pathPattern");
                bean.pathPrefix = attributes.getValue("android:pathPrefix");

                curDataBeans.add(bean);
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);

        switch (qName) {
            case "intent-filter":
                if (curActions != null) {
                    for (String action : curActions) {
                        curFilter.addAction(action);
                    }
                }
                if (curCategories != null) {
                    for (String cate : curCategories) {
                        curFilter.addCategory(cate);
                    }
                }

                if (curDataBeans != null) {
                    for (DataBean bean : curDataBeans) {
                        if (!TextUtils.isEmpty(bean.scheme)) {
                            curFilter.addDataScheme(bean.scheme);
                        }

                        if (!TextUtils.isEmpty(bean.host) && !TextUtils.isEmpty(bean.port)) {
                            curFilter.addDataAuthority(bean.host, bean.port);
                        }

                        if (!TextUtils.isEmpty(bean.path)) {
                            curFilter.addDataPath(bean.path, bean.getPatternMatcherType());
                        }

                        try {
                            if (!TextUtils.isEmpty(bean.mimeType)) {
                                curFilter.addDataType(bean.mimeType);
                            }
                        } catch (IntentFilter.MalformedMimeTypeException e) {
                            e.printStackTrace();
                        }
                    }
                }

                curActions = null;
                curCategories = null;
                curDataBeans = null;
                break;
            case "activity":
                activities.add(curComponent);
                break;
            case "service":
                services.add(curComponent);
                break;
            case "receiver":
                receivers.add(curComponent);
                break;
        }
    }

    /**
     * 如果 android:name 中未包含 pkg，则添加 pkg
     *
     * @param val android:name 属性的值
     * @return 包含 package 的 android:name
     */
    private String repairAttrName(String val) {
        // val 中未包含 pkg
        if (!val.startsWith(".")) {
            return val;
        } else {
            return (pkg + val).intern();
        }
    }
}
