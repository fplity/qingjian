# 青简

项目仓库：https://github.com/fplity/qingjian

**青简**是一款面向高中语文学习的本地古诗文阅读 Android 应用。它提供安静、清爽的阅读界面，方便查看古诗词和文言文的原文、作者信息与白话翻译。

> 首个计划发布标签为 `v1.0.0-debug`，是用于体验与测试的 **Debug 预发布版本**，不是稳定版。Android 真机/模拟器上的完整视觉和交互验证仍在等待完成；请在安装前阅读 [发布说明](RELEASE_NOTES_v1.0.0-debug.md)。

## 主要功能

- 浏览内置的高中古诗词与文言文；文章数据随应用保存在本地 JSON 文件中。
- 按“全部 / 古诗词 / 文言文”筛选文章，并查看标题、作者、朝代、分类和原文预览。
- 在文章详情中切换阅读原文与逐段白话翻译。
- 收藏或取消收藏文章；收藏、字体大小和阅读模式会保存在设备本地。
- 在“收藏”页面集中查看已收藏文章。
- 调整正文大小（小、标准、大），并选择浅色或跟随系统的阅读模式。

## 不包含的功能

青简刻意保持为纯本地阅读工具。当前版本不包含背诵/逐句练习、语音识别、AI 功能、账号登录、云同步、联网请求、后端服务、数据库、广告或会员系统。

## 技术与数据

- Kotlin、Jetpack Compose、Material 3、Navigation Compose。
- 单 Activity Android 应用，最低支持 Android 7.0（API 24）。
- 文章内容：`app/src/main/assets/articles.json`。
- 用户偏好：设备本地的 DataStore Preferences；应用不上传或同步这些数据。

## 在 Android Studio 中运行

1. 使用 Android Studio 打开本项目根目录。
2. 等待 Gradle 同步完成，并选择已连接的 Android 设备或模拟器。
3. 运行 `app` 配置，或在项目根目录执行：

   ```powershell
   .\gradlew.bat :app:assembleDebug --no-daemon --console=plain
   ```

4. 生成的调试 APK 位于 `app/build/outputs/apk/debug/app-debug.apk`。

项目当前配置使用 JDK 11、`minSdk 24` 和 Android SDK 36。首次构建可能需要 Android Studio 下载相应 SDK 或 Gradle 依赖。

## Debug 预发布 APK

计划发布到 GitHub 的 `v1.0.0-debug` 预发布仅附带一个文件：`qingjian-v1.0.0-debug.apk`。它由上述 Debug 构建产物上传，**不会**提交到 Git 仓库。

该 APK 仅供测试：安装 Android Debug 构建时，设备可能要求开启“允许通过 USB 安装”或允许从相应来源安装。由于完整真机/模拟器运行验收尚未完成，请不要将它视为稳定发行版。

## 项目结构

```text
app/
├─ src/main/assets/articles.json              # 本地文章数据
├─ src/main/java/com/fplity/recitemate/
│  ├─ data/                                  # 数据模型、JSON 读取与本地偏好
│  ├─ navigation/                            # Navigation Compose 路由与底部导航
│  ├─ ui/components/                         # 可复用文章卡片与空状态组件
│  ├─ ui/screens/                            # 学习、详情、收藏、设置页面
│  └─ ui/theme/                              # Compose 颜色、排版与主题
└─ build.gradle.kts                          # App 模块构建配置
```

## 版本记录

详见 [CHANGELOG.md](CHANGELOG.md)。

## 许可证

本项目采用 [MIT License](LICENSE)。
