# shared — 通用约定

## 备份 JSON Schema

### JSON 格式

```json
{
  "kind": "shiroha_quiz_full_backup",
  "version": 2,
  "exportedAt": 1715971200000,
  "activeBankId": "uuid",
  "banks": [
    {
      "id": "uuid",
      "name": "题库名",
      "questions": [ /* 见 packages/types */ ]
    }
  ],
  "wrongBook": [ /* 错题记录 */ ],
  "studyRecords": [ /* 学习记录 */ ]
}
```

### ZIP 格式

原生端导出 ZIP 包含 `backup.json` + `assets/` 目录（图片素材）。Web 端导入时自动将 assets 转为内嵌 base64 图片。

## 版本号

- Web 版：`v0.4.x-alpha`
- 原生版：`v0.4.x-native`
- 统一发布：`v1.1.x-beta`

## 跨端兼容

- Web 导出 JSON 可由原生版导入，反之亦然
- 原生端导出 ZIP 含图片素材，Web 端可直接导入并自动转换
- `version` 变化向后兼容

## 导入格式文档

- [Markdown](../../docs/标准题库格式示例.md)
- [Word](../../docs/标准题库格式示例.docx)
- [PDF](../../docs/标准题库格式示例.pdf)
- App 内：`StandardImportFormatScreen`
