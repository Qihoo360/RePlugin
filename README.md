<p align="center">
  <a href="https://github.com/Qihoo360/RePlugin/wiki">
    <img alt="RePlugin Logo" src="https://github.com/Qihoo360/RePlugin/wiki/img/RePlugin.png" width="400"/>
  </a>
</p>

[![license](http://img.shields.io/badge/license-Apache2.0-brightgreen.svg?style=flat)](https://github.com/Qihoo360/RePlugin/blob/master/LICENSE)
[![Release Version](https://img.shields.io/badge/release-2.1.0-brightgreen.svg)](https://github.com/Qihoo360/RePlugin/wiki/%E5%8F%91%E8%A1%8C%E6%B3%A8%E8%AE%B0)


## RePlugin —— 历经三年多考验，数亿设备使用的，稳定占坑类插件化方案

RePlugin是一套完整的、稳定的、适合全面使用的，占坑类插件化方案，由360手机卫士的RePlugin Team研发，也是业内首个提出”全面插件化“（全面特性、全面兼容、全面使用）的方案。

其主要优势有：
* **极其灵活**：主程序无需升级（无需在Manifest中预埋组件），即可支持新增的四大组件，甚至全新的插件
* **非常稳定**：Hook点仅有一处（ClassLoader）。其**崩溃率仅为“万分之一”，并完美兼容市面上近乎所有的Android ROM**
* **特性丰富**：支持近乎所有在“单品”开发时的特性。**包括静态Receiver、Task-Affinity坑位、自定义Theme、进程坑位、AppCompat、DataBinding等**
* **易于集成**：无论插件还是主程序，**只需“数行”就能完成接入**
* **管理成熟**：拥有成熟稳定的“插件管理方案”，支持插件安装、升级、卸载、版本管理，甚至包括进程通讯、协议版本、安全校验等
* **数亿支撑**：有360手机卫士庞大的**数亿**用户做支撑，**三年多的残酷验证**，确保App用到的方案是最稳定、最适合使用的

截止2017年6月底，RePlugin的：

| 特性 | 描述 |
|:-------------:|:-------------:|
| **插件数** | **103（核心57个）** |
| **插件占应用比** | **高达83%** |
| **年发版次数** | **高达596次（工作日均2次）** |
| **崩溃率** | **万分之一（0.01%），极低** |
| **时间** | **2014年应用，3年验证** |

目前360公司几乎**所有的亿级用户量的APP**，以及多款主流第三方APP，都采用了RePlugin方案。

有关RePlugin的详细介绍，请[点击这里阅读《RePlugin 官方 WiKi》](https://github.com/Qihoo360/RePlugin/wiki)。

### 我们还支持以下特性

| 特性 | 描述 |
|:-------------:|:-------------:|
| 组件 | **四大组件（含静态Receiver）** |
| 升级无需改主程序Manifest | **完美支持** |
| Android特性 | **支持近乎所有（包括SO库等）** |
| 插件类型 | **支持自带插件（*自识别*）、外置插件** |
| TaskAffinity & 多进程 | **支持（*坑位方案*）** |
| 插件间耦合 | **支持Binder、Class Loader、资源等** |
| 进程间通讯 | **支持同步、异步、Binder、广播等** |
| 自定义Theme & AppComat | **支持** |
| DataBinding | **支持** |
| 安全校验 | **支持** |
| 资源方案 | **独立资源 + Context传递（相对稳定）** |
| Android 版本 | **API Level 9+ （2.3及以上）** |

## 愿景

让插件化能**飞入寻常应用家**，做到稳定、灵活、自由，大小项目兼用。

## RePlugin 架构图

<p align="center">
  <a href="https://github.com/Qihoo360/RePlugin/wiki">
    <img alt="RePlugin Framework" src="https://github.com/Qihoo360/RePlugin/wiki/img/RePluginFramePic.jpeg" height="600" />
  </a>
</p>

以360手机卫士为例：

* **系统层——Android**：为Android Framework层。**只有ClassLoader是Hook的**，而AMS、Resources等都没有做Hook，确保了其稳定性。
* **框架层——RePlugin框架**：RePlugin框架层，**只有RePlugin是对“上层完全公开”的**，其余均为Internal，或“动态编译方案”生效后的调用，对开发者而言是“无需关心”的。
* **插件层——各插件**：“标蓝部分”是各插件，包括大部分的业务插件（如体检、清理、桌面插件等）。而其中“标黄部分”是支撑一个应用的各种基础插件，如WebView、Download、Share，甚至Protobuf都能成为基础插件。

## 使用方法

RePlugin的使用方法非常简单，大部分情况下和“单品”开发无异。

若您是第一次接触RePlugin，则[请点击这里阅读《快速上手》](https://github.com/Qihoo360/RePlugin/wiki/%E5%BF%AB%E9%80%9F%E4%B8%8A%E6%89%8B)，跟随我们的指引，了解更多的内容。

若您想了解更多有关RePlugin的玩法，则[请点击这里阅读《详细教程》](https://github.com/Qihoo360/RePlugin/wiki/%E8%AF%A6%E7%BB%86%E6%95%99%E7%A8%8B)，了解更多好玩的玩法。

若您想看下RePlugin的Sample工程，进而了解框架的具体用法，则[请点击这里查看Sample源代码](https://github.com/Qihoo360/RePlugin/blob/master/replugin-sample)。

## 常见问题

以下针对在Issue中提到的“最常见问题”做出回答，希望能对您有帮助。

* Q：您们和360之前发的DroidPlugin的主要区别是什么？
* Q：插件与插件、主程序间可以共用资源吗？
* Q：您们是否支持DataBinding？

#### Q：您们和360之前发的DroidPlugin的主要区别是什么？
A：这个问题问得很好。很多人都有这个疑惑——“*为什么你们360要开发两套不同的插件化框架呢*”？

其实归根结底，最根本的区别是——**目标的不同**：

* DroidPlugin主要解决的是各个独立功能拼装在一起，能够快速发布，其间不需要有任何的交互。**目前市面上的一些双开应用，和DroidPlugin的思路有共同之处**。当然了，要做到完整的双开，则仍需要大量的修改，如Native Hook等。

* RePlugin解决的是各个功能模块能独立升级，又能需要和宿主、插件之间有一定交互和耦合。

此外，从技术层面上，其最核心的区别就一个：**Hook点的多少**。

* DroidPlugin可以做到**让APK“直接运行在主程序”中**，无需任何额外修改。但需要Hook大量的API（包括AMS、PackageManager等），在适配上需要做大量的工作。

* RePlugin只Hook了ClassLoader，所以**极为稳定**，且同样**支持绝大多数单品的特性**，但需要插件做“少许修改”。好在作为插件开发者而言无需过于关心，因为通过“动态编译方案”，开发者可做到“无需开发者修改Java Code，即可运行在主程序中”的效果。

可以肯定的是，**DroidPlugin也是一款业界公认的，优秀的免安装插件方案**。我相信，随着时间的推移，RePlugin和DroidPlugin会分别在各自领域（全面插件化 & 应用免安装）打造出属于自己的一番天地。

#### Q：插件与插件、主程序间可以共用资源吗？

A：可以的，RePlugin会同时把Host和Plugin的Context传递给插件，供开发者选择。

补充：目前业内用得较多的，我们称之为“共享资源”方案——也即需要修改Aapt、做addAssetPath，以达到“宿主和插件用同一套资源”的效果。其好处是插件和主程序可以“直接使用”各自的资源，交互容易。**但代价是需要做“资源分区”，以及针对不同机型（如ZTEResources、MiuiResources）等做适配，稳定性上值得考量**。

> 细心的朋友，可以看一下360手机卫士在2013年的APK，会发现一个小惊喜：其实，我们当年就是这个方案（彼时业内还没有相应公开方案）

而对于RePlugin而言，**稳定永远是第一要义**，因此我们采用的**是“独立资源”方案，插件与应用间是完全独立的**，每个插件都是一个Resources。

但是，“独立”不代表“不能共用”。想象一下，我们卫士有很多插件（如WebView等）都是需要将自己的资源（如Layout-XML等）共享给宿主和其它插件。这时候我们的做法是有两种：
* 可以直接通过调用RePlugin.fetchResources接口，直接拿到资源
* 通过反射获取View来间接拿到资源。

例如360手机助手所用的“换肤CommonView”就是采用第二种方式来做的，目前来看，没有问题。

#### Q：您们是否支持DataBinding？

A：支持。我们有几个插件在用。除此之外，我们的Sample工程，其Demo2就是用DataBinding做的，而Demo1是ButterKnife。您们可以体验一下。

#### Q: java.lang.ClassNotFoundException: Didn't find class "xxx.loader.p.ProviderN1"

A：通常遇到这个问题是因为没有在主程序的AndroidManifest.xml中声明Application，或在Application中没有调用RePlugin.App.attachBaseContext等方法导致。

请严格按照“[主程序接入指南](https://github.com/Qihoo360/RePlugin/wiki/%E4%B8%BB%E7%A8%8B%E5%BA%8F%E6%8E%A5%E5%85%A5%E6%8C%87%E5%8D%97)”所述来完成接入，一共只有三个步骤，非常简单。

当然，如果在严格按照接入文档后，仍出现这个问题（这种情况非常罕见），请向我们提交Issue。Issue中应包括：完整的Logcat信息（含崩溃前后上下文）、手机型号、ROM版本、Android版本等。感谢您的理解。


## 已接入RePlugin的应用

我们诚挚期待您成为咱们RePlugin应用大家庭中的一员！

除了**360集团旗下的亿级别应用**以外，还有一些对**稳定要求极其严苛的“金融类”产品**，及第三方合作应用也接入了RePlugin（含SDK）：

**360 手机卫士** | **360 手机助手** | **360 手机浏览器** | **花椒相机** | **360 清理大师** |
-------------------------------------------------------------------|----------|---------------|--------|--------------|
[<img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/mobilesafe.png" width="80" height="80">](https://shouji.360.cn/index.html) | [<img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/appstore.png" width="80" height="80">](http://sj.360.cn/index.html) | [<img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/browser.png" width="80" height="80">](http://mse.360.cn/m/index.html) | [<img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/camera.png" width="80" height="80">](http://xj.huajiao.com/xji/home/pc) | [<img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/clean.png" width="80" height="80">](https://shouji.360.cn/360cleandroid/index.html)| 
**360 影视大全** | **借钱吧** | **借了吗** | **海淘一号** | **华润通** |
[<img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/movie.png" width="80" height="80">](http://www.360kan.com/appdownload) | [<img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/jieqianba.png" width="80" height="80">](http://www.jielem.com/) | <img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/jielema.png" width="80" height="80"> | [<img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/haitao1hao.png" width="80" height="80">](http://www.1haitao.com/) | [<img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/huaruntong.png" width="80" height="80">](http://www.huaruntong.com/)| 
**360OS 应用** | **360借条** | **（即将公开）** | **（即将公开）** | **你的App** |
[<img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/360os.jpg" width="80" height="80">](http://www.qiku.com/product/360os2/index.html) | [<img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/360jietiao.jpg" width="80" height="80">](https://www.360jie.com.cn/) | <img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/yourapps.png" width="80" height="80"> | <img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/yourapps.png" width="80" height="80"> | <img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/yourapps.png" width="80" height="80">| 

这里**衷心感谢** “360手机助手”，以及其它各App团队成员，帮助我们发现了很多需要改进的地方，并给予了非常积极的反馈。您们的鼓励与支持，让咱们的RePlugin能走的更远、更好！

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
