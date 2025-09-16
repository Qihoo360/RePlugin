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

package com.qihoo360.replugin.gradle.matrix

/**
 * Matrix Gradle Plugin Constants
 * @author RePlugin Team
 */
class AppConstant {

    /** 版本号 */
    def static final VER = "1.0.0"

    /** 打印信息时候的前缀 */
    def static final TAG = "< replugin-matrix-v${VER} >"

    /** 外部用户配置信息 */
    def static final USER_CONFIG = "repluginMatrixConfig"

    /** 用户Task组 */
    def static final TASKS_GROUP = "replugin-matrix"

    /** Task前缀 */
    def static final TASKS_PREFIX = "rpMatrix"

    /** 用户Task:初始化Matrix配置 */
    def static final TASK_INIT_MATRIX = TASKS_PREFIX + "Init"

    /** 用户Task:生成Matrix配置 */
    def static final TASK_GENERATE_MATRIX_CONFIG = TASKS_PREFIX + "GenerateConfig"

    /** 配置例子 */
    static final String CONFIG_EXAMPLE = '''
// Matrix APM integration for RePlugin
apply plugin: 'replugin-matrix-gradle'
repluginMatrixConfig {
    enable = true
    // APM components
    apm {
        ioCanary = true
        batteryCanary = true
        sqliteCanary = true
        memoryCanary = true
    }
    // Trace components
    trace {
        enable = true
        baseMethodMapFile = "app/mapping.txt"
        blackListFile = "blackMethodList.txt"
    }
}
'''

    private AppConstant() {}
}