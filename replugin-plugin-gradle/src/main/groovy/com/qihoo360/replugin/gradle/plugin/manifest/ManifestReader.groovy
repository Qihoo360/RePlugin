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

/**
 * @author RePlugin Team
 */
public class ManifestReader implements IManifest {

    /* AndroidManifest 文件路径 */
    def final filePath

    def manifest

    public ManifestReader(String path) {
        filePath = path
    }

    @Override
    List<String> getActivities() {
        init()

        def activities = []
        String pkg = manifest.@package

        manifest.application.activity.each {
            String name = it.'@android:name'
            if (name.substring(0, 1) == '.') {
                name = pkg + name
            }
            activities << name
        }

        activities
    }

    @Override
    String getPackageName() {
        init()
        manifest.@package
    }

    def private init() {
        if (manifest == null) {
            manifest = new XmlSlurper().parse(filePath)
        }
    }
}
