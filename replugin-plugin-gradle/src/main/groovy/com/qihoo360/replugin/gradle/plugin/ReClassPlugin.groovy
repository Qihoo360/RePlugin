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

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.qihoo360.replugin.gradle.plugin.inner.ReClassTransform
import com.qihoo360.replugin.gradle.plugin.debugger.PluginDebugger
import com.qihoo360.replugin.gradle.plugin.inner.CommonData
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * @author RePlugin Team
 */
public class ReClassPlugin implements Plugin<Project> {

    static Project sProject

    // SDK 目录
    static String sSDKDir

    @Override
    public void apply(Project project) {

        println "${AppConstant.TAG} Welcome to replugin world ! "

        sProject = project

        /* Extensions */
        project.extensions.create(AppConstant.USER_CONFIG, ReClassConfig)

        def isApp = project.plugins.hasPlugin(AppPlugin)
        if (isApp) {

            def config = project.extensions.getByName(AppConstant.USER_CONFIG)

            def apkBuildTask = project.tasks.findByName(config.apkBuildTask)

            PluginDebugger pluginDebugger = new PluginDebugger(project)

            //安装任务（依赖编译任务）
            Task installTask = project.task(AppConstant.TASK_INSTALL_PLUGIN).doLast {
                pluginDebugger.install()
            }.dependsOn(apkBuildTask)
            installTask.group = AppConstant.TASKS_GROUP

            //运行任务
            Task runPlugin = project.task(AppConstant.TASK_RUN_PLUGIN).doLast {
                pluginDebugger.run()
            }
            runPlugin.group = AppConstant.TASKS_GROUP

            //安装并运行任务(依赖安装任务)
            Task installAndRunPlugin = project.task(AppConstant.TASK_INSTALL_AND_RUN_PLUGIN).doLast {
                pluginDebugger.run()
            }.dependsOn(installTask)
            installAndRunPlugin.group = AppConstant.TASKS_GROUP




            def android = project.extensions.getByType(AppExtension)
            sSDKDir = android.sdkDirectory.getAbsolutePath()

            CommonData.appPackage = android.defaultConfig.applicationId
            CommonData.compileSdkVersion = android.compileSdkVersion
            CommonData.buildToolsVersion = android.buildToolsVersion

            println ">>> APP_PACKAGE " + CommonData.appPackage
            println ">>> SDK_DIR " + android.sdkDirectory

            def transform = new ReClassTransform(project)
            // 将 transform 注册到 android
            android.registerTransform(transform)
        }
    }
}

class ReClassConfig {

    /** 编译的 App Module 的名称 */
    def appModule = ':app'

    /** 用户声明的 注入器的名称，如果未声明，则不进行注入操作 */
    def includedInjectors = []

    /** 执行 LoaderActivity 替换时，用户声明不需要替换的 Activity */
    def ignoredActivities = []

    /** 自定义的注入器 */
    def customInjectors = []

    /** 配置你的adb文件绝对路径 */
    def adbFilePath = null

    /** 插件名字,默认null */
    def pluginName = null

    /** 插件生成任务,默认"assembleDebug" */
    def apkBuildTask = "assembleDebug"

    /** 被安装插件apk的后缀,默认"-debug.apk" */
    def apkPostfix = "-debug.apk"

    /** 手机存储目录,默认"/sdcard/" */
    def phoneStorageDir = "/sdcard/"

    /** 宿主包名,默认"com.qihoo360.repluginapp" */
    def hostApplicationId = "com.qihoo360.repluginapp"


}
