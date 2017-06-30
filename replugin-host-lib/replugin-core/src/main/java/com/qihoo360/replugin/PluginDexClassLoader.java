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

package com.qihoo360.replugin;

import com.qihoo360.replugin.helper.LogDebug;

import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * 插件的DexClassLoader。用来做一些“更高级”的特性，在RePluginConfig中可直接配置
 * <p>注：原本只需要DexClassLoader即可，但若要支持一些高级特性（如可自由使用宿主的Class），则仍需实现相应方法
 *
 * @author RePlugin Team
 */

public class PluginDexClassLoader extends DexClassLoader {

    private static final String TAG = "PluginDexClassLoader";

    private final ClassLoader mHostClassLoader;

    private static Method sLoadClassMethod;

    /**
     * 初始化插件的DexClassLoader的构造函数。插件化框架会调用此函数。
     *
     * @param dexPath the list of jar/apk files containing classes and
     *     resources, delimited by {@code File.pathSeparator}, which
     *     defaults to {@code ":"} on Android
     * @param optimizedDirectory directory where optimized dex files
     *     should be written; must not be {@code null}
     * @param librarySearchPath the list of directories containing native
     *     libraries, delimited by {@code File.pathSeparator}; may be
     *     {@code null}
     * @param parent the parent class loader
     */
    public PluginDexClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
        mHostClassLoader = RePluginInternal.getAppClassLoader();

        initMethods(mHostClassLoader);
    }

    private static void initMethods(ClassLoader cl) {
        Class<?> clz = cl.getClass();
        if (sLoadClassMethod == null) {
            sLoadClassMethod = MethodUtils.getMatchingMethod(clz, "loadClass", String.class, Boolean.TYPE);
            if (sLoadClassMethod == null) {
                throw new NoSuchMethodError("loadClass");
            }
        }
    }

    @Override
    protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
        // 插件自己的Class。从自己开始一直到BootClassLoader，采用正常的双亲委派模型流程，读到了就直接返回
        Class<?> pc = super.loadClass(className, resolve);
        if (pc != null) {
            // 只有开启“详细日志”才会输出，防止“刷屏”现象
            if (LogDebug.LOG && RePlugin.getConfig().isPrintDetailLog()) {
                LogDebug.d(TAG, "loadClass: load plugin class, cn=" + className);
            }
            return pc;
        }

        // 若插件里没有此类，则会从宿主ClassLoader中找，找到了则直接返回
        // 注意：需要读取isUseHostClassIfNotFound开关。默认为关闭的。可参见该开关的说明
        if (RePlugin.getConfig().isUseHostClassIfNotFound()) {
            try {
                pc = (Class<?>) sLoadClassMethod.invoke(mHostClassLoader, className, resolve);
                if (pc != null) {
                    // 只有开启“详细日志”才会输出，防止“刷屏”现象
                    if (LogDebug.LOG && RePlugin.getConfig().isPrintDetailLog()) {
                        LogDebug.w(TAG, "loadClass: load host class, cn=" + className);
                    }
                }
            } catch (IllegalAccessException e) {
                // Just rethrow
                throw new ClassNotFoundException("iae", e);
            } catch (InvocationTargetException e) {
                // Just rethrow
                throw new ClassNotFoundException("ite", e);
            }
        }
        return pc;
    }
}
