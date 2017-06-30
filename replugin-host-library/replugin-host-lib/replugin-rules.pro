# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ./sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# ——————————————————————————————————————
# aar 的接入方在构建 apk 时会使用的混淆规则
#
# 即：此处 keep 的内容可以被宿主'插件'调用，宿主的 Release 中，以下内容也可以访问。
# 注：因为 proguardFiles 也添加了此文件，所以生成 aar 包时，也会应用此混淆规则。
# ————————————————————————————————————————————————————————————————————————————

# RePlugin混淆时必须用到的。有两个目的：
# 1. 不添加该方法，则插件在运行时会抛出各种异常（因为需要调用一些Keep住的类）
# 2. 防止恶意开发者对RePlugin做反编译，并对您的应用做出一些破坏性的处理

# ——————————————————————————————————————

# 混淆时将LogDebug全部干掉，防止泄露到外界。需使用sdk/proguard-android-optimize.txt
# 【有问题，已注释掉】，因为它对“带操作符”和“需要函数调用”的地方不会被优化掉。如，原来是
#       LogDebug.d("XXX", "YYY" + a);
# 优化后变成：
#       new StringBuilder("YYY").append(a);
# 等于还是输出了Log。
# -assumenosideeffects class com.qihoo360.replugin.helper.LogDebug

# -keepattributes SourceFile,LineNumberTable
-keepattributes LineNumberTable

# 架构基础类
-keep class com.qihoo360.replugin.RePlugin {
    public protected *;
}
# LocalBroadcastManager，插件会用
-keep public class android.support.v4.content.LocalBroadcastManager {
    public *;
}

# 架构具体实现，和插件反射调用部分
-keep class com.qihoo360.replugin.model.PluginInfo {
    public protected *;
}
-keep class com.qihoo360.replugin.IBinderGetter {
     public protected *;
}
-keep class com.qihoo360.replugin.component.ComponentList {
    public protected *;
}
-keep class com.qihoo360.framework.** {
    public protected *;
}
-keep class com.qihoo360.i.** {
    public protected *;
}
-keep class com.qihoo360.plugins.** {
    public protected *;
}
-keep class com.qihoo360.plugin.** {
    public protected *;
}
-keep class com.qihoo360.replugin.component.dummy.** {
    public protected *;
}
-keep class com.qihoo360.replugin.component.provider.PluginProviderClient {
    public protected *;
}
-keep class com.qihoo360.replugin.component.provider.PluginProviderClient2 {
    public protected *;
}
-keep class com.qihoo360.replugin.component.service.PluginServiceClient {
    public protected *;
}
-keep class com.qihoo360.replugin.component.provider.PluginPitProviderP0 { public protected *; }
-keep class com.qihoo360.replugin.component.provider.PluginPitProviderP1 { public protected *; }
-keep class com.qihoo360.replugin.component.provider.PluginPitProviderP2 { public protected *; }

# ProcessPitProviderP0 未被自动 keep
-keep class com.qihoo360.replugin.component.process.ProcessPitProviderP0 { public protected *; }
-keep class com.qihoo360.replugin.component.process.ProcessPitProviderP1 { public protected *; }
-keep class com.qihoo360.replugin.component.process.ProcessPitProviderP2 { public protected *; }

# TODO 可能要废弃的类。目前旧卫士插件在用
# Pref
-keep public class com.qihoo360.mobilesafe.api.Pref {
    public *;
}
# IPC
-keep public class com.qihoo360.mobilesafe.api.IPC {
    public *;
}
# QihooServiceManager
-keep public class com.qihoo360.mobilesafe.svcmanager.QihooServiceManager {
    public *;
}
# Old PPC/PSC
-keep class com.qihoo360.loader2.mgr.PluginProviderClient {
    public protected *;
}
-keep class com.qihoo360.loader2.mgr.PluginServiceClient {
    public protected *;
}

# ------------ keep 以下类，以防卫士主程序 AOP DEBUG 失败 ------------
-keep class com.qihoo360.replugin.component.activity.ActivityInjector { *;}

# replugin-host-gradle 生成的 java 文件
-keep class com.qihoo360.replugin.gen.RePluginHostConfig { public *; }
