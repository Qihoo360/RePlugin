package com.qihoo360.replugin.gradle.plugin.inner

import org.gradle.api.Project

import java.security.MessageDigest

/**
 * 记录已经修改过的文件，避免重复操作
 */
public class InjectHistory {
    def static final PROP_FILE = "injectHistory.prop"

    def static history = null as Properties

    def static boolean contains(Project project, String filePath) {
        initHistory(project)
        //test
        String key = getKey(project, filePath)
        String value = history.get(key)
        if (value == null) {
            return false
        }
        String md5 = getMd5(new File(filePath))
        return value == md5
    }

    def private static String getKey(Project project, String filePath){
        return filePath.replace(project.buildDir.absolutePath + "\\intermediates\\classes\\", "")
    }
    def static void put(Project project, String filePath) {
        initHistory(project)
        //不记录libs下jar解压后的class、png等(有的jar有assets)，因为以后编译时直接忽略jar
        if (!filePath.endsWith(".jar") && filePath.startsWith(new File(project.projectDir, "libs").absolutePath)) {
            return
        }
        //不记录provided下的jar解压后的class、png等
        if (!filePath.endsWith(".jar") && filePath.startsWith(new File(project.projectDir, "provided").absolutePath)) {
            return
        }

        String md5 = getMd5(new File(filePath))
        String key = getKey(project, filePath)
        history.put(key, md5)
        //println("InjectHistory put " + filePath + " -----> " + md5)
    }

    def static void save(Project project) {
        if (history != null) {
            history.store(new File(project.projectDir.absolutePath, PROP_FILE).newWriter(), null)
            //每次执行完成后都清除"缓存"，下次执行时从文件加载。删除文件可以强制重新执行
            history.clear()
        }
    }

    def private static void initHistory(Project project) {
        if (history == null) {
            history = new Properties()
        }
        if (history.isEmpty()) {
            def projectDir = project.getProjectDir().absolutePath
            File historyFile = new File(projectDir, PROP_FILE)
            if (!historyFile.exists()) {
                historyFile.createNewFile()
            }
            try {
                historyFile.withInputStream {
                    history.load(it)
                }
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
    }

    def static String getMd5(File file) {
        def digest = MessageDigest.getInstance("MD5")
        file.eachByte(4096) { buffer, length ->
            digest.update(buffer, 0, length)
        }
        digest.digest().encodeHex() as String
    }
}