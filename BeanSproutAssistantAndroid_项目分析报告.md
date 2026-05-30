# BeanSproutAssistantAndroid 项目分析报告

> 分析日期：2026-05-30 | 项目版本：v1.5.4 (build 154)

---

## 一、项目基本信息表

| 属性 | 值 |
|---|---|
| **项目名称** | 赤槿 - 打工鸡APP (BeanSproutAssistantAndroid) |
| **包名** | `com.wly.beansprout` |
| **作者** | WLY（王力杨，wangliyang206@163.com） |
| **开发语言** | Java |
| **Android Gradle Plugin** | 4.2.2 |
| **compileSdk** | 29 |
| **minSdk** | 24 (Android 7.0) |
| **targetSdk** | 34 (Android 14) |
| **versionCode** | 154 |
| **versionName** | 1.5.4 |
| **模块数量** | 2（app + checkupdatelib） |
| **代码总行数** | 约 5200+ 行（63 + 28 个 .java 文件） |
| **后端服务器** | `http://www.dagongji.xin/` (备用 `http://47.115.223.27/`) |
| **服务器 API 版本** | v1 |

---

## 二、项目根目录结构概览

```
E:\Projects\BeanSproutAssistantAndroid/
├── .gradle/                          # Gradle 缓存
├── .idea/                            # IDE 配置
├── app/                              # 主模块
│   ├── build.gradle                  # 应用构建配置
│   ├── libs/                         # 本地 jar/aar
│   ├── release/                      # 签名密钥
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/wly/beansprout/  # Java 源码
│       └── res/                      # 资源文件
├── checkupdatelib/                   # App 更新库模块
│   ├── build.gradle
│   └── src/main/java/com/qiangxi/checkupdatelibrary/
├── gradle/                           # Gradle wrapper
│   └── wrapper/
├── image/                            # 项目图片素材
├── build.gradle                      # 根构建脚本
├── settings.gradle                   # 模块声明
├── gradle.properties                 # Gradle 属性
├── local.properties                  # 本地 SDK 路径
└── README.md                         # 项目说明
```

---

## 三、构建配置详情

### 3.1 根目录 build.gradle
- AGP 版本：`com.android.tools.build:gradle:4.2.2`
- 代码仓库：aliyun maven、jitpack、google、jcenter、mavenCentral

### 3.2 settings.gradle
- 包含模块：`:app`、`:checkupdatelib`

### 3.3 gradle.properties
- `android.useAndroidX=true`
- `android.enableJetifier=true`
- `org.gradle.jvmargs=-Xmx1536m`

### 3.4 app/build.gradle 核心配置

| 配置项 | 值 |
|---|---|
| compileSdk | 29 |
| minSdk | 24 |
| targetSdk | 34 |
| versionCode | 154 |
| versionName | 1.5.4 |
| 签名 | release/debug 双签名配置 |

**完整依赖清单：**

| 依赖 | 版本 | 用途 |
|---|---|---|
| `androidx.appcompat:appcompat` | 1.2.0 | 兼容库 |
| `androidx.constraintlayout:constraintlayout` | 2.0.4 | 约束布局 |
| `androidx.recyclerview:recyclerview` | 1.1.0 | 列表控件 |
| `com.google.android.material:material` | 1.3.0 | Material Design |
| `com.google.android:flexbox` | 1.1.0 | 弹性布局（功能选择） |
| `io.reactivex.rxjava2:rxjava` | 2.2.21 | 响应式编程 |
| `io.reactivex.rxjava2:rxandroid` | 2.1.1 | RxJava Android 调度器 |
| `com.jakewharton.rxbinding3:rxbinding` | 3.1.0 | Rx 绑定 UI 控件 |
| `com.squareup.okhttp3:okhttp` | 4.9.0 | HTTP 客户端 |
| `com.squareup.okhttp3:logging-interceptor` | 4.9.0 | HTTP 日志 |
| `com.squareup.retrofit2:retrofit` | 2.7.2 | REST 客户端 |
| `com.squareup.retrofit2:converter-gson` | 2.7.2 | JSON 转换 |
| `com.squareup.retrofit2:adapter-rxjava2` | 2.7.2 | RxJava 适配 |
| `com.google.code.gson:gson` | 2.8.6 | JSON 解析 |
| `org.greenrobot:eventbus` | 3.1.1 | 事件总线 |
| `com.afollestad.material-dialogs:core` | 0.9.6.0 | Material 对话框 |
| `com.umeng.umsdk:common` | 9.7.7 | 友盟基础 |
| `com.umeng.umsdk:asms` | 1.8.1 | 友盟 ASMS |
| `com.umeng.umsdk:apm` | 1.9.3 | 友盟 APM |
| `com.umeng.umsdk:abtest` | 1.0.1 | 友盟 AB 测试 |
| `com.umeng.umsdk:game` | 9.2.0-Game | 友盟游戏统计 |
| `com.umeng.umsdk:crash` | 0.0.5 | 友盟崩溃统计 |
| `qiu.niorgai:StatusBarCompat` | 2.3.3 | 状态栏兼容 |
| `net.grandcentrix.tray:tray` | 0.12.0 | SharedPreferences 替代 |
| `com.github.zcweng:switch-button` | 0.0.3@aar | 开关按钮控件 |
| `com.github.hackware1993:MagicIndicator` | 1.5.0 | 指示器控件 |
| `com.amitshekhar.android:debug-db` | 1.0.4 | Debug 数据库调试 |
| `com.github.mmin18:realtimeblurview` | 1.2.1 | 实时模糊视图 |

**buildTypes：**
- `release`：开启 ProGuard 混淆，release 签名
- `debug`：关闭混淆，debug 签名

---

## 四、AndroidManifest.xml 分析

- **包名**：`com.wly.beansprout`
- **应用入口**：`SplashActivity`

### 四大组件清单

**Activity（6 个）：**

| Activity | 说明 |
|---|---|
| `SplashActivity` | 启动页（LAUNCHER），隐私政策检查 + 自动登录 |
| `LoginActivity` | 登录页 |
| `RegisterActivity` | 注册页 |
| `MainActivity` | 主页（功能选择/配置） |
| `VideoPlayerActivity` | 视频播放页 |
| `WebViewActivity` | WebView 页（协议展示） |

**Service（2 个）：**

| Service | 说明 |
|---|---|
| `AutoTouchService` | 无障碍服务（核心），执行触控自动化 |
| `FloatingService` | 悬浮窗服务，显示小鸡形象动画 |

**ContentProvider：**
- `FileProvider`（`androidx.core.content.FileProvider`），`authorities="com.wly.beansprout.fileprovider"`

### 权限清单

| 权限 | 用途 |
|---|---|
| `INTERNET` | 网络请求 |
| `ACCESS_NETWORK_STATE` | 网络状态检测 |
| `CHANGE_NETWORK_STATE` | 网络切换 |
| `ACCESS_WIFI_STATE` | WiFi 状态 |
| `CHANGE_WIFI_STATE` | WiFi 切换 |
| `READ_PHONE_STATE` | 读取 IMEI/设备信息 |
| `READ_SMS` / `READ_PHONE_NUMBERS` | 读取手机号（SIM卡） |
| `WRITE_EXTERNAL_STORAGE` / `READ_EXTERNAL_STORAGE` | 存储读写 |
| `REQUEST_INSTALL_PACKAGES` | APK 安装权限 |
| `SYSTEM_ALERT_WINDOW` / `SYSTEM_OVERLAY_WINDOW` | 悬浮窗权限 |
| `BIND_ACCESSIBILITY_SERVICE` | 无障碍服务绑定 |

---

## 五、Java 源码包结构

```
com.wly.beansprout
├── SplashActivity.java           # 启动/隐私政策/自动登录
├── LoginActivity.java             # 用户名密码登录
├── RegisterActivity.java          # 手机号注册
├── MainActivity.java              # 主功能页面（469行）
├── MyApplication.java             # Application（友盟初始化）
├── WebViewActivity.java           # WebView（协议展示）
├── VideoPlayerActivity.java       # 视频播放
│
├── api/
│   └── AccountService.java        # Retrofit API 接口定义
│
├── bean/                          # 数据实体（11个）
│   ├── LoginResponse.java         # 登录响应
│   ├── CommonResponse.java        # 通用响应
│   ├── AppUpdate.java             # 版本更新（Parcelable）
│   ├── TouchPoint.java            # 触控点配置
│   ├── TouchEvent.java            # 触控事件（EventBus）
│   ├── Point.java                 # 坐标点
│   ├── GsonRequest.java           # 请求基类
│   ├── GsonResponse.java          # 响应基类
│   ├── ClientInfo.java            # 设备信息
│   ├── ErrorInfo.java             # 错误信息
│   └── ErrorCode.java             # 错误码常量
│
├── adapter/
│   └── TouchPointAdapter.java     # 触控点 RecyclerView 适配器
│
├── dialog/                        # 对话框（7个）
│   ├── BaseServiceDialog.java     # 悬浮窗基类 Dialog
│   ├── MenuDialog.java            # 功能菜单主弹窗
│   ├── AddPointDialog.java        # 添加触控点
│   ├── AutomaticReplyScriptDialog.java  # 自动回复话术配置
│   ├── PrivacyPolicyDialog.java   # 隐私政策同意
│   ├── NotPrivacyPolicyDialog.java      # 不同意隐私政策二次确认
│   └── CommTipsDialog.java        # 通用提示对话框
│
├── fw_permission/                 # 悬浮窗权限请求
│   ├── FloatWindowManager.java
│   └── ...
│
├── global/                        # 全局管理类
│   ├── Constant.java              # 常量（服务器地址/API版本等）
│   ├── AccountManager.java        # 本地账户管理（SharedPreferences）
│   └── TouchEventManager.java     # 触控状态管理器（单例）
│
├── http/                          # 网络层（9个文件）
│   ├── AbstractHttpClient.java    # Retrofit 抽象基类
│   ├── MyHttpClient.java          # 具体实现（BASE_URL + 拦截器）
│   ├── RequestMapper.java         # 请求参数映射（含设备信息）
│   ├── IRequestMapper.java        # 请求映射接口
│   ├── ApiOperator.java           # RxJava 链式操作封装
│   ├── ApiAction.java             # API 转换 Action
│   ├── ApiOtherAction.java        # 其他 API Action
│   └── ApiException.java          # API 异常类
│
├── service/                       # 后台服务
│   ├── AutoTouchService.java      # 无障碍触控服务（870行，核心）
│   └── FloatingService.java       # 悬浮窗动画服务（438行）
│
├── utils/                         # 工具类（13个）
│   ├── AccessibilityUtil.java     # 无障碍开关检测/跳转
│   ├── AppPreferencesHelper.java  # Preferences 辅助
│   ├── CommonUtils.java           # 通用工具（IMEI/SIM/Screen等）
│   ├── DensityUtil.java           # 像素密度转换
│   ├── DialogUtils.java           # Dialog 管理工具
│   ├── FindTargetNodeUtil.java    # 无障碍节点树 BFS 搜索
│   ├── GsonUtils.java             # Gson 工具封装
│   ├── MyClickableSpan.java       # 可点击文本 Spannable
│   ├── PathFinder.java            # 悬浮窗路径动画计算
│   ├── SpUtils.java               # SharedPreferences 封装
│   ├── StatusBarCompatUtils.java  # 状态栏工具
│   ├── ToastUtil.java             # Toast 封装
│   └── WindowUtils.java           # 屏幕/窗口工具类
│
└── view/                          # 自定义 View（若干）
```

### 模块二：checkupdatelib（28 个 Java 文件）

独立的 App 内更新库模块，包名 `com.qiangxi.checkupdatelibrary`，含：
- 更新请求/任务管理（request/task）
- 下载回调/调度（callback/dispatcher）
- UI 对话框（dialog）
- 依赖 OkHttp

---

## 六、架构模式分析

### 6.1 整体架构：RxJava 驱动的简约 MVC

```
┌─────────────────────────────────────────────────────┐
│                      UI Layer                        │
│  SplashActivity / LoginActivity / RegisterActivity  │
│  MainActivity / Dialogs (View Logic)                │
│  FloatingService (悬浮窗 UI)                         │
└────────────────┬────────────────────────────────────┘
                 │ EventBus (TouchEvent)
                 │ RxJava Observable Streams
┌────────────────▼────────────────────────────────────┐
│                  Business Layer                      │
│  MyHttpClient (网络请求封装)                          │
│  ApiOperator + ApiAction (RxJava 链式处理)            │
│  AccountManager (本地数据)                            │
│  TouchEventManager (触控状态单例)                     │
└────────────────┬────────────────────────────────────┘
                 │ Retrofit2 + OkHttp
┌────────────────▼────────────────────────────────────┐
│                  Service Layer                       │
│  AutoTouchService (AccessibilityService)             │
│  FloatingService (WindowManager 悬浮窗)              │
└─────────────────────────────────────────────────────┘
```

**特点：**
1. **非标准 MVP/MVVM/MVC**：Activity 直接持有 MyHttpClient 和 AccountManager，没有 Repository/ViewModel/Presenter 中间层
2. **RxJava 响应式流**：网络请求全部通过 RxJava Observable 链式处理（已封装 ApiOperator）
3. **EventBus 跨组件通信**：Activity ↔ Service 通过 EventBus.post(TouchEvent) 通信
4. **单例状态管理**：TouchEventManager（DCL 双重检查锁单例）
5. **SharedPreferences 持久化**：AccountManager + SpUtils 负责本地存储

### 6.2 核心业务流程

```
用户打开App → SplashActivity(隐私/自动登录) → MainActivity
    │
    ├── 选择功能(1-8) → FlexboxRadioGroup
    ├── 设置专属平台(抖音/快手)
    ├── 设置卡福袋时间
    └── 开始 → EventBus.post(TouchEvent.ACTION_START)
                    │
                    ▼
         AutoTouchService.onEvent(TouchEvent)
                    │
            GestureDescription 模拟触控
                    │
         FloatingService 悬浮窗动画反馈
```

---

## 七、核心业务逻辑详解

### 7.1 AutoTouchService（870行，项目核心）

基于 Android `AccessibilityService`，支持 8 种功能类型：

| 编号 | 功能 | 实现方式 |
|---|---|---|
| 1 | 单击 | `GestureDescription.Builder.addStroke` 单笔画 |
| 2 | 点赞（双击） | 连续两次 GestureDescription |
| 3 | 向下滑动 | `Path(0,0)→(0,height*0.6)` |
| 4 | 上下滑动 | 先向下再向上回原位 |
| 5 | 向左滑动 | `Path(width*0.9,0)→(-width*0.8,0)` |
| 6 | 向右滑动 | 反向路径 |
| 7 | 自动回复 | AccessibilityNodeInfo 查找输入框 + 发送按钮，随机选取预设话术 |
| 8 | 抢福袋 | 复杂多步骤流程：识别超级福袋控件 → 点击 → 一键评论/参与抽奖 → 观看直播 → 关闭界面 |

**抢福袋详细流程：**
1. 通过 `FindTargetNodeUtil` BFS 遍历节点树，匹配 `com.lynx.tasm` 福袋控件
2. 解析 `ContentDescription` 提取剩余时间（"超级福袋 3分56秒"）
3. 对比用户设置的卡福袋时间阈值，决定是否参与
4. 依次执行：点击福袋 → 点击评论按钮 → 点击发送 → 开始观看 → 等待 → 关闭弹窗

**悬浮窗联动：**
- 暂停/继续通过 `onAccessibilityEvent` 监听窗口内容变化实现（专属模式下切换 App 自动暂停）

### 7.2 FloatingService（438行）

- 使用 `WindowManager.addView()` 创建悬浮窗
- 显示小鸡形象序列帧动画（支持多种动画：功德小鸡、跳绳、闪现、随机动作）
- 跟随手指拖动，松手后 `MenuDialog` 弹出
- 路径动画通过 `PathFinder` 计算贝塞尔曲线逐点移动
- 跳绳动画模式下小鸡持续跳动

### 7.3 网络请求框架

```
请求流程：
  MyHttpClient.login(mobile, password)
    → ApiOperator.chain(params, action)
      → RequestMapper.transform(params)         // 包装 GsonRequest
        → AccountService.login(gsonRequest)     // Retrofit POST
          → ApiOperator.transformResponse()     // 解包 GsonResponse
            → Observer.onNext(LoginResponse)    // 回传 Activity
```

**请求体结构（GsonRequest）：**
```json
{
  "userId": "xxx",
  "version": 1,
  "client": { "deviceid": "...", "os": "android", "osver": "29", ... },
  "token": "xxx",
  "language": "ZH",
  "data": { "username": "138...", "password": "..." }
}
```

**响应体结构（GsonResponse）：**
```json
{
  "version": "1",
  "errorinfo": { "errorcode": "0", "errormessage": "ok" },
  "data": { "token": "...", "userId": "...", ... }
}
```

### 7.4 本地存储结构

| 存储方式 | 内容 |
|---|---|
| `AccountManager` (SharedPreferences) | Token、UserId、用户名、密码、昵称、手机、头像URL、自动回复脚本、隐私政策状态、弹框日期 |
| `SpUtils` (SharedPreferences + Gson) | 触控点列表 JSON、通用 KV |
| `tray` 库 | SharedPreferences 多进程替代（声明依赖但 AccountManager 未实际使用） |

---

## 八、资源目录分析

| 目录 | 数量 | 说明 |
|---|---|---|
| `res/layout/` | 16个 | Activity 布局、Dialog 布局、悬浮窗布局 |
| `res/drawable/` | 26个 | XML drawable（形状、选择器） |
| `res/mipmap-xhdpi/` | 210个 | **最多资源**，小鸡动画序列帧（png图片） |
| `res/mipmap-xxhdpi/` | 7个 | 应用图标 |
| `res/mipmap-hdpi/mdpi/xxxhdpi/` | 4个 | 适配图标 |
| `res/raw/` | 1个 | 原始资源（可能为音效） |
| `res/values/` | 5个 | strings、colors、dimens、styles、themes |
| `res/xml/` | 3个 | `accessibility_service_config.xml` 等配置 |

**关键 strings.xml 内容：**
- `app_name`：打工鸡 / Test打工鸡（debug）
- 友盟 AppKey：debug `66b9b018...` / release `66b9aa29...`
- 隐私政策文案 + 服务协议提示

---

## 九、模块依赖图

```
app
├── checkupdatelib (本地 library)
├── androidx.* (appcompat, constraintlayout, recyclerview)
├── com.google.android.material
├── com.google.android:flexbox
├── io.reactivex.rxjava2 (rxjava, rxandroid)
├── com.jakewharton.rxbinding3
├── com.squareup.* (retrofit2, okhttp3, gson-converter, rxjava2-adapter)
├── com.google.code.gson
├── org.greenrobot:eventbus
├── com.afollestad.material-dialogs
├── com.umeng.umsdk:* (common, asms, apm, abtest, game, crash)
├── qiu.niorgai:StatusBarCompat
├── net.grandcentrix.tray
├── com.github.zcweng:switch-button
├── com.github.hackware1993:MagicIndicator
├── com.amitshekhar.android:debug-db (仅 debug)
└── com.github.mmin18:realtimeblurview

checkupdatelib
└── com.squareup.okhttp3:okhttp
```

---

## 十、代码统计

### app 模块

| 子包 | 文件数 | 主要职责 |
|---|---|---|
| 根包 | 8 | Activity (Splash/Login/Register/Main/Web/Video) + Application |
| `api/` | 1 | Retrofit 接口定义 |
| `adapter/` | 1 | RecyclerView 适配器 |
| `bean/` | 11 | 数据实体 |
| `dialog/` | 7 | 弹窗组件 |
| `fw_permission/` | 2 | 悬浮窗权限管理 |
| `global/` | 3 | 常量/账户管理/触控状态 |
| `http/` | 9 | 网络框架封装 |
| `service/` | 2 | 无障碍服务(870行) + 悬浮窗服务(438行) |
| `utils/` | 13 | 工具类集合 |
| `view/` | 若干 | 自定义视图 |
| **合计** | **约63个** | -- |

### checkupdatelib 模块
- 28个 Java 文件，独立更新库

---

## 十一、代码质量评估

| 维度 | 评分 | 说明 |
|---|---|---|
| **架构规范** | ⭐⭐ | 非标准架构，Activity 直接耦合网络层；无 Repository/ViewModel 分层 |
| **网络层封装** | ⭐⭐⭐⭐ | ApiOperator + ApiAction 对 RxJava 链式处理封装较好；统一 GsonRequest/GsonResponse 结构 |
| **无障碍服务** | ⭐⭐⭐ | AutoTouchService 功能完整，但 870 行单文件过长，部分逻辑耦合度高；福袋识别依赖硬编码 className |
| **代码复用** | ⭐⭐⭐ | 工具类较完善；但 LoginActivity/RegisterActivity 存在大量重复代码（状态栏、RxBinding 监听等） |
| **安全性** | ⭐⭐ | 信任所有 SSL 证书（忽略证书校验）；SharedPreferences 明文存储密码 |
| **可维护性** | ⭐⭐ | 无单元测试；无依赖注入；日志使用混杂（Log.d/Log.i/自定义TAG）；部分硬编码 URL |
| **资源管理** | ⭐⭐⭐ | 210 张小鸡动画帧存于 mipmap-xhdpi（应放在 drawable-nodpi 或 assets） |

**关键问题：**
1. SSL 全部信任（AbstractHttpClient 中 `TrustAll`），存在中间人攻击风险
2. 密码明文存入 SharedPreferences（AccountManager）
3. 无障碍服务硬编码抖音/快手包名和控件 className，版本更新后可能失效
4. Activity 生命周期管理不规范（静态变量持有 Context 引用）
5. 无 ProGuard 规则文件分析（release 开启混淆但未知保留规则）

---

## 十二、项目目录-资源映射表

| 资源 | 实际操作 | 文件路径 |
|---|---|---|
| 小鸡动画帧 | 序列帧图片 | `res/mipmap-xhdpi/*.png` |
| 无障碍配置 | XML | `res/xml/accessibility_service_config.xml` |
| 文件共享 | FileProvider | `res/xml/file_paths.xml` |
| 网络请求 | Retrofit POST | `api/AccountService.java` |
| SP 存储 | SharedPreferences | `global/AccountManager.java` |
| 签名文件 | jks | `app/release/beansprout.jks` |

---

## 十三、数据模型一览

### LoginResponse
```
token: String        -- 认证令牌
userId: String       -- 用户 ID
userName: String     -- 用户名
userPhone: String    -- 手机号
status: int          -- 1待审核/2审核中/3已退回/4使用中/5已停用/6体验中/9已删除
daysRemaining: int   -- 体验剩余天数
```

### TouchPoint
```
name: String         -- 触控点名称
x: int               -- 屏幕 X 坐标
y: int               -- 屏幕 Y 坐标
delay: int           -- 操作间隔(ms)
isStartClick: bool   -- 是否已启动
functionType: int    -- 1单击/2点赞/3下滑/4上下滑/5左滑/6右滑/7自动回复/8抢福袋
luckybagTime: int    -- 抢福袋时间阈值
```

### ClientInfo
```
cell: String         -- 手机号
deviceid: String     -- IMEI
simid: String        -- SIM 卡序列号
os: String           -- "android"
osver: String        -- SDK 版本
vercode: String      -- APP versionCode
vername: String      -- APP versionName
ppiheight: String    -- 屏幕高度(px)
ppiwidth: String     -- 屏幕宽度(px)
```

### AppUpdate
```
verCode: int         -- 最新版本号
verName: String      -- 最新版本名
name: String         -- APP 显示名称
fileName: String     -- APK 文件名
filePath: String     -- 下载地址 URL
appForce: int        -- 是否强制升级(1强制/0可选)
newAppSize: float    -- APK 大小
newAppUpdateDesc: String -- 更新日志
```

---

*报告完*