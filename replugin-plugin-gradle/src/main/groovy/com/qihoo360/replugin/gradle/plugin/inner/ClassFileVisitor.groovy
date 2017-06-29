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

import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author RePlugin Team
 */
public class ClassFileVisitor extends SimpleFileVisitor<Path> {

    def baseDir

    @Override
    FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String path = file.toString()
        if (path.endsWith('.class')
                && !path.contains(File.separator + 'R$')
                && !path.endsWith(File.separator + 'R.class')) {

            def index = baseDir.length() + 1
            def className = path.substring(index).replace('\\', '.').replace('/', '.').replace('.class', '')

            CommonData.putClassAndPath(className, baseDir)
            // println className + ' -> ' + baseDir
        }
        return super.visitFile(file, attrs)
    }
}
