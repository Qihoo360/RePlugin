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

package com.qihoo360.replugin.gradle.host.creator.impl.json

/**
 * 插件信息模型
 * @author RePlugin Team
 */
class PluginInfo {

    /** 插件文件路径 */
    def path
    /** 插件包名 */
    def pkg
    /** 插件名 */
    def name
    /** 插件最低兼容版本 */
    Long low
    /** 插件最高兼容版本 */
    Long high
    /** 插件版本号 */
    Long ver
    /** 框架版本号 */
    Long frm

}
