package com.yiqiu.shirohaquiz.importer.parser

object CompactQuestionRepair {
    private val separatedOptionRegex = Regex("""(?m)(?:^|[;；\s])([A-Ga-g])\s*([.、．:：)）])\s*\S+""")
    private val joinedOptionRegex = Regex("""[^\n;；\s][A-G][.、．:：)）]""")
    private val inlineAnswerRegex = Regex("""\S\s+(?:[【\[]?\s*(?:答案|正确答案|参考答案|标准答案)\s*[:：])""")
    private val inlineAnalysisRegex = Regex("""\S\s+(?:[【\[]?\s*(?:解析|答案解析|说明)\s*[:：])""")

    /**
     * 轻量判断是否存在紧凑排版特征。
     * 只检测通用版式，不依赖任何题库内容或业务词。
     */
    fun hasCompactPattern(raw: String): Boolean {
        if (raw.isBlank()) return false

        raw.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.length < 12) return@forEach

            val optionHits = separatedOptionRegex.findAll(line).take(4).count()
            if (optionHits >= 2 && line.count { it == '\n' } == 0) return true
            if (joinedOptionRegex.containsMatchIn(line)) return true
            if (inlineAnswerRegex.containsMatchIn(line)) return true
            if (inlineAnalysisRegex.containsMatchIn(line)) return true
        }
        return false
    }

    fun repair(raw: String): String {
        if (raw.isBlank()) return raw
        if (!hasCompactPattern(raw)) return raw
        return raw.lineSequence()
            .flatMap { repairLine(it).lineSequence() }
            .joinToString("\n")
            .replace(Regex("""\n{3,}"""), "\n\n")
            .trim()
    }

    private fun repairLine(rawLine: String): String {
        var line = rawLine.trimEnd()
        if (line.isBlank()) return line

        line = line
            .replace(Regex("""([;；])\s*([A-Ga-g])\s*([.、．:：)）])"""), "\n$2$3")
            .replace(Regex("""\s+([A-Ga-g]\s*[.、．:：)）])"""), "\n$1")
            .replace(Regex("""\s+([【\[]?\s*(?:答案|正确答案|参考答案|标准答案)\s*[:：])"""), "\n$1")
            .replace(Regex("""\s+([【\[]?\s*(?:解析|答案解析|说明)\s*[:：])"""), "\n$1")

        // 对 A.选项B.选项 这种复制粘连格式做保守拆分；只处理大写选项标记。
        line = line.replace(Regex("""([^\n;；\s])([A-G][.、．:：)）])"""), "$1\n$2")

        return line
    }
}
