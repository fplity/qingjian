# Changelog

All notable changes to 青简 are documented in this file.

## [Source integration] - 2026-10-06

### Integrated

- 保存 main、PetFunction、PoetMain 全部已有提交和最新阅读注释改动。
- 篇目扩充至 72 篇：40 篇古诗词、32 篇文言文。
- 加入今日小目标、遮句回忆、自主确认完成及按篇目去重的成长叶奖励。
- 加入连续学习、五级花园、小笺互动、纪念物和完成反馈。
- 整合稳定注释 ID、点击释义、长按翻译及定位弹窗的最新实现。
- 调整注释测试样例，核对完整句子、配对 ID、字词位置和 ID 唯一性。
- 更新 README 和 PROJECT_STATUS.md，保存旧 stash 的远程归档标签。
- 保存当前 Debug APK 至 `v1.0.0-integration-20261006-debug` 调试预发布。

### Verification

- 文章 JSON 基本完整性检查通过，9 项单测全部通过，Debug APK 构建任务成功。
- Lint 因缓存依赖缺失未完成；本轮未执行设备验收。详细结果见 [PROJECT_STATUS.md](PROJECT_STATUS.md)。
- 历史 v1.0.0-debug APK 仍对应 2026-07-13 的功能范围。

## [v1.0.0-debug] - 2026-07-13

### Added

- Local browsing for built-in high-school classical Chinese prose and poetry.
- Category filters, article detail pages, original-text and translation views.
- Device-local favorites, font-size preferences, and light/system reading mode.
- A three-tab navigation experience for learning, favorites, and settings.

### Release status

- This is a debug prerelease preview, not a stable production release.
- Complete Android device/emulator visual and interaction verification remains pending.

Repository: https://github.com/fplity/qingjian

[v1.0.0-debug]: RELEASE_NOTES_v1.0.0-debug.md
