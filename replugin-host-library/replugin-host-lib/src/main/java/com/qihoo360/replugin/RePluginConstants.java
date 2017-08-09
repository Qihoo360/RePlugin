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
 * RePlugin用到的通用的常量集合
 *
 * @author RePlugin Team
 */

public class RePluginConstants {

    /**
     * 表示收到一个新的插件
     * <p>
     * [LocalBroadcast，各进程]
     * <p>
     * 注意：为确保和卫士老插件兼容，请不要修改此值，以免影响插件的运行
     *
     * @see RePlugin#registerInstalledReceiver(android.content.Context, android.content.BroadcastReceiver)
     */
    public static final String ACTION_NEW_PLUGIN = "com.qihoo360.loader2.ACTION_NEW_PLUGIN";

    /**
     * 安装插件的广播
     */
    public static final String ACTION_INSTALL_PLUGIN = "com.qihoo360.replugin.ACTION_INSTALL_PLUGIN";

    /**
     * 启动 Activity 完成的广播
     */
    public static final String ACTION_START_ACTIVITY = "com.qihoo360.replugin.ACTION_START_ACTIVITY";

    /**
     * 插件名称
     */
    public static final String KEY_PLUGIN_NAME = "plugin_name";

    /**
     * 插件安装时的 path
     */
    public static final String KEY_PLUGIN_PATH = "plugin_path";

    /**
     * 插件版本
     */
    public static final String KEY_PLUGIN_VERSION = "plugin_ver";

    /**
     * Activity 名称
     */
    public static final String KEY_PLUGIN_ACTIVITY = "plugin_activity";

    /**
     * 安装插件 Activity 是否成功
     */
    public static final String KEY_INSTALL_PLUGIN_RESULT = "install_plugin_result";

    /**
     * 启动插件 Activity 是否成功
     */
    public static final String KEY_START_ACTIVITY_RESULT = "start_activity_result";

    /**
     * 插件信息
     *
     * @see #ACTION_NEW_PLUGIN
     * @see PluginInfo
     */
    public static final String KEY_PLUGIN_INFO = "plugin_info";

    /**
     * 常驻进程是否需要重启
     *
     * @see #ACTION_NEW_PLUGIN
     * @deprecated 应废弃此Key，或换用更合适的
     */
    public static final String KEY_PERSIST_NEED_RESTART = "persist_need_restart";

    /**
     * 自己进程是否需要重启
     *
     * @see #ACTION_NEW_PLUGIN
     * @deprecated 应废弃此Key，或换用更合适的
     */
    public static final String KEY_SELF_NEED_RESTART = "self_need_restart";
}