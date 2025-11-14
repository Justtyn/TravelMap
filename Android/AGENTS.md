# Repository Guidelines

## Project Structure & Module Organization
当前仓库只有 `app` 模块（`settings.gradle` 已声明）。正式代码放在 `app/src/main/java/com/justyn/travelmap`，同层包含 `AndroidManifest.xml` 与 UI 资源目录 `app/src/main/res`。`app/src/androidTest` 存放设备端测试，`app/src/test` 为 JVM 单元测试，后端 API 说明集中在 `app/API_DOC.md`，修改接口请同步维护。根目录下的 `build.gradle`、`gradle.properties`、`local.properties` 固定了依赖版本和 SDK 配置，请勿随意移除。所有编译产物会写入 `app/build`，避免把大文件或生成物提交到 Git。

## Build, Test, and Development Commands
`./gradlew assembleDebug` 生成调试 APK（输出于 `app/build/outputs/apk/debug/app-debug.apk`）。若需直接安装到设备/模拟器，执行 `./gradlew installDebug`。`./gradlew testDebugUnitTest` 运行 JVM 单测，`./gradlew connectedDebugAndroidTest` 触发 Espresso 仪器测试，确保设备已连接。提交前必跑 `./gradlew lint` 修复静态检查；界面调试时可加 `--stacktrace` 获取更详细日志，例如 `./gradlew :app:assembleDebug --stacktrace`。

## Coding Style & Naming Conventions
Java 代码遵循 AOSP 规范：4 空格缩进、花括号同行、类名使用 `UpperCamelCase`（如 `HomeFragment`），方法与字段为 `lowerCamelCase`。按照既有包结构（`fragment/`、`data/remote/` 等）放置文件，防止交叉依赖。资源文件统一使用 `lowercase_with_underscores`，如 `activity_login.xml`、`color_primary`，字符串常量抽取到 `res/values/strings.xml`。新增或修改接口时先更新 `app/API_DOC.md`，然后在 `ApiClient` 保持一致。提交前在 Android Studio 执行 “Reformat Code” 并应用项目级别的检查。考虑到这是本科安卓课程设计，请在关键逻辑附近写出简洁易懂的中文注释，并优先使用直观的语法与结构（避免过度使用反射、协程黑魔法或复杂 DSL），让评审和答辩时更易讲解。

## 登录态与本地存储
登录成功后务必调用 `data/local/UserPreferences` 将 `app/API_DOC.md` 中返回的完整 `user` JSON 写入 SharedPreferences（目前存储键为 `travelmap_user_pref`）；进入 `MainActivity` 时需通过同一工具类校验是否存在用户信息，无数据则立即跳回 `LoginActivity`。使用缓存数据时优先通过 `UserProfile` 对象获取 `username/nickname/phone/email/avatar_url`，避免直接解析 JSON；登出时必须调用 `UserPreferences.clear()` 清空本地登录态再跳转登录页。后续扩展本地缓存逻辑（如 token、收藏）也应集中在 `data/local` 包中，方便统一管理。

## UI 与主题一致性
所有页面遵循统一的配色、排版与组件样式，优先复用既有布局、`styles.xml` 与 `theme.xml` 中的样式，并确保图标、按钮、间距在不同 Fragment/Activity 间一致。全局 UI 统一采用 Material 3 设计规范：输入框使用 `TextInputLayout`、按钮/卡片用 Material 组件并保持圆角/阴影一致，任何自定义控件都要与系统主题（含浅色/深色）适配。新增 UI 时同步提供 `values-night` 资源，避免硬编码颜色或尺寸。测试时在浅色与深色主题下各运行一次，确保文字对比度、图标透明度和背景层级都符合设计预期，保持整体观感美观稳定。

## Testing Guidelines
单元测试位于 `app/src/test/java`，使用 JUnit4，命名为 `<Feature>Test`（示例：`AuthRepositoryTest`），重点覆盖仓库与服务逻辑。需要真实设备环境的流程放在 `app/src/androidTest/java`，并以 `<Feature>InstrumentedTest` 结尾，结合 Espresso 或 UI Automator。网络或数据库改动必须提供 mock 响应测试，涉及导航的功能至少补一条仪器测试。严禁在 `testDebugUnitTest`、`connectedDebugAndroidTest` 或 `lint` 失败的情况下合并。

## Commit & Pull Request Guidelines
现有提交记录保持 “简洁动词 + 结果” 的写法（常见中文短句），沿用该风格，单行不超过 72 字符。PR 描述必须包含：变更目的、主要文件或模块、执行过的 `./gradlew …` 命令、关联 Issue，以及 UI 改动的截图/录屏。若需要额外配置（例如 `local.properties` 里的密钥），请写在描述中，便于评审者复现。只有在 lint、单测、仪器测试全部通过后才能请求合并。
