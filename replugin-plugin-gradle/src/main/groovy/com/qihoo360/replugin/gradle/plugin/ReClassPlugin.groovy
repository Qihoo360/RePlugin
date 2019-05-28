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
import com.qihoo360.replugin.gradle.compat.VariantCompat
import com.qihoo360.replugin.gradle.plugin.debugger.PluginDebugger
import com.qihoo360.replugin.gradle.plugin.inner.CommonData
import com.qihoo360.replugin.gradle.plugin.inner.ReClassTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author RePlugin Team
 */
public class ReClassPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        println "${AppConstant.TAG} Welcome to replugin world ! "

        /* Extensions */
        project.extensions.create(AppConstant.USER_CONFIG, ReClassConfig)

        def isApp = project.plugins.hasPlugin(AppPlugin)
        if (isApp) {

            def config = project.extensions.getByName(AppConstant.USER_CONFIG)

            def android = project.extensions.getByType(AppExtension)

            def forceStopHostAppTask = null
            def startHostAppTask = null
            def restartHostAppTask = null

            android.applicationVariants.all { variant ->
                PluginDebugger pluginDebugger = new PluginDebugger(project, config, variant)

                def variantData = variant.variantData
                def scope = variantData.scope

                def assembleTask = VariantCompat.getAssembleTask(variant)

                def installPluginTaskName = scope.getTaskName(AppConstant.TASK_INSTALL_PLUGIN, "")
                def installPluginTask = project.task(installPluginTaskName)

                installPluginTask.doLast {
                    pluginDebugger.startHostApp()
                    pluginDebugger.uninstall()
                    pluginDebugger.forceStopHostApp()
                    pluginDebugger.startHostApp()
                    pluginDebugger.install()
                }
                installPluginTask.group = AppConstant.TASKS_GROUP


                def uninstallPluginTaskName = scope.getTaskName(AppConstant.TASK_UNINSTALL_PLUGIN, "")
                def uninstallPluginTask = project.task(uninstallPluginTaskName)

                uninstallPluginTask.doLast {
                    //generate json
                    pluginDebugger.uninstall()
                }
                uninstallPluginTask.group = AppConstant.TASKS_GROUP


                if (null == forceStopHostAppTask) {
                    forceStopHostAppTask = project.task(AppConstant.TASK_FORCE_STOP_HOST_APP)
                    forceStopHostAppTask.doLast {
                        //generate json
                        pluginDebugger.forceStopHostApp()
                    }
                    forceStopHostAppTask.group = AppConstant.TASKS_GROUP
                }

                if (null == startHostAppTask) {
                    startHostAppTask = project.task(AppConstant.TASK_START_HOST_APP)
                    startHostAppTask.doLast {
                        //generate json
                        pluginDebugger.startHostApp()
                    }
                    startHostAppTask.group = AppConstant.TASKS_GROUP
                }

                if (null == restartHostAppTask) {
                    restartHostAppTask = project.task(AppConstant.TASK_RESTART_HOST_APP)
                    restartHostAppTask.doLast {
                        //generate json
                        pluginDebugger.startHostApp()
                    }
                    restartHostAppTask.group = AppConstant.TASKS_GROUP
                    restartHostAppTask.dependsOn(forceStopHostAppTask)
                }


                if (assembleTask) {
                    installPluginTask.dependsOn assembleTask
                }

                def runPluginTaskName = scope.getTaskName(AppConstant.TASK_RUN_PLUGIN, "")
                def runPluginTask = project.task(runPluginTaskName)
                runPluginTask.doLast {
                    pluginDebugger.run()
                }
                runPluginTask.group = AppConstant.TASKS_GROUP

                def installAndRunPluginTaskName = scope.getTaskName(AppConstant.TASK_INSTALL_AND_RUN_PLUGIN, "")
                def installAndRunPluginTask = project.task(installAndRunPluginTaskName)
                installAndRunPluginTask.doLast {
                    pluginDebugger.run()
                }
                installAndRunPluginTask.group = AppConstant.TASKS_GROUP
                installAndRunPluginTask.dependsOn installPluginTask
            }

            CommonData.appPackage = android.defaultConfig.applicationId

            println ">>> APP_PACKAGE " + CommonData.appPackage

            def transform = new ReClassTransform(project)
            // 将 transform 注册到 android
            android.registerTransform(transform)
        }
    }
}

class ReClassConfig {

    /** 编译的 App Module 的名称 */
    def appModule = ':app'

    /** 用户声明要忽略的注入器 */
    def ignoredInjectors = []

    /** 执行 LoaderActivity 替换时，用户声明不需要替换的 Activity */
    def ignoredActivities = []

    /** 自定义的注入器 */
    def customInjectors = []

    /** 插件名字,默认null */
    def pluginName = null

    /** 手机存储目录,默认"/sdcard/" */
    def phoneStorageDir = "/sdcard/"

    /** 宿主包名,默认null */
    def hostApplicationId = null

    /** 宿主launcherActivity,默认null */
    def hostAppLauncherActivity = null
}
