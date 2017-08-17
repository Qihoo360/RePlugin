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

package com.qihoo360.replugin.helper;

import com.qihoo360.replugin.utils.ReflectUtils;

/**
 * 从宿主的 RePluginHostConfig 中获取一些字段值，
 * RepluginHostConfig 文件由 replugin-host-gradle 自动生成
 *
 * @author RePlugin Team
 */

public class HostConfigHelper {

    private static final String HOST_CONFIG_FILE_PATH = "com.qihoo360.replugin.gen.";
    private static final String HOST_CONFIG_FILE_NAME = "RePluginHostConfig";

    private static Class<?> HOST_CONFIG_CLASS;

    //------------------------------------------------------------
    // RePlugin 坑位默认配置项
    // 注意:以下配置项必须和 replugin-host-gradle 插件中的配置相同
    //------------------------------------------------------------

    // 是否使用“常驻进程”（见PERSISTENT_NAME）作为插件的管理进程
    public static boolean PERSISTENT_ENABLE = true;

    // 常驻进程名
    public static String PERSISTENT_NAME = ":GuardService";

    // 背景透明的坑的数量（每种 launchMode 不同）
    public static int ACTIVITY_PIT_COUNT_TS_STANDARD = 2;
    public static int ACTIVITY_PIT_COUNT_TS_SINGLE_TOP = 2;
    public static int ACTIVITY_PIT_COUNT_TS_SINGLE_TASK = 2;
    public static int ACTIVITY_PIT_COUNT_TS_SINGLE_INSTANCE = 3;

    // 背景不透明的坑的数量（每种 launchMode 不同）
    public static int ACTIVITY_PIT_COUNT_NTS_STANDARD = 6;
    public static int ACTIVITY_PIT_COUNT_NTS_SINGLE_TOP = 2;
    public static int ACTIVITY_PIT_COUNT_NTS_SINGLE_TASK = 3;
    public static int ACTIVITY_PIT_COUNT_NTS_SINGLE_INSTANCE = 2;

    // TaskAffinity 组数
    public static int ACTIVITY_PIT_COUNT_TASK = 2;

    // 是否使用 AppCompat 库
    public static boolean ACTIVITY_PIT_USE_APPCOMPAT = false;

    //------------------------------------------------------------
    // 主程序支持的插件版本范围
    //------------------------------------------------------------

    // HOST 向下兼容的插件版本
    public static int ADAPTER_COMPATIBLE_VERSION = 10;

    // HOST 插件版本
    public static int ADAPTER_CURRENT_VERSION = 12;

    static {

        try {
            HOST_CONFIG_CLASS = ReflectUtils.getClass(HOST_CONFIG_FILE_PATH + HOST_CONFIG_FILE_NAME);
        } catch (ClassNotFoundException e) {
            // Ignore, Just use default value
        }

        try {
            PERSISTENT_ENABLE = readField("PERSISTENT_ENABLE");
        } catch (NoSuchFieldException e) {
            // Ignore, Just use default value
        }

        try {
            PERSISTENT_NAME = readField("PERSISTENT_NAME");
        } catch (NoSuchFieldException e) {
            // Ignore, Just use default value
        }

        try {
            ACTIVITY_PIT_USE_APPCOMPAT = readField("ACTIVITY_PIT_USE_APPCOMPAT");
        } catch (NoSuchFieldException e) {
            // Ignore, Just use default value
        }

        try {
            ACTIVITY_PIT_COUNT_TS_STANDARD = readField("ACTIVITY_PIT_COUNT_TS_STANDARD");
        } catch (NoSuchFieldException e) {
            // Ignore, Just use default value
        }

        try {
            ACTIVITY_PIT_COUNT_TS_SINGLE_TOP = readField("ACTIVITY_PIT_COUNT_TS_SINGLE_TOP");
        } catch (NoSuchFieldException e) {
            // Ignore, Just use default value
        }

        try {
            ACTIVITY_PIT_COUNT_TS_SINGLE_TASK = readField("ACTIVITY_PIT_COUNT_TS_SINGLE_TASK");
        } catch (NoSuchFieldException e) {
            // Ignore, Just use default value
        }

        try {
            ACTIVITY_PIT_COUNT_TS_SINGLE_INSTANCE = readField("ACTIVITY_PIT_COUNT_TS_SINGLE_INSTANCE");
        } catch (NoSuchFieldException e) {
            // Ignore, Just use default value
        }

        try {
            ACTIVITY_PIT_COUNT_NTS_STANDARD = readField("ACTIVITY_PIT_COUNT_NTS_STANDARD");
        } catch (NoSuchFieldException e) {
            // Ignore, Just use default value
        }

        try {
            ACTIVITY_PIT_COUNT_NTS_SINGLE_TOP = readField("ACTIVITY_PIT_COUNT_NTS_SINGLE_TOP");
        } catch (NoSuchFieldException e) {
            // Ignore, Just use default value
        }

        try {
            ACTIVITY_PIT_COUNT_NTS_SINGLE_TASK = readField("ACTIVITY_PIT_COUNT_NTS_SINGLE_TASK");
        } catch (NoSuchFieldException e) {
            // Ignore, Just use default value
        }

        try {
            ACTIVITY_PIT_COUNT_NTS_SINGLE_INSTANCE = readField("ACTIVITY_PIT_COUNT_NTS_SINGLE_INSTANCE");
        } catch (NoSuchFieldException e) {
            // Ignore, Just use default value
        }

        try {
            ACTIVITY_PIT_COUNT_TASK = readField("ACTIVITY_PIT_COUNT_TASK");
        } catch (NoSuchFieldException e) {
            // Ignore, Just use default value
        }

        try {
            ADAPTER_COMPATIBLE_VERSION = readField("ADAPTER_COMPATIBLE_VERSION");
        } catch (NoSuchFieldException e) {
            // Ignore, Just use default value
        }

        try {
            ADAPTER_CURRENT_VERSION = readField("ADAPTER_CURRENT_VERSION");
        } catch (NoSuchFieldException e) {
            // Ignore, Just use default value
        }
    }

    private static <T> T readField(String name) throws NoSuchFieldException {
        try {
            // 就是要强转
            //noinspection unchecked
            return (T) ReflectUtils.readStaticField(HOST_CONFIG_CLASS, name);
        } catch (IllegalAccessException e) {
            // 此Field可能为非Public权限，不过由于我们做了Accessible处理，可能性非常低
            // NOTE 因为类型转换发生在readField返回值之后才做，故“ClassCastException”只会出现在static方法块内
            // NOTE 故在此处做Catch ClassCastException是无效的
            throw new IllegalStateException(e);
        }

        // NOTE 不需要Catch NoSuchFieldException，因为只要此Field找不到就抛，符合预期
    }

    public static void init() {
        // Nothing, Just init on "static" block
    }
}
