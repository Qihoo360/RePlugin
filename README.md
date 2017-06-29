# RePlugin

首先感谢您关注RePlugin项目。目前RePlugin正通过严苛的**360安全及代码审核**（也是为了能用到更安全的方案），360手机卫士的团队成员在为开源前做最后的冲刺！

但我们知道大家的期待。所以，我们不会等所有审核通过再发，而是每完成一个项目的审核，就**逐步**公开其成果（共5个步骤），直到**几天后的彻底开源**。因此，请大家**点击右上角的“Watch”，随时收到最新的进展**。在此感谢大家的理解和支持！

此外，由于方案已经过验证，故**我们“不搞内测”，而是直接开源上线。您们拿到的，就是我们在用的**。

开源进度：■■■■■■■■■■□□ **[ 96.5% ]**

开源进展，**6月29日更新：（2/5）发布“动态编译方案”（Gradle）的完整代码**

剧透：在6月30日，我们会公开**Sample工程**（即将审完）供大家参阅。同时正搞定 JCenter（他们也会审）。

对了，祖传一个小技巧：听妈妈说，**加“Star”越多，开源进度越快哦（右上角）**。你看，本来说7月初的，这不，加快了吗？ :-)

- - -

RePlugin是一套完整的、稳定的、适合全面使用的，占坑类插件化方案。其主要优势有：
* **极其灵活**：主程序无需升级（无需在Manifest中预埋组件），即可支持新增的四大组件，甚至全新的插件
* **非常稳定**：Hook点仅有一处（ClassLoader）。其崩溃率仅为“万分之一”，并完美兼容市面上近乎所有的Android ROM
* **特性丰富**：支持近乎所有在“单品”开发时的特性。包括静态Receiver、Task-Affinity坑位、自定义Theme、进程坑位、AppCompat、DataBinding等
* **易于集成**：无论插件还是主程序，只需“数行”就能完成接入
* **进程任意**：可让各组件跑在UI、常驻，甚至是“**任意坑位进程**”
* **自由隔离**：想隔离就隔离（如不稳定或占资源的插件，易于释放），不想隔离的模块就混用（如各种基础、UI插件，都跑在UI进程内，性能优异）
* **管理成熟**：拥有成熟稳定的“插件管理方案”，支持插件安装、升级、卸载、版本管理，甚至包括进程通讯、协议版本、安全校验等
* **数亿支撑**：有360手机卫士庞大的**数亿**用户做支撑，**三年多的残酷验证**，确保App用到的方案是最稳定、最适合使用的

截止2017年6月底，RePlugin的：

* **插件数 已达102个**（其中，核心插件57个）
* **插件占应用比 高达83%**（指把代码资源铺开，插件占整个应用的比例）
* **年发版次数 高达596次**（平均每个工作日发版2-3次）

目前360公司几乎**所有的亿级用户量的APP**，以及多款主流第三方APP，都采用了RePlugin方案。

支持Android 2.3+及以上版本

有关RePlugin的详细介绍，请[点击这里阅读《RePlugin 官方 WiKi》](https://github.com/Qihoo360/RePlugin/wiki)。

## 愿景
让插件化能**飞入寻常应用家**，做到稳定、灵活、自由，大小项目兼用。

## 使用方法

RePlugin的使用方法非常简单，大部分情况下和“单品”开发无异。

若您是第一次接触RePlugin，则[请点击这里阅读《快速上手》](https://github.com/Qihoo360/RePlugin/wiki/%E5%BF%AB%E9%80%9F%E4%B8%8A%E6%89%8B)，跟随我们的指引，了解更多的内容。

若您想了解更多有关RePlugin的玩法，则[请点击这里阅读《详细教程》](https://github.com/Qihoo360/RePlugin/wiki/%E8%AF%A6%E7%BB%86%E6%95%99%E7%A8%8B)，了解更多好玩的玩法。

若您想看下RePlugin的Sample工程，进而了解框架的具体用法，则[请点击这里查看Sample源代码](https://github.com/Qihoo360/RePlugin/blob/master/replugin-sample)。

## 已接入RePlugin的插件

目前已有的插件，可以分为以下几类，供各App开发者参考：
* **展示插件**：如**卫士首页**（是的，你没看错）、体检、信息流等
* **业务插件**：如清理、骚扰拦截、悬浮窗等
* **合作插件**：如程序锁、免费WiFi、安全桌面等
* **后台插件**：如Push、服务管理、Protobuf等
* **基础插件**：如安全WebView、分享、定位等

截止2017年6月底，这样的插件，我们有**102**个。衷心希望您能成为这个数字中的新的一员！

## 贡献自己的力量

我们欢迎任何形式的贡献，并致以诚挚的感谢！

你可以贡献代码、提出问题、编写文档等。有关“贡献”相关的内容，请[点击这里阅读《贡献力量》](https://github.com/Qihoo360/RePlugin/wiki/%E8%B4%A1%E7%8C%AE%E5%8A%9B%E9%87%8F)

## License

RePlugin is [Apache v2.0 licensed](./LICENSE).

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.