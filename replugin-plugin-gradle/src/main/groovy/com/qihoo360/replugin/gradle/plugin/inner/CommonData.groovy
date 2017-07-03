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

/**
 * @author RePlugin Team
 */
public class CommonData {

    /** 保存类文件名和 class 文件路径的关系 */
    def static classAndPath = [:]

    /** App Module 的名称, 如 ':app', 传 '' 时，使用项目根目录为 App Module */
    def static String appModule

    def static String appPackage

    /** 执行 LoaderActivity 替换时，不需要替换的 Activity */
    def static ignoredActivities = []

    def static putClassAndPath(def className, def classFilePath) {
        classAndPath.put(className, classFilePath)
    }

    def static getClassPath(def className) {
        return classAndPath.get(className)
    }
}
