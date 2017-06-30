<p align="center">
  <a href="https://github.com/Qihoo360/RePlugin/wiki">
    <img alt="RePlugin Logo" src="https://github.com/Qihoo360/RePlugin/wiki/img/RePlugin.png" width="400"/>
  </a>
</p>

[![license](http://img.shields.io/badge/license-Apache2.0-brightgreen.svg?style=flat)](https://github.com/Qihoo360/RePlugin/blob/master/LICENSE)
[![Release Version](https://img.shields.io/badge/release-2.1.0-brightgreen.svg)](https://github.com/Qihoo360/RePlugin/wiki/%E5%8F%91%E8%A1%8C%E6%B3%A8%E8%AE%B0)

# 号外！RePlugin 开·源·啦！

2017年6月30日23:51分，**RePlugin已完成全部测试**，并在保证一切通过的情况下，**正式公开源代码！**

感谢您们一直以来对关注RePlugin的关注和支持。希望大家能一如既往的支持我们，无论是加Star，还是提Issue，还是未来提PR，我们都非常的欢迎。

**让“全面插件化”（无论大小项目均可使用，稳定与灵活兼得）时代，因RePlugin和您的出现，而更加精彩！**

PS：RePlugin Team的全体成员，大家辛苦了！

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

### 除此之外，我们还支持

| 特性 | 描述 |
|:-------------:|:-------------:|
| **组件** | **四大组件 + Application** |
| **升级无需改主程序Manifest** | **完美支持** |
| **Android特性** | **支持近乎所有** |
| **插件类型** | **支持自带插件（*自识别*）、外置插件** |
| **TaskAffinity** | **支持（*坑位方案*）** |
| **多进程任意分配** | **支持** |
| **插件间耦合** | **支持Binder、Class Loader、资源等** |
| **进程间通讯** | **支持同步、异步、Binder、广播等** |
| **自定义Theme & AppComat** | **支持** |
| **DataBinding** | **支持** |
| **SO库随心用** | **支持** |
| **跨插件资源** | **支持** |
| **安全校验** | **支持** |
| **PendingIntent** | **支持** |
| **首页也能变插件** | **支持** |
| **兼容性** | **几乎所有的设备** |
| **Android版本** | **API Level 9+ （2.3及以上）** |

截止2017年6月底，RePlugin的：

| 特性 | 描述 |
|:-------------:|:-------------:|
| **插件数** | **103个** |
| **核心插件** | **57个** |
| **插件占应用比** | **高达83%** |
| **年发版次数** | **高达596次** |
| **平均每工作日发版** | **2~3次** |
| **崩溃率** | **万分之一（0.01%）** |
| **时间** | **2014年，3年验证** |

目前360公司几乎**所有的亿级用户量的APP**，以及多款主流第三方APP，都采用了RePlugin方案。

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

除了360集团旗下的亿级别应用，还有一些对**稳定要求极其严苛的“金融类”产品**，及第三方应用，也接入了RePlugin（目前为SDK）：

**360 手机卫士** | **360 手机助手** | **360 手机浏览器** | **花椒相机** | **360 清理大师** |
-------------------------------------------------------------------|----------|---------------|--------|--------------|
[<img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/mobilesafe.png" width="80" height="80">](https://shouji.360.cn/index.html) | [<img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/appstore.png" width="80" height="80">](http://sj.360.cn/index.html) | [<img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/browser.png" width="80" height="80">](http://mse.360.cn/m/index.html) | [<img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/camera.png" width="80" height="80">](http://xj.huajiao.com/xji/home/pc) | [<img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/clean.png" width="80" height="80">](https://shouji.360.cn/360cleandroid/index.html)| 
**360 影视大全** | **借钱吧** | **借了吗** | **海淘一号** | **华润通** |
[<img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/movie.png" width="80" height="80">](http://www.360kan.com/appdownload) | [<img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/jieqianba.png" width="80" height="80">](http://www.jielem.com/) | <img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/jielema.png" width="80" height="80"> | [<img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/haitao1hao.png" width="80" height="80">](http://www.1haitao.com/) | [<img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/huaruntong.png" width="80" height="80">](http://www.huaruntong.com/)| 
**360OS 应用** | **360借条** | **你的App** | **你的App** | **你的App** |
[<img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/360os.jpg" width="80" height="80">](http://www.qiku.com/product/360os2/index.html) | [<img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/360jietiao.jpg" width="80" height="80">](https://www.360jie.com.cn/) | <img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/yourapps.png" width="80" height="80"> | <img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/yourapps.png" width="80" height="80"> | <img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/yourapps.png" width="80" height="80">| 

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

截止2017年6月底，这样的插件，我们有**103**个。衷心希望您能成为这个数字中的新的一员！

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
