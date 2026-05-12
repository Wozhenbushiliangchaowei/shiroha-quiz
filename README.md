<div align="center">
  <img src="assets/promo.png" width="800" alt="Shiroha Quiz">
</div>

# Shiroha Quiz

Shiroha Quiz 是一个轻量、开源、可扩展的通用刷题项目，面向**自导入题库、多题型练习、多端使用**等场景。

它的核心目标是：

> 把手里零散的 Word、PDF、TXT、JSON、题目 + 答案分离文件，整理成可以直接练习、考试、复盘的本地题库。

当前项目采用双线推进：

- **Web 端**：功能完整，是题库导入、格式验证、交互能力和算法策略的主要迭代入口。
- **Android WebView 壳**：加载本地 Web 资源，适合移动端刷题使用，已可作为 App 体验版本。
- **Android 原生 Compose**：使用 Kotlin + Jetpack Compose + Material3 开发，正在逐步重写原生 App 体验。
- **共享层规划**：后续沉淀题库解析、刷题核心逻辑和通用数据结构。

---

## 当前能力

### 刷题与考试

- 练习模式：上一题 / 下一题、答题卡跳转、提交答案、查看解析。
- 考试模式：题量设置、限时考试、自动交卷、及格线判定、得分报告。
- 错题本：自动记录错题、掌握状态标记、错题重练。
- 刷题记录：保存练习 / 考试明细，可查看作答详情。
- 多题型支持：单选题、多选题、判断题、填空题、简答题。

### 题库导入

- 智能识别：自动检测题号、题干、选项、答案、解析和题型。
- 多格式支持：`txt`、`json`、`docx`、文字层 `pdf`。
- 双文件导入：支持“题目文件 + 答案文件”分离导入。
- 识别预览：导入前可查看识别结果、异常题、待确认题。
- 手动修正：支持在导入预览阶段编辑识别结果。
- 策略评分：多种解析策略自动评分，优先选择更可信的结果。
- 备份恢复：支持全部数据备份、批量题库 JSON 导出与恢复。

### 多端支持

- Web 端完整功能。
- Android WebView 壳版本，离线加载本地 Web 资源。
- Android 原生 Compose 版本，独立版本号，持续开发中。
- 默认内置 C1 科目一题库，方便首次体验和功能测试。

---

## Android 双入口架构

Android 工程通过 `productFlavors` 同时维护两个入口，二者独立构建、独立包名、独立版本号。

| Flavor | 包名 | 入口 | 技术路线 | 当前版本 |
|---|---|---|---|---|
| `web` | `com.yiqiu.shirohaquiz` | `WebShellActivity` | WebView 加载本地 Web 资源 | `0.4.2-alpha` |
| `native` | `com.reqir.shirohaquiz` | `MainActivity` | Kotlin + Jetpack Compose + Material3 | `0.2.2` |

主要目录：

```text
apps/android/
├── app/src/main/assets/web/        # Android WebView 壳内置 Web 资源
├── app/src/main/java/              # WebView 壳相关原生入口
└── app/src/native/java/            # 原生 Compose 版本代码
```

---

## 原生 Compose 进度

原生 Android 版正在开发中，目标是逐步形成独立的原生刷题 App 体验。

当前方向：

- 使用 `MainActivity` 作为原生入口。
- 使用 `ShirohaAppShell` 管理页面外壳和底部导航。
- 使用 `QuizRepository` 管理题库、练习、考试、错题、记录等状态。
- 使用 importer 模块承载题库解析、双文件合并、答案区识别和校验。
- 使用 Material3 + 自定义组件保持蓝白、轻量、卡片化风格。

核心页面包括：

| 页面 | 说明 |
|---|---|
| `HomeScreen` | 首页、题库概览、快速入口 |
| `ImportScreen` | 文件选择、文本解析、双文件导入、识别预览 |
| `PracticeScreen` | 题卡展示、选项交互、提交答案、解析展示 |
| `ExamScreen` | 考试设置、计时、交卷、成绩统计 |
| `BankDetailScreen` / 题库相关页面 | 题库题目列表与题库查看 |
| `MeScreen` / 设置相关页面 | 设置、关于、数据管理入口 |

导入链路大致为：

```text
parser/       # 标准题库解析、答案区解析、双文件解析
score/        # 导入策略评分
validate/     # 导入结果校验
state/        # QuizRepository 状态管理
ui/           # Compose 页面与组件
```

详见：

- [原生开发进度](docs/native/原生开发进度.md)
- [原生安卓图片素材使用建议](docs/Shiroha%20Quiz%20原生安卓图片素材使用建议.md)

---

## 使用说明

### Web 端快速上手

1. 打开 `apps/web/index.html`，或访问在线版。
2. 进入 **导入题库**，粘贴文本或上传文件。
3. 系统自动识别题型、选项、答案和解析。
4. 在识别预览中确认题目无误。
5. 进入 **刷题练习** 或 **考试模式** 开始使用。
6. 答错的题会进入 **错题本**。
7. 定期在 **设置/导出** 中导出备份。

### 数据备份建议

Shiroha Quiz 的题库和记录主要保存在本地浏览器或 App WebView 的本地存储中。

建议：

- 重要题库导入后，及时导出全部数据备份。
- 换设备、清理缓存、卸载 App 前，先导出备份 JSON。
- 从 Shiroha Quiz 导出的备份 JSON，应在 **设置/导出 → 导入配置 / 备份 JSON** 中导入，不要放进普通题库导入区。

---

## 导入格式与策略

支持：

```text
txt
json
docx
文字层 pdf
题目文件 + 答案文件双文件导入
```

系统采用多策略评分，自动选择更可信的解析方式。导入后会进入识别预览，用户可以在确认前检查异常题和待确认题。

详细说明：

- [题库导入策略与使用指南](docs/Shiroha%20Quiz%20题库导入策略与使用指南.md)
- [题目导入解析方法说明](docs/Shiroha%20Quiz%20题目导入解析方法说明.md)
- 标准题库格式示例：[Markdown](docs/标准题库格式示例.md) / [Word](docs/标准题库格式示例.docx) / [PDF](docs/标准题库格式示例.pdf)

如果原题库格式非常混乱，且题目没有保密需求，可以先使用 LLM 智能体进行数据清洗，例如豆包、深度求索、通义千问等。清洗目标不是改题，而是统一题号、选项、答案和解析格式。

---

## 仓库结构

```text
shiroha-quiz/
├── .github/                         # Issue 模板与 GitHub Actions
├── apps/
│   ├── web/                         # Web 版
│   │   ├── index.html
│   │   ├── app.js
│   │   ├── styles.css
│   │   ├── question-bank.js
│   │   ├── media/                   # Web 插画素材
│   │   ├── data/                    # 内置题库
│   │   └── libs/                    # PDF.js 等本地库
│   └── android/                     # Android 工程
│       └── app/
│           ├── src/main/assets/web/ # Android WebView 壳内置 Web 资源
│           ├── src/main/java/       # WebView 壳入口与原生启动层
│           └── src/native/java/     # 原生 Compose 代码
├── docs/
│   ├── web/                         # Web 端设计与开发文档
│   ├── native/                      # 原生开发进度与设计规范
│   └── universal/                   # 通用架构建议
├── assets/                          # 宣传图、素材源文件
├── packages/                        # 规划中的共享模块
├── CHANGELOG.md
├── CONTRIBUTING.md
├── LICENSE
└── README.md
```

---

## 本地运行

### Web 端

`apps/web/` 是纯静态页面，无需构建。

```bash
# 方式一：直接打开
apps/web/index.html

# 方式二：本地静态服务
npx serve apps/web
```

在线版：

```text
https://reiqr.github.io/shiroha-quiz
```

### Android 端

进入 Android 工程目录：

```bash
cd apps/android
```

构建 WebView 壳版本：

```bash
./gradlew assembleWebDebug
```

构建原生 Compose 版本：

```bash
./gradlew assembleNativeDebug
```

Windows PowerShell 可使用：

```powershell
.\gradlew.bat assembleWebDebug
.\gradlew.bat assembleNativeDebug
```

构建输出通常位于：

```text
apps/android/app/build/outputs/
```

---

## 下载与使用

当前代码版本：

| 类型 | 版本 |
|---|---|
| Web / WebView flavor | `0.4.2-alpha` |
| Native Compose flavor | `0.2.2` |

下载入口：

- [GitHub Releases](https://github.com/reiqr/shiroha-quiz/releases)
- [在线体验](https://reiqr.github.io/shiroha-quiz)

每次发布通常包含：

- Android APK
- Web 离线包
- 相关说明文档

> 当前仍为 alpha 测试阶段，不建议用于正式考试、生产培训或高风险场景。

---

## 开发计划

近期重点：

- 优化原生 Compose 版本稳定性。
- 继续完善导入解析、双文件导入和识别预览。
- 优化移动端流畅度与启动体验。
- 接入原生 Android 图片素材，但保持轻量，不挤压答题区域。
- 推进题库、错题、记录、备份恢复等核心流程测试。
- 后续逐步沉淀共享解析层和通用数据结构。

---

## 参与贡献

欢迎通过 Issue 提交：

- Bug 反馈
- 题库格式兼容问题
- 导入失败样例
- UI / 交互优化建议
- Android 适配问题
- 文档补充建议

详见：

- [CONTRIBUTING.md](./CONTRIBUTING.md)
- [CHANGELOG.md](./CHANGELOG.md)

---

## 许可证

本项目采用 `GPL-3.0` 开源。
