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

package com.qihoo360.replugin.component.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ComponentInfo;
import android.text.TextUtils;

import com.qihoo360.i.Factory;
import com.qihoo360.i.IPluginManager;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.base.IPC;
import com.qihoo360.replugin.component.process.PluginProcessHost;
import com.qihoo360.replugin.helper.HostConfigHelper;
import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.helper.LogRelease;

import static com.qihoo360.replugin.component.process.PluginProcessHost.PROCESS_INT_MAP;
import static com.qihoo360.replugin.helper.LogDebug.LOG;
import static com.qihoo360.replugin.helper.LogDebug.PLUGIN_TAG;
import static com.qihoo360.replugin.helper.LogRelease.LOGR;

/**
 * 和插件Client有关的帮助类
 *
 * @author RePlugin Team
 */
public class PluginClientHelper {

    /**
     * 根据Context所带的插件信息，来填充Intent的ComponentName对象
     * 若外界将Context.xxxService方法用PluginServiceClient代替（如编译工具所做的），则这里会做如下事情：
     *      1、将外界传递的Intent为com.qihoo360.mobilesafe的包名，变成一个特定插件的包名
     *      2、这样交给外界的时候，其ComponentName已变成“插件名-类名”的形式，可以做下一步处理
     *      3、若其后处理失败，则仍会调用系统的相应方法（但不是该函数的职责）
     *
     * @param c    根据Context内容来决定如何填充Intent，此为那个Context对象
     * @param from ComponentName
     * @return 新的ComponentName。或者如果插件有问题，则返回from
     */
    public static ComponentName getComponentNameByContext(Context c, ComponentName from) {
        if (from == null) {
            // 如果Intent里面没有带ComponentName，则我们不支持此特性，直接返回null
            // 外界会直接调用其系统的对应方法
            return null;
        }
        String appPackage = IPC.getPackageName();
        if (!TextUtils.equals(from.getPackageName(), appPackage)) {
            // 自己已填好了要使用的插件名（作为CN的Key），这里不做处理
            return from;
        }

        // 根据Context的ClassLoader来看到底属于哪个插件，还是只是主程序
        ClassLoader cl = c.getClassLoader();
        String pn = Factory.fetchPluginName(cl);
        if (TextUtils.isEmpty(pn)) {
            // 获得了无效的插件信息，这种情况很少见，故打出错误信息，什么也不做
            if (LOGR) {
                LogRelease.e(PLUGIN_TAG, "pch.iibc: pn is n. n=" + from);
            }
        } else if (TextUtils.equals(pn, RePlugin.PLUGIN_NAME_MAIN)) {
            // 此Context属于主工程，则也什么都不做。稍后会直接走“主程序的Context”来做处理
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "PluginClientHelper.iibc(): Call Main! n=" + from);
            }
        } else {
            // 将Context所在的插件名写入CN中，待后面的方法去处理
            if (LOG) {
                LogDebug.d(PLUGIN_TAG, "PluginClientHelper.iibc(): Call Plugin! n=" + from);
            }
            return new ComponentName(pn, from.getClassName());
        }
        return from;
    }

    /**
     * 根据进程名称获取进程ID
     *
     * @param processName 进程名称
     */
    public static Integer getProcessInt(String processName) {
        if (!TextUtils.isEmpty(processName)) {
            // 插件若想将组件定义成在"常驻进程"中运行，则可以在android:process中定义：

            // 1. （推荐）":GuardService"。这样无论宿主的常驻进程名是什么，都会定向到"常驻进程"
            String pntl = processName.toLowerCase();
            String ppdntl = HostConfigHelper.PERSISTENT_NAME.toLowerCase();
            if (pntl.contains(ppdntl)) {
                return IPluginManager.PROCESS_PERSIST;
            }

            // 2. 和宿主常驻进程名相同，这样也会定向到"常驻进程"，但若移植到其它宿主上则会出现问题
            String ppntl = IPC.getPersistentProcessName().toLowerCase();
            if (TextUtils.equals(pntl, ppntl)) {
                return IPluginManager.PROCESS_PERSIST;
            }

            // 3. 用户自定义进程时，从 Map 中取数据
            // (根据冒号之后的名称来判断是否是自定义进程)
            processName = PluginProcessHost.processTail(processName.toLowerCase());
            if (PROCESS_INT_MAP.containsKey(processName)) {
                return PROCESS_INT_MAP.get(processName);
            }
        }
        return IPluginManager.PROCESS_UI;
    }

    /**
     * 从 ComponentInfo 获取 插件名称
     */
    public static String getPluginName(ComponentInfo ci) {
        if (ci != null && ci.packageName != null) {
            int indexOfLastDot = ci.packageName.lastIndexOf(".");
            if (indexOfLastDot > 0) {
                return ci.packageName.substring(indexOfLastDot + 1);
            }
        }
        return "";
    }

    /**
     * 用来告诉外界，可以直接调用系统方法来处理<p>
     * 注意：仅框架内部使用
     *
     * @author RePlugin Team
     */
    public static class ShouldCallSystem extends RuntimeException {
        private static final long serialVersionUID = -2987516993124234548L;

        public ShouldCallSystem() {
        }
    }
}
