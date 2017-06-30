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

import com.qihoo360.replugin.model.PluginInfo;

/**
 * 插件化框架对外事件回调接口集
 * <p>
 * 宿主需继承DefaultRePluginEventCallbacks，并复写相应的方法来自定义插件框架
 * <p>
 * 因为经常添加相应方法，故请不要直接实现此接口
 *
 * @author RePlugin Team
 * @see DefaultRePluginEventCallbacks
 */
public interface RePluginEventCallbacks {

    /**
     * 安装插件失败
     *
     * @param path 插件路径
     * @param code 安装失败的原因
     */
    void onInstallPluginFailed(String path, InstallResult code);

    /**
     * 安装插件成功
     *
     * @param info 插件信息
     */
    void onInstallPluginSucceed(PluginInfo info);

    /**
     * 启动 Activity 完成
     *
     * @param plugin   插件名称
     * @param activity 插件 Activity
     * @param result   启动是否成功
     */
    void onStartActivityCompleted(String plugin, String activity, boolean result);

    /**
     * 插件安装结果值
     */
    enum InstallResult {
        SUCCEED,
        V5_FILE_BUILD_FAIL,
        V5_FILE_UPDATE_FAIL,
        READ_PKG_INFO_FAIL,
        VERIFY_SIGN_FAIL,
        VERIFY_VER_FAIL,
        COPY_APK_FAIL
    }
}