package com.qihoo360.replugin;

/**
 * PluginDexClassLoader's Patch
 *
 * @author RePlugin Team
 */
public class PluginDexClassLoaderPatch {

    // org.apache.http.legacy.jar 中的包
    private static final String[] APACHE_HTTP_LEGACY_PACKAGES = {
            "android.net.http", "android.net.compatibility", "com.android.internal.http.multipart",
            "org.apache.commons.codec", "org.apache.commons.logging", "org.apache.http"
    };

    // OkHttp3 中的包
    private static final String[] OKHTTP3_PACKAGES = {
            "okhttp3", "okio"
    };

    /**
     * 当一个类，从插件中找不到时，是否需要再从宿主中找一找
     *
     * @param className
     * @return
     */
    public static boolean need2LoadFromHost(String className) {
        return isOkHttp3(className) || isApacheHttpLegacy(className) ;
    }

    /**
     * 是否为 org.apache.http.legacy.jar 中的类
     * <p>
     * Android P 之前，org.apache.http.legacy.jar 是被 BootClassLoader 加载的。
     * Android P， org.apache.http.legacy.jar 改成了被 PathClassLoader 加载。
     * <p>
     * 带来的影响：插件ClassLoader，如果parent ClassLoader是BootClassLoader，就肯定找不到org.apache.http.legacy.jar中的类
     * 这时候，需要使用hostClassLoader加载一下
     *
     * @param className
     */
    private static boolean isApacheHttpLegacy(String className) {
        for (String packagePrefix : APACHE_HTTP_LEGACY_PACKAGES) {
            if (className.startsWith(packagePrefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否为 OkHttp3 中的类
     *
     * @param className
     */
    private static boolean isOkHttp3(String className) {
        for (String packagePrefix : OKHTTP3_PACKAGES) {
            if (className.startsWith(packagePrefix)) {
                return true;
            }
        }
        return false;
    }
}