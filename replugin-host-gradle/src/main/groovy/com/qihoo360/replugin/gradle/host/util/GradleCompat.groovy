package com.qihoo360.replugin.gradle.host.util

class GradleCompat {

    def static getMergeAssets(def variant) {
        try {
            return get(variant.getMergeAssetsProvider())
        } catch (Exception e) {
            return variant.getMergeAssets()
        }
    }

    def static getGenerateBuildConfig(def variant) {
        try {
            return get(variant.getGenerateBuildConfigProvider())
        } catch (Exception e) {
            return variant.getGenerateBuildConfig()
        }
    }

    def static getProcessManifest(def variant){
        try {
            return get(variant.getProcessManifestProvider())
        } catch (Exception e) {
            return variant.getProcessManifest()
        }
    }

    def static get(def provider) {
        return provider == null ? null : provider.get()
    }
}