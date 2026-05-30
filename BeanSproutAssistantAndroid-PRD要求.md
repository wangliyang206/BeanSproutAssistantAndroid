# 打工鸡 APP（BeanSproutAssistantAndroid）产品需求文档

> **版本**：1.0 | **日期**：2026-05-30 | **状态**：基于 v1.5.4 代码反向推导

---

## 一、项目背景与定位

### 1.1 产品概述

"打工鸡"是一款 Android 平台自动化辅助工具，利用系统无障碍服务（AccessibilityService）在抖音和快手等短视频/直播平台上模拟用户操作行为。

### 1.2 产品定位

短视频平台互动自动化助手。核心价值主张：帮助用户解放双手，自动完成直播互动中的重复性操作（点赞、刷视频、抢福袋、自动回复等）。

### 1.3 产品概念

以**小鸡卡通形象**为交互载体，包装为"雇佣小鸡帮你打工"的概念，降低用户对"自动化脚本"的负面认知。小鸡在屏幕上以悬浮窗形式呈现，支持多种动画效果，用户通过点击小鸡唤出功能菜单。

### 1.4 商业模式

- **15 天免费体验**：新用户注册后享有 15 天全功能免费体验
- **付费会员制**：体验期结束后需付费使用（具体付费方案由服务端控制）
- **服务端用户状态**：1待审核 / 2审核中 / 3已退回 / 4使用中 / 5已停用 / 6体验中 / 9已删除

---

## 二、目标用户画像

| 画像维度 | 描述 |
|---------|------|
| **主要人群** | 抖音/快手直播重度用户（主播粉丝、直播带货关注者） |
| **次要人群** | 电商直播运营人员、MCN 机构从业者 |
| **年龄** | 18-45 岁 |
| **设备** | Android 7.0+ 智能手机 |
| **核心痛点** | 手动点赞疲劳、抢福袋耗时耗力、刷视频手酸、多个直播间无法同时操作 |
| **典型使用场景** | ① 睡前自动刷视频 ② 直播期间自动点赞与互动 ③ 定时蹲守抢福袋 ④ 挂机自动回复话术 |
| **技术接受度** | 中等偏低（需引导开启无障碍服务和悬浮窗权限） |

---

## 三、功能需求清单

### 3.1 功能分组总览

| 分组 | 功能数 | P0 | P1 | P2 | P3 |
|------|--------|-----|-----|-----|-----|
| 账号体系 | 6 | 5 | 1 | 0 | 0 |
| 核心触控 | 10 | 7 | 3 | 0 | 0 |
| 平台与动画 | 5 | 1 | 3 | 1 | 0 |
| 系统集成 | 4 | 4 | 0 | 0 | 0 |
| 应用管理 | 5 | 1 | 2 | 2 | 0 |
| **合计** | **30** | **18** | **9** | **3** | **0** |

### 3.2 账号体系

| ID | 功能 | 优先级 | 描述 | 验收标准 |
|----|------|--------|------|---------|
| F01 | 手机号注册 | **P0** | 手机号 + 密码 + 确认密码注册，验证手机号格式（`^1[3-9]\d{9}$`）和两次密码一致性 | 非法手机号拦截；两次密码不一致提示；注册成功跳转登录页 |
| F02 | 密码登录 | **P0** | 手机号 + 密码登录，成功后保存 Token 及用户信息到本地 | 正确凭据登录成功并存储 Token；错误凭据显示服务端错误信息 |
| F03 | Token 自动验证 | **P0** | 应用启动时自动调用 `validToken` 接口校验 Token 有效性，有效则跳过登录直接进入主页 | 有效 Token 无感进入主页；无效 Token 跳转登录页 |
| F04 | 用户状态管理 | **P0** | 区分 6 种用户状态（待审核/审核中/已退回/使用中/已停用/体验中），不同状态对应不同功能权限 | 体验中用户显示剩余天数；停用用户提示联系客服 |
| F05 | 免费体验机制 | **P0** | 新用户注册后 15 天免费体验，主页和登录后显示剩余天数 | 剩余天数准确递减；过期后提示续费 |
| F06 | 退出登录 | P1 | 清除本地 Token、账号、密码、用户信息等所有持久化数据 | 退出后跳转登录页；Token 不再自动填充 |

### 3.3 核心触控功能

| ID | 功能 | 优先级 | 描述 | 实现要点 |
|----|------|--------|------|---------|
| F07 | 轻点触发 | **P0** | 在指定屏幕坐标执行单击操作，支持自定义点击间隔（毫秒） | 基于 `GestureDescription.StrokeDescription` 单点触控 |
| F08 | 直播点赞 | **P0** | 连续快速点击屏幕指定位置，模拟直播点赞行为 | 循环点击 + 短暂延时，频率约 100-200ms/次 |
| F09 | 向下滑动 | **P0** | 模拟向下滑动手势，用于刷下一个视频 | `Path.lineTo` 生成起点到终点路径，支持调节滑动步数 |
| F10 | 向上滑动 | P1 | 模拟向上滑动手势 | 同 F09，方向相反 |
| F11 | 向左滑动 | P1 | 模拟向左滑动手势 | 同 F09，方向相反 |
| F12 | 向右滑动 | P1 | 模拟向右滑动手势 | 同 F09，方向相反 |
| F13 | 自动回复 | **P0** | 在直播间自动发送预设话术，支持配置多条随机发送（分号分隔） | 通过 `AccessibilityNodeInfo` 查找输入框控件 → 设置文本 → 查找并点击发送按钮 |
| F14 | 超级福袋 | **P0** | 自动检测并参与超级福袋抽奖，支持卡时间配置（如只抢 5 分钟内的福袋） | 多层控件路径匹配 → 提取倒计时 → 判断卡点时间区间 → 模拟点击参与按钮 |
| F15 | 触控点管理 | **P0** | 添加/删除触控点，每个触控点包含名称、屏幕坐标(x,y)、间隔时间(ms)，数据 JSON 序列化存储在 SharedPreferences | 添加时通过全屏覆盖层捕获用户触摸坐标；列表展示已添加的触控点 |
| F16 | 触控启停控制 | **P0** | 通过悬浮窗菜单开始/暂停/继续/停止触控，通过 EventBus 发送 TouchEvent | 开始：发送 START 事件 + TouchPoint；暂停：PAUSE；继续：CONTINUE；停止：STOP |

### 3.4 平台与动画

| ID | 功能 | 优先级 | 描述 | 实现要点 |
|----|------|--------|------|---------|
| F17 | 多平台支持 | **P0** | 支持抖音（`com.ss.android.ugc.aweme`）和快手（`com.smile.gifmaker`） | TouchEventManager 管理专属包名；触控时校验前台应用包名 |
| F18 | 专属模式 | P1 | 设置专属应用，打开目标应用时自动启动/暂停触控操作 | 监听前台应用切换，匹配专属包名时自动执行预设触控动作 |
| F19 | 功德小鸡动画 | P1 | 默认小鸡模型，包含眨眼、挥手、缩地闪现等动画 | 多帧 drawable 资源，定时切换实现动画效果 |
| F20 | 跳绳小鸡动画 | P1 | 备选小鸡模型，包含路径移动、转圈、跳绳等动画 | 路径计算 + 帧动画 + 旋转动画组合 |
| F21 | 随机动画 | P2 | 小鸡随机执行扭动/变身/扭头等额外动画效果 | 定时器触发，随机选取动画序列执行 |

### 3.5 系统集成

| ID | 功能 | 优先级 | 描述 | 验收标准 |
|----|------|--------|------|---------|
| F22 | 无障碍服务引导 | **P0** | 检测无障碍服务是否开启，未开启时引导用户跳转系统设置页开启 | 检测到未开启时弹出 Dialog 提示，确认后跳转系统无障碍设置页 |
| F23 | 悬浮窗权限引导 | **P0** | 检测 `SYSTEM_ALERT_WINDOW` 权限是否授予，未授予时引导用户授权 | 通过 `Settings.canDrawOverlays()` 检测，未授权时引导授权 |
| F24 | 悬浮窗交互 | **P0** | 小鸡浮窗支持触摸拖拽移动位置，点击弹出功能控制菜单（MenuDialog） | 拖拽流畅无卡顿；点击响应 100ms 内弹出菜单 |
| F25 | 后台服务保活 | P1 | FloatingService 和 AutoTouchService 在后台持续运行，不被系统杀死 | 前台服务通知；WakeLock 防止休眠 |

### 3.6 应用管理

| ID | 功能 | 优先级 | 描述 | 验收标准 |
|----|------|--------|------|---------|
| F26 | 版本更新检查 | P1 | 启动时调用 `getVersion` 接口检查服务端最新版本，支持强制更新（`appForce=1`）和可选更新 | 可选更新显示更新日志和取消按钮；强制更新只能点击"立即更新" |
| F27 | APK 下载安装 | P1 | 通过浏览器下载最新 APK，下载完成后通过 FileProvider 调起系统安装 | 下载有进度提示；安装兼容 Android 7.0+ FileProvider |
| F28 | 隐私政策弹窗 | **P0** | 首次启动强制展示隐私政策和服务协议，同意后才能进入应用 | 同意写入标记到 SP；拒绝则退出应用 |
| F29 | 教程视频 | P2 | 内置教程视频播放（`res/raw/tutorial_video.mp4`），视频有进度条和播放控制 | VideoPlayerActivity 全屏播放，支持暂停/进度拖拽 |
| F30 | 数据统计 | P2 | 接入友盟 SDK 进行应用使用数据统计 | 统计基础使用数据，含错误上报（友盟 APM） |

---

## 四、页面与交互流程

### 4.1 应用启动流程

```
用户点击应用图标
    │
    ▼
┌─────────────────────────┐
│   SplashActivity 启动页  │
│   - 品牌 Logo 展示       │
│   - 加载动画             │
└──────────┬──────────────┘
           │
           ▼
    ┌─ 隐私政策是否已同意？─┐
    │                       │
   否                       是
    │                       │
    ▼                       ▼
┌──────────────┐   ┌── Token 是否有效？──┐
│ 弹窗展示      │   │                    │
│ 隐私政策      │   无效/不存在           有效
│ + 服务协议    │   │                    │
│              │   ▼                    ▼
│ [同意] [拒绝] │  LoginActivity    MainActivity
└──┬──────┬────┘
   │      │
  同意   拒绝
   │      │
  保存    退出
  SP      应用
   │
   ▼
  Token校验...
```

### 4.2 登录注册流程

```
┌─────────────────────────────────────┐
│           LoginActivity              │
│  - 手机号输入框                       │
│  - 密码输入框（支持显示/隐藏切换）       │
│  - 同意协议复选框 + 协议文本链接        │
│  - [登录] 按钮                        │
│  - "还没有账号？去注册" 链接           │
└───────────┬─────────────────────────┘
            │ 点击登录
            ▼
     校验输入完整性 + 协议勾选
            │
            ▼
   POST /member/login
   { mobile, password }
            │
     ┌──────┴──────┐
   成功            失败
     │              │
     ▼              ▼
 保存 Token      显示 ErrorInfo
 用户信息        .errormessage
     │
     ▼
 MainActivity

═══════════════════════════════════

┌─────────────────────────────────────┐
│         RegisterActivity             │
│  - 手机号输入框                       │
│  - 密码输入框                         │
│  - 确认密码输入框                     │
│  - [注册] 按钮                        │
│  - "已有账号？去登录" 链接            │
└───────────┬─────────────────────────┘
            │ 点击注册
            ▼
   校验手机号格式（^1[3-9]\d{9}$）
   校验两次密码一致
            │
            ▼
   POST /member/register
   { mobile, password }
            │
     ┌──────┴──────┐
   成功            失败
     │              │
     ▼              ▼
 跳转登录页      显示错误信息
```

### 4.3 主页交互流程

```
┌───────────────────────────────────────────┐
│              MainActivity                  │
│  ┌─────────────────────────────────────┐  │
│  │  标题栏：打工鸡          [版本信息]  │  │
│  ├─────────────────────────────────────┤  │
│  │  功能选择（RadioGroup）              │  │
│  │  ○ 轻点触发  ○ 直播点赞              │  │
│  │  ○ 向下滑动  ○ 向上滑动              │  │
│  │  ○ 向左滑动  ○ 向右滑动              │  │
│  │  ○ 自动回复  ○ 超级福袋              │  │
│  ├─────────────────────────────────────┤  │
│  │  平台选择： ○ 抖音  ○ 快手          │  │
│  │  模型选择： ○ 功德小鸡  ○ 跳绳小鸡   │  │
│  ├─────────────────────────────────────┤  │
│  │  用户信息：手机号 / 剩余天数         │  │
│  │  [检查更新]                 [退出]  │  │
│  ├─────────────────────────────────────┤  │
│  │         [ 启动悬浮窗 ]              │  │
│  └─────────────────────────────────────┘  │
└───────────────────────────────────────────┘
                    │ 点击启动悬浮窗
                    ▼
         ┌─ 无障碍服务已开启？──┐
         │                      │
         否                     是
         │                      │
         ▼                      ▼
   跳转系统设置          ┌─ 悬浮窗权限已授予？──┐
   无障碍页面            │                      │
                        否                     是
                        │                      │
                        ▼                      ▼
                   引导用户授权          启动 FloatingService
                                        显示小鸡悬浮窗
```

### 4.4 悬浮窗交互流程

```
┌──────────────────────────────┐
│     FloatingService 悬浮窗    │
│                              │
│   ┌──────────────────┐      │
│   │   小鸡动画视图    │      │
│   │   ┌──────┐       │      │
│   │   │ 🐤   │       │      │
│   │   └──────┘       │      │
│   └──────────────────┘      │
│       可拖拽移动              │
│       点击弹出菜单            │
└──────────┬───────────────────┘
           │ 点击小鸡
           ▼
┌──────────────────────────────────┐
│          MenuDialog              │
│  ┌────────────────────────────┐  │
│  │ 触控点列表 (RecyclerView)   │  │
│  │ ┌──────────────────────┐   │  │
│  │ │ 🟢 点1-首页点赞(500ms)│   │  │
│  │ │ ⭕ 点2-福袋点击(200ms)│   │  │
│  │ │ ⭕ 点3-向下滑(1000ms) │   │  │
│  │ └──────────────────────┘   │  │
│  ├────────────────────────────┤  │
│  │ [添加触控点] [自动回复话术] │  │
│  │ [停止触控]   [退出悬浮窗]   │  │
│  └────────────────────────────┘  │
└──────┬───────────────────────────┘
       │
       ├─ 点击触控点 → EventBus 发送 TouchEvent(START)
       │               → AutoTouchService 接收并执行
       │
       ├─ 停止 → EventBus 发送 TouchEvent(STOP)
       │
       ├─ 添加 → 弹出 AddPointDialog（全屏透明覆盖层）
       │          触摸屏幕任意位置 → 获取坐标(x, y)
       │          → 输入名称和间隔 → 保存到 SP
       │
       ├─ 自动回复 → 弹出 AutomaticReplyScriptDialog
       │              输入话术（分号分隔多条）
       │              → 保存到 AccountManager
       │
       └─ 退出 → 停止服务 + removeView
```

---

## 五、非功能需求

### 5.1 性能

| 指标 | 目标值 | 测量方法 |
|------|--------|---------|
| 应用冷启动时间 | ≤ 2 秒 | 从点击图标到 SplashActivity 可见 |
| 触控响应延迟 | ≤ 100ms | 从发送 TouchEvent 到首次触控执行 |
| 悬浮窗动画帧率 | ≥ 30 FPS | WindowManager 帧回调 |
| 内存占用（空闲） | ≤ 80MB | ActivityManager.getMemoryInfo |
| 内存占用（触控运行中） | ≤ 150MB | 同上 |
| 网络请求超时 | 连接 120s / 读写 60s | OkHttp 配置 |
| APK 体积 | ≤ 20MB | release 构建产物 |

### 5.2 安全

| 安全项 | 要求 | 当前状态 |
|--------|------|---------|
| 密码存储加密 | 本地密码使用 EncryptedSharedPreferences 或 Android KeyStore 加密 | ⚠️ 明文存储 |
| Token 安全管理 | Token 本地存储 + 服务端 30 分钟过期机制 | ✅ 已实现 |
| 网络传输 | HTTPS 加密传输，验证服务端证书 | ⚠️ 信任所有 SSL 证书 |
| APK 安装 | 安装前验证签名一致性 | ⚠️ 未验证 |
| 隐私政策 | 首次启动强制弹窗确认，明确告知数据收集范围 | ✅ 已实现 |
| 混淆保护 | release 构建启用 ProGuard 代码混淆 + 资源压缩 | ✅ 已实现 |
| 敏感权限 | 运行时动态请求，用户可拒绝非核心功能相关权限 | ⚠️ 部分权限未做运行时请求 |

### 5.3 兼容性

| 维度 | 要求 |
|------|------|
| 最低系统版本 | Android 7.0 (API 24) |
| 目标系统版本 | Android 14 (API 34) |
| 主流厂商适配 | 小米（MIUI）、华为（HarmonyOS/EMUI）、OPPO（ColorOS）、vivo（OriginOS） |
| 无障碍服务适配 | 各厂商 ROM 定制无障碍服务路径兼容（跳转设置页链接） |
| 悬浮窗适配 | 各厂商悬浮窗权限管理路径兼容 |
| 屏幕适配 | 支持 4.0-7.0 英寸屏幕，分辨率 720P-2K |
| 架构支持 | ARM64-v8a + ARMv7a（当前未明确声明，需补充） |

### 5.4 可用性

| 要求 | 实现方式 |
|------|---------|
| 首次使用引导 | 启动后引导开启无障碍服务和悬浮窗权限 |
| 操作反馈 | 触控点添加/删除/启停均有 Toast 提示 |
| 错误提示 | 网络异常、登录失败、Token 过期等服务端错误明确展示 errormessage |
| 帮助入口 | 内置教程视频（`tutorial_video.mp4`） |
| 防误操作 | 退出应用时双击确认；快速点击按钮防连击（800ms 间隔） |
| 状态可视化 | 触控点列表项显示启停状态指示；主页显示用户剩余体验天数 |

---

## 六、技术架构建议（用于 claude code 重建）

### 6.1 推荐架构：MVVM + Repository + Clean Architecture

当前项目采用简化的 MVP 混合架构（Activity 直接持有 HttpClient），建议重建时升级为以下分层架构：

```
┌─────────────────────────────────────────┐
│                UI Layer                  │
│  Activity / Fragment + ViewModel         │
│  DataBinding / ViewBinding               │
│  Compose UI（可选，现代化 UI）            │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│             Domain Layer                 │
│  UseCase（封装单一业务动作）               │
│  Domain Model（纯 Kotlin data class）     │
│  Repository Interface                    │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│              Data Layer                  │
│  Repository Implementation               │
│  ├── Remote: Retrofit2 API Service       │
│  ├── Local: Room DAO + DataStore         │
│  └── Mapper: DTO ↔ Domain ↔ Entity       │
└─────────────────────────────────────────┘
```

### 6.2 推荐技术栈对比

| 层面 | 当前技术（Java） | 推荐技术（Kotlin） | 理由 |
|------|-----------------|-------------------|------|
| 语言 | Java | **Kotlin** | 更简洁、空安全、协程原生支持、Google 官方推荐 |
| 异步 | RxJava2 | **Kotlin Coroutines + Flow** | 协程更轻量、更易读、与 Jetpack 深度集成 |
| DI | 无 | **Hilt (Dagger2)** | 标准化依赖注入，减少模板代码 |
| 数据库 | 无（JSON 存 SP） | **Room** | 类型安全、编译时校验、支持 Flow 响应式查询 |
| 本地存储 | Tray + SharedPreferences | **DataStore** | 协程原生支持、类型安全、替代 SP |
| 架构组件 | 无 | **ViewModel + LiveData/StateFlow** | 生命周期感知、配置变更数据保留 |
| 图片加载 | Glide | **Coil** 或保留 Glide | Coil 更轻量、Kotlin 协程原生 |
| 事件总线 | EventBus | **SharedFlow / Channel** | Kotlin 原生、类型安全、无需第三方依赖 |
| 网络 | Retrofit2 | Retrofit2 (保留) | 成熟稳定，与协程无缝集成 |

### 6.3 推荐模块结构

```
app/
├── di/                          # Hilt 依赖注入模块
│   ├── AppModule                # Application 级依赖
│   ├── NetworkModule            # 网络层依赖
│   └── DatabaseModule           # 数据库依赖
├── data/                        # 数据层
│   ├── local/
│   │   ├── dao/                 # Room DAO 接口
│   │   ├── entity/              # Room Entity
│   │   └── datastore/           # DataStore 管理
│   ├── remote/
│   │   ├── api/                 # Retrofit API 接口
│   │   ├── dto/                 # 网络 DTO 数据类
│   │   └── interceptor/         # OkHttp Interceptor
│   └── repository/              # Repository 实现
├── domain/                      # 领域层
│   ├── model/                   # 领域模型
│   ├── repository/              # Repository 接口
│   └── usecase/                 # UseCase
├── ui/                          # UI 层
│   ├── splash/                  # 启动页
│   ├── login/                   # 登录/注册
│   ├── main/                    # 主页
│   └── common/                  # 公共 UI 组件
├── service/                     # 后台服务
│   ├── accessibility/           # 无障碍服务核心
│   │   ├── AutoTouchService
│   │   ├── TouchExecutor        # 触控执行器（策略模式）
│   │   │   ├── ClickExecutor
│   │   │   ├── SwipeExecutor
│   │   │   ├── LikeExecutor
│   │   │   ├── ReplyExecutor
│   │   │   └── LuckyBagExecutor
│   │   └── PlatformAdapter      # 平台适配器
│   │       ├── DouyinAdapter
│   │       └── KuaishouAdapter
│   └── floating/                # 悬浮窗服务
│       ├── FloatingService
│       └── ChickenAnimation     # 动画管理器
├── util/                        # 工具类
└── MyApplication                # Application
```

### 6.4 关键架构决策

| 决策点 | 方案 | 理由 |
|--------|------|------|
| 触控执行器解耦 | 策略模式：TouchExecutor 接口 + 每种功能独立实现 | 当前 AutoTouchService 870 行过于臃肿，拆分后每类触控逻辑独立、可测试 |
| 平台适配 | 适配器模式：PlatformAdapter 接口 + 抖音/快手独立实现 | 控点查找路径、UI 控件层级因平台而异，独立适配便于扩展新平台 |
| 触控点持久化 | Room 替代 JSON 存 SP | 结构化管理、支持复杂查询、避免 SP 大数据序列化卡顿 |
| 安全加固 | 移除 TrustAllCerts + 密码加密存储 | 消除中间人攻击风险；EncryptedSharedPreferences 保护用户凭据 |

---

## 七、API 接口列表

### 7.1 接口总览

> 服务器地址：`http://www.dagongji.xin/`（⚠️ HTTP，建议升级为 HTTPS）

| 序号 | 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|------|
| 1 | 密码登录 | POST | `/member/login` | 手机号+密码登录 |
| 2 | 手机号注册 | POST | `/member/register` | 手机号+密码注册 |
| 3 | Token 验证 | POST | `/member/validToken` | 验证当前 Token 有效性 |
| 4 | 快捷登录 | POST | `/member/quickLogin` | 快捷登录（待实现） |
| 5 | 版本检查 | POST | `/system/getVersion` | 获取最新版本信息 |

### 7.2 接口详情

#### 接口 1：密码登录

```
POST /member/login

Request (GsonRequest<Map>):
{
  "userId": "",
  "version": 1,
  "token": "",
  "language": "ZH",
  "client": { ClientInfo },
  "data": {
    "mobile": "13800138000",
    "password": "xxx"
  }
}

Response (GsonResponse<LoginResponse>):
{
  "version": "1",
  "errorinfo": { "errorcode": "0", "errormessage": "成功" },
  "data": {
    "token": "xxx",
    "userId": "123456",
    "userName": "用户昵称",
    "userPhone": "13800138000",
    "status": 6,
    "daysRemaining": 15
  }
}

错误码: -900 服务器错误 / -999 未知错误
```

#### 接口 2：手机号注册

```
POST /member/register

Request:
{
  "data": {
    "mobile": "13800138000",
    "password": "xxx"
  }
}

Response (GsonResponse<CommonResponse>):
{
  "errorinfo": { "errorcode": "0", "errormessage": "成功" },
  "data": { "succ": 1, "result": 0 }
}
```

#### 接口 3：Token 验证

```
POST /member/validToken

Request (GsonRequest<Void>):
- 公共字段自动注入（token, userId 等）
- data 为空

Response (GsonResponse<LoginResponse>):
- 返回最新的用户信息和 Token
- Token 过期返回 errorcode 10030
```

#### 接口 4：快捷登录

```
POST /member/quickLogin

状态：接口已定义但功能待实现
Response: GsonResponse<LoginResponse>
```

#### 接口 5：版本检查

```
POST /system/getVersion

Request (GsonRequest<Void>):
- data 为空

Response (GsonResponse<AppUpdate>):
{
  "data": {
    "_id": 1,
    "verCode": 155,
    "verName": "1.5.5",
    "name": "打工鸡",
    "fileName": "beansprout_155.apk",
    "filePath": "http://xxx/download/beansprout_155.apk",
    "appForce": 0,
    "newAppSize": 15.2,
    "newAppUpdateDesc": "1. 修复福袋检测问题\n2. 优化动画性能"
  }
}
```

### 7.3 通用数据格式

**GsonRequest<T>（统一请求）**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | String | 是 | 用户 ID（登录后自动注入） |
| version | int | 是 | API 版本号（固定为 1） |
| token | String | 是 | 登录令牌 |
| language | String | 是 | 语言（ZH/EN/AR） |
| client | Object | 是 | 客户端设备信息（ClientInfo） |
| data | T | 否 | 业务参数，无参数时可为空对象 |

**GsonResponse<T>（统一响应）**：

| 字段 | 类型 | 说明 |
|------|------|------|
| version | String | 响应版本号 |
| errorinfo | ErrorInfo | 错误信息对象 |
| data | T | 业务数据 |

**ErrorInfo**：

| 字段 | 类型 | 说明 |
|------|------|------|
| errorcode | String | 错误码（负数 = 错误，正数 = 警告，0 = 成功） |
| errormessage | String | 可展示给用户的错误提示文本 |

---

## 八、数据模型

### 8.1 核心实体类

#### LoginResponse

| 字段 | 类型 | 说明 |
|------|------|------|
| token | String | 登录令牌，有效期约 30 分钟 |
| userId | String | 用户唯一 ID |
| userName | String | 用户昵称 |
| userPhone | String | 注册手机号 |
| status | int | 账户状态码 |
| daysRemaining | int | 剩余天数（体验中=体验剩余/使用中=会员剩余） |

**status 状态枚举**：

| 值 | 含义 | 功能权限 |
|----|------|---------|
| 1 | 待审核 | 不可使用 |
| 2 | 审核中 | 不可使用 |
| 3 | 已退回 | 不可使用 |
| 4 | 使用中（付费会员） | 全功能 |
| 5 | 已停用 | 不可使用 |
| 6 | 体验中 | 全功能，倒计时 |
| 9 | 已删除 | 不可使用 |

#### AppUpdate

| 字段 | 类型 | 说明 |
|------|------|------|
| _id | int | 自增 ID |
| verCode | int | 版本号（整数，如 154） |
| verName | String | 版本名（如 "1.5.4"） |
| name | String | APP 显示名称 |
| fileName | String | APK 文件名 |
| filePath | String | APK 下载地址（完整 URL） |
| appForce | int | 强制更新标记（1=强制 / 0=可选） |
| newAppSize | float | APK 文件大小（MB） |
| newAppUpdateDesc | String | 版本更新日志 |

#### TouchPoint

| 字段 | 类型 | 说明 |
|------|------|------|
| name | String | 触控点名称（用户自定义） |
| x | int | 屏幕 X 坐标（px） |
| y | int | 屏幕 Y 坐标（px） |
| delay | int | 点击间隔（毫秒） |
| isStartClick | boolean | 当前是否已开启 |
| functionType | int | 功能类型码 |
| luckybagTime | int | 福袋卡点时间（分钟，仅福袋功能使用） |

**functionType 枚举**：

| 值 | 功能 |
|----|------|
| 0 | 其他 |
| 1 | 轻点触发（单击） |
| 2 | 直播点赞 |
| 3 | 向下滑动 |
| 4 | 向上滑动 |
| 5 | 向左滑动 |
| 6 | 向右滑动 |
| 7 | 自动回复 |
| 8 | 抢福袋 |

#### TouchEvent

| 字段 | 类型 | 说明 |
|------|------|------|
| action | int | 动作类型 |
| touchPoint | TouchPoint | 关联触控点（仅 START 时携带） |

**action 枚举**：

| 值 | 含义 |
|----|------|
| 1 | ACTION_START — 开始触控 |
| 2 | ACTION_PAUSE — 暂停触控 |
| 3 | ACTION_CONTINUE — 继续触控 |
| 4 | ACTION_STOP — 停止触控 |

#### ClientInfo

| 字段 | 类型 | 说明 | 获取方式 |
|------|------|------|---------|
| cell | String | 手机号 | 用户输入或 TelephonyManager |
| deviceid | String | 设备 IMEI | TelephonyManager.getDeviceId() |
| simid | String | SIM 卡序列号 | TelephonyManager.getSimSerialNumber() |
| os | String | 操作系统 | 固定 "android" |
| osver | String | 系统版本 | Build.VERSION.RELEASE |
| vercode | String | APP 版本号 | BuildConfig.VERSION_CODE |
| vername | String | APP 版本名 | BuildConfig.VERSION_NAME |
| ppiheight | String | 屏幕高度(px) | Resources.getDisplayMetrics() |
| ppiwidth | String | 屏幕宽度(px) | Resources.getDisplayMetrics() |

#### CommonResponse

| 字段 | 类型 | 说明 |
|------|------|------|
| succ | int | 1=成功 / 0=失败 |
| result | int | 结果码 |

#### Point

| 字段 | 类型 | 说明 |
|------|------|------|
| x | int | X 坐标 |
| y | int | Y 坐标 |

### 8.2 本地存储模型

**AccountManager（Tray Preferences 实现）**：

| Key | 类型 | 说明 |
|-----|------|------|
| Account | String | 登录手机号 |
| Password | String | 密码（⚠️ 明文） |
| Token | String | 登录令牌 |
| Userid | String | 用户 ID |
| UserName | String | 用户昵称 |
| photoUrl | String | 头像 URL |
| recyclePhone | String | 绑定手机号 |
| autoReplyScript | String | 自动回复话术（分号 `;` 分隔多条） |
| eSignPopContract | String | 每日弹框控制日期（格式 yyyy-MM-dd） |
| privacyPolicy | boolean | 隐私政策是否已同意 |

**SpUtils（原生 SharedPreferences）- touch_list**：

| Key | 类型 | 说明 |
|-----|------|------|
| touch_list | String (JSON Array) | `TouchPoint[]` 的 Gson 序列化字符串 |
