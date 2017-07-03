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

package com.qihoo360.replugin.gradle.host.creator

import com.qihoo360.replugin.gradle.host.AppConstant
import com.qihoo360.replugin.gradle.host.creator.impl.java.RePluginHostConfigCreator
import com.qihoo360.replugin.gradle.host.creator.impl.json.PluginBuiltinJsonCreator
import org.gradle.api.Project

/**
 * @author RePlugin Team
 */
public class FileCreators {

    def creators

    def init(Project project, def variant, def config) {
        creators = []
        creators << new RePluginHostConfigCreator(project, variant, config)

        if (config.autoManageBuiltInJsonFile) {
            creators << new PluginBuiltinJsonCreator(project, variant, config)
        }
        return this
    }

    def create() {
        creators.each { IFileCreator creator ->
            def dir = creator.getFileDir()
            if (!dir.exists()) {
                println "${AppConstant.TAG} mkdirs ${dir.getAbsolutePath()} : ${dir.mkdirs()}"
            }

            def targetFile = new File(dir, creator.getFileName())
            targetFile.write(creator.getFileContent(), 'UTF-8')
            println "${AppConstant.TAG} rewrite ${targetFile.getAbsoluteFile()}"
        }
    }
}
