package com.qihoo360.replugin.gradle.compat

import com.android.sdklib.IAndroidTarget

/**
 * @author hyongbai
 */
class ScopeCompat {
    static def getAdbExecutable(def scope) {
        final MetaClass scopeClz = scope.metaClass
        if (scopeClz.hasProperty(scope, "androidBuilder")) {
            return scope.androidBuilder.sdkInfo.adb
        }
        if (scopeClz.hasProperty(scope, "sdkComponents")) {
            return scope.sdkComponents.adbExecutableProvider.get()
        }
    }

    // TODO: getBuilderTarget
//    static def getBuilderTarget(def scope, def target){
//        final MetaClass scopeClz = scope.metaClass
//
//        if (scopeClz.hasProperty(scope, "androidBuilder")) {
//            return scope.getAndroidBuilder().getTarget().getPath(target) //IAndroidTarget.ANDROID_JAR
//        }
//
//        return globalScope.getAndroidBuilder().getTarget().getPath(IAndroidTarget.ANDROID_JAR)
//    }

    static def getAndroidJar(def scope){
        final MetaClass scopeClz = scope.metaClass

        if (scopeClz.hasProperty(scope, "androidBuilder")) {
            return scope.getAndroidBuilder().getTarget().getPath(IAndroidTarget.ANDROID_JAR)
        }
        if (scopeClz.hasProperty(scope, "sdkComponents")) {
            return scope.sdkComponents.androidJarProvider.get().getAbsolutePath()
        }
    }
}