
package com.qihoo360.replugin.gradle.plugin.inner

import com.qihoo360.replugin.gradle.plugin.AppConstant

/**
 * @author 247321453
 */
public class JarPatchInfo {

    def String jarMd5

    def String pluginVersion

    def String manifestActivitiesMd5

    /**
     * gson需要
     */
    JarPatchInfo(){

    }

    JarPatchInfo(File jar,String activitiesMd5){
        this.jarMd5 = Util.md5File(jar)
        this.pluginVersion = AppConstant.VER
        this.manifestActivitiesMd5 = activitiesMd5
    }

}
