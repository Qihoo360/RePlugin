# RePlugin

首先感谢您关注RePlugin项目。目前RePlugin正通过严苛的**360安全及代码审核**（也是为了能用到更安全的方案），360手机卫士的团队成员在为开源前做最后的冲刺！

（目前RePlugin**已有15款应用接入**，下面会有介绍。所以我们要：稳，稳！）

我们理解大家的期待。所以，在和集团安全审核部门通力协作的同时，我们每完成一个项目的审核，且测试通过后，就**逐步**公开其成果（共5个步骤），直到**很快的、彻底的开源**。因此，请大家**点击右上角的“Watch”，随时收到最新的进展**。在此感谢大家的理解和支持！

此外，由于方案的核心功能都已经过多年的验证，故**我们“不搞内测”，而是直接开源上线。您们拿到的，就是我们在用的**。

开源进度：■■■■■■■■■■□□ **[ 96.5% ]**

开源进展，**6月29日更新：（2/5）发布“动态编译方案”（Gradle）的完整代码**

剧透：在6月30日，我们会公开**Sample工程**（即将审完）供大家参阅。同时正搞定 JCenter（他们也会审）。

对了，祖传一个小技巧：听妈妈说，**加“Star”越多，开源进度越快哦（右上角）**。是不是已经感觉到了？ :-)

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

## 常见问题

以下针对在Issue中提到的“最常见问题”做出回答，希望能对您有帮助。

#### Q：您们和360之前发的DroidPlugin的主要区别是什么？
A：
其实最主要的技术区别就一个：**Hook点的多少**。

DroidPlugin要Hook的点非常多，好处是可以让APK“直接运行在主程序中”，但代价是失去了一定的稳定性（这点非常关键）。

相反，RePlugin只Hook了ClassLoader，所以极为稳定，且同样支持绝大多数单品的特性。而APK只需要通过我们的Gradle编译，无需开发者修改Java Code，即可运行在主程序中。但出于稳定性考虑，我们不支持（也不打算支持）“直接将APK，不经修改就放到主程序中”。

当然，还有一点，就是使用场景的不同：

DroidPlugin解决的是各个独立功能拼装在一起，能够快速发布，不需要有任何的交互。目前市面上的一些双开应用，和DroidPlugin的思路有共同之处。当然了，要做到完整的双开，则仍需要大量的修改，如Native Hook等。

RePlugin解决的是各个功能模块能独立升级，又能需要和宿主之间有一定交互。其宗旨是“允许插件少量修改，来换得最佳稳定和灵活”。

另外在项目启动时间上，RePlugin最早是2014年中旬用于360手机卫士（完整占坑类方案），DroidPlugin是2015年下旬用于360手机助手。

#### Q：您们是否支持DataBinding？

A：
支持。我们有几个插件在用。除此之外，我们的Sample工程，其Demo2就是用DataBinding做的，而Demo1是ButterKnife。您们可以体验一下。

#### Q：插件可以复用主程序的资源吗？

A：
可以的，RePlugin会同时把Host和Plugin的Context传递给插件，供开发者选择。

这块儿后续我们还会有文章来专门介绍，敬请期待。


## 已接入RePlugin的应用

我们诚挚期待您成为咱们RePlugin应用大家庭中的一员！

以下是RePlugin接入的应用情况（包括之前的卫士插件化框架）：

* 360 手机卫士
* 360 手机助手
* 360 清理大师
* 360 影视大全
* 360 浏览器
* 花椒相机
* 360OS 应用
* 还有几个”孵化项目”

怎么都是360集团旗下的亿级别应用？当然不仅如此，我们还有一些对**稳定要求极其严苛的“金融类”产品**，及第三方应用，也接入了RePlugin（目前为SDK）：

* 360 借条
* 借钱吧
* 海淘一号
* 华润通
* 借了吗

其中，**360手机助手App——即DroidPlugin的创始团队的应用——，现已“全面切换到RePlugin”上**。

这里**衷心感谢** “360手机助手”，以及其它各App团队成员，帮助我们发现了很多需要改进的地方，并给予了非常积极的反馈。您们的鼓励与支持，让咱们的RePlugin能走的更远、更好！

// TODO 说好的图和链接呢？稍安勿躁

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