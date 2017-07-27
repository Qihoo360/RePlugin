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

package com.qihoo360.replugin.gradle.plugin.inner

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformInput
import com.android.build.gradle.internal.scope.GlobalScope
import com.android.sdklib.IAndroidTarget
import org.apache.commons.io.FileUtils
import com.google.common.base.Charsets
import com.google.common.hash.Hashing
import org.gradle.api.Project

import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipFile

import static com.android.builder.model.AndroidProject.FD_INTERMEDIATES;

/**
 * @author RePlugin Team
 */
public class Util {

    /** 生成 ClassPool 使用的 ClassPath 集合，同时将要处理的 jar 写入 includeJars */
    def
    static getClassPaths(Project project, GlobalScope globalScope, Collection<TransformInput> inputs, Set<String> includeJars, Map<String, String> map) {
        def classpathList = []

        // android.jar
        classpathList.add(getAndroidJarPath(globalScope))

        // 原始项目中引用的 classpathList
        getProjectClassPath(project, inputs, includeJars, map).each {
            classpathList.add(it)
        }

        newSection()
        println ">>> ClassPath:"
        classpathList
    }

    /** 获取原始项目中的 ClassPath */
    def private static getProjectClassPath(Project project,
                                           Collection<TransformInput> inputs,
                                           Set<String> includeJars, Map<String, String> map) {
        def classPath = []
        def visitor = new ClassFileVisitor()
        def projectDir = project.getRootDir().absolutePath

        println ">>> Unzip Jar ..."

        inputs.each { TransformInput input ->

            input.directoryInputs.each { DirectoryInput dirInput ->
                def dir = dirInput.file.absolutePath
                classPath << dir

                visitor.setBaseDir(dir)
                Files.walkFileTree(Paths.get(dir), visitor)
            }

            input.jarInputs.each { JarInput jarInput ->
                File jar = jarInput.file
                def jarPath = jar.absolutePath

                if (!jarPath.contains(projectDir)) {

                    String jarZipDir = project.getBuildDir().path +
                            File.separator + FD_INTERMEDIATES + File.separator + "exploded-aar" +
                            File.separator + Hashing.sha1().hashString(jarPath, Charsets.UTF_16LE).toString() + File.separator + "class";
                    if (unzip(jarPath, jarZipDir)) {
                        def jarZip = jarZipDir + ".jar"
                        includeJars << jarPath
                        classPath << jarZipDir
                        visitor.setBaseDir(jarZipDir)
                        Files.walkFileTree(Paths.get(jarZipDir), visitor)
                        map.put(jarPath, jarZip)
                    }

                } else {

                    includeJars << jarPath
                    map.put(jarPath, jarPath)

                    /* 将 jar 包解压，并将解压后的目录加入 classpath */
                    // println ">>> 解压Jar${jarPath}"
                    String jarZipDir = jar.getParent() + File.separatorChar + jar.getName().replace('.jar', '')
                    if (unzip(jarPath, jarZipDir)) {
                        classPath << jarZipDir

                        visitor.setBaseDir(jarZipDir)
                        Files.walkFileTree(Paths.get(jarZipDir), visitor)
                    }

                    // 删除 jar
                    FileUtils.forceDelete(jar)
                }
            }
        }
        return classPath
    }

    /**
     * 编译环境中 android.jar 的路径
     */
    def static getAndroidJarPath(GlobalScope globalScope) {
        return globalScope.getAndroidBuilder().getTarget().getPath(IAndroidTarget.ANDROID_JAR)
    }

    /**
     * 压缩 dirPath 到 zipFilePath
     */
    def static zipDir(String dirPath, String zipFilePath) {
        new AntBuilder().zip(destfile: zipFilePath, basedir: dirPath)
    }

    /**
     * 解压 zipFilePath 到 目录 dirPath
     */
    def private static boolean unzip(String zipFilePath, String dirPath) {
        // 若这个Zip包是空内容的（如引入了Bugly就会出现），则直接忽略
        if (isZipEmpty(zipFilePath)) {
            println ">>> Zip file is empty! Ignore";
            return false;
        }

        new AntBuilder().unzip(src: zipFilePath, dest: dirPath, overwrite: 'true')
        return true;
    }

    /**
     * 获取 App Project 目录
     */
    def static appModuleDir(Project project) {
        appProject(project).projectDir.absolutePath
    }

    /**
     * 获取 App Project
     */
    def static appProject(Project project) {
        def modelName = CommonData.appModule.trim()
        if ('' == modelName || ':' == modelName) {
            project
        }
        project.project(modelName)
    }

    /**
     * 将字符串的某个字符转换成 小写
     *
     * @param str 字符串
     * @param index 索引
     *
     * @return 转换后的字符串
     */
    def public static lowerCaseAtIndex(String str, int index) {
        def len = str.length()
        if (index > -1 && index < len) {
            def arr = str.toCharArray()
            char c = arr[index]
            if (c >= 'A' && c <= 'Z') {
                c += 32
            }

            arr[index] = c
            arr.toString()
        } else {
            str
        }
    }

    def static newSection() {
        50.times {
            print '--'
        }
        println()
    }

    def static boolean isZipEmpty(String zipFilePath) {
        ZipFile z;
        try {
            z = new ZipFile(zipFilePath)
            return z.size() == 0
        } finally {
            z.close();
        }
    }
}
