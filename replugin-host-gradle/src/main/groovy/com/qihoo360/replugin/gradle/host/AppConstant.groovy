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

package com.qihoo360.replugin.gradle.host

/**
 * 程序常量定义区
 * @author RePlugin Team
 */
class AppConstant {

    /** 版本号 */
    def static final VER = "2.2.4"

    /** 打印信息时候的前缀 */
    def static final TAG = "< replugin-host-v${VER} >"

    /** 外部用户配置信息 */
    def static final USER_CONFIG = "repluginHostConfig"

    /** 用户Task组 */
    def static final TASKS_GROUP = "replugin-plugin"

    /** Task前缀 */
    def static final TASKS_PREFIX = "rp"

    /** 用户Task:安装插件 */
    def static final TASK_SHOW_PLUGIN = TASKS_PREFIX + "ShowPlugins"

    /** 用户Task:Generate任务 */
    def static final TASK_GENERATE = TASKS_PREFIX + "Generate"


    private AppConstant() {}
}
