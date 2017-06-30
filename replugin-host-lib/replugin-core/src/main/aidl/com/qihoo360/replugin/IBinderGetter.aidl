package com.qihoo360.replugin;

/**
 * Binder的获取器，可用于延迟加载IBinder的情况。
 * <p>
 * 目前用于：
 * <p>
 * * RePlugin.registerGlobalBinderDelayed
 *
 * @author RePlugin Team
 */
interface IBinderGetter {

    /**
     * 获取IBinder对象
     */
    IBinder get();
}
