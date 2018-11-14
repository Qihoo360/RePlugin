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

import com.android.build.api.transform.*
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.internal.pipeline.TransformManager
import com.qihoo360.replugin.gradle.plugin.injector.IClassInjector
import com.qihoo360.replugin.gradle.plugin.injector.Injectors
import javassist.ClassPool
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.util.regex.Pattern

/**
 * @author RePlugin Team
 */
public class ReClassTransform extends Transform {

    private Project project
    private def globalScope

    /* 需要处理的 jar 包 */
    def includeJars = [] as Set
    def map = [:]

    public ReClassTransform(Project p) {
        this.project = p
        def appPlugin = project.plugins.getPlugin(AppPlugin)
        // taskManager 在 2.1.3 中为 protected 访问类型的，在之后的版本为 private 访问类型的，
        // 使用反射访问
        def taskManager = BasePlugin.metaClass.getProperty(appPlugin, "taskManager")
        this.globalScope = taskManager.globalScope;
    }

    @Override
    String getName() {
        return '___ReClass___'
    }

    @Override
    void transform(Context context,
                   Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider,
                   boolean isIncremental) throws IOException, TransformException, InterruptedException {

        welcome()

        /* 读取用户配置 */
        def config = project.extensions.getByName('repluginPluginConfig')


        File rootLocation = null
        try {
            rootLocation = outputProvider.rootLocation
        } catch (Throwable e) {
            //android gradle plugin 3.0.0+ 修改了私有变量，将其移动到了IntermediateFolderUtils中去
            rootLocation = outputProvider.folderUtils.getRootFolder()
        }
        if (rootLocation == null) {
            throw new GradleException("can't get transform root location")
        }
        println ">>> rootLocation: ${rootLocation}"
        // Compatible with path separators for window and Linux, and fit split param based on 'Pattern.quote'
        def variantDir = rootLocation.absolutePath.split(getName() + Pattern.quote(File.separator))[1]
        println ">>> variantDir: ${variantDir}"

        CommonData.appModule = config.appModule
        CommonData.ignoredActivities = config.ignoredActivities

        def injectors = includedInjectors(config, variantDir)
        if (injectors.isEmpty()) {
            copyResult(inputs, outputProvider) // 跳过 reclass
        } else {
            doTransform(inputs, outputProvider, config, injectors) // 执行 reclass
        }
    }

    /**
     * 返回用户未忽略的注入器的集合
     */
    def includedInjectors(def cfg, String variantDir) {
        def injectors = []
        Injectors.values().each {
            //设置project
            it.injector.setProject(project)
            //设置variant关键dir
            it.injector.setVariantDir(variantDir)
            if (!(it.nickName in cfg.ignoredInjectors)) {
                injectors << it.nickName
            }
        }
        injectors
    }

    /**
     * 执行 Transform
     */
    def doTransform(Collection<TransformInput> inputs,
                    TransformOutputProvider outputProvider,
                    Object config,
                    def injectors) {

        /* 初始化 ClassPool */
        Object pool = initClassPool(inputs)

        /* 进行注入操作 */
        Util.newSection()
        Injectors.values().each {
            if (it.nickName in injectors) {
                println ">>> Do: ${it.nickName}"
                // 将 NickName 的第 0 个字符转换成小写，用作对应配置的名称
                def configPre = Util.lowerCaseAtIndex(it.nickName, 0)
                doInject(inputs, pool, it.injector, config.properties["${configPre}Config"])
            } else {
                println ">>> Skip: ${it.nickName}"
            }
        }

        if (config.customInjectors != null) {
            config.customInjectors.each {
                doInject(inputs, pool, it)
            }
        }

        /* 重打包 */
        repackage()

        /* 拷贝 class 和 jar 包 */
        copyResult(inputs, outputProvider)

        Util.newSection()
    }

    /**
     * 拷贝处理结果
     */
    def copyResult(def inputs, def outputs) {
        // Util.newSection()
        inputs.each { TransformInput input ->
            input.directoryInputs.each { DirectoryInput dirInput ->
                copyDir(outputs, dirInput)
            }
            input.jarInputs.each { JarInput jarInput ->
                copyJar(outputs, jarInput)
            }
        }
    }

    /**
     * 将解压的 class 文件重新打包，然后删除 class 文件
     */
    def repackage() {
        Util.newSection()
        println '>>> Repackage...'
        includeJars.each {
            File jar = new File(it)
            String JarAfterzip = map.get(jar.getParent() + File.separatorChar + jar.getName())
            String dirAfterUnzip = JarAfterzip.replace('.jar', '')
            // println ">>> 压缩目录 $dirAfterUnzip"
            
            Util.zipDir(dirAfterUnzip, JarAfterzip)

            // println ">>> 删除目录 $dirAfterUnzip"
            FileUtils.deleteDirectory(new File(dirAfterUnzip))
        }
    }

    /**
     * 执行注入操作
     */
    def doInject(Collection<TransformInput> inputs, ClassPool pool,
                 IClassInjector injector, Object config) {
        try {
            inputs.each { TransformInput input ->
                input.directoryInputs.each {
                    handleDir(pool, it, injector, config)
                }
                input.jarInputs.each {
                    handleJar(pool, it, injector, config)
                }
            }
        } catch (Throwable t) {
            println t.toString()
        }
    }

    /**
     * 初始化 ClassPool
     */
    def initClassPool(Collection<TransformInput> inputs) {
        Util.newSection()
        def pool = new ClassPool(true)
        // 添加编译时需要引用的到类到 ClassPool, 同时记录要修改的 jar 到 includeJars
        Util.getClassPaths(project, globalScope, inputs, includeJars, map).each {
            println "    $it"
            pool.insertClassPath(it)
        }
        pool
    }

    /**
     * 处理 jar
     */
    def handleJar(ClassPool pool, JarInput input, IClassInjector injector, Object config) {
        File jar = input.file
        if (jar.absolutePath in includeJars) {
            println ">>> Handle Jar: ${jar.absolutePath}"
            String dirAfterUnzip = map.get(jar.getParent() + File.separatorChar + jar.getName()).replace('.jar', '')
            injector.injectClass(pool, dirAfterUnzip, config)
        }
    }

    /**
     * 拷贝 Jar
     */
    def copyJar(TransformOutputProvider output, JarInput input) {
        File jar = input.file
        String jarPath = map.get(jar.absolutePath);
        if (jarPath != null) {
            jar = new File(jarPath)
        }

        if(!jar.exists()){
            return
        }

        String destName = input.name
        def hexName = DigestUtils.md5Hex(jar.absolutePath)
        if (destName.endsWith('.jar')) {
            destName = destName.substring(0, destName.length() - 4)
        }
        File dest = output.getContentLocation(destName + '_' + hexName, input.contentTypes, input.scopes, Format.JAR)
        FileUtils.copyFile(jar, dest)

/*
        def path = jar.absolutePath
        if (path in CommonData.includeJars) {
            println ">>> 拷贝Jar ${path} 到 ${dest.absolutePath}"
        }
*/
    }

    /**
     * 处理目录中的 class 文件
     */
    def handleDir(ClassPool pool, DirectoryInput input, IClassInjector injector, Object config) {
        println ">>> Handle Dir: ${input.file.absolutePath}"
        injector.injectClass(pool, input.file.absolutePath, config)
    }

    /**
     * 拷贝目录
     */
    def copyDir(TransformOutputProvider output, DirectoryInput input) {
        File dest = output.getContentLocation(input.name, input.contentTypes, input.scopes, Format.DIRECTORY)
        FileUtils.copyDirectory(input.file, dest)
//        println ">>> 拷贝目录 ${input.file.absolutePath} 到 ${dest.absolutePath}"
    }

    /**
     * 欢迎
     */
    def welcome() {
        println '\n'
        60.times { print '=' }
        println '\n                    replugin-plugin-gradle'
        60.times { print '=' }
        println("""
Add repluginPluginConfig to your build.gradle to enable this plugin:

repluginPluginConfig {
    // Name of 'App Module'，use '' if root dir is 'App Module'. ':app' as default.
    appModule = ':app'

    // Injectors ignored
    // LoaderActivityInjector: Replace Activity to LoaderActivity
    // ProviderInjector: Inject provider method call.
    ignoredInjectors = ['LoaderActivityInjector']
}""")
        println('\n')
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }
}
