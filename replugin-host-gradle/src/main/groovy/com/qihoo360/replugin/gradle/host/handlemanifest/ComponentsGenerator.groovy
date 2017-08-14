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

package com.qihoo360.replugin.gradle.host.handlemanifest

import groovy.xml.MarkupBuilder

/**
 * @author RePlugin Team
 */
class ComponentsGenerator {

    def static final infix = 'loader.a.Activity'

    def static final name = 'android:name'
    def static final process = 'android:process'
    def static final task = 'android:taskAffinity'
    def static final launchMode = 'android:launchMode'
    def static final authorities = 'android:authorities'
    def static final multiprocess = 'android:multiprocess'

    def static final cfg = 'android:configChanges'
    def static final cfgV = 'keyboard|keyboardHidden|orientation|screenSize'

    def static final exp = 'android:exported'
    def static final expV = 'false'

    def static final ori = 'android:screenOrientation'
    def static final oriV = 'portrait'

    def static final theme = 'android:theme'
    def static final themeTS = '@android:style/Theme.Translucent.NoTitleBar'

    def static final THEME_NTS_USE_APP_COMPAT = '@style/Theme.AppCompat'
    def static final THEME_NTS_NOT_USE_APP_COMPAT = '@android:style/Theme.NoTitleBar'
    def static themeNTS = THEME_NTS_NOT_USE_APP_COMPAT

    /**
     * 动态生成插件化框架中需要的组件
     *
     * @param applicationID 宿主的 applicationID
     * @param config 用户配置
     * @return String       插件化框架中需要的组件
     */
    def static generateComponent(def applicationID, def config) {
        // 是否使用 AppCompat 库（涉及到默认主题）
        if (config.useAppCompat) {
            themeNTS = THEME_NTS_USE_APP_COMPAT
        } else {
            themeNTS = THEME_NTS_NOT_USE_APP_COMPAT
        }

        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)

        /* UI 进程 */
        xml.application {

            /* 需要编译期动态修改进程名的组件*/

            String pluginMgrProcessName = config.persistentEnable ? config.persistentName : applicationID

            // 常驻进程Provider
            provider(
                    "${name}":"com.qihoo360.replugin.component.process.ProcessPitProviderPersist",
                    "${authorities}":"${applicationID}.loader.p.main",
                    "${exp}":"false",
                    "${process}":"${pluginMgrProcessName}")

            provider(
                    "${name}":"com.qihoo360.replugin.component.provider.PluginPitProviderPersist",
                    "${authorities}":"${applicationID}.Plugin.NP.PSP",
                    "${exp}":"false",
                    "${process}":"${pluginMgrProcessName}")

            // ServiceManager 服务框架
            provider(
                    "${name}":"com.qihoo360.mobilesafe.svcmanager.ServiceProvider",
                    "${authorities}":"${applicationID}.svcmanager",
                    "${exp}":"false",
                    "${multiprocess}":"false",
                    "${process}":"${pluginMgrProcessName}")

            service(
                    "${name}":"com.qihoo360.replugin.component.service.server.PluginPitServiceGuard",
                    "${process}":"${pluginMgrProcessName}")

            /* 透明坑 */
            config.countTranslucentStandard.times {
                activity(
                        "${name}": "${applicationID}.${infix}N1NRTS${it}",
                        "${cfg}": "${cfgV}",
                        "${exp}": "${expV}",
                        "${ori}": "${oriV}",
                        "${theme}": "${themeTS}")
            }
            config.countTranslucentSingleTop.times {
                activity(
                        "${name}": "${applicationID}.${infix}N1STPTS${it}",
                        "${cfg}": "${cfgV}",
                        "${exp}": "${expV}",
                        "${ori}": "${oriV}",
                        "${theme}": "${themeTS}",
                        "${launchMode}": "singleTop")
            }
            config.countTranslucentSingleTask.times {
                activity(
                        "${name}": "${applicationID}.${infix}N1STTS${it}",
                        "${cfg}": "${cfgV}",
                        "${exp}": "${expV}",
                        "${ori}": "${oriV}",
                        "${theme}": "${themeTS}",
                        "${launchMode}": "singleTask")
            }
            config.countTranslucentSingleInstance.times {
                activity(
                        "${name}": "${applicationID}.${infix}N1SITS${it}",
                        "${cfg}": "${cfgV}",
                        "${exp}": "${expV}",
                        "${ori}": "${oriV}",
                        "${theme}": "${themeTS}",
                        "${launchMode}": "singleInstance")
            }

            /* 不透明坑 */
            config.countNotTranslucentStandard.times {
                activity(
                        "${name}": "${applicationID}.${infix}N1NRNTS${it}",
                        "${cfg}": "${cfgV}",
                        "${exp}": "${expV}",
                        "${ori}": "${oriV}",
                        "${theme}": "${themeNTS}")
            }
            config.countNotTranslucentSingleTop.times {
                activity(
                        "${name}": "${applicationID}.${infix}N1STPNTS${it}",
                        "${cfg}": "${cfgV}",
                        "${exp}": "${expV}",
                        "${ori}": "${oriV}",
                        "${theme}": "${themeNTS}",
                        "${launchMode}": "singleTop")
            }
            config.countNotTranslucentSingleTask.times {
                activity(
                        "${name}": "${applicationID}.${infix}N1STNTS${it}",
                        "${cfg}": "${cfgV}",
                        "${exp}": "${expV}",
                        "${ori}": "${oriV}",
                        "${theme}": "${themeNTS}",
                        "${launchMode}": "singleTask",)
            }
            config.countNotTranslucentSingleInstance.times {
                activity(
                        "${name}": "${applicationID}.${infix}N1SINTS${it}",
                        "${cfg}": "${cfgV}",
                        "${exp}": "${expV}",
                        "${ori}": "${oriV}",
                        "${theme}": "${themeNTS}",
                        "${launchMode}": "singleInstance")
            }

            /* TaskAffinity */
            // N1TA0NRTS1：UI进程->第0组->standardMode->透明主题->第1个坑位 (T: Task, NR: Standard, TS: Translucent)
            config.countTask.times { i ->
                config.countTranslucentStandard.times { j ->
                    activity(
                            "${name}": "${applicationID}.${infix}N1TA${i}NRTS${j}",
                            "${cfg}": "${cfgV}",
                            "${exp}": "${expV}",
                            "${ori}": "${oriV}",
                            "${theme}": "${themeTS}",
                            "${task}": ":t${i}")
                }
                config.countTranslucentSingleTop.times { j ->
                    activity(
                            "${name}": "${applicationID}.${infix}N1TA${i}STPTS${j}",
                            "${cfg}": "${cfgV}",
                            "${exp}": "${expV}",
                            "${ori}": "${oriV}",
                            "${theme}": "${themeTS}",
                            "${task}": ":t${i}",
                            "${launchMode}": "singleTop")
                }
                config.countTranslucentSingleTask.times { j ->
                    activity(
                            "${name}": "${applicationID}.${infix}N1TA${i}STTS${j}",
                            "${cfg}": "${cfgV}",
                            "${exp}": "${expV}",
                            "${ori}": "${oriV}",
                            "${theme}": "${themeTS}",
                            "${task}": ":t${i}",
                            "${launchMode}": "singleTask")
                }

                config.countNotTranslucentStandard.times { j ->
                    activity(
                            "${name}": "${applicationID}.${infix}N1TA${i}NRNTS${j}",
                            "${cfg}": "${cfgV}",
                            "${exp}": "${expV}",
                            "${ori}": "${oriV}",
                            "${theme}": "${themeNTS}",
                            "${task}": ":t${i}")
                }
                config.countNotTranslucentSingleTop.times { j ->
                    activity(
                            "${name}": "${applicationID}.${infix}N1TA${i}STPNTS${j}",
                            "${cfg}": "${cfgV}",
                            "${exp}": "${expV}",
                            "${ori}": "${oriV}",
                            "${theme}": "${themeNTS}",
                            "${task}": ":t${i}",
                            "${launchMode}": "singleTop")
                }
                config.countNotTranslucentSingleTask.times { j ->
                    activity(
                            "${name}": "${applicationID}.${infix}N1TA${i}STNTS${j}",
                            "${cfg}": "${cfgV}",
                            "${exp}": "${expV}",
                            "${ori}": "${oriV}",
                            "${theme}": "${themeNTS}",
                            "${task}": ":t${i}",
                            "${launchMode}": "singleTask")
                }
            }
        }
        // 删除 application 标签
        def normalStr = writer.toString().replace("<application>", "").replace("</application>", "")

        // 将单进程和多进程的组件相加
        normalStr + generateMultiProcessComponent(applicationID, config)
    }

    /**
     * 生成多进程坑位配置
     */
    def static generateMultiProcessComponent(def applicationID, def config) {
        if (config.countProcess == 0) {
            return ''
        }

        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)

        /* 自定义进程 */
        xml.application {
            config.countProcess.times { p ->
                config.countTranslucentStandard.times {
                    activity(
                            "${name}": "${applicationID}.${infix}P${p}NRTS${it}",
                            "${cfg}": "${cfgV}",
                            "${exp}": "${expV}",
                            "${ori}": "${oriV}",
                            "${theme}": "${themeTS}",
                            "${process}": ":p${p}")
                }
                config.countTranslucentSingleTop.times {
                    activity(
                            "${name}": "${applicationID}.${infix}P${p}STPTS${it}",
                            "${cfg}": "${cfgV}",
                            "${exp}": "${expV}",
                            "${ori}": "${oriV}",
                            "${theme}": "${themeTS}",
                            "${process}": ":p${p}",
                            "${launchMode}": "singleTop")
                }
                config.countTranslucentSingleTask.times {
                    activity(
                            "${name}": "${applicationID}.${infix}P${p}STTS${it}",
                            "${cfg}": "${cfgV}",
                            "${exp}": "${expV}",
                            "${ori}": "${oriV}",
                            "${theme}": "${themeTS}",
                            "${process}": ":p${p}",
                            "${launchMode}": "singleTask")
                }
                config.countTranslucentSingleInstance.times {
                    activity(
                            "${name}": "${applicationID}.${infix}P${p}SITS${it}",
                            "${cfg}": "${cfgV}",
                            "${exp}": "${expV}",
                            "${ori}": "${oriV}",
                            "${theme}": "${themeTS}",
                            "${process}": ":p${p}",
                            "${launchMode}": "singleInstance")
                }
                config.countNotTranslucentStandard.times {
                    activity(
                            "${name}": "${applicationID}.${infix}P${p}NRNTS${it}",
                            "${cfg}": "${cfgV}",
                            "${exp}": "${expV}",
                            "${ori}": "${oriV}",
                            "${theme}": "${themeNTS}",
                            "${process}": ":p${p}")
                }
                config.countNotTranslucentSingleTop.times {
                    activity(
                            "${name}": "${applicationID}.${infix}P${p}STPNTS${it}",
                            "${cfg}": "${cfgV}",
                            "${exp}": "${expV}",
                            "${ori}": "${oriV}",
                            "${theme}": "${themeNTS}",
                            "${process}": ":p${p}",
                            "${launchMode}": "singleTop")
                }
                config.countNotTranslucentSingleTask.times {
                    activity(
                            "${name}": "${applicationID}.${infix}P${p}STNTS${it}",
                            "${cfg}": "${cfgV}",
                            "${exp}": "${expV}",
                            "${ori}": "${oriV}",
                            "${theme}": "${themeNTS}",
                            "${process}": ":p${p}",
                            "${launchMode}": "singleTask")
                }
                config.countNotTranslucentSingleInstance.times {
                    activity(
                            "${name}": "${applicationID}.${infix}P${p}SINTS${it}",
                            "${cfg}": "${cfgV}",
                            "${exp}": "${expV}",
                            "${ori}": "${oriV}",
                            "${theme}": "${themeNTS}",
                            "${process}": ":p${p}",
                            "${launchMode}": "singleInstance")
                }

                /* TaskAffinity */
                config.countTask.times { i ->
                    config.countTranslucentStandard.times { j ->
                        activity(
                                "${name}": "${applicationID}.${infix}P${p}TA${i}NRTS${j}",
                                "${cfg}": "${cfgV}",
                                "${exp}": "${expV}",
                                "${ori}": "${oriV}",
                                "${theme}": "${themeTS}",
                                "${process}": ":p${p}",
                                "${task}": ":t${i}")
                    }
                    config.countTranslucentSingleTop.times { j ->
                        activity(
                                "${name}": "${applicationID}.${infix}P${p}TA${i}STPTS${j}",
                                "${cfg}": "${cfgV}",
                                "${exp}": "${expV}",
                                "${ori}": "${oriV}",
                                "${theme}": "${themeTS}",
                                "${launchMode}": "singleTop",
                                "${process}": ":p${p}",
                                "${task}": ":t${i}")
                    }
                    config.countTranslucentSingleTask.times { j ->
                        activity(
                                "${name}": "${applicationID}.${infix}P${p}TA${i}STTS${j}",
                                "${cfg}": "${cfgV}",
                                "${exp}": "${expV}",
                                "${ori}": "${oriV}",
                                "${theme}": "${themeTS}",
                                "${launchMode}": "singleTask",
                                "${process}": ":p${p}",
                                "${task}": ":t${i}")
                    }
                    config.countNotTranslucentStandard.times { j ->
                        activity(
                                "${name}": "${applicationID}.${infix}P${p}TA${i}NRNTS${j}",
                                "${cfg}": "${cfgV}",
                                "${exp}": "${expV}",
                                "${ori}": "${oriV}",
                                "${theme}": "${themeNTS}",
                                "${process}": ":p${p}",
                                "${task}": ":t${i}")
                    }
                    config.countNotTranslucentSingleTop.times { j ->
                        activity(
                                "${name}": "${applicationID}.${infix}P${p}TA${i}STPNTS${j}",
                                "${cfg}": "${cfgV}",
                                "${exp}": "${expV}",
                                "${ori}": "${oriV}",
                                "${theme}": "${themeNTS}",
                                "${launchMode}": "singleTop",
                                "${process}": ":p${p}",
                                "${task}": ":t${i}")
                    }
                    config.countNotTranslucentSingleTask.times { j ->
                        activity(
                                "${name}": "${applicationID}.${infix}P${p}TA${i}STNTS${j}",
                                "${cfg}": "${cfgV}",
                                "${exp}": "${expV}",
                                "${ori}": "${oriV}",
                                "${theme}": "${themeNTS}",
                                "${launchMode}": "singleTask",
                                "${process}": ":p${p}",
                                "${task}": ":t${i}")
                    }
                }

                /* Provider */
                // 支持插件中的 Provider 调用
                provider("${name}": "com.qihoo360.replugin.component.provider.PluginPitProviderP${p}",
                        "android:authorities": "${applicationID}.Plugin.NP.${p}",
                        "${process}": ":p${p}",
                        "${exp}": "${expV}")

                // fixme hujunjie 100 不写死
                // 支持进程Provider拉起
                provider("${name}": "com.qihoo360.replugin.component.process.ProcessPitProviderP${p}",
                        "android:authorities": "${applicationID}.loader.p.mainN${100 - p}",
                        "${process}": ":p${p}",
                        "${exp}": "${expV}")

                /* Service */
                // 支持使用插件的Service坑位
                // Added by Jiongxuan Zhang
                service("${name}": "com.qihoo360.replugin.component.service.server.PluginPitServiceP${p}",
                        "${process}": ":p${p}",
                        "${exp}": "${expV}")
            }
        }

        // 删除 application 标签
        return writer.toString().replace("<application>", "").replace("</application>", "")
    }
}
