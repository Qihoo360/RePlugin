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
 *
 */

package com.qihoo360.replugin.gradle.plugin

/**
 * @author RePlugin Team
 */
class AppConstant {

    /** 版本号 */
    def static final VER = "2.3.0"


    /** 打印信息时候的前缀 */
    def static final TAG = "< replugin-plugin-v${VER} >"

    /** 外部用户配置信息 */
    def static final USER_CONFIG = "repluginPluginConfig"

    /** 用户Task组 */
    def static final TASKS_GROUP = "replugin-plugin"

    /** Task前缀 */
    def static final TASKS_PREFIX = "rp"


    /** 用户Task:强制停止宿主app */
    def static final TASK_FORCE_STOP_HOST_APP = TASKS_PREFIX + "ForceStopHostApp"

    /** 用户Task:启动宿主app */
    def static final TASK_START_HOST_APP = TASKS_PREFIX + "StartHostApp"

    /** 用户Task:重启宿主app */
    def static final TASK_RESTART_HOST_APP = TASKS_PREFIX + "RestartHostApp"


    /** 用户Task:安装插件 */
    def static final TASK_INSTALL_PLUGIN = TASKS_PREFIX + "InstallPlugin"

    /** 用户Task:安装插件 */
    def static final TASK_UNINSTALL_PLUGIN = TASKS_PREFIX + "UninstallPlugin"

    /** 用户Task:运行插件 */
    def static final TASK_RUN_PLUGIN = TASKS_PREFIX + "RunPlugin"

    /** 用户Task:安装并运行插件 */
    def static final TASK_INSTALL_AND_RUN_PLUGIN = TASKS_PREFIX + "InstallAndRunPlugin"


    /** 配置例子 */
    static final String CONFIG_EXAMPLE = '''
// 这个plugin需要放在android配置之后，因为需要读取android中的配置项
apply plugin: 'replugin-plugin-gradle\'
repluginPluginConfig {
    pluginName = "demo3"
    hostApplicationId = "com.qihoo360.replugin.sample.host"
    hostAppLauncherActivity = "com.qihoo360.replugin.sample.host.MainActivity"
}
'''

    private AppConstant() {}
}
