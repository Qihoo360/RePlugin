package com.qihoo360.replugin.gradle.plugin.util

public class GradleCompat {

    def static getAssemble(def variant) {
        try {
            return get(variant.getAssembleProvider())
        } catch (Exception e) {
            return variant.getAssemble()
        }
    }

    def static getManifestOutputFile(def task) {
        try {
            return get(task.getManifestOutputFile())
        } catch (Exception e) {
            try {
                return new File(task.getManifestOutputDirectory().get().getAsFile(), "AndroidManifest.xml")
            } catch (Exception e2) {
                try {
                    return task.getManifestOutputFile()
                } catch (Exception e3) {
                    return new File(task.getManifestOutputDirectory(), "AndroidManifest.xml")
                }
            }
        }
    }

    def static getInstantRunManifestOutputFile(def task){
        try{
            return get(task.getInstantRunManifestOutputFile())
        }catch(Exception e){
            try {
                return new File(task.getInstantRunManifestOutputDirectory().get().getAsFile(), "AndroidManifest.xml")
            } catch (Exception e2) {
                try {
                    return task.getInstantRunManifestOutputFile()
                } catch (Exception e3) {
                    return new File(task.getInstantRunManifestOutputDirectory(), "AndroidManifest.xml")
                }
            }
        }
    }

    def static get(def provider) {
        return provider == null ? null : provider.get()
    }
}