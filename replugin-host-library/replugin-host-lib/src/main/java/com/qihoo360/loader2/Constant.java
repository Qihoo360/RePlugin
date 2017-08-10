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

package com.qihoo360.loader2;

import com.qihoo360.replugin.helper.HostConfigHelper;

/**
 * @author RePlugin Team
 */
public class Constant {

    /**
     * HOST向下兼容版本
     */
    public static final int ADAPTER_COMPATIBLE_VERSION = HostConfigHelper.ADAPTER_COMPATIBLE_VERSION;

    /**
     * HOST版本
     */
    public static final int ADAPTER_CURRENT_VERSION = HostConfigHelper.ADAPTER_CURRENT_VERSION;

    /**
     * 插件存放目录
     */
    public static final String LOCAL_PLUGIN_SUB_DIR = "plugins_v3";

    /**
     * 插件ODEX存放目录
     */
    public static final String LOCAL_PLUGIN_ODEX_SUB_DIR = "plugins_v3_odex";

    /**
     * 插件data存放目录
     */
    public static final String LOCAL_PLUGIN_DATA_SUB_DIR = "plugins_v3_data";

    /**
     * 插件Native（SO库）存放目录
     * Added by Jiongxuan Zhang
     */
    public static final String LOCAL_PLUGIN_DATA_LIB_DIR = "plugins_v3_libs";

    /**
     * "纯APK"插件存放目录
     * Added by Jiongxuan Zhang
     */
    public static final String LOCAL_PLUGIN_APK_SUB_DIR = "p_a";

    /**
     * "纯APK"中释放Odex的目录
     * Added by Jiongxuan Zhang
     */
    public static final String LOCAL_PLUGIN_APK_ODEX_SUB_DIR = "p_od";

    /**
     * 纯"APK"插件的Native（SO库）存放目录
     * Added by Jiongxuan Zhang
     */
    public static final String LOCAL_PLUGIN_APK_LIB_DIR = "p_n";

    /**
     * "纯APK"插件同版本升级时插件、Odex、Native（SO库）的用于覆盖的存放目录
     */
    public static final String LOCAL_PLUGIN_APK_COVER_DIR = "p_c";

    /**
     * 插件extra dex（优化前）释放的以插件名独立隔离的子目录
     * 适用于 android 5.0 以下，5.0以上不会用到该目录
     */
    public static final String LOCAL_PLUGIN_INDEPENDENT_EXTRA_DEX_SUB_DIR = "_ed";

    /**
     * 插件extra dex（优化后）释放的以插件名独立隔离的子目录
     */
    public static final String LOCAL_PLUGIN_INDEPENDENT_EXTRA_ODEX_SUB_DIR = "_eod";

    /**
     * 插件文件名，name-low-high-current.jar
     * 插件文件名规范：barcode-1-10-2.jar
     */
    public static final String LOCAL_PLUGIN_FILE_PATTERN = "^([^-]+)-([0-9]+)-([0-9]+)-([0-9]+).jar$";

    /**
     * 插件加载时的进程锁文件,插件间不共用一把锁
     */
    public static final String LOAD_PLUGIN_LOCK = "plugin_v3_%s.lock";

    /**
     * Stub进程数：不能超过10个
     */
    public static final int STUB_PROCESS_COUNT = 2;

    /**
     * Stub进程后缀
     */
    public static final String STUB_PROCESS_SUFFIX_PATTERN = "^(.*):loader([0-" + (STUB_PROCESS_COUNT - 1) + "])$";

    /**
     *
     */
    public static final String PLUGIN_NAME_UI = "ui";

    /**
     *
     */
    public static final boolean LOG_V5_FILE_SEARCH = false;

    /**
     *
     */
    public static final boolean SIMPLE_QUIT_CONTROLLER = false;

    /**
     *
     */
    public static final boolean ENABLE_PLUGIN_ACTIVITY_AND_BINDER_RUN_IN_MAIN_UI_PROCESS = true;
}
