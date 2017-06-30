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
# 构建 aar 时的混淆规则。
# 即：此处 keep 的内容可以被宿主调用，但是宿主的 Release 包中，以下内容'可能'无法访问。
# ——————————————————————————————————————————————————————————————————————————————

# -keepattributes SourceFile,LineNumberTable
-keepattributes LineNumberTable

-keep class com.qihoo360.replugin.** { public protected *;}

# 架构具体实现，和插件反射调用部分
-keep class com.qihoo360.replugin.model.PluginInfo {
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
-keep class com.qihoo360.replugin.component.service.PluginServiceClient {
    public protected *;
}

-keep class com.qihoo360.i.IModule { public *;}
-keep class com.qihoo360.i.Factory2 { public *;}
-keep class com.qihoo360.i.IPluginManager { public *;}

-keep class com.qihoo360.loader.utils.SysUtils { public *;}
-keep class com.qihoo360.loader.utils.StringUtils { public *;}
-keep class com.qihoo360.loader.utils.ReflectUtils { public *;}

-keep class com.qihoo360.loader2.MP { public *;}
-keep class com.qihoo360.loader2.PMF { public *;}
-keep class com.qihoo360.loader2.PmBase { public *;}
-keep class com.qihoo360.loader2.sp.IPref { public *;}
-keep class com.qihoo360.loader2.CertUtils { public *;}
-keep class com.qihoo360.loader2.V5FileInfo { public *;}
-keep class com.qihoo360.loader2.PluginDesc { public *;}
-keep class com.qihoo360.loader2.PmLocalImpl { public *;}
-keep class com.qihoo360.loader2.sp.PrefImpl { public *;}
-keep class com.qihoo360.loader2.BinderCursor { public *;}
-keep class com.qihoo360.loader2.PluginManager { public *;}
-keep class com.qihoo360.loader2.MP$PluginBinder { public *;}
-keep class com.qihoo360.loader2.PluginProviderStub { public * ;}
-keep class com.qihoo360.replugin.component.provider.PluginPitProviderUI { public *;}
-keep class com.qihoo360.loader2.PluginStatusController { public *;}
-keep class com.qihoo360.replugin.component.utils.PluginClientHelper { public * ;}
-keep class com.qihoo360.replugin.component.provider.PluginPitProviderPersist { public *;}
-keep class com.qihoo360.loader2.ActivityLifecycleCallbacks { public *;}
-keep class com.qihoo360.replugin.component.app.PluginApplicationClient { public * ;}
-keep class com.qihoo360.replugin.component.activity.DynamicClassProxyActivity { public *;}

-keep class com.qihoo360.mobilesafe.api.IPC { public *;}
-keep class com.qihoo360.mobilesafe.api.Pref { public *;}
-keep class com.qihoo360.mobilesafe.api.Tasks { public *;}
-keep class com.qihoo360.mobilesafe.api.Intents { public *;}
-keep class com.qihoo360.loader2.PluginOverride { public *;}

-keep class com.qihoo360.replugin.component.process.ProcessPitProviderPersist { public * ;}
-keep class com.qihoo360.replugin.component.process.ProcessPitProviderBase { public * ;}

