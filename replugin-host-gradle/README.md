# RePlugin Host Gradle

RePlugin Host Gradle是一个Gradle插件，由 **主程序** 负责引入。

该Gradle插件主要负责在主程序的编译期中做一些事情，此外，开发者可通过修改其属性而做一些自定义的操作。

大致包括：

* 生成带 RePlugin 插件坑位的 AndroidManifest.xml（允许自定义数量）
* 生成HostBuildConfig类，方便插件框架读取并自定义其属性

开发者需要依赖此Gradle插件，以实现对RePlugin的接入。请参见WiKi以了解接入方法。

有关RePlugin Host Gradle的详细描述，请访问我们的WiKi，以了解更多的内容。
（文档正在完善，请耐心等待）