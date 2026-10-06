# 青简 · ReciteMate

[项目仓库](https://github.com/fplity/qingjian) · [更新记录](CHANGELOG.md) · [项目整合与验证说明](PROJECT_STATUS.md)

**青简**是一款面向高中语文学习的本地古诗文阅读与背诵 Android 应用。通过阅读原文、查看翻译、遮句回忆和自主确认学习完成，积累成长叶，与陪伴角色“小笺”一起建设花园。

2026-10-06 的整合保存了本地三个分支的全部已有提交和最新阅读注释改动。功能范围、验证结果和恢复方法见下文及 [PROJECT_STATUS.md](PROJECT_STATUS.md)。

## 功能与使用流程

底部导航包含“浏览、学习、小笺、收藏、设置”五个入口。

### 浏览与阅读

- 内置 **72 篇**古诗文：**40 篇古诗词、32 篇文言文**，提供标题、作者、朝代、分类、原文与白话翻译。
- 通过“全部 / 古诗词 / 文言文”筛选篇目，查看文章预览并进入详情。
- 首页显示今日小目标、成长叶数量和连续学习天数，可以直接进入推荐篇目。
- 详情页切换原文与翻译；点击原文字词显示释义，长按句子显示翻译弹窗。
- 每个句子、译文、字词和释义都有稳定 ID；字词按出现位置匹配，优先匹配较长词组。
- 当前注释包含预置词语和部分《论语》句子的对应译文。未覆盖的字词使用理解提示，未覆盖的句子使用已有译文回退；**释义和句子译文仍需逐篇校对**。

### 遮句练习

- 学习页提供开始练习、隐藏一句原文、查看原句和自主确认完成的流程。
- 详情页也可以确认学习完成，奖励依据实际保存结果反馈。
- 首次完成一篇新篇目获得一片成长叶；重复完成同一篇不会重复奖励。
- 采用学习者自评，没有录音、语音识别或自动背诵评分。

### 小笺与学习激励

- “小笺的花园”展示陪伴角色、成长阶段、进度、心情、学习记录和纪念物。
- **5 / 15 / 30 / 50** 片成长叶分别解锁第 **2 / 3 / 4 / 5** 级，花园画面随等级变化。
- 提供打招呼、摸摸头、一起复习等互动；对话依日期、心情与互动次数确定。
- 首次学习、等级提升和连续 **3 / 7 / 14** 天学习可解锁纪念物；普通陪伴互动不会直接发放学习奖励。
- 同一天完成多篇新篇目可以获得多片叶子，连续天数当天只记录一次；日期中断后，下次完成新篇目从一天开始。
- 已完成篇目的重复练习不会刷新完成日期或连续学习记录。

### 收藏与阅读设置

- 支持收藏、取消收藏与集中浏览已收藏篇目。
- 正文字号包含小、标准、大三档；主题包含浅色和跟随系统。
- 收藏、阅读偏好、成长叶、已学习篇目、连续学习与陪伴互动信息保存在设备本地。

## 数据与应用范围

- 文章数据：[`app/src/main/assets/articles.json`](app/src/main/assets/articles.json)。
- 用户数据：DataStore Preferences，存储名为 `qingjian_preferences`。
- 当前没有账号、云同步、后端、AI 服务、广告或会员；Android 清单未声明网络权限。
- 应用关闭系统备份，当前没有应用内数据导入导出。卸载或清除应用数据会影响本地学习记录。
- 这是一份本地篇目集；教材、地区与考试范围的适用性应逐篇核对。

## 技术栈与环境

| 项目 | 当前配置 |
| --- | --- |
| 语言与界面 | Kotlin、Jetpack Compose、Material 3 |
| 导航与状态 | Navigation Compose、Compose 状态、协程与 Flow |
| 本地持久化 | AndroidX DataStore Preferences |
| 应用 ID | `com.fplity.recitemate` |
| SDK | `minSdk 24`、`targetSdk 36`、`compileSdk 36.1` |
| Android Gradle Plugin | `9.2.1` |
| Gradle Wrapper | `9.4.1`，配置分发包 SHA-256 校验 |
| Compose 编译器插件 / BOM | `2.2.10` / `2026.02.01` |
| 构建 JVM | Gradle Daemon 配置要求 JDK 21 |
| Java 源码/目标兼容级别 | Java 11 |
| 测试 | JUnit 4；AndroidX 仪器测试配置 |

`minSdk 24` 是项目配置值。低版本 Android、主题、字号、弹窗与实际设备交互仍需在目标设备上验证，具体限制见 [验证说明](PROJECT_STATUS.md)。

## 克隆与构建

```powershell
git clone https://github.com/fplity/qingjian.git
cd qingjian
.\gradlew.bat :app:assembleDebug --no-daemon --console=plain
```

使用 Android Studio 打开项目根目录，安装所需 SDK，并配置自己的 SDK 路径。`local.properties` 属于本机配置，未提交到仓库。调试 APK 默认生成在 `app/build/outputs/apk/debug/app-debug.apk`。

测试与静态检查：

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --no-daemon --console=plain
```

首次构建需访问 Google Maven、Maven Central 和 Gradle 插件仓库；访问受阻时，需恢复依赖仓库访问或准备完整缓存。本次实际执行结果见 [PROJECT_STATUS.md](PROJECT_STATUS.md)。

## 源码结构

```text
app/src/
├─ main/
│  ├─ assets/articles.json                 # 72 篇古诗文
│  ├─ java/com/fplity/recitemate/
│  │  ├─ MainActivity.kt                   # 入口与状态连接
│  │  ├─ data/local/PreferenceManager.kt   # 本地偏好、奖励与陪伴规则
│  │  ├─ data/model/Article.kt             # 文章、句子与字词模型
│  │  ├─ data/repository/                  # 本地读取与注释构建
│  │  ├─ navigation/AppNavGraph.kt         # 五个入口与详情路由
│  │  └─ ui/                              # 组件、页面与主题
│  └─ res/                                # 图标与陪伴角色资源
├─ test/                                  # 激励规则与注释单元测试
└─ androidTest/                           # 仪器测试入口
```

## 历史发布与归档

- [2026-10-06 整合调试预发布](https://github.com/fplity/qingjian/releases/tag/v1.0.0-integration-20261006-debug)保存当前 Debug APK。9 项单测通过、APK 构建任务成功；Lint 依赖不全，本轮未做设备验收，具体见[本次发布说明](RELEASE_NOTES_2026-10-06.md)。
- [v1.0.0-debug](https://github.com/fplity/qingjian/releases/tag/v1.0.0-debug) 是 2026-07-13 的历史调试预发布，其 APK 对应当时的基础阅读版本；后来加入的学习与陪伴功能以当前源码为准。
- [历史发布说明](RELEASE_NOTES_v1.0.0-debug.md)保留该版本的实际范围。
- 旧本地暂存快照保存为标签 `archive/local-stash-20260718`；当前整合代码位于 `main`。
- 本机 IDE 配置、SDK 路径、Gradle 缓存和过程日志未提交到源码仓库。

## 许可证

本项目采用 [MIT License](LICENSE)。
