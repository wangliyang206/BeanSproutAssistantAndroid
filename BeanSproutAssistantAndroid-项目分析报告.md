# BeanSproutAssistantAndroid 项目分析报告

> **核心发现**：打工鸡是一款基于 Android 无障碍服务的短视频平台自动化辅助工具，采用 Java + Retrofit2 + RxJava2 + EventBus 技术栈，支持抖音/快手双平台。项目架构偏 MVP 混合模式，网络层封装规范，无障碍服务实现完整，但存在 Activity 臃肿、硬编码、持久化方案不统一等可改进项。

---

## 一、项目基本信息表

| 项目 | 值 |
|------|-----|
| 项目名称 | 赤槿-打工鸡APP (BeanSproutAssistantAndroid) |
| 包名 | com.wly.beansprout |
| 应用名 | 打工鸡 |
| 版本号 | versionCode 154, versionName 1.5.4 |
| compileSdk | 29 (Android 10) |
| minSdk | 24 (Android 7.0) |
| targetSdk | 34 (Android 14) |
| 服务器地址 | http://www.dagongji.xin/ |
| AGP | 4.2.2 |
| Gradle | 6.7.1 |
| AndroidX | 已启用 |
| Jetifier | 已启用 |
| 主要开发语言 | Java |
| 作者 | 王力杨 (wly) |
| 模块数 | 2（app + checkupdatelib） |
| 主模块 Java 文件 | 64 个 |
| 总 Java 文件 | 93 个 |
| 签名密钥 | ChickenAtWork.keystore |

---

## 二、项目根目录结构概览

```
BeanSproutAssistantAndroid/
├── .gradle/                     # Gradle 缓存目录
├── .idea/                       # IntelliJ IDEA 项目配置
├── app/                         # 主应用模块（64 个 Java 文件）
├── checkupdatelib/              # 应用内更新库模块（29 个 Java 文件）
├── gradle/                      # Gradle Wrapper 配置
├── image/                       # 项目设计图片资源
├── keystore/                    # 发布签名密钥
├── build.gradle                 # 根构建脚本（AGP 4.2.2）
├── settings.gradle              # 模块声明（:app, :checkupdatelib）
├── gradle.properties            # Gradle 属性（AndroidX + Jetifier 启用）
├── local.properties             # 本地 SDK 路径（D:\Android_SDK）
└── README.md                    # 项目说明文档
```

---

## 三、构建配置分析

### 3.1 根 build.gradle

- AGP 版本：`com.android.tools.build:gradle:4.2.2`
- 仓库源：`google()`, `jcenter()`, `maven { url 'https://maven.aliyun.com/repository/public' }`, `mavenCentral()`
- 仓库优先级：阿里云镜像优先于 jcenter，存在 `jcenter()` 已停止服务风险，建议后续移除

### 3.2 settings.gradle

```
include ':app'
include ':checkupdatelib'
```

标准双模块项目结构，`checkupdatelib` 为独立 library module。

### 3.3 gradle.properties

| 属性 | 值 | 说明 |
|------|-----|------|
| android.useAndroidX | true | 启用 AndroidX 迁移 |
| android.enableJetifier | true | 自动转换旧依赖为 AndroidX |
| org.gradle.jvmargs | -Xmx2048m -Dfile.encoding=UTF-8 | JVM 内存 2048MB |

### 3.4 app/build.gradle 详细分析

**基础配置**：
- 包名：`com.wly.beansprout`
- compileSdkVersion：29，minSdkVersion：24，targetSdkVersion：34
- versionCode：154，versionName："1.5.4"
- 签名配置：`storeFile "ChickenAtWork.keystore"`，`keyAlias "ChickenAtWork"`，密码通过 `KEYSTORE_PWD` / `KEY_PWD` 属性读取

**buildTypes 配置**：

| 配置项 | release | debug |
|--------|---------|-------|
| minifyEnabled | true | false |
| shrinkResources | true | false |
| proguardFiles | proguard-rules.pro | — |
| 友盟 Key | 正式 Key | Debug Key |

**完整 Dependencies**：

```
// ===== AndroidX 基础组件 =====
implementation 'androidx.appcompat:appcompat:1.0.0'
implementation 'androidx.recyclerview:recyclerview:1.1.0'
implementation 'androidx.constraintlayout:constraintlayout:1.1.3'
implementation 'com.google.android.material:material:1.4.0'
implementation 'androidx.cardview:cardview:1.0.0'

// ===== RxJava2 响应式编程 =====
implementation 'io.reactivex.rxjava2:rxandroid:2.0.2'
implementation 'io.reactivex.rxjava2:rxjava:2.2.21'

// ===== 网络层 Retrofit2 + OkHttp3 =====
implementation 'com.squareup.retrofit2:retrofit:2.6.1'
implementation 'com.squareup.retrofit2:adapter-rxjava2:2.3.0'
implementation 'com.squareup.retrofit2:converter-gson:2.6.1'
implementation 'com.squareup.okhttp3:okhttp:3.12.0'
implementation 'com.squareup.okhttp3:logging-interceptor:3.9.1'

// ===== JSON 序列化 =====
implementation 'com.google.code.gson:gson:2.8.2'

// ===== 事件总线 =====
implementation 'org.greenrobot:eventbus:3.3.1'

// ===== Material Dialogs =====
implementation 'com.afollestad.material-dialogs:commons:0.9.6.0'
implementation 'com.afollestad.material-dialogs:core:0.9.6.0'

// ===== UI 组件 =====
implementation 'me.jessyan:autosize:1.2.1'
implementation 'net.grandcentrix.tray:tray:0.12.0'
implementation 'com.google.android:flexbox:2.0.1'
implementation 'q.rorbin:badgeview:1.1.3'

// ===== 图片加载 =====
implementation 'com.github.bumptech.glide:glide:4.16.0'

// ===== 沉浸式状态栏 =====
implementation 'com.gyf.immersionbar:immersionbar:3.0.0'
implementation 'com.jaeger.statusbarutil:library:1.5.1'

// ===== 友盟统计 =====
implementation 'com.umeng.umsdk:common:9.5.2'
implementation 'com.umeng.umsdk:asms:1.4.1'
implementation 'com.umeng.umsdk:apm:1.5.2'

// ===== 其他 =====
implementation 'com.jakewharton.rxbinding2:rxbinding:2.2.0'
implementation 'pub.devrel:easypermissions:0.3.0'

// ===== 本地模块 / JAR =====
implementation project(path: ':checkupdatelib')
implementation files('libs/BaiduLBS_Android.jar')
```

### 3.5 checkupdatelib/build.gradle

| 属性 | 值 |
|------|-----|
| 类型 | Android Library |
| compileSdk | 31 |
| minSdk | 24 |
| targetSdk | 31 |
| 依赖 | `androidx.appcompat`, `okhttp:3.12.0` |

独立的应用内更新模块，负责版本检查与 APK 下载。

---

## 四、AndroidManifest 分析

### 4.1 包名

`com.wly.beansprout`

### 4.2 权限清单

| 权限 | 级别 | 用途 |
|------|------|------|
| `android.permission.INTERNET` | Normal | 网络请求 |
| `android.permission.ACCESS_NETWORK_STATE` | Normal | 网络状态检测 |
| `android.permission.ACCESS_WIFI_STATE` | Normal | WiFi 状态 |
| `android.permission.BIND_ACCESSIBILITY_SERVICE` | Signature | 绑定无障碍服务（核心） |
| `android.permission.SYSTEM_ALERT_WINDOW` | Special | 悬浮窗权限 |
| `android.permission.SYSTEM_OVERLAY_WINDOW` | Special | 系统覆盖层 |
| `android.permission.READ_EXTERNAL_STORAGE` | Dangerous | 读取存储 |
| `android.permission.WRITE_EXTERNAL_STORAGE` | Dangerous | 写入存储 |
| `android.permission.REQUEST_INSTALL_PACKAGES` | Special | 安装 APK 更新 |
| `android.permission.READ_PHONE_STATE` | Dangerous | 读取设备 IMEI |
| `android.permission.CHANGE_WIFI_STATE` | Normal | 修改 WiFi 状态 |
| `android.permission.WAKE_LOCK` | Normal | 保持唤醒 |

### 4.3 四大组件

| 组件类型 | 类名 | 说明 |
|---------|------|------|
| Activity | `SplashActivity` | 启动页（隐私政策 + Token 校验） |
| Activity | `LoginActivity` | 登录页（手机号+密码） |
| Activity | `RegisterActivity` | 注册页（手机号+密码+确认密码） |
| Activity | `MainActivity` | 主页面（功能/平台/动画选择） |
| Activity | `VideoPlayerActivity` | 教程视频播放页 |
| Activity | `WebViewActivity` | 内嵌 WebView 页（协议/更新下载） |
| Service | `AutoTouchService` | 无障碍服务（模拟触控核心） |
| Service | `FloatingService` | 悬浮窗服务（小鸡动画+菜单） |
| Provider | `FileProvider` | 文件分享（安装 APK） |

---

## 五、Java 源码架构分析

### 5.1 包结构层级

```
com.wly.beansprout
├── (根包)                     # 6 个 Activity + MyApplication（约 2000 行）
│   ├── SplashActivity          # 启动页（246行）
│   ├── LoginActivity           # 登录页（295行）
│   ├── RegisterActivity        # 注册页（317行）
│   ├── MainActivity            # 主页（469行）
│   ├── VideoPlayerActivity     # 视频播放页（122行）
│   ├── WebViewActivity         # WebView 页（315行）
│   └── MyApplication           # Application 初始化
├── adapter/                    # RecyclerView 适配器
│   └── TouchPointAdapter       # 触控点列表适配器
├── api/                        # Retrofit 接口定义
│   └── AccountService          # 账号相关 API（5 个接口）
├── bean/                       # 数据实体/模型（10+ 类）
│   ├── AppUpdate               # 版本更新信息（Parcelable）
│   ├── ClientInfo              # 客户端设备信息
│   ├── CommonResponse          # 通用响应
│   ├── ErrorCode               # 错误码常量接口
│   ├── ErrorInfo               # 错误信息结构
│   ├── GsonRequest<T>          # 统一请求体包装
│   ├── GsonResponse<T>         # 统一响应体包装
│   ├── LoginResponse           # 登录/Token 验证响应
│   ├── Point                   # 屏幕坐标点
│   ├── TouchEvent              # 触控事件（EventBus 传输）
│   └── TouchPoint              # 触控点配置
├── dialog/                     # 悬浮窗对话框（Service 中使用）
│   ├── BaseServiceDialog       # Service 中 Dialog 基类
│   ├── MenuDialog              # 触控菜单（219行）
│   ├── AddPointDialog          # 触控坐标采集（全屏覆盖层）
│   └── AutomaticReplyScriptDialog  # 自动回复话术编辑
├── fw_permission/              # 权限请求封装
│   └── FWPermission            # 权限请求类
├── global/                     # 全局状态管理
│   ├── AccountManager          # 用户账号管理器（SP 封装，259行）
│   ├── Constant                # 全局常量（服务器地址/API版本/URL等）
│   └── TouchEventManager       # 触控状态管理器（线程安全单例）
├── http/                       # 网络请求层（8 个文件）
│   ├── AbstractHttpClient      # HTTP 客户端抽象基类
│   ├── ApiAction               # 网络请求动作封装
│   ├── ApiException            # 自定义网络异常
│   ├── ApiOperator             # RxJava 链式请求操作器
│   ├── ApiOtherAction          # 其他 API 动作
│   ├── HeaderInterceptor       # 请求头拦截器
│   ├── IRequestMapper           # 请求映射接口
│   ├── MyHttpClient            # 具体 HTTP 客户端实现
│   └── RequestMapper           # 请求参数→GsonRequest 转换器
├── service/                    # 后台服务（核心业务）
│   ├── AutoTouchService        # 无障碍触控服务（870行，核心）
│   └── FloatingService         # 悬浮窗服务（438行）
├── utils/                      # 工具类（11 个）
│   ├── AccessibilityUtil       # 无障碍服务检测与跳转
│   ├── AppPreferencesHelper    # Tray SharedPreferences 封装
│   ├── CommonUtils             # 通用工具（防快击、正则校验、设备信息）
│   ├── DensityUtil             # 屏幕密度转换
│   ├── DialogUtils             # 对话框工具
│   ├── FindTargetNodeUtil      # 无障碍节点查找（BFS 遍历）
│   ├── GsonUtils               # Gson 序列化工具
│   ├── PathFinder              # 滑动路径生成器
│   ├── SpUtils                 # 原生 SharedPreferences 工具
│   ├── StatusBarCompatUtils    # 状态栏适配
│   └── ToastUtil               # Toast 工具
└── view/                       # 自定义视图
    └── ProgressWebView         # 带进度条的 WebView
```

### 5.2 代码规模统计

| 指标 | 数量 |
|------|------|
| 主模块 Java 文件 | 64 个 |
| checkupdatelib Java 文件 | 29 个 |
| **总 Java 文件** | **93 个** |
| 布局文件 (layout) | 16 个 |
| Activity | 6 个 |
| Service | 2 个 |
| 核心工具类 | 11 个 |
| 数据 Bean | 10 个 |
| 对话框（Service 内） | 4 个 |
| 网络层文件 | 8 个 |
| 主模块估算代码行数 | 约 6,000 - 8,000 行 |
| checkupdatelib 估算 | 约 2,000 - 3,000 行 |
| **总估算代码行数** | **约 8,000 - 11,000 行** |

### 5.3 架构模式

**采用简化的 MVP 变体（偏 MVVM 混合）**：

- **View 层**：Activity 直接持有 `MyHttpClient` 引用并处理 UI 逻辑，未通过 Contract 接口解耦
- **Model 层**：`bean/` 包定义数据实体，`global/AccountManager` + `utils/SpUtils` 管理本地持久化
- **网络层（自研封装）**：`AbstractHttpClient → MyHttpClient → ApiOperator → RequestMapper → Retrofit` 形成五级请求-响应转换链，通过 RxJava2 处理异步与线程切换
- **事件通信**：`EventBus 3.x` 实现 Service ↔ UI 解耦，`TouchEventManager`（单例）管理触控全局状态
- **持久化**：两套方案并存——`AppPreferencesHelper`（基于 Tray ContentProvider）和 `SpUtils`（原生 SharedPreferences）

**架构图**：

```
┌───────────────  UI 层 ───────────────┐
│  Splash → Login → Register → Main    │
│       VideoPlayer / WebView          │
└──────────┬───────────────────────────┘
           │ EventBus                │ 直接引用
           ▼                         ▼
┌──────────  Service 层 ──────┐  ┌──── HTTP 层 ────┐
│  FloatingService            │  │  MyHttpClient    │
│  AutoTouchService (870行)   │  │  ApiOperator     │
│  TouchEventManager(单例)    │  │  RequestMapper   │
└──────────┬──────────────────┘  │  Retrofit2       │
           │                     └────┬─────────────┘
           │ AccessibilityService     │ OkHttp3 + RxJava2
           ▼                          ▼
┌────────── 数据层 ────────────────────────────┐
│  AccountManager (Tray SP)    SpUtils (原生 SP) │
│  bean/ (LoginResponse, TouchPoint, etc.)      │
└───────────────────────────────────────────────┘
```

### 5.4 核心业务逻辑分析

#### 5.4.1 网络层架构

- **`AbstractHttpClient`**：抽象基类，配置 OkHttp 客户端参数——连接超时 120s、读写超时 60s，信任所有 SSL 证书（`TrustAllCerts`），构建 Retrofit 实例
- **`MyHttpClient`**：具体实现，封装 `login` / `register` / `validToken` / `getVersion` 四个业务 API 调用
- **`ApiOperator<T, P>`**：RxJava 链式转换器，执行 `参数 Map → GsonRequest（RequestMapper）→ 网络请求 → GsonResponse → 业务数据 P` 的完整流程，统一处理错误码（负数=错误/正数=警告），抛出 `ApiException`
- **`HeaderInterceptor`**：添加 `Content-Type: application/json`
- **`RequestMapper`**：将业务参数 Map 转换为 `GsonRequest`，自动注入 `userId`、`token`、`version`、`client`（设备信息）、`language` 公共字段

#### 5.4.2 无障碍服务 AutoTouchService（核心）

- **生命周期控制**：通过 EventBus 接收 `TouchEvent`（ACTION_START / CONTINUE / PAUSE / STOP），控制触控动作的启停
- **功能类型枚举**：

| 类型码 | 功能 | 实现方式 |
|--------|------|---------|
| 1 | 单击 | `GestureDescription.StrokeDescription` 单点 |
| 2 | 直播点赞 | 连续快速点击（循环 + 短暂 sleep） |
| 3 | 向下滑动 | `Path.lineTo` 模拟滑动手势 |
| 4 | 向上滑动 | 同上 |
| 5 | 向左滑动 | 同上 |
| 6 | 向右滑动 | 同上 |
| 7 | 自动回复 | `AccessibilityNodeInfo` 查找输入框（EditText/input）和发送按钮，设置文本 + 模拟点击 |
| 8 | 抢福袋 | 多层控件路径匹配（LynxFlattenUI / UIView），查找倒计时标签 → 判断卡点时间 → 模拟点击"一键发表评论""参与抽奖""开始观看直播任务" |

- **抢福袋逻辑细节**：
  1. 匹配控件特征：超级福袋 / 生活服务-直播-福袋等
  2. 提取倒计时标签，解析剩余时间（分钟数）
  3. 对比配置的卡点时间区间（0-5 分钟 / 5-10 分钟，支持随机）
  4. 满足条件时按控件路径点击参与按钮
- **平台支持包名**：
  - 抖音：`com.ss.android.ugc.aweme`
  - 快手：`com.smile.gifmaker`

#### 5.4.3 悬浮窗服务 FloatingService

- 通过 `WindowManager` 绑定小鸡动画视图，支持触摸拖拽移动
- **动画系统**：
  - 基础动画：眨眼、挥手
  - 功德小鸡（闪现鸡）：缩地闪现特效
  - 跳绳小鸡（溜达鸡）：路径移动 + 转圈 + 跳绳
  - 随机动画：扭动 / 变身 / 扭头
- **MenuDialog**：点击小鸡弹出控制菜单，支持触控点选择、开始/停止、添加/删除触控点、自动回复话术配置

#### 5.4.4 用户体系

- 手机号 + 密码注册/登录，注册时校验手机号格式（`^1[3-9]\d{9}$`）和两次密码一致性
- Token 自动管理：登录/注册成功后存储，后续请求通过 `RequestMapper` 自动携带
- 新用户 15 天免费体验机制（`status=6` 表示体验中，`daysRemaining` 显示剩余天数）
- 用户状态枚举：1待审核 / 2审核中 / 3已退回 / 4使用中 / 5已停用 / 6体验中 / 9已删除

#### 5.4.5 应用初始化流程

```
用户点击图标
  → SplashActivity：隐私政策检查
    ├── 未同意：弹窗展示隐私政策，同意后继续
    └── 已同意：调用 validToken 校验 Token
          ├── 有效：→ MainActivity
          └── 无效/不存在：→ LoginActivity
              ├── 登录成功：→ MainActivity
              └── 无账号：→ RegisterActivity → LoginActivity
MainActivity：
  → 选择功能类型（单击/点赞/滑动/自动回复/福袋）
  → 选择平台（抖音/快手）
  → 选择动画模型（功德小鸡/跳绳小鸡）
  → 检查版本更新（getVersion）
  → 权限检测（无障碍服务 + 悬浮窗）
  → 启动 FloatingService
FloatingService：
  → 显示小鸡悬浮窗
  → 点击弹出 MenuDialog → 选择触控点
  → EventBus 发送 TouchEvent → AutoTouchService 执行
```

---

## 六、res 资源分析

### 6.1 布局文件

| 文件 | 用途 |
|------|------|
| `activity_splash.xml` | 启动页布局 |
| `activity_login.xml` | 登录页布局 |
| `activity_register.xml` | 注册页布局 |
| `activity_main.xml` | 主页面布局 |
| `activity_videoplayer.xml` | 视频播放页布局 |
| `comm_top_dark.xml` | 通用深色顶部栏 |
| `comm_top_light.xml` | 通用浅色顶部栏 |
| `dialog_menu.xml` | 悬浮窗触控菜单弹窗 |
| `dialog_add_point.xml` | 添加触控点弹窗 |
| `dialog_auto_reply_script.xml` | 自动回复话术编辑弹窗 |
| `item_touch_point.xml` | 触控点列表项 |
| `layout_window.xml` | 小鸡悬浮窗布局 |
| `pop_commtips_dialog.xml` | 通用提示弹窗 |
| `pop_notprivacypolicy_dialog.xml` | 拒绝隐私政策弹窗 |
| `pop_privacypolicy_dialog.xml` | 隐私政策确认弹窗 |
| `webview_activity.xml` | WebView 页面布局 |

### 6.2 strings.xml 关键字符串

| Key | 值 |
|-----|-----|
| `app_name` | 打工鸡 |
| `debug_app_name` | Test打工鸡 |
| `um_app_key`（正式） | `66b9aa29cac2a664de8a7f6b` |
| `um_app_key`（调试） | Debug Key |
| `privacy_policy_title` | 隐私政策提示文案 |
| `service_agreement_text` | 服务协议/隐私政策相关文案 |

### 6.3 其他资源

| 资源类型 | 内容 | 说明 |
|---------|------|------|
| `drawable/` | 小鸡动画帧图片（多套） | 功德小鸡 + 跳绳小鸡多帧动画 |
| `mipmap/` | 启动图标（多密度） | mdpi/hdpi/xhdpi/xxhdpi |
| `raw/` | `tutorial_video.mp4` | 内置教程视频 |

---

## 七、依赖清单汇总

### 网络与数据
| 依赖 | 版本 | 类别 |
|------|------|------|
| Retrofit2 | 2.6.1 | HTTP 客户端 |
| adapter-rxjava2 | 2.3.0 | Retrofit RxJava 适配器 |
| converter-gson | 2.6.1 | Retrofit Gson 转换器 |
| OkHttp3 | 3.12.0 | HTTP 引擎 |
| logging-interceptor | 3.9.1 | 网络日志 |
| Gson | 2.8.2 | JSON 序列化 |

### 响应式编程
| 依赖 | 版本 |
|------|------|
| RxJava2 | 2.2.21 |
| RxAndroid | 2.0.2 |
| RxBinding2 | 2.2.0 |

### 事件通信
| 依赖 | 版本 |
|------|------|
| EventBus | 3.3.1 |

### UI 框架
| 依赖 | 版本 | 用途 |
|------|------|------|
| AndroidX AppCompat | 1.0.0 | 兼容性支持库 |
| RecyclerView | 1.1.0 | 列表控件 |
| ConstraintLayout | 1.1.3 | 约束布局 |
| Material Design | 1.4.0 | Material 组件 |
| CardView | 1.0.0 | 卡片控件 |
| Material Dialogs | 0.9.6.0 | 对话框组件 |
| FlexboxLayout | 2.0.1 | 弹性布局 |
| AutoSize | 1.2.1 | 屏幕适配 |

### 图片与状态栏
| 依赖 | 版本 |
|------|------|
| Glide | 4.16.0 |
| ImmersionBar | 3.0.0 |
| StatusBarUtil | 1.5.1 |

### 持久化
| 方案 | 版本 | 说明 |
|------|------|------|
| Tray | 0.12.0 | ContentProvider 替代 SharedPreferences |
| SharedPreferences | 原生 | 触控点列表持久化 |

### 统计
| 依赖 | 版本 | 用途 |
|------|------|------|
| 友盟 common | 9.5.2 | 统计分析基线包 |
| 友盟 asms | 1.4.1 | 必选组件 |
| 友盟 apm | 1.5.2 | 应用性能监控/错误统计 |

### 权限
| 依赖 | 版本 |
|------|------|
| EasyPermissions | 0.3.0 |

### 本地模块
| 模块 | 说明 |
|------|------|
| `checkupdatelib` | 应用内更新模块（OkHttp + AppCompat） |
| `BaiduLBS_Android.jar` | 百度定位 SDK（本地 JAR） |

---

## 八、代码质量评估

### 8.1 优点

1. **网络层设计规范**：`AbstractHttpClient → MyHttpClient → ApiOperator → RequestMapper → Retrofit` 五级封装，职责清晰，请求-响应转换链路完整统一
2. **RxJava 全链路响应式**：网络请求全程 RxJava2 链式调用，线程切换（subscribeOn/observeOn）规范
3. **EventBus 解耦良好**：Service 与 UI 层通过 EventBus 通信，避免直接引用，降低模块耦合度
4. **无障碍服务实现完整**：覆盖单击/滑动/点赞/自动回复/抢福袋全部功能类型，支持抖音和快手双平台
5. **隐私合规处理**：首次启动强制弹窗确认隐私政策和服务协议
6. **混淆配置到位**：release 构建启用代码混淆和资源压缩，proguard-rules.pro 已配置
7. **悬浮窗交互体验**：小鸡动画丰富（多套动画模型），可拖拽，操作直观

### 8.2 待改进项

| 问题 | 严重程度 | 说明 | 建议 |
|------|---------|------|------|
| 架构非标准 MVP/MVVM | 中 | Activity 臃肿（MainActivity 469 行），View 层与逻辑层耦合 | 引入 ViewModel + LiveData，拆分业务逻辑到 Repository |
| 持久化方案不统一 | 中 | 同时使用 Tray 和原生 SharedPreferences，维护成本高 | 统一为 Room 或 DataStore |
| 硬编码问题 | 中 | 服务器地址硬编码在 Constant 接口，无法动态切换环境 | 使用 BuildConfig + productFlavors |
| 无障碍服务耦合度高 | 中 | AutoTouchService（870行）包含过多平台适配和业务逻辑 | 拆分策略模式，每平台独立 Handler |
| 缺乏依赖注入 | 低 | 对象创建散落各处，无 DI 框架 | 引入 Hilt/Dagger2 |
| compileSdk 版本滞后 | 低 | compileSdk 29 但 targetSdk 34，存在版本不一致 | 升级 compileSdk 至 34 |
| 未使用 ORM 数据库 | 低 | 触控点配置直接 JSON 序列化存 SP | 复杂数据改用 Room |
| 密码明文存储 | 高 | `AccountManager` 中密码以明文存入 SharedPreferences | 使用 EncryptedSharedPreferences 或 KeyStore |
| 信任所有 SSL 证书 | 高 | `AbstractHttpClient` 中配置 `TrustAllCerts`，存在中间人攻击风险 | 仅 debug 构建信任所有证书，release 使用正规证书校验 |
| jcenter 仓库依赖 | 低 | jcenter 已于 2022 年停止服务 | 迁移到 mavenCentral |

### 8.3 代码规模总览

| 维度 | 数据 |
|------|------|
| 总 Java 文件 | 93 个 |
| 总估算代码行数 | 8,000 - 11,000 行 |
| 最大单文件 | AutoTouchService（870 行） |
| Activity 平均行数 | 约 300 行 |
| 模块数 | 2（app + checkupdatelib） |
| 第三方依赖 | 约 25 个 |
| 最低支持 API | 24（Android 7.0） |
| 目标 API | 34（Android 14） |
