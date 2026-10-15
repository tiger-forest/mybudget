# MyBudget (生活费与日常记账管家) 💰

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![Room](https://img.shields.io/badge/Room-Database-orange.svg)](https://developer.android.com/training/data-storage/room)
[![Version](https://img.shields.io/badge/Version-1.1.2-brightgreen.svg)]()

**MyBudget** 是一款专为学生及青年群体打造的轻量级生活费预算与记账应用。采用现代 Android 开发技术栈（纯 Kotlin + Jetpack Compose + Material 3 + Room 本地数据库），拒绝繁琐复杂的功能，专注于生活费规划、直观消费追踪与生活成本管理。数据 100% 存储于本地，无多余权限，保护个人隐私。

---

## ✨ 核心特性

- 🎯 **月度生活费预算管理**：
  - 支持自定义基准月度生活费与特定月份预算。
  - 动态计算当月剩余天数与**每日建议消费额度**，花销节奏清晰可见。

- 📝 **极速记账与灵活标签**：
  - 极简底部抽屉式记账面板，开箱即用。
  - 支持自定义标签：Emoji 图标、颜色分类。
  - **特色「频次统计」支持**：可针对特定兴趣/高频消费（如奶茶、谷子/潮玩等）单独开启消费次数统计，精准掌握消费习惯。

- 📊 **可视化统计看板**：
  - 精美的交互式环形消费图表（Donut Chart）。
  - 按月查看支出总览、分类占比，支出结构一目了然。

- 🧰 **百宝箱 (实用工具库)**：
  - **预制/囤货食品成本管家**：记录速食、代餐、零食、佐餐等的单价与规格（包/桶/块），方便计算自备餐食成本与精打细算。
  - 原生手势返回与丝滑导航体验。

- 🔒 **纯净与隐私**：
  - 零网络请求、零广告、零冗余权限。
  - 数据完全离线存储于本地 SQLite / Room 数据库。

---

## 🛠️ 技术栈

| 模块 | 技术选型 | 说明 |
| :--- | :--- | :--- |
| **编程语言** | Kotlin | 100% 现代化 Kotlin 开发 |
| **UI 框架** | Jetpack Compose | 声明式 UI + Material Design 3 |
| **架构模式** | MVVM + Repository | 清晰的分层与单向数据流 |
| **持久层** | Room + KSP | SQLite 现代化封装，Flow 响应式数据流 |
| **依赖与生命周期** | ViewModel + Navigation Compose | 单 Activity 现代化页面导航 |
| **异步处理** | Kotlin Coroutines & Flow | 协程异步与响应式数据流 |

---

## 📂 项目结构

```text
MyBudget/
├── app/
│   ├── src/main/java/com/example/mybudget/
│   │   ├── data/
│   │   │   ├── dao/             # Room DAO 数据访问接口
│   │   │   ├── db/              # Room Database 配置
│   │   │   ├── model/           # 数据实体（Entity）
│   │   │   └── repository/      # 数据仓库层
│   │   ├── ui/
│   │   │   ├── components/      # 通用 Compose 组件（图表、抽屉等）
│   │   │   ├── screens/         # 主页面（首页、统计、工具、标签管理等）
│   │   │   ├── theme/           # 主题配色与字体排版
│   │   │   └── viewmodel/       # 视图模型层（MainViewModel）
│   │   ├── util/                # 日期与格式化工具类
│   │   ├── MainActivity.kt      # 单 Activity 入口
│   │   └── MyBudgetApplication.kt
│   └── build.gradle.kts
├── gradle/
└── README.md
```

---

## 🚀 编译与运行指南

### 环境要求
- **Android Studio**: Android Studio Koala / Ladybug 或更高版本
- **JDK**: Java 11 或更高版本
- **Android SDK**: 
  - `minSdk`: 26 (Android 8.0)
  - `targetSdk` / `compileSdk`: 35+

### 本地运行
1. 克隆本项目：
   ```bash
   git clone https://github.com/tiger-forest/mybudget.git
   ```
2. 打开 Android Studio，选择 **Open an existing project**，选中 `MyBudget` 目录。
3. 等待 Gradle 同步完成。
4. 连接 Android 设备或启动虚拟机，点击 **Run 'app'** 即可启动。

---

## 📝 版本历史

- **v1.1.2**
  - 优化工具箱二级页面的手势返回与导航交互。
  - 细节体验与动画优化。
- **v1.1.1**
  - 优化百宝箱界面排版与视觉展示。
- **v1.1.0**
  - 新增「百宝箱」模块。
  - 新增「预制/囤货食品成本管理」功能。
- **v1.0.0**
  - 初始发布：月度生活费预算、记账、自定义标签与消费统计图表。

---

## 📄 开源许可

本项目遵循 MIT License 许可协议。
