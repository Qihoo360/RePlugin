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

package com.qihoo360.replugin.gradle.host.creator.impl.java

import com.qihoo360.replugin.gradle.host.creator.IFileCreator

/**
 * @author RePlugin Team
 */
public class RePluginHostConfigCreator implements IFileCreator {

    def static final HOST_CONFIG_PATH = '/com/qihoo360/replugin/gen/'
    def static final HOST_CONFIG_NAME = 'RePluginHostConfig.java'

    def config
    def project
    def variant
    def fileDir
    def fileName

    def RePluginHostConfigCreator(def project, def variant, def cfg) {
        this.project = project
        this.variant = variant;
        this.config = cfg
        //make it generated in buildConfig output dir so that we don't need to hook anything
        File buildConfigGeneratedDir = this.variant.getVariantData().getScope().getBuildConfigSourceOutputDir()
        fileName = HOST_CONFIG_NAME;
        fileDir = new File(buildConfigGeneratedDir, HOST_CONFIG_PATH)
    }

    @Override
    String getFileName() {
        fileName
    }

    @Override
    File getFileDir() {
        fileDir
    }

    @Override
    String getFileContent() {
        return """
package com.qihoo360.replugin.gen;

/**
 * 注意：此文件由插件化框架自动生成，请不要手动修改。
 */
public class RePluginHostConfig {

    // 常驻进程名字
    public static String PERSISTENT_NAME = "${config.persistentName}";

    // 是否使用“常驻进程”（见PERSISTENT_NAME）作为插件的管理进程。若为False，则会使用默认进程
    public static boolean PERSISTENT_ENABLE = ${config.persistentEnable};

    // 背景透明的坑的数量（每种 launchMode 不同）
    public static int ACTIVITY_PIT_COUNT_TS_STANDARD = ${config.countTranslucentStandard};
    public static int ACTIVITY_PIT_COUNT_TS_SINGLE_TOP = ${config.countTranslucentSingleTop};
    public static int ACTIVITY_PIT_COUNT_TS_SINGLE_TASK = ${config.countTranslucentSingleTask};
    public static int ACTIVITY_PIT_COUNT_TS_SINGLE_INSTANCE = ${
            config.countTranslucentSingleInstance
        };

    // 背景不透明的坑的数量（每种 launchMode 不同）
    public static int ACTIVITY_PIT_COUNT_NTS_STANDARD = ${config.countNotTranslucentStandard};
    public static int ACTIVITY_PIT_COUNT_NTS_SINGLE_TOP = ${config.countNotTranslucentSingleTop};
    public static int ACTIVITY_PIT_COUNT_NTS_SINGLE_TASK = ${config.countNotTranslucentSingleTask};
    public static int ACTIVITY_PIT_COUNT_NTS_SINGLE_INSTANCE = ${
            config.countNotTranslucentSingleInstance
        };

    // TaskAffinity 组数
    public static int ACTIVITY_PIT_COUNT_TASK = ${config.countTask};

    // 是否使用 AppCompat 库
    public static boolean ACTIVITY_PIT_USE_APPCOMPAT = ${config.useAppCompat};

    //------------------------------------------------------------
    // 主程序支持的插件版本范围
    //------------------------------------------------------------

    // HOST 向下兼容的插件版本
    public static int ADAPTER_COMPATIBLE_VERSION = ${config.compatibleVersion};

    // HOST 插件版本
    public static int ADAPTER_CURRENT_VERSION = ${config.currentVersion};
}"""
    }
}
