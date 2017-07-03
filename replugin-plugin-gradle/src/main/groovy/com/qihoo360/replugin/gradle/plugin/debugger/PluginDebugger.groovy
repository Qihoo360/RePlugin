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
import org.gradle.api.Project

/**
 * @author RePlugin Team
 */
class PluginDebugger {

    def project
    def config
    def variant
    File apkFile
    File adbFile

    public PluginDebugger(Project project, def config, def variant) {
        this.project = project
        this.config = config
        this.variant = variant
        def variantData = this.variant.variantData
        def scope = variantData.scope
        def globalScope = scope.globalScope
        def variantConfiguration = variantData.variantConfiguration
        String archivesBaseName = globalScope.getArchivesBaseName();
        String apkBaseName = archivesBaseName + "-" + variantConfiguration.getBaseName()

        File apkDir = new File(globalScope.getBuildDir(), "outputs/apk")

        String unsigned = (variantConfiguration.getSigningConfig() == null
                ? "-unsigned.apk"
                : ".apk");
        String apkName = apkBaseName + unsigned

        apkFile = new File(apkDir, apkName)

        adbFile = globalScope.androidBuilder.sdkInfo.adb;
    }

    /**
     * 安装插件
     * @return 是否命令执行成功
     */
    public boolean install() {

        if (isConfigNull(config)) {
            return false
        }

        //检查adb环境
        if (null == adbFile || !adbFile.exists()) {
            System.err.println "${AppConstant.TAG} Could not find the adb file !!!"
        }

        //检查apk文件是否存在
        if (null == apkFile || !apkFile.exists()) {
            System.err.println "${AppConstant.TAG} Could not find the available apk !!!"
            return false
        }

        //推送apk文件到手机
        String pushCmd = "${adbFile.absolutePath} push ${apkFile.absolutePath} ${config.phoneStorageDir}"
        if (0 != CmdUtil.syncExecute(pushCmd)) {
            return false
        }

        File destStorageFile = new File(config.phoneStorageDir, apkFile.name)
        //发送安装广播
        String installBrCmd = "${adbFile.absolutePath} shell am broadcast -a ${config.hostApplicationId}.replugin.install -e path ${destStorageFile.absolutePath} -e immediately true "
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
            return false
        }

        if (null == config.pluginName) {
            System.err.println "${AppConstant.TAG} you must to config the pluginName !!!"
            return false
        }

        //发送运行广播
        // adb shell am broadcast -a com.qihoo360.repluginapp.replugin.start_activity -e plugin [Name] -e activity [Class]
        String installBrCmd = "${adbFile.absolutePath} shell am broadcast -a ${config.hostApplicationId}.replugin.start_activity -e plugin ${config.pluginName}"
        if (0 != CmdUtil.syncExecute(installBrCmd)) {
            return false
        }
    }

    /**
     * 检查用户配置项是否为空
     * @param config
     * @return
     */
    private static boolean isConfigNull(def config) {
        if (null == config) {
            System.err.println "${AppConstant.TAG} the config object can not be null!!!"
            return true
        }
        return false
    }


}
