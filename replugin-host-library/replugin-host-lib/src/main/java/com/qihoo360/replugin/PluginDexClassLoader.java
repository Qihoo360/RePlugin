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

import android.os.Build;

import com.qihoo360.replugin.helper.LogDebug;
import com.qihoo360.replugin.model.PluginInfo;
import com.qihoo360.replugin.utils.CloseableUtils;
import com.qihoo360.replugin.utils.FileUtils;
import com.qihoo360.replugin.utils.ReflectUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

    private String mPluginName;

    /**
     * 初始化插件的DexClassLoader的构造函数。插件化框架会调用此函数。
     *
     * @param pi                 the plugin's info,refer to {@link PluginInfo}
     * @param dexPath            the list of jar/apk files containing classes and
     *                           resources, delimited by {@code File.pathSeparator}, which
     *                           defaults to {@code ":"} on Android
     * @param optimizedDirectory directory where optimized dex files
     *                           should be written; must not be {@code null}
     * @param librarySearchPath  the list of directories containing native
     *                           libraries, delimited by {@code File.pathSeparator}; may be
     *                           {@code null}
     * @param parent             the parent class loader
     */
    public PluginDexClassLoader(PluginInfo pi, String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);

        mPluginName = pi.getName();

        installMultiDexesBeforeLollipop(pi, dexPath, parent);

        mHostClassLoader = RePluginInternal.getAppClassLoader();

        initMethods(mHostClassLoader);
    }

    private static void initMethods(ClassLoader cl) {
        Class<?> clz = cl.getClass();
        if (sLoadClassMethod == null) {
            sLoadClassMethod = ReflectUtils.getMethod(clz, "loadClass", String.class, Boolean.TYPE);
            if (sLoadClassMethod == null) {
                throw new NoSuchMethodError("loadClass");
            }
        }
    }

    @Override
    protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
        // 插件自己的Class。从自己开始一直到BootClassLoader，采用正常的双亲委派模型流程，读到了就直接返回
        Class<?> pc = null;
        ClassNotFoundException cnfException = null;
        try {
            pc = super.loadClass(className, resolve);
            if (pc != null) {
                // 只有开启“详细日志”才会输出，防止“刷屏”现象
                if (LogDebug.LOG && RePlugin.getConfig().isPrintDetailLog()) {
                    LogDebug.d(TAG, "loadClass: load plugin class, cn=" + className);
                }
                return pc;
            }
        } catch (ClassNotFoundException e) {
            // Do not throw "e" now
            cnfException = e;

            if (PluginDexClassLoaderPatch.need2LoadFromHost(className)) {
                try {
                    return loadClassFromHost(className, resolve);
                } catch (ClassNotFoundException e1) {
                    // Do not throw "e1" now
                    cnfException = e1;

                    if (LogDebug.LOG) {
                        LogDebug.e(TAG, "loadClass ClassNotFoundException, from HostClassLoader&&PluginClassLoader, cn=" + className + ", pluginName=" + mPluginName);
                    }
                }
            } else {
                if (LogDebug.LOG) {
                    LogDebug.e(TAG, "loadClass ClassNotFoundException, from PluginClassLoader, cn=" + className + ", pluginName=" + mPluginName);
                }
            }
        }

        // 若插件里没有此类，则会从宿主ClassLoader中找，找到了则直接返回
        // 注意：需要读取isUseHostClassIfNotFound开关。默认为关闭的。可参见该开关的说明
        if (RePlugin.getConfig().isUseHostClassIfNotFound()) {
            try {
                return loadClassFromHost(className, resolve);
            } catch (ClassNotFoundException e) {
                // Do not throw "e" now
                cnfException = e;
            }
        }

        // At this point we can throw the previous exception
        if (cnfException != null) {
            throw cnfException;
        }
        return null;
    }

    private Class<?> loadClassFromHost(String className, boolean resolve) throws ClassNotFoundException {
        Class<?> c;
        try {
            c = (Class<?>) sLoadClassMethod.invoke(mHostClassLoader, className, resolve);
            // 只有开启“详细日志”才会输出，防止“刷屏”现象
            if (LogDebug.LOG && RePlugin.getConfig().isPrintDetailLog()) {
                LogDebug.w(TAG, "loadClass: load host class, cn=" + className + ", cz=" + c);
            }
        } catch (IllegalAccessException e) {
            // Just rethrow
            throw new ClassNotFoundException("Calling the loadClass method failed (IllegalAccessException)", e);
        } catch (InvocationTargetException e) {
            // Just rethrow
            throw new ClassNotFoundException("Calling the loadClass method failed (InvocationTargetException)", e);
        }
        return c;
    }

    /**
     * install extra dexes
     *
     * @param pi
     * @param dexPath
     * @param parent
     * @deprecated apply to ROM before Lollipop,may be deprecated
     */
    private void installMultiDexesBeforeLollipop(PluginInfo pi, String dexPath, ClassLoader parent) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        try {

            // get paths of extra dex
            List<File> dexFiles = getExtraDexFiles(pi, dexPath);

            if (dexFiles != null && dexFiles.size() > 0) {

                List<Object[]> allElements = new LinkedList<>();

                // get dexElements of main dex
                Class<?> clz = Class.forName("dalvik.system.BaseDexClassLoader");
                Object pathList = ReflectUtils.readField(clz, this, "pathList");
                Object[] mainElements = (Object[]) ReflectUtils.readField(pathList.getClass(), pathList, "dexElements");
                allElements.add(mainElements);

                // get dexElements of extra dex (need to load dex first)
                String optimizedDirectory = pi.getExtraOdexDir().getAbsolutePath();

                for (File file : dexFiles) {
                    if (LogDebug.LOG && RePlugin.getConfig().isPrintDetailLog()) {
                        LogDebug.d(TAG, "dex file:" + file.getName());
                    }

                    DexClassLoader dexClassLoader = new DexClassLoader(file.getAbsolutePath(), optimizedDirectory, optimizedDirectory, parent);

                    Object obj = ReflectUtils.readField(clz, dexClassLoader, "pathList");
                    Object[] dexElements = (Object[]) ReflectUtils.readField(obj.getClass(), obj, "dexElements");
                    allElements.add(dexElements);
                }

                // combine Elements
                Object combineElements = combineArray(allElements);

                // rewrite Elements combined to classLoader
                ReflectUtils.writeField(pathList.getClass(), pathList, "dexElements", combineElements);

                // delete extra dex, after optimized
                FileUtils.forceDelete(pi.getExtraDexDir());

                //Test whether the Extra Dex is installed
                if (LogDebug.LOG && RePlugin.getConfig().isPrintDetailLog()) {

                    Object object = ReflectUtils.readField(pathList.getClass(), pathList, "dexElements");
                    int length = Array.getLength(object);
                    LogDebug.d(TAG, "dexElements length:" + length);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * combine dexElements Array
     *
     * @param allElements all dexElements of dexes
     * @return the combined dexElements
     */
    private Object combineArray(List<Object[]> allElements) {

        int startIndex = 0;
        int arrayLength = 0;
        Object[] originalElements = null;

        for (Object[] elements : allElements) {

            if (originalElements == null) {
                originalElements = elements;
            }

            arrayLength += elements.length;
        }

        Object[] combined = (Object[]) Array.newInstance(
                originalElements.getClass().getComponentType(), arrayLength);

        for (Object[] elements : allElements) {

            System.arraycopy(elements, 0, combined, startIndex, elements.length);
            startIndex += elements.length;
        }

        return combined;
    }

    /**
     * get paths of extra dex
     *
     * @param pi
     * @param dexPath
     * @return the File list of the extra dexes
     */
    private List<File> getExtraDexFiles(PluginInfo pi, String dexPath) {

        ZipFile zipFile = null;
        List<File> files = null;

        try {

            if (pi != null) {
                zipFile = new ZipFile(dexPath);
                files = traverseExtraDex(pi, zipFile);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseableUtils.closeQuietly(zipFile);
        }

        return files;

    }

    /**
     * traverse extra dex files
     *
     * @param pi
     * @param zipFile
     * @return the File list of the extra dexes
     */
    private static List<File> traverseExtraDex(PluginInfo pi, ZipFile zipFile) {

        String dir = null;
        List<File> files = new LinkedList<>();
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.contains("../")) {
                // 过滤，防止被攻击
                continue;
            }

            try {
                if (name.contains(".dex") && !name.equals("classes.dex")) {

                    if (dir == null) {
                        dir = pi.getExtraDexDir().getAbsolutePath();
                    }

                    File file = new File(dir, name);
                    extractFile(zipFile, entry, file);
                    files.add(file);

                    if (LogDebug.LOG && RePlugin.getConfig().isPrintDetailLog()) {
                        LogDebug.d(TAG, "dex path:" + file.getAbsolutePath());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return files;
    }

    /**
     * extract File
     *
     * @param zipFile
     * @param ze
     * @param outFile
     * @throws IOException
     */
    private static void extractFile(ZipFile zipFile, ZipEntry ze, File outFile) throws IOException {
        InputStream in = null;
        try {
            in = zipFile.getInputStream(ze);
            FileUtils.copyInputStreamToFile(in, outFile);
            if (LogDebug.LOG && RePlugin.getConfig().isPrintDetailLog()) {
                LogDebug.d(TAG, "extractFile(): Success! fn=" + outFile.getName());
            }
        } finally {
            CloseableUtils.closeQuietly(in);
        }
    }

}
