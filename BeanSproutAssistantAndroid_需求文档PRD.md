# 赤槿 - 打工鸡APP 产品需求文档（PRD）

> 版本：v1.0 | 日期：2026-05-30  
> 基于 BeanSproutAssistantAndroid v1.5.4 (build 154) 代码反向推导

---

## 一、项目背景与定位

### 1.1 项目概述

"打工鸡"（赤槿）是一款面向抖音/快手用户的**手机自动化助手**。基于 Android 无障碍服务（AccessibilityService）实现模拟触控操作，帮助用户在短视频平台上自动完成重复性交互行为，提升直播间互动效率、降低人工操作成本。

### 1.2 产品定位

- **目标市场**：国内短视频平台（抖音 / 快手）用户
- **核心价值**：自动化执行直播互动、刷视频、抢福袋等高频重复操作
- **商业模式**：新用户 15 天免费体验，后续需付费激活

### 1.3 产品名称

| 属性 | 值 |
|---|---|
| 产品名称 | 打工鸡 / 赤槿 |
| 内部代号 | BeanSproutAssistant |
| 服务器域名 | dagongji.xin |

---

## 二、目标用户画像

### 2.1 核心用户

| 用户类型 | 特征 | 核心需求 |
|---|---|---|
| 直播观众 | 经常观看抖音/快手直播，希望自动点赞、抢福袋 | 直播互动自动化 |
| 短视频消费者 | 大量刷短视频，希望自动滑动切换 | 自动刷视频 |
| 小说/内容阅读者 | 在平台内阅读内容，希望自动翻页 | 自动翻页/阅读 |
| 直播间运营者 | 需要自动回复粉丝留言 | 自动回复话术 |

### 2.2 用户场景

| 场景 | 需求描述 |
|---|---|
| 场景一：直播点赞 | 用户进入直播间后，设置点赞频率和位置，APP 持续自动双击点赞 |
| 场景二：抢福袋 | 设置卡福袋时间阈值，APP 自动识别并从福袋出现到参与一气呵成 |
| 场景三：自动刷视频 | 设置滑动方向和间隔，APP 自动上下滑动切换视频 |
| 场景四：直播间自动回复 | 预设回复话术，APP 检测到新消息时自动输入并发送 |
| 场景五：免手持阅读 | 设置定时点击/滑动，长时间免手动操作 |

---

## 三、功能需求清单

### P0 — 核心功能（必须具备）

#### F-001：无障碍服务触控引擎
- **描述**：基于 Android AccessibilityService 实现模拟屏幕触控操作
- **详细需求**：
  - 支持通过 `GestureDescription` API 模拟手势操作
  - 支持单击、长按、滑动等基础手势
  - 与悬浮窗联动，实时控制启停
- **技术约束**：需用户手动在系统设置中开启无障碍服务

#### F-002：用户注册与登录
- **描述**：手机号 + 密码注册/登录
- **详细需求**：
  - 注册：输入手机号（11位校验）+ 密码（6-20位）+ 确认密码
  - 登录：输入手机号 + 密码，成功后保存 Token
  - 自动登录：已有有效 Token 时跳过登录页直接进入主页
  - Token 校验：启动时验证 Token 有效性，失效则跳转登录页
- **服务端接口**：`POST member/register`, `POST member/login`, `POST member/validToken`

#### F-003：触控点管理
- **描述**：支持用户在屏幕上创建多个自定义触控点，配置触控行为
- **详细需求**：
  - 添加触控点：命名 + 选择屏幕位置 + 设置操作间隔(ms)
  - 列表展示已添加的触控点
  - 删除/启动单个触控点
  - 数据持久化到 SharedPreferences

#### F-004：8 种自动化功能

| 功能编号 | 功能名称 | 行为描述 |
|---|---|---|
| 1 | 单击 | 在指定坐标执行单次点击 |
| 2 | 点赞 | 在指定坐标执行快速双击 |
| 3 | 向下滑动 | 从指定坐标向下滑动屏幕60%高度 |
| 4 | 上下滑动 | 先向下再向上回原位 |
| 5 | 向左滑动 | 从屏幕右侧向左滑 |
| 6 | 向右滑动 | 从屏幕左侧向右滑 |
| 7 | 自动回复 | 识别直播间输入框，随机选取预设话术发送 |
| 8 | 抢福袋 | 识别福袋控件，自动点击参与 + 完成观看任务 |

### P1 — 重要功能

#### F-005：悬浮窗控制面板
- **描述**：始终在顶层的悬浮窗，显示小鸡形象动画，提供快捷操作入口
- **详细需求**：
  - 小鸡序列帧动画（支持多种动画模式：功德小鸡、跳绳、闪现、随机动作）
  - 拖动悬浮窗到屏幕任意位置
  - 点击悬浮窗弹出 MenuDialog 功能菜单
  - 松手自动弹出菜单

#### F-006：自动回复话术配置
- **描述**：用户预设多段话术文本，自动回复时随机选取
- **详细需求**：
  - 文本输入框，支持多行输入
  - 话术以换行分隔存储
  - 自动回复时随机选取一行发送
  - 持久化保存

#### F-007：专属平台模式
- **描述**：用户可选择仅在指定 App 前台时执行自动化
- **支持平台**：
  - 抖音（`com.ss.android.ugc.aweme`）
  - 快手（`com.smile.gifmaker`）
  - 不限制（所有应用）
- **行为**：切换到非专属 App 时自动暂停，切换回时自动继续

#### F-008：App 版本更新
- **描述**：启动时检查服务器最新版本，支持强制/可选升级
- **详细需求**：
  - 通过 `system/getVersion` 获取最新版本信息
  - 比较本地版本号
  - 可选升级弹窗（有取消按钮）
  - 强制升级弹窗（不可取消）
  - 下载 APK 并调用系统安装

### P2 — 体验增强功能

#### F-009：隐私政策合规
- **描述**：首次启动展示隐私政策和服务协议，用户必须同意才能使用
- **详细需求**：
  - 首次启动弹出隐私政策同意弹窗（含《服务协议》《隐私政策》链接）
  - 不同意时弹出二次确认，第二次仍不同意则退出 APP
  - WebViewActivity 展示完整协议内容

#### F-010：抢福袋时间阈值
- **描述**：用户可设置参与福袋的最小剩余时间，避免时间过短抢不到
- **配置选项**：无限制 / 8分钟 / 6分钟 / 4分钟 / 3分钟 / 2分钟以下

#### F-011：友盟数据统计
- **描述**：集成友盟统计 SDK，追踪用户行为和应用性能
- **埋点事件**：
  - 登录/注册事件
  - 主页打开 (open_main)
  - 各功能使用事件
  - 页面自动采集 (AUTO 模式)
  - 崩溃监控 (UMCrash)

### P3 — 辅助功能

#### F-012：应用内 WebView 协议页
- **描述**：WebViewActivity 展示服务协议和隐私政策完整内容
- **URL**：`http://47.115.223.27/privacypolicy.html`

#### F-013：视频播放器
- **描述**：VideoPlayerActivity 提供视频播放能力（用途待确认）

---

## 四、页面/交互流程

### 4.1 整体流程图

```
用户启动APP
    │
    ▼
SplashActivity
    │
    ├─[首次]→ PrivacyPolicyDialog ──[同意]──┐
    │         └─[不同意]→ NotPrivacyPolicyDialog  │
    │                       ├─[同意]──┐    │
    │                       └─[不同意]→ 退出APP  │
    │                                    │    │
    ├─[已同意，未登录]────────────────────────────────→ LoginActivity
    │                                                    │
    │                                           ├─[登录成功]→ MainActivity
    │                                           └─[注册]→ RegisterActivity
    │                                                        │
    │                                                        └─[注册成功]→ LoginActivity
    │
    └─[已登录，Token有效]────────────────────────────────→ MainActivity
                                                              │
    ┌─────────────────────────────────────────────────────────┘
    ▼
MainActivity
    ├── 选择功能(1-8) → FlexboxRadioGroup
    ├── 选择触控点列表
    ├── 设置专属平台(抖音/快手/不限)
    ├── 设置卡福袋时间(抢福袋模式)
    ├── 选择动画模式(功德/跳绳/随机)
    └── [开始] → 启动触控服务
                    │
                    ▼
         AutoTouchService 执行触控
                    │
         FloatingService 悬浮窗动画
                    │
             点击悬浮窗 → MenuDialog
                    ├── 添加触控点
                    ├── 配置自动回复话术
                    ├── 停止触控
                    └── 退出助手
```

### 4.2 页面清单

| 页面 | 布局文件 | 功能 |
|---|---|---|
| 启动页 | `activity_splash.xml` | 隐私检查 + 自动登录 |
| 登录页 | `activity_login.xml` | 手机号/密码登录 |
| 注册页 | `activity_register.xml` | 手机号注册 |
| 主页 | `activity_main.xml` | 功能选择 + 配置 |
| WebView 页 | `webview_activity.xml` | 协议展示 |
| 视频页 | `activity_videoplayer.xml` | 视频播放 |
| 菜单弹窗 | `dialog_menu.xml` | 触控快捷操作 |
| 添加触控点 | `dialog_add_point.xml` | 触控点配置 |
| 话术配置 | `dialog_auto_reply_script.xml` | 自动回复话术 |
| 隐私政策 | `pop_privacypolicy_dialog.xml` | 首次同意 |
| 二次确认 | `pop_notprivacypolicy_dialog.xml` | 不同意二次确认 |
| 通用提示 | `pop_commtips_dialog.xml` | 通用确认对话框 |
| 悬浮窗 | `layout_window.xml` | 小鸡动画悬浮窗 |

---

## 五、非功能需求

### 5.1 性能要求

| 指标 | 要求 |
|---|---|
| 触控响应延迟 | < 100ms（从触发到执行） |
| 帧动画流畅度 | ≥ 30fps（悬浮窗小鸡动画） |
| 内存占用 | 空闲 < 80MB，运行时 < 150MB |
| APK 包体 | < 30MB |
| 网络请求超时 | 连接 120s，读写 60s |

### 5.2 安全要求

| 要求 | 说明 |
|---|---|
| Token 存储 | 使用 SharedPreferences 存储（需注意：当前明文存储密码，建议加密） |
| HTTPS | 当前使用 HTTP，后续需升级为 HTTPS 并配置 SSL 证书校验 |
| 代码混淆 | Release 版本必须开启 ProGuard 混淆 |
| 签名保护 | 使用独立签名文件签名 |

### 5.3 兼容性

| 维度 | 范围 |
|---|---|
| 最低 Android 版本 | Android 7.0 (API 24) |
| 目标 SDK | Android 14 (API 34) |
| 编译 SDK | Android 10 (API 29) |
| 目标平台 | 抖音 (`com.ss.android.ugc.aweme`)、快手 (`com.smile.gifmaker`) |
| 屏幕适配 | 各分辨率均支持（动态获取屏幕尺寸计算手势路径） |

### 5.4 可靠性

- 无障碍服务被杀后需提示用户重新开启
- 悬浮窗权限被收回时需引导用户重新授权
- 网络请求失败需给出明确错误提示
- Token 过期自动跳转登录页

---

## 六、技术架构建议

### 6.1 原始架构回顾

项目当前采用 **RxJava 驱动的轻量级 MVC**，存在以下问题：
- Activity 直接耦合网络层，无中间层解耦
- 密码明文存储
- SSL 证书信任全部
- 无依赖注入
- 无障碍服务单文件过长（870行）

### 6.2 重建时建议的技术架构

```
┌─────────────────────────────────────────┐
│              Presentation                │
│  Jetpack Compose / XML Views            │
│  ViewModel (MVVM)                       │
│  StateFlow / LiveData                   │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│              Domain Layer                │
│  UseCase / Repository Interface         │
│  Business Logic Models                  │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│              Data Layer                  │
│  Repository Implementation              │
│  Retrofit + OkHttp API Service          │
│  DataStore / Room                        │
│  Accessibility Service Engine           │
└─────────────────────────────────────────┘
```

**技术选型建议：**

| 组件 | 建议方案 | 原因 |
|---|---|---|
| UI | Jetpack Compose + Material3 | 现代声明式UI，减少 XML |
| 架构 | MVVM + Repository | Activity/Fragment 解耦 |
| 网络 | Retrofit2 + OkHttp + Coroutines | 替代 RxJava，更轻量 |
| 本地存储 | DataStore / Room | 替代 SharedPreferences |
| 依赖注入 | Hilt (Dagger) | 标准 DI 方案 |
| 异步 | Kotlin Coroutines + Flow | 替代 RxJava |
| 状态管理 | StateFlow / SharedFlow | 替代 EventBus |
| 图片加载 | Coil | 小鸡动画帧加载 |
| 导航 | Jetpack Navigation | Fragment 导航 |
| 混淆 | R8 + ProGuard | release 混淆 |

### 6.3 推荐项目结构

```
app/src/main/java/com/wly/beansprout/
├── App.kt                          # Application
├── MainActivity.kt                 # 单 Activity 架构
├── di/                             # Hilt 模块
│   └── AppModule.kt
├── data/
│   ├── local/                      # DataStore / Room
│   │   ├── AccountDataStore.kt
│   │   └── TouchPointDatabase.kt
│   ├── remote/                     # Retrofit
│   │   ├── ApiService.kt
│   │   ├── dto/                    # 网络 DTO
│   │   └── interceptor/
│   └── repository/
│       ├── AuthRepository.kt
│       └── UpdateRepository.kt
├── domain/
│   ├── model/                      # 领域模型
│   └── usecase/
├── ui/
│   ├── splash/
│   ├── auth/                       # 登录/注册
│   ├── home/                       # 主页
│   ├── floating/                   # 悬浮窗
│   └── components/                 # 通用组件
├── service/
│   ├── AutoTouchService.kt         # 无障碍服务
│   └── FloatingService.kt          # 悬浮窗服务
├── engine/
│   ├── TouchEngine.kt              # 触控引擎
│   ├── NodeFinder.kt               # 节点搜索
│   └── LuckyBagEngine.kt           # 福袋引擎
└── util/
```

---

## 七、API 接口列表

基于代码 `AccountService.java` 提取，所有接口均使用 HTTP POST + JSON Body。

**Base URL**：`http://www.dagongji.xin/`（备用 `http://47.115.223.27/`）

### 7.1 账户相关

#### POST /member/login
- **描述**：用户登录
- **请求体**：
```json
{
  "userId": "",
  "version": 1,
  "client": { "deviceid": "xxx", "os": "android", "osver": "29", "ppiheight": "2340", "ppiwidth": "1080", "vercode": "154", "vername": "1.5.4", "cell": "", "simid": "" },
  "token": "",
  "language": "ZH",
  "data": {
    "username": "13800138000",
    "password": "xxxxxx"
  }
}
```
- **响应体**：
```json
{
  "version": "1",
  "errorinfo": null,
  "data": {
    "token": "xxx",
    "userId": "xxx",
    "userName": "用户昵称",
    "userPhone": "13800138000",
    "status": 4,
    "daysRemaining": 15
  }
}
```
- **错误码**：`10030` = Token 失效

#### POST /member/register
- **描述**：用户注册
- **请求体**：同上，data 含 `username` + `password`
- **响应体**：
```json
{
  "data": {
    "succ": 1,
    "result": 0
  }
}
```
- `succ=1` 表示成功

#### POST /member/validToken
- **描述**：验证 Token 有效性（登录状态保持）
- **请求体**：携带已保存的 `token` + `userId`
- **响应体**：与 `/member/login` 相同

#### POST /member/quickLogin
- **描述**：快捷登录（备用，当前未使用）
- **请求体/响应体**：与 `/member/login` 相同

### 7.2 系统相关

#### POST /system/getVersion
- **描述**：获取 APP 最新版本信息
- **请求体**：
```json
{
  "userId": "xxx",
  "version": 1,
  "token": "xxx",
  "client": { ... },
  "data": { }
}
```
- **响应体**：
```json
{
  "data": {
    "verCode": 155,
    "verName": "1.5.5",
    "name": "打工鸡",
    "fileName": "beansprout_1.5.5.apk",
    "filePath": "http://www.dagongji.xin/download/beansprout_1.5.5.apk",
    "appForce": 0,
    "newAppSize": 25.5,
    "newAppUpdateDesc": "更新内容：\n1.修复一些已知问题"
  }
}
```

---

## 八、数据模型

### 8.1 核心实体

#### User（用户信息）
| 字段 | 类型 | 说明 |
|---|---|---|
| token | String | 认证令牌 |
| userId | String | 用户唯一标识 |
| userName | String | 用户昵称 |
| userPhone | String | 手机号 |
| status | Int | 1=待审核, 2=审核中, 3=已退回, 4=使用中, 5=已停用, 6=体验中, 9=已删除 |
| daysRemaining | Int | 体验剩余天数 |

#### TouchPoint（触控点）
| 字段 | 类型 | 说明 |
|---|---|---|
| name | String | 触控点名称 |
| x | Int | 屏幕 X 坐标 (px) |
| y | Int | 屏幕 Y 坐标 (px) |
| delay | Int | 操作间隔 (ms) |
| functionType | Int | 1=单击, 2=点赞, 3=下滑, 4=上下滑, 5=左滑, 6=右滑, 7=自动回复, 8=抢福袋 |
| isStartClick | Boolean | 是否已激活 |
| luckybagTime | Int | 抢福袋时间阈值（分钟），-1/999=不限 |

#### DeviceInfo（设备信息）
| 字段 | 类型 | 说明 |
|---|---|---|
| deviceid | String | IMEI |
| simid | String | SIM 卡序列号 |
| os | String | 固定 "android" |
| osver | String | Android SDK 版本号 |
| vercode | String | APP versionCode |
| vername | String | APP versionName |
| cell | String | 手机号码 |
| ppiwidth | String | 屏幕宽度 (px) |
| ppiheight | String | 屏幕高度 (px) |

#### AppUpdate（版本升级）
| 字段 | 类型 | 说明 |
|---|---|---|
| verCode | Int | 最新版本号 |
| verName | String | 最新版本名 |
| name | String | APP 名称 |
| fileName | String | APK 文件名 |
| filePath | String | 下载地址 |
| appForce | Int | 1=强制升级, 0=可选 |
| newAppSize | Float | 文件大小 (MB) |
| newAppUpdateDesc | String | 更新日志 |

#### TouchEvent（触控事件）
| 字段 | 类型 | 说明 |
|---|---|---|
| action | Int | 1=开始, 2=暂停, 3=继续, 4=停止 |
| touchPoint | TouchPoint | 关联的触控点配置 |

### 8.2 通用数据结构

#### GsonRequest\<T\>（请求封装）
| 字段 | 类型 | 说明 |
|---|---|---|
| userId | String | 用户 ID |
| version | Int | API 版本 (1) |
| client | Object (ClientInfo) | 设备信息 |
| token | String | 认证令牌 |
| language | String | ZH/EN/AR |
| data | T | 业务数据 |

#### GsonResponse\<T\>（响应封装）
| 字段 | 类型 | 说明 |
|---|---|---|
| version | String | 响应版本 |
| errorinfo | ErrorInfo? | 错误信息，null 表示成功 |
| data | T | 业务数据 |

#### ErrorInfo
| 字段 | 类型 | 说明 |
|---|---|---|
| errorcode | String | 错误码，负数=错误，正数=警告 |
| errormessage | String | 错误提示文案 |

#### CommonResponse（通用响应）
| 字段 | 类型 | 说明 |
|---|---|---|
| succ | Int | 1=成功, 0=失败 |
| result | Int | 结果码 |

---

## 九、状态管理

### 9.1 TouchEventManager（触控状态）

| 状态 | 说明 |
|---|---|
| ACTION_START (1) | 触控已启动，正在执行 |
| ACTION_CONTINUE (3) | 从暂停恢复继续 |
| ACTION_PAUSE (2) | 暂停（切换 App 时） |
| ACTION_STOP (4) | 完全停止 |

**状态判断：**
- `isTouching()` = action == START || action == CONTINUE
- `isPaused()` = action == PAUSE
- `isStart()` = action == START

### 9.2 AccountManager（账户状态）

- `isLogin()` = `!TextUtils.isEmpty(token) && !TextUtils.isEmpty(userId)`

---

## 十、本地存储设计

### 10.1 AccountManager (SharedPreferences)

| Key | 类型 | 说明 |
|---|---|---|
| `token` | String | 认证令牌 |
| `userId` | String | 用户 ID |
| `username` | String | 手机号 |
| `password` | String | 密码（明文—需改进） |
| `nickname` | String | 昵称 |
| `userPhone` | String | 手机号 |
| `headImgUrl` | String | 头像 URL |
| `autoReplyScript` | String | 自动回复话术（换行分隔） |
| `status` | Int | 账户状态 |
| `daysRemaining` | Int | 剩余天数 |
| `privacyPolicy` | Boolean | 是否同意隐私政策 |
| `privacyPolicyClickData` | String | 隐私政策点击日期 |

### 10.2 SpUtils (SharedPreferences)

| Key | 类型 | 说明 |
|---|---|---|
| `touch_list` | JSON String | 触控点列表（Gson 序列化） |

### 10.3 建议改进

| 当前方案 | 建议方案 |
|---|---|
| SharedPreferences 明文 | EncryptedSharedPreferences / DataStore + 加密 |
| 密码明文存储 | 仅存 Token，不缓存密码 |
| Gson JSON 字符串 | Room 数据库 / DataStore Proto |

---

## 十一、关键非功能约束补充

| 约束 | 说明 |
|---|---|
| 无障碍服务声明 | 必须在 `res/xml/accessibility_service_config.xml` 配置 description |
| 悬浮窗权限 | Android 6.0+ 需 `SYSTEM_ALERT_WINDOW`，8.0+ 需 `TYPE_APPLICATION_OVERLAY` |
| 文件共享 | 使用 FileProvider（`com.wly.beansprout.fileprovider`）暴露 APK 安装文件 |
| 友盟渠道 | `test_channel`，发布时需更换 |
| 隐私合规 | 友盟 SDK 初始化前必须获取用户同意（`UMConfigure.submitPolicyGrantResult`） |

---

## 十二、重建优先级建议

| 阶段 | 内容 | 说明 |
|---|---|---|
| Phase 1 | 无障碍触控引擎 + 悬浮窗 | 核心能力，先跑通 |
| Phase 2 | 用户系统 + 注册登录 + Token 管理 | 接入后端 |
| Phase 3 | 触控点管理 + 8 种功能 + 菜单弹窗 | 完整功能 |
| Phase 4 | 专属平台 + 抢福袋时间阈值 + 自动回复 | 体验增强 |
| Phase 5 | 版本更新 + 隐私政策 + 友盟统计 | 运营支撑 |
| Phase 6 | 重构架构 (MVVM + Hilt + Coroutines) | 技术债清理 |

---

*文档完*