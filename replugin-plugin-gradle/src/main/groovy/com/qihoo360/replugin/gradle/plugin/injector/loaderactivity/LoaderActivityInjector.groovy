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

package com.qihoo360.replugin.gradle.plugin.injector.loaderactivity

import com.qihoo360.replugin.gradle.plugin.injector.BaseInjector
import com.qihoo360.replugin.gradle.plugin.inner.CommonData
import com.qihoo360.replugin.gradle.plugin.manifest.ManifestAPI
import javassist.CannotCompileException
import javassist.ClassPool
import javassist.CtClass
import javassist.expr.ExprEditor
import javassist.expr.MethodCall

/**
 * LOADER_ACTIVITY_CHECK_INJECTOR
 *
 * 修改普通的 Activity 为 PluginActivity
 *
 * @author RePlugin Team
 */
public class LoaderActivityInjector extends BaseInjector {

    def private static LOADER_PROP_FILE = 'loader_activities.properties'

    /* LoaderActivity 替换规则 */
    def private static loaderActivityRules = [
            'android.app.Activity'                    : 'com.qihoo360.replugin.loader.a.PluginActivity',
            'android.app.TabActivity'                 : 'com.qihoo360.replugin.loader.a.PluginTabActivity',
            'android.app.ListActivity'                : 'com.qihoo360.replugin.loader.a.PluginListActivity',
            'android.app.ActivityGroup'               : 'com.qihoo360.replugin.loader.a.PluginActivityGroup',
            'android.support.v4.app.FragmentActivity' : 'com.qihoo360.replugin.loader.a.PluginFragmentActivity',
            'android.support.v7.app.AppCompatActivity': 'com.qihoo360.replugin.loader.a.PluginAppCompatActivity',
            'android.preference.PreferenceActivity'   : 'com.qihoo360.replugin.loader.a.PluginPreferenceActivity',
            'android.app.ExpandableListActivity'      : 'com.qihoo360.replugin.loader.a.PluginExpandableListActivity'
    ]

    @Override
    def injectClass(ClassPool pool, String dir, Map config) {
        init()

        /* 遍历程序中声明的所有 Activity */
        //每次都new一下，否则多个variant一起构建时只会获取到首个manifest
        new ManifestAPI().getActivities(project, variantDir).each {
            // 处理没有被忽略的 Activity
            if (!(it in CommonData.ignoredActivities)) {
                handleActivity(pool, it, dir)
            }
        }
    }

    /**
     * 处理 Activity
     *
     * @param pool
     * @param activity Activity 名称
     * @param classesDir class 文件目录
     */
    private def handleActivity(ClassPool pool, String activity, String classesDir) {
        def clsFilePath = classesDir + File.separatorChar + activity.replaceAll('\\.', '/') + '.class'
        if (!new File(clsFilePath).exists()) {
            return
        }

        println ">>> Handle $activity"

        def stream, ctCls
        try {
            stream = new FileInputStream(clsFilePath)
            ctCls = pool.makeClass(stream);
/*
             // 打印当前 Activity 的所有父类
            CtClass tmpSuper = ctCls.superclass
            while (tmpSuper != null) {
                println(tmpSuper.name)
                tmpSuper = tmpSuper.superclass
            }
*/
            // ctCls 之前的父类
            def originSuperCls = ctCls.superclass

            /* 从当前 Activity 往上回溯，直到找到需要替换的 Activity */
            def superCls = originSuperCls
            while (superCls != null && !(superCls.name in loaderActivityRules.keySet())) {
                // println ">>> 向上查找 $superCls.name"
                ctCls = superCls
                superCls = ctCls.superclass
            }

            // 如果 ctCls 已经是 LoaderActivity，则不修改
            if (ctCls.name in loaderActivityRules.values()) {
                // println "    跳过 ${ctCls.getName()}"
                return
            }

            /* 找到需要替换的 Activity, 修改 Activity 的父类为 LoaderActivity */
            if (superCls != null) {
                def targetSuperClsName = loaderActivityRules.get(superCls.name)
                // println "    ${ctCls.getName()} 的父类 $superCls.name 需要替换为 ${targetSuperClsName}"
                CtClass targetSuperCls = pool.get(targetSuperClsName)

                if (ctCls.isFrozen()) {
                    ctCls.defrost()
                }
                ctCls.setSuperclass(targetSuperCls)

                // 修改声明的父类后，还需要方法中所有的 super 调用。
                ctCls.getDeclaredMethods().each { outerMethod ->
                    outerMethod.instrument(new ExprEditor() {
                        @Override
                        void edit(MethodCall call) throws CannotCompileException {
                            if (call.isSuper()) {
                                if (call.getMethod().getReturnType().getName() == 'void') {
                                    call.replace('{super.' + call.getMethodName() + '($$);}')
                                } else {
                                    call.replace('{$_ = super.' + call.getMethodName() + '($$);}')
                                }
                            }
                        }
                    })
                }

                ctCls.writeFile(CommonData.getClassPath(ctCls.name))
                println "    Replace ${ctCls.name}'s SuperClass ${superCls.name} to ${targetSuperCls.name}"
            }

        } catch (Throwable t) {
            println "    [Warning] --> ${t.toString()}"
        } finally {
            if (ctCls != null) {
                ctCls.detach()
            }
            if (stream != null) {
                stream.close()
            }
        }
    }

    def private init() {
        /* 延迟初始化 loaderActivityRules */
        // todo 从配置中读取，而不是写死在代码中
        if (loaderActivityRules == null) {
            def buildSrcPath = project.project(':buildsrc').projectDir.absolutePath
            def loaderConfigPath = String.join(File.separator, buildSrcPath, 'res', LOADER_PROP_FILE)

            loaderActivityRules = new Properties()
            new File(loaderConfigPath).withInputStream {
                loaderActivityRules.load(it)
            }

            println '\n>>> Activity Rules：'
            loaderActivityRules.each {
                println it
            }
            println()
        }
    }
}
