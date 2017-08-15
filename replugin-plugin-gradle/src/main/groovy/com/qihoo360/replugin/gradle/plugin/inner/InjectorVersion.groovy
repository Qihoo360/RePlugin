
package com.qihoo360.replugin.gradle.plugin.inner

import com.qihoo360.replugin.gradle.plugin.AppConstant

/**
 * @author 247321453
 */
public class InjectorVersion {

    def String jarMd5

    def String pluginVersion

    InjectorVersion(){

    }

    InjectorVersion(String jarMd5, String pluginVersion) {
        this.jarMd5 = jarMd5
        this.pluginVersion = pluginVersion
    }

    InjectorVersion(File jar){
        this.jarMd5 = Util.md5File(jar)
        this.pluginVersion = AppConstant.VER
    }
}
