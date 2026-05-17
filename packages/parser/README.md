# parser — 题库解析接口

两端解析器独立实现，输入输出契约一致。

## 支持格式

| 格式 | 说明 |
|------|------|
| 标准文本 | 题号 + 选项 + 答案分行 |
| 紧凑格式 | 选项在一行，自动修复 |
| 分卷分区 | "一、单选题" 等标题继承题型 |
| 双文件 | 题目 + 答案分离导入 |
| 整卷兜底 | 格式混乱时尽力解析 |
| Excel 表格 | xlsx/xls/xlsm，自动提取表列 |
| docx | 提取 word/document.xml，含内嵌图片 |
| JSON | 结构化导入，支持 ZIP 含素材 |

## 答案行格式

两端均识别：

```
答案：A
正确答案：B
参考答案：ABCD
标准答案：正确
答案：A/B/C
本题答案：D
```

## 文本编辑器

原生端支持：
- 全文查找/替换，支持正则表达式
- "选项：A. xxx B. xxx" 前缀自动剥离

## 填空题识别

关键词覆盖：填空、填入、空白、空格、横线、括号内、____ 等。

## 解析流程

```
原始文本 → TextImportDecoder（格式判断）
  → QuestionTextNormalizer（清洗）
  → SectionTitleParser（分区识别）
  → QuestionBlockSplitter（切块）
  → StandardQuestionParser（解析）
  → convertEmbeddedDataImages（内嵌图片提取）
  → ImportStrategyScorer（评分）
  → ImportValidator（校验）
  → ImportResult
```

## 图片处理

- 原生端：docx 内嵌图片提取 + JSON 中 base64 图片转为本地文件
- Web 端：ZIP 导入时 assets 图片转为内嵌 data URL
- 图片选项题不触发"缺少选项"警告

## 题号识别

支持 `1.` `1、` `【1】` `1)` `1 题干`，不强制题型前缀。

## AI 清洗提示词

见 `docs/标准题库格式示例.md` 第十一节。

## 实现位置

- **原生版**：`apps/android/app/src/native/java/.../importer/parser/`
- **Web 版**：`apps/web/app.js` 的 `parseTextQuestions()`、`parseAnswerEntries()`
