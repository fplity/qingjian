# 青简项目整合与验证说明

日期：2026-10-06（America/Tijuana）。仓库：[fplity/qingjian](https://github.com/fplity/qingjian)，交付分支：`main`。

## 本次交付范围

将本地所有分支已有提交与当前工作区修改保存到远程，更新实际功能说明，并在远程完整核对之后删除本地目录 `D:\app\AndroidStudioProjects\ReciteMate`。

本次交付为源码整合与本地清理。设备验收与正式发行状态按实际证据单独记录。

## 整合来源

| 来源 | 核对结果 |
| --- | --- |
| 原远程 main | `4dd0ec060b57c3d5bde370eba0f0fb7394434041` |
| 本地 main / PetFunction / PoetMain | 均指向 `a3dd301cf82da56c041cf64e383d7288f6453c77` |
| 未推送历史 | `4a2bfc4` 学习激励与篇目集；`cfa56b6` 合并；`a3dd301` 阅读注释与陪伴奖励 |
| 原工作区改动 | Article.kt、ArticleAnnotationBuilder.kt、ArticleRepository.kt、DetailScreen.kt |
| 本次测试维护 | 注释测试改为两句完整原文，保留 ID 配对验证并增加字词位置、ID 唯一性检查 |
| 旧 stash | 标签 `archive/local-stash-20260718` 保存原快照及其暂存、未跟踪文件父提交 |
| 工作树 | 只有当前项目目录，无其它关联工作树 |

以最新工作区源码为准，保留已有 Git 历史；三个本地分支的提交均应可从最终主线追溯。

## 当前已实现功能

1. 72 篇本地古诗文与分类浏览，标题、作者、朝代、预览和翻译。
2. 原文/翻译切换，点击字词释义、长按句子翻译及稳定注释 ID。
3. 今日小目标、遮句回忆、自主确认背诵完成。
4. 按篇目去重的成长叶奖励、同日多篇奖励与连续学习记录。
5. 小笺陪伴、五级花园、心情、对话、打招呼、摸摸头和一起复习。
6. 首次学习、等级提升、连续 3 / 7 / 14 天的纪念物与完成反馈。
7. 本地收藏、字号、阅读主题及 DataStore 学习/陪伴状态保存。

使用流程、技术配置与构建方法见 [README.md](README.md)。

## 本轮验证

| 检查 | 结果 |
| --- | --- |
| Git 差异格式 | `git diff --check` 通过 |
| 文章数据 | 72 篇：古诗词 40、文言文 32；重复 ID 0、缺失必需字段 0 |
| 联网 Gradle 检查 | 依赖解析阶段失败，Maven Central 多项请求返回 HTTP 403，尚未执行构建与测试 |
| 首次离线单测 | 9 项中 1 项失败；旧测试按逗号拆句，与最新按句末标点保留完整句子的逻辑不符 |
| 修正后离线单测 | 9 项全部通过：学习激励规则 7 项、注释构建 2 项；失败 0、错误 0、跳过 0 |
| Debug APK | `:app:assembleDebug` 成功，Gradle 确认现有产物与当前输入一致（UP-TO-DATE）；大小 21,082,175 字节 |
| Android Lint | 未完成：离线缓存缺少 lint-gradle 32.2.1、ui-test-junit4 1.10.4、Espresso 3.5.1 等依赖；没有得到 Lint 通过结果 |
| 本轮真机/模拟器验收 | 未执行 |

执行命令：

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --no-daemon --console=plain
```

离线复验使用项目已有 Gradle 缓存，增加 `--continue --offline --no-configuration-cache --gradle-user-home <已有缓存路径>`。缓存不提交到 Git。

该复验使用 `--continue`：单元测试和 APK 任务成功后，整体命令因 Lint 依赖缺失返回失败。各项结果分别记录，不能将整体命令描述为全部检查通过。

测试结果来源为 `app/build/test-results/testDebugUnitTest/TEST-*.xml`。测试检查涵盖成长级别阈值、奖励去重、连续学习、心情、确定性回复、纪念物和注释位置/ID。

当前 Debug APK SHA-256：`e316d3a22e6ad453c3acfd3d44d7e2add690d49c3207b0eec7a65ae0625579e7`。产物保存至 [整合调试预发布](https://github.com/fplity/qingjian/releases/tag/v1.0.0-integration-20261006-debug)，应用内部版本仍为 1.0。此包供体验与测试，不代表正式发行验收。

历史宠物专项账本记录过其范围内的测试与设备检查通过；该历史结果不证明当前修改后的整个应用已经验收。

## 后续工作与发布状态

- 全部篇目的释义和句子译文需要逐篇校对；当前存在通用字义提示及译文回退。
- 目标设备上的首次启动、导航、收藏、字号、主题、弹窗、练习、奖励、日期变化和持久化需进一步验收。
- 项目配置 `minSdk 24`，代码使用 `java.time.LocalDate`；Android 7.x 的运行兼容性需验证。
- 应用版本仍为 `versionCode = 1`、`versionName = "1.0"`；正式发布需安排版本号和正式签名。
- 仪器测试入口不等于完整端到端回归覆盖。
- 历史 `v1.0.0-debug` APK 不包含本次全部学习与陪伴功能。

## 远程保存与本地删除

删除前核对远程主线提交与本地一致、所有本地分支历史均被主线包含、旧 stash 标签和文件可恢复，以及没有尚未保存的源码改动。

删除范围仅为 `D:\app\AndroidStudioProjects\ReciteMate` 及其源码、Git 数据、IDE 配置、缓存和构建产物。远程仓库、Android Studio、SDK、其它项目和外置记忆库保留。

## 恢复开发

```powershell
git clone https://github.com/fplity/qingjian.git ReciteMate
cd ReciteMate
```

然后通过 Android Studio 配置 SDK，以 JDK 21 运行 Gradle。查看旧暂存内容：

```powershell
git show archive/local-stash-20260718:app/src/main/java/com/fplity/recitemate/ui/screens/DetailScreen.kt
git show archive/local-stash-20260718^3:app/src/main/java/com/fplity/recitemate/data/repository/ArticleAnnotationBuilder.kt
```
