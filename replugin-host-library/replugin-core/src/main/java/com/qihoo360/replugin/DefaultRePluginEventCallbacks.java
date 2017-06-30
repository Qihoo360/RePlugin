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

package com.qihoo360.replugin;

import android.content.Context;

import com.qihoo360.replugin.model.PluginInfo;

/**
 * 插件化框架对外事件回调接口集
 *
 * 插件框架对外事件回调接口集，其中有一套默认的实现，宿主可直接继承此类，并复写要自定义的接口，来自定义插件化框架的事件逻辑
 * <p>
 * 要了解每个接口的用途，请参见RePluginEventCallbacks接口的说明
 *
 * @author RePlugin Team
 */
public class DefaultRePluginEventCallbacks implements RePluginEventCallbacks {

    private final Context mContext;

    public DefaultRePluginEventCallbacks(Context context) {
        mContext = context;
    }

    @Override
    public void onInstallPluginFailed(String path, InstallResult code) {

    }

    @Override
    public void onInstallPluginSucceed(PluginInfo info) {

    }

    @Override
    public void onStartActivityCompleted(String plugin, String activity, boolean result) {

    }
}
