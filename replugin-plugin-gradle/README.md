# RePlugin Plugin Gradle

RePlugin Plugin Gradle是一个Gradle插件，由 **插件** 负责引入。

该Gradle插件主要负责在插件的编译期中做一些事情，是“动态编译方案”的主要实现者。此外，开发者可通过修改其属性而做一些自定义的操作。

大致包括：

* 动态修改主要调用代码，改为调用RePlugin Plugin Gradle（如Activity的继承、Provider的重定向等）

开发者需要依赖此Gradle插件，以实现对RePlugin的接入。请参见WiKi以了解接入方法。

有关RePlugin Host Gradle的详细描述，请访问我们的WiKi，以了解更多的内容。
（文档正在完善，请耐心等待）