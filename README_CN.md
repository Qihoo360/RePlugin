<p align="center">
  <a href="https://github.com/Qihoo360/RePlugin/wiki">
    <img alt="RePlugin Logo" src="https://github.com/Qihoo360/RePlugin/wiki/img/RePlugin.png" width="400"/>
  </a>
</p>

[![license](http://img.shields.io/badge/license-Apache2.0-brightgreen.svg?style=flat)](https://github.com/Qihoo360/RePlugin/blob/master/LICENSE)
[![Release Version](https://img.shields.io/badge/release-2.2.4-brightgreen.svg)](https://github.com/Qihoo360/RePlugin/releases)


## RePlugin —— 历经三年多考验，数亿设备使用的，稳定占坑类插件化方案

RePlugin是一套完整的、稳定的、适合全面使用的，占坑类插件化方案，由360手机卫士的RePlugin Team研发，也是业内首个提出”全面插件化“（全面特性、全面兼容、全面使用）的方案。

其主要优势有：
* **极其灵活**：主程序无需升级（无需在Manifest中预埋组件），即可支持新增的四大组件，甚至全新的插件
* **非常稳定**：Hook点**仅有一处（ClassLoader），无任何Binder Hook**！如此可做到其**崩溃率仅为“万分之一”，并完美兼容市面上近乎所有的Android ROM**
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
| TaskAffinity & 多进程 | **支持（*坑位方案*）** |
| 插件类型 | **支持自带插件（*自识别*）、外置插件** |
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

若您是**第一次接触RePlugin，则[请点击这里阅读《快速上手》](https://github.com/Qihoo360/RePlugin/wiki/%E5%BF%AB%E9%80%9F%E4%B8%8A%E6%89%8B)**，跟随我们的指引，了解更多的内容。

若您想**了解更多有关RePlugin的玩法，则[请点击这里阅读《详细教程》](https://github.com/Qihoo360/RePlugin/wiki/%E8%AF%A6%E7%BB%86%E6%95%99%E7%A8%8B)**，了解更多好玩的玩法。

若您想**看下RePlugin的Sample工程，进而了解框架的具体用法，则[请点击这里查看Sample源代码](https://github.com/Qihoo360/RePlugin/blob/master/replugin-sample)**。

若您在接入RePlugin中**遇到了任何问题，则[请点击这里阅读《FAQ》](https://github.com/Qihoo360/RePlugin/wiki/FAQ)**，相信会有您想要的答案。

## 插件管理服务—与RePlugin配套的插件管理、下发、统计服务

至今为止有数不清的用户联系我们让做配套的插件管理功能，所以 RePlugin 团队联合 360 Web 平台部，合力推出 RePlugin 插件管理服务，再次大幅降低了用户使用RePlugin的门槛，插件管理服务功能介绍如下：

* **插件版本管理**：对APK插件包名、别名和版本号的交集限制，防止下发出错。

* **打点统计**：上报即显示下发量、下载量、安装量和错误量数据。

* **升版管理**：严格要求用户新建下发任务为面向虚拟用户或部分真实用户的“测试版”下发任务（适用于内测、AB和灰度），测试没问题以后才能切换到面对所有真实用户的“线上版”下发任务，防止出错。

* **下发限速**：开发者可自定义自己想要的插件下发速度。

* **运营商和厂商限制**：开发者可自定义自己想下发的运营商和目标终端厂商。

* **灵活的下发条件设置**：根据用户群体对下发条件的要求程度，我们提供了4种条件设置功能，对下发条件要求不高的用户可直接使用便捷条件下发（包括按人数和指定设备下发）；对下发条件要求高的用户可使用自定义条件下发（包括文字条件编辑器和代码条件编辑器）。

**PS**：我们原创的文字版条件编辑器很有意思哦，它能将复杂繁琐的条件代码还原成有语法有逻辑的中国话，真的是能让非技术人员第一次使用就看得懂会操作的优秀功能，目前为止我们应该是第一个将体验做到如此细腻的产品。

使用地址：[360移动开发者-RePlugin插件管理](https://dc.360.cn/)

## 已接入RePlugin的应用

我们诚挚期待您成为咱们RePlugin应用大家庭中的一员！

除了**360集团旗下的亿级别应用**以外，还有一些对**稳定要求极其严苛的“金融类”产品**，及第三方合作应用也接入了RePlugin（含SDK）：


<table align="center">
    <tr align="center">
        <td><img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/mobilesafe.png" width="80" height="80"/></td>
        <td><img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/appstore.png" width="80" height="80"/></td>
        <td><img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/browser.png" width="80" height="80"/></td>
        <td><img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/camera.png" width="80" height="80"/></td>
        <td><img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/clean.png" width="80" height="80"/></td>
    </tr>
    <tr align="center">
        <td><b><a href="https://shouji.360.cn/index.html">360 手机卫士</a></b></td>
        <td><b><a href="http://sj.360.cn/index.html">360 手机助手</a></b></td>
        <td><b><a href="http://mse.360.cn/m/index.html">360 手机浏览器</a></b></td>
        <td><b><a href="http://xj.huajiao.com/xji/home/pc">花椒相机</a></b></td>
        <td><b><a href="https://shouji.360.cn/360cleandroid/index.html">360 清理大师</a></b></td>
    </tr>
    <tr align="center">
        <td><img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/movie.png" width="80" height="80"/></td>
        <td><img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/jieqianba.png" width="80" height="80"/></td>
        <td><img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/haitao1hao.png" width="80" height="80"/></td>
        <td><img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/huaruntong.png" width="80" height="80"/></td>
        <td><img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/jielema.png" width="80" height="80"/></td>
    </tr>
    <tr align="center">
        <td><b><a href="http://www.360kan.com/appdownload">360 影视大全</a></b></td>
        <td><b><a href="">J借钱吧</a></b></td>
        <td><b><a href="http://www.1haitao.com/">海淘1号</a></b></td>
        <td><b><a href="http://www.huaruntong.com/">华润通</a></b></td>
        <td><b><a href="http://www.jielem.com/">借了吗</a></b></td>
    </tr>
    <tr align="center">
        <td><img src="https://raw.githubusercontent.com/wiki/Qihoo360/RePlugin/img/apps/qihoo_os.jpg" width="80" height="80"/></td>
        <td><img src="https://raw.githubusercontent.com/wiki/Qihoo360/RePlugin/img/apps/qihoo_jietiao.jpg" width="80" height="80"/></td>
        <td><img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/yourapps.png" width="80" height="80"/></td>
        <td><img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/yourapps.png" width="80" height="80"/></td>
        <td><img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/yourapps.png" width="80" height="80"/></td>
    </tr>
    <tr align="center">
        <td><b><a href="http://www.qiku.com/product/360os2/index.html">360OS 系统应用</a></b></td>
        <td><b><a href="https://www.360jie.com.cn/">360 借条</a></b></td>
        <td><b><a href="">(即将发布)</a></b></td>
        <td><b><a href="">(即将发布)</a></b></td>
        <td><b><a href="">(即将发布)</a></b></td>
    </tr>
</table>

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

## 与我们联系

欢迎您加入到我们的RePlugin微信群、QQ群大家庭。

微信群已超过上限，请进入我们的QQ群

QQ群 1：**653205923** QQ群 2：**589652294**

## License

RePlugin is [Apache v2.0 licensed](./LICENSE).
