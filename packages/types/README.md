# types — 共享数据结构

Web 版和原生 Compose 版共用的数据模型。两端独立实现，结构保持一致。

## Question

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | string | 唯一标识 |
| `number` | string | 题号，如 "1" |
| `type` | QuestionType | 题型枚举 |
| `question` | string | 题干文本 |
| `options` | Option[] | 选项列表 |
| `answer` | string[] | 单选 1 个，多选多个，判断 A/B，填空/简答为文本 |
| `analysis` | string | 解析，可为空 |
| `category` | string | 分区分卷名，如 "一、单选题" |
| `images` | QuestionImage[] | 关联图片 |
| `score` | number? | 分值，可为空 |

## QuestionType

| 值 | 含义 |
|----|------|
| `SINGLE` | 单选题 |
| `MULTIPLE` | 多选题 |
| `JUDGE` | 判断题 |
| `BLANK` | 填空题 |
| `SHORT` | 简答题 |

## Option

| 字段 | 类型 | 说明 |
|------|------|------|
| `key` | string | 选项标记，如 "A" |
| `text` | string | 选项文本 |

## QuestionImage

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | string | 图片唯一标识 |
| `localPath` | string | 本地文件路径（原生端） |
| `sourceName` | string | 原始文件名 |
| `order` | number | 排序序号 |
| `width` | number? | 图片宽度（可选） |
| `height` | number? | 图片高度（可选） |
| `sizeBytes` | number? | 文件大小（可选） |

Web 端支持在题干中内嵌 `data:image/...;base64` 格式图片，原生端导入时自动转为本地文件。

## WrongBookEntry

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | string | 题目 ID |
| `bankId` | string | 所属题库 |
| `wrongCount` | number | 错误次数 |
| `rightCount` | number | 正确次数 |
| `lastWrongAt` | string | 最后错题时间 |
| `status` | string | 掌握状态 |

## ImportResult

| 字段 | 类型 | 说明 |
|------|------|------|
| `questions` | Question[] | 解析出的题目 |
| `strategyName` | string | 解析策略名称 |
| `warnings` | ImportWarning[] | 警告列表 |
| `diagnostics` | ImportDiagnostics | 诊断信息 |

## 实现位置

- **原生版**：`apps/android/app/src/native/java/.../importer/model/ImportModels.kt`
- **Web 版**：`apps/web/app.js` 的 `normalizeQuestion()`
