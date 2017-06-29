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

package com.qihoo360.replugin.gradle.plugin.debugger

import com.qihoo360.replugin.gradle.plugin.AppConstant
import com.qihoo360.replugin.gradle.plugin.util.CmdUtil
import groovy.io.FileType
import org.gradle.api.Project

/**
 * @author RePlugin Team
 */
class PluginDebugger {

    def Project project
    def config

    public PluginDebugger(Project project) {
        this.project = project
        config = project.extensions.getByName(AppConstant.USER_CONFIG)



    }

    /**
     * 安装插件
     * @return 是否命令执行成功
     */
    public boolean install() {

        if (isConfigNull(config)) {
            return
        }

        String buildDir = project.buildDir.absolutePath.toString()

        String apkDir = buildDir + File.separator + "outputs" + File.separator + "apk"

        File apkDirFile = new File(apkDir)

        //检查adb环境
        if (null == config.adbFilePath){
            System.err.println "${AppConstant.TAG} please config the adbFilePath !!!"
        }

        //找到要安装的xxx.apk
        String debugApk = null
        apkDirFile.eachFileMatch(FileType.FILES, ~/.*\${config.apkPostfix}/) {
            debugApk = it.absolutePath.toString()
        }
        if (null == debugApk) {
            System.err.println "${AppConstant.TAG} Could not find the available debug apk !!!"
            return false
        }

        //推送xxx.apk到手机
        String pushCmd = "${config.adbFilePath} push ${debugApk} ${config.phoneStorageDir}"
        if (0 != CmdUtil.syncExecute(pushCmd)) {
            return false
        }

        //发送安装广播
        String installBrCmd = "${config.adbFilePath} shell am broadcast -a ${config.hostApplicationId}.replugin.install -e path ${config.phoneStorageDir}app-debug.apk -e immediately true "
        if (0 != CmdUtil.syncExecute(installBrCmd)) {
            return false
        }

        return true
    }

    /**
     * 运行插件
     * @return 是否命令执行成功
     */
    public boolean run() {

        if (isConfigNull(config)) {
            return
        }

        if (null == config.pluginName) {
            System.err.println "${AppConstant.TAG} you must to config the pluginName !!!"
            return false
        }

        //发送运行广播
        // adb shell am broadcast -a com.qihoo360.repluginapp.replugin.start_activity -e plugin [Name] -e activity [Class]
        String installBrCmd = "${config.adbFilePath} shell am broadcast -a ${config.hostApplicationId}.replugin.start_activity -e plugin ${config.pluginName}"
        if (0 != CmdUtil.syncExecute(installBrCmd)) {
            return false
        }
    }

    /**
     * 检查用户配置项是否为空
     * @param config
     * @return
     */
    private boolean isConfigNull(def config) {
        if (null == config) {
            System.err.println "${AppConstant.TAG} the config object can not be null!!!"
            return false
        }
    }


}
