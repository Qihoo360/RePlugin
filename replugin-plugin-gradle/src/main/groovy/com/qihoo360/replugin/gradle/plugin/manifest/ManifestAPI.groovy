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

package com.qihoo360.replugin.gradle.plugin.manifest

import com.qihoo360.replugin.gradle.plugin.ReClassPlugin
import com.qihoo360.replugin.gradle.plugin.inner.Util

/**
 * @author RePlugin Team
 */
public class ManifestAPI {

    def private static IManifest sManifestAPIImpl

    def static getActivities() {
        if (sManifestAPIImpl == null) {
            sManifestAPIImpl = new ManifestReader(manifestPath())
        }
        sManifestAPIImpl.activities
    }

    /**
     * 获取 AndroidManifest.xml 路径
     * @return
     */
    def static private manifestPath() {
        String buildDir = Util.appProject(ReClassPlugin.sProject).buildDir.absolutePath
        String xmlPath = String.join(File.separator, buildDir,
                'intermediates', 'manifests', 'full', 'release', 'AndroidManifest.xml')

        // build/.../release 目录下不存在 AndroidManifest.xml，检查 debug 目录
        if (!new File(xmlPath).exists()) {
            xmlPath = String.join(File.separator, buildDir,
                    'intermediates', 'manifests', 'full', 'debug', 'AndroidManifest.xml')
        }
        println "AndroidManifest.xml 路径：$xmlPath"

        xmlPath
    }
}
