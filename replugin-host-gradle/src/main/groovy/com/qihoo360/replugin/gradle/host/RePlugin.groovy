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

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.qihoo360.replugin.gradle.host.creator.FileCreators
import com.qihoo360.replugin.gradle.host.creator.IFileCreator
import com.qihoo360.replugin.gradle.host.creator.impl.json.PluginBuiltinJsonCreator
import com.qihoo360.replugin.gradle.host.handlemanifest.ComponentsGenerator
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel

/**
 * @author RePlugin Team
 */
public class Replugin implements Plugin<Project> {

    def static TAG = AppConstant.TAG
    def project
    def config
    def newManifest

    @Override
    public void apply(Project project) {
        println "${TAG} Welcome to replugin world ! "

        this.project = project

        /* Extensions */
        project.extensions.create(AppConstant.USER_CONFIG, RepluginConfig)

        if (project.plugins.hasPlugin(AppPlugin)) {

            addShowPluginTask()

            def android = project.extensions.getByType(AppExtension)
            android.applicationVariants.all { variant ->
                variant.outputs.each { output ->

                    if (config == null) {
                        config = project.extensions.getByName(AppConstant.USER_CONFIG)
                        def appID = variant.generateBuildConfig.appPackageName
                        checkUserConfig(config, appID)
                        newManifest = ComponentsGenerator.generateComponent(appID, config)
                    }

                    output.processResources.doFirst {
                        new FileCreators().init(project, config).create()
                    }

                    output.processManifest.doLast {
                        def manifestPath = output.processManifest.outputFile.absolutePath
                        def updatedContent = new File(manifestPath).getText("UTF-8").replaceAll("</application>", newManifest + "</application>")
                        new File(manifestPath).write(updatedContent, 'UTF-8')
                    }
                }
            }
        }
    }

    // 添加 showPlugins 任务
    def addShowPluginTask() {
        project.task('showPlugins').doLast {
            IFileCreator creator = new PluginBuiltinJsonCreator(project, config)
            def dir = creator.getFileDir()
            // 如果 src 路径不存在，创建之
            if (!dir.exists()) {
                println "$TAG mkdirs ${dir.getAbsolutePath()} : ${dir.mkdirs()}"
            }
            new File(dir, creator.getFileName()).write(creator.getFileContent(), 'UTF-8')
        }
    }

    /**
     * 检查用户配置项
     */
    def checkUserConfig(config, appID) {
/*
        def persistentName = config.persistentName

        if (persistentName == null || persistentName.trim().equals("")) {
            project.logger.log(LogLevel.ERROR, "\n---------------------------------------------------------------------------------")
            project.logger.log(LogLevel.ERROR, " ERROR: persistentName can'te be empty, please set persistentName in replugin. ")
            project.logger.log(LogLevel.ERROR, "---------------------------------------------------------------------------------\n")
            System.exit(0)
            return
        }
*/
        doCheckConfig("countProcess", config.countProcess)
        doCheckConfig("countTranslucentStandard", config.countTranslucentStandard)
        doCheckConfig("countTranslucentSingleTop", config.countTranslucentSingleTop)
        doCheckConfig("countTranslucentSingleTask", config.countTranslucentSingleTask)
        doCheckConfig("countTranslucentSingleInstance", config.countTranslucentSingleInstance)
        doCheckConfig("countNotTranslucentStandard", config.countNotTranslucentStandard)
        doCheckConfig("countNotTranslucentSingleTop", config.countNotTranslucentSingleTop)
        doCheckConfig("countNotTranslucentSingleTask", config.countNotTranslucentSingleTask)
        doCheckConfig("countNotTranslucentSingleInstance", config.countNotTranslucentSingleInstance)
        doCheckConfig("countTask", config.countTask)

        println '--------------------------------------------------------------------------'
        println "${TAG} appID=${appID}"
        println "${TAG} useAppCompat=${config.useAppCompat}"
        // println "${TAG} persistentName=${config.persistentName}"
        println "${TAG} countProcess=${config.countProcess}"

        println "${TAG} countTranslucentStandard=${config.countTranslucentStandard}"
        println "${TAG} countTranslucentSingleTop=${config.countTranslucentSingleTop}"
        println "${TAG} countTranslucentSingleTask=${config.countTranslucentSingleTask}"
        println "${TAG} countTranslucentSingleInstance=${config.countTranslucentSingleInstance}"
        println "${TAG} countNotTranslucentStandard=${config.countNotTranslucentStandard}"
        println "${TAG} countNotTranslucentSingleTop=${config.countNotTranslucentSingleTop}"
        println "${TAG} countNotTranslucentSingleTask=${config.countNotTranslucentSingleTask}"
        println "${TAG} countNotTranslucentSingleInstance=${config.countNotTranslucentSingleInstance}"

        println "${TAG} countTask=${config.countTask}"
        println '--------------------------------------------------------------------------'
    }

    /**
     * 检查配置项是否正确
     * @param name 配置项
     * @param count 配置值
     */
    def doCheckConfig(def name, def count) {
        if (!(count instanceof Integer) || count < 0) {
            this.project.logger.log(LogLevel.ERROR, "\n--------------------------------------------------------")
            this.project.logger.log(LogLevel.ERROR, " ERROR: ${name} must be an positive integer. ")
            this.project.logger.log(LogLevel.ERROR, "--------------------------------------------------------\n")
            System.exit(0)
        }
    }
}

class RepluginConfig {

    /** 自定义进程的数量(除 UI 和 Persistent 进程) */
    def static countProcess = 3

    /** 常驻进程名称（暂未使用）*/
    def static persistentName = ':GuardService'

    /** 背景不透明的坑的数量 */
    def static countNotTranslucentStandard = 6
    def static countNotTranslucentSingleTop = 2
    def static countNotTranslucentSingleTask = 3
    def static countNotTranslucentSingleInstance = 2

    /** 背景透明的坑的数量（每种 launchMode 相同） */
    def static countTranslucentStandard = 2
    def static countTranslucentSingleTop = 2
    def static countTranslucentSingleTask = 2
    def static countTranslucentSingleInstance = 2

    /** 宿主中声明的 TaskAffinity 的组数 */
    def static countTask = 2

    /**
     * 是否使用 AppCompat 库
     * com.android.support:appcompat-v7:25.2.0
     */
    def static useAppCompat = false

    // HOST 向下兼容的插件版本
    def static compatibleVersion = 10

    // HOST 插件版本
    def static currentVersion = 12

    /** plugins-builtin.json 文件名自定义,默认是 "plugins-builtin.json" */
    def static builtInJsonFileName = "plugins-builtin.json"

    /** 是否自动管理 plugins-builtin.json 文件,默认自动管理 */
    def static autoManageBuiltInJsonFile = true

    /** assert目录下放置插件文件的目录自定义,默认是 assert 的 "plugins" */
    def static pluginDir = "plugins"

    /** 插件文件的后缀自定义,默认是".jar" 暂时支持 jar 格式*/
    def static pluginFilePostfix = ".jar"

    /** 当发现插件目录下面有不合法的插件 jar (有可能是特殊定制 jar)时是否停止构建,默认是 true */
    def static enablePluginFileIllegalStopBuild = true
}
