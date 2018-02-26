<p align="center">
  <a href="https://github.com/Qihoo360/RePlugin/wiki">
    <img alt="RePlugin Logo" src="https://github.com/Qihoo360/RePlugin/wiki/img/RePlugin.png" width="400"/>
  </a>
</p>

[![license](http://img.shields.io/badge/license-Apache2.0-brightgreen.svg?style=flat)](https://github.com/Qihoo360/RePlugin/blob/master/LICENSE)
[![Release Version](https://img.shields.io/badge/release-2.2.4-brightgreen.svg)](https://github.com/Qihoo360/RePlugin/releases)


## RePlugin —— A flexible, stable, easy-to-use Android Plug-in Framework

RePlugin is a complete Android plug-in solution which is suitable for general use.

（[文档，还是中文的好](./README_CN.md)）

It is major strengths are:
* **Extreme flexibility**: Apps do not need to be upgraded to support new components, **even brand new plug-ins**.
* **Extraordinary stability**: With only **ONE** hook (ClassLoader), **NO BINDER HOOK**. RePlugin’s Crash ratio is **as low as Ten thousandth (0.01%)**. In addition, RePlugin is compatible with almost **ALL Android ROMs** in the market.
* **Rich features**: RePlugin supports **almost all features seamlessly as an installed application**, including static Receiver, Task-Affinity, user-defined Theme, AppCompat, DataBinding, etc.
* **Easy integration**: It takes only couple lines to access, whether plug-ins or main programs. 
* **Mature management**:　RePlugin owns stable plug-in management solution which supports installation, upgrade, uninstallation and version management. Process communication, protocol versions and security check are also included. 
* **Hundreds of millions support**: RePlugin possesses **hundreds of millions users from 360 MobileSafe.** After more than three-year verification, we guarantee the solution that Apps use is the most stable and suitable.

By the end of June 2017, RePlugin has already made some achievements:

| Feature | Achievement |
|:-------------:|:-------------:|
| **Plug-in Number** | **103** |
| **Ratio of plug-ins to applications** | **83%** |
| **Version released pre year** | **596** |
| **Crash** | **0.01%, Extraordinary stability** |
| **First Release** | **2014** |

At present, almost **all Apps with hundreds of millions users from 360, and many mainstream third-party Apps, are using RePlugin solution**. 

### We support:

| Feature | Description |
|:-------------:|:-------------:|
| Components | **Activity, Service, Provider, Receiver(Including static)** |
| Not need to upgrade when brand a new Plug-in | **Supported** |
| Android Feature | **Supported almost all features** |
| TaskAffinity & Multi-Process | **Perfect supported!** |
| Support Plug-in Type | **Built-in (Only Two Step) and External(Download)** |
| Plug-in Coupling | **Binder, Class Loader, Resources, etc.** |
| Interprocess communication | **Sync, Async, Binder and Cross-plug-in broadcast** |
| User-Defined Theme & AppComat | **Supported** |
| DataBinding | **Supported** |
| Safety check when installed | **Supported** |
| Resources Solution | **Independent Resources + Context pass(No Adaptation ROM)** |
| Android Version | **API Level 9 (Android 2.3 and above)** |

## Our Vision
Make RePlugin be used in all kinds of ordinary Apps; and provide stable, flexible, liberal plug-ins which adopt for both large and small projects.

## RePlugin Architecture

<p align="center">
  <a href="https://github.com/Qihoo360/RePlugin/wiki">
    <img alt="RePlugin Framework" src="https://github.com/Qihoo360/RePlugin/wiki/img/RePluginFramePic.jpeg" height="600" />
  </a>
</p>

## How to Use RePlugin
Using RePlugin is very simple. Under most conditions, using it is no different than developing an App.

If you are **the first-time user, please [click here to read Quick Start Guide(Chinese Version)](https://github.com/Qihoo360/RePlugin/wiki/%E5%BF%AB%E9%80%9F%E4%B8%8A%E6%89%8B).** Following our guide, you will learn more about RePlugin.

If you wish to **learn more gameplays about RePlugin, please [click here to read Step-by-step Tutorial(Chinese Version)](https://github.com/Qihoo360/RePlugin/wiki/%E8%AF%A6%E7%BB%86%E6%95%99%E7%A8%8B)**.

If you want to **view RePlugin’s sample project, and learn concrete usage of the frame, please [click here to check Sample SC](https://github.com/Qihoo360/RePlugin/blob/master/replugin-sample)**.

If you **have any question, please [click here to read FAQ(Chinese Version)](https://github.com/Qihoo360/RePlugin/wiki/FAQ)**.


## These apps are using RePlugin

<table align="center">
    <tr align="center">
        <td><img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/mobilesafe.png" width="80" height="80"/></td>
        <td><img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/appstore.png" width="80" height="80"/></td>
        <td><img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/browser.png" width="80" height="80"/></td>
        <td><img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/camera.png" width="80" height="80"/></td>
        <td><img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/clean.png" width="80" height="80"/></td>
    </tr>
    <tr align="center">
        <td><b><a href="https://shouji.360.cn/index.html">360 Mobile Safe</a></b></td>
        <td><b><a href="http://sj.360.cn/index.html">360 App Store</a></b></td>
        <td><b><a href="http://mse.360.cn/m/index.html">360 Mobile Browser</a></b></td>
        <td><b><a href="http://xj.huajiao.com/xji/home/pc">HuaJiao Camera</a></b></td>
        <td><b><a href="https://shouji.360.cn/360cleandroid/index.html">360 Clean Master</a></b></td>
    </tr>
    <tr align="center">
        <td><img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/movie.png" width="80" height="80"/></td>
        <td><img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/jieqianba.png" width="80" height="80"/></td>
        <td><img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/haitao1hao.png" width="80" height="80"/></td>
        <td><img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/huaruntong.png" width="80" height="80"/></td>
        <td><img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/jielema.png" width="80" height="80"/></td>
    </tr>
    <tr align="center">
        <td><b><a href="http://www.360kan.com/appdownload">360 Kan Movie</a></b></td>
        <td><b><a href="">JieQianBa</a></b></td>
        <td><b><a href="http://www.1haitao.com/">1 HaiTao</a></b></td>
        <td><b><a href="http://www.huaruntong.com/">HuaRun Tong</a></b></td>
        <td><b><a href="http://www.jielem.com/">JieLeMa</a></b></td>
    </tr>
    <tr align="center">
        <td><img src="https://raw.githubusercontent.com/wiki/Qihoo360/RePlugin/img/apps/qihoo_os.jpg" width="80" height="80"/></td>
        <td><img src="https://raw.githubusercontent.com/wiki/Qihoo360/RePlugin/img/apps/qihoo_jietiao.jpg" width="80" height="80"/></td>
        <td><img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/yourapps.png" width="80" height="80"/></td>
        <td><img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/yourapps.png" width="80" height="80"/></td>
        <td><img src="https://github.com/Qihoo360/RePlugin/wiki/img/apps/yourapps.png" width="80" height="80"/></td>
    </tr>
    <tr align="center">
        <td><b><a href="http://www.qiku.com/product/360os2/index.html">360OS App</a></b></td>
        <td><b><a href="https://www.360jie.com.cn/">360 Loan</a></b></td>
        <td><b><a href="">(Internal App)</a></b></td>
        <td><b><a href="">(Internal App)</a></b></td>
        <td><b><a href="">(Internal App)</a></b></td>
    </tr>
</table>

## Plug-ins Accessed in RePlugin

For your reference, plug-ins accessed can be classified into following categories: 

* **Expo plug-ins**: Safe Home Page, physical examination, information flow, etc. 
* **Business plug-ins**: cleaning, disturbance intercept, floating window, etc.
* **Cooperation plug-ins**: App Lock, free Wi-Fi, security desktop, etc.
* **Background plug-ins**: Push, service management, Protobuf, etc.
* **Base plug-ins**: Security WebView, share, location service, etc.

By the end of June 2017, we already have 102 plug-ins like these. We look forward to you becoming a part of RePlugin family!

## Contribute Your Share
We sincerely welcome and appreciate your contribution of any kind. You can submit code, raise suggestions, write documentation, etc. For more information, please [click here to read Contribute Your Share(Chinese Version)](https://github.com/Qihoo360/RePlugin/wiki/%E8%B4%A1%E7%8C%AE%E5%8A%9B%E9%87%8F).


## License

RePlugin is [Apache v2.0 licensed](./LICENSE).

(Thanks Xiezihan（谢子晗） for providing the translations.)
