package com.qihoo360.replugin.gradle.plugin.injector

import com.qihoo360.replugin.gradle.plugin.inner.InjectHistory
import com.qihoo360.replugin.gradle.plugin.inner.Util
import javassist.ClassPool

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

/**
 * 放在Injectors的末尾，所有其它injector执行完成后遍历文件，记录到InjectHistory里
 * @author jsh
 */
public class HistoryInjector extends BaseInjector {

    @Override
    def injectClass(ClassPool pool, String dir, Map config) {
        Util.newSection()
        println dir

        Files.walkFileTree(Paths.get(dir), new SimpleFileVisitor<Path>() {
            @Override
            FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                //todo only .class
                String filePath = file.toString()
                InjectHistory.put(project, filePath)
                return super.visitFile(file, attrs)
            }
        })
    }
}