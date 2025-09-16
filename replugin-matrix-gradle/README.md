# RePlugin Matrix Gradle Plugin

RePlugin Matrix Gradle是一个Gradle插件，为RePlugin项目提供Matrix APM（Android性能监控）集成。

该Gradle插件主要负责在编译期中集成Matrix APM功能，为RePlugin宿主应用和插件提供性能监控能力。

## 主要功能

### APM监控组件
* **IO Canary** - 文件I/O监控，检测文件读写性能问题
* **Battery Canary** - 电量消耗监控，检测耗电异常
* **SQLite Canary** - 数据库操作监控，检测数据库性能问题
* **Memory Canary** - 内存泄漏检测，监控内存使用情况

### Trace监控
* **方法调用追踪** - 监控方法执行时间和调用栈
* **启动性能分析** - 分析应用启动耗时
* **卡顿检测** - 检测UI线程卡顿情况

### 资源优化
* **无用资源移除** - 自动移除未使用的资源文件

## 使用方法

### 1. 在项目中应用插件

```gradle
// 在app的build.gradle中应用插件
apply plugin: 'replugin-matrix-gradle'
```

### 2. 配置Matrix参数

```gradle
repluginMatrixConfig {
    // 是否启用Matrix集成
    enable = true
    
    // APM组件配置
    apm {
        enable = true
        ioCanary = true      // IO监控
        batteryCanary = true // 电量监控
        sqliteCanary = true  // 数据库监控
        memoryCanary = true  // 内存监控
    }
    
    // Trace配置
    trace {
        enable = true
        baseMethodMapFile = "app/mapping.txt"    // 方法映射文件
        blackListFile = "blackMethodList.txt"    // 黑名单文件
    }
    
    // 资源优化配置
    removeUnusedResources {
        enable = false
        variant = 'release'
        needSign = true
    }
}
```

### 3. 可用任务

* `rpMatrixInit` - 初始化Matrix APM配置
* `rpMatrixGenerateConfig` - 生成Matrix配置文件

### 4. 运行任务

```bash
# 初始化Matrix配置
./gradlew rpMatrixInit

# 生成Matrix配置文件
./gradlew rpMatrixGenerateConfig
```

## 集成效果

启用Matrix插件后，将自动：

1. 添加Matrix APM依赖库
2. 配置Matrix监控组件
3. 生成Matrix配置文件
4. 在编译过程中集成监控代码

## 依赖要求

* Android Gradle Plugin 7.0+
* Gradle 7.0+
* Matrix APM 2.0.8+

## 配置示例

完整的配置示例：

```gradle
apply plugin: 'com.android.application'
apply plugin: 'replugin-host-gradle'    // RePlugin宿主插件
apply plugin: 'replugin-matrix-gradle'  // Matrix APM插件

android {
    // Android配置
}

repluginHostConfig {
    // RePlugin宿主配置
}

repluginMatrixConfig {
    enable = true
    apm {
        enable = true
        ioCanary = true
        batteryCanary = true
        sqliteCanary = true
        memoryCanary = true
    }
    trace {
        enable = true
        baseMethodMapFile = "app/mapping.txt"
    }
}
```

## 注意事项

1. 该插件需要在Android Application插件之后应用
2. Matrix监控会对应用性能产生轻微影响，建议在Debug版本中启用
3. 生产环境使用时请仔细评估性能影响
4. 确保Matrix APM依赖库版本兼容

## 技术支持

有关RePlugin Matrix Gradle的详细描述，请访问我们的Wiki，以了解更多的内容。