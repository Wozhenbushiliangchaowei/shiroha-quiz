package com.yiqiu.shirohaquiz.importer.parser

import com.yiqiu.shirohaquiz.importer.model.Option
import com.yiqiu.shirohaquiz.importer.model.Question
import com.yiqiu.shirohaquiz.importer.model.QuestionType

object StandardQuestionParser {
    private val optionRegex = Regex("""^\s*([A-Ga-g])\s*[.:：]\s*(.+)$""")
    private val answerLineRegex = Regex("""^\s*(?:答案|正确答案|参考答案|标准答案)\s*[:：]\s*(.+)$""")
    private val analysisLineRegex = Regex("""^\s*(?:解析|说明)\s*[:：]?\s*(.*)$""")
    private val trailingChoiceAnswerRegex = Regex("""\(([A-Ga-g1-9]{1,7})\)\s*$""")
    private val trailingJudgeAnswerRegex = Regex("""\((对|错|正确|错误|是|否|√|×|True|False)\)\s*$""", RegexOption.IGNORE_CASE)

    fun parse(text: String): List<Question> {
        return QuestionBlockSplitter.split(text).mapNotNull(::parseBlock)
    }

    private fun parseBlock(block: QuestionBlock): Question? {
        if (block.lines.isEmpty()) return null

        val options = mutableListOf<Option>()
        val analysisLines = mutableListOf<String>()
        val stemLines = mutableListOf<String>()
        var answer = mutableListOf<String>()
        var inAnalysis = false

        block.lines.forEach { rawLine ->
            val line = rawLine.trim()
            when {
                inAnalysis -> analysisLines += line

                analysisLineRegex.matches(line) -> {
                    inAnalysis = true
                    analysisLines += analysisLineRegex.find(line)?.groupValues?.get(1).orEmpty()
                }

                answerLineRegex.matches(line) -> {
                    answer = splitAnswer(answerLineRegex.find(line)?.groupValues?.get(1).orEmpty()).toMutableList()
                }

                optionRegex.matches(line) -> {
                    val match = optionRegex.find(line) ?: return@forEach
                    options += Option(match.groupValues[1].uppercase(), match.groupValues[2].trim())
                }

                options.isNotEmpty() -> {
                    val last = options.removeLast()
                    options += last.copy(text = "${last.text} $line".trim())
                }

                else -> stemLines += line
            }
        }

        var stem = stemLines.joinToString(" ").replace(Regex("\\s+"), " ").trim()

        trailingJudgeAnswerRegex.find(stem)?.let { hit ->
            answer.addAll(splitAnswer(hit.groupValues[1]))
            stem = stem.removeRange(hit.range).trim()
        }
        trailingChoiceAnswerRegex.find(stem)?.let { hit ->
            answer.addAll(splitAnswer(hit.groupValues[1]))
            stem = stem.removeRange(hit.range).trim()
        }

        val type = inferType(stem, options, answer)
        val finalOptions = if (type == QuestionType.JUDGE && options.isEmpty()) {
            listOf(Option("A", "正确"), Option("B", "错误"))
        } else {
            options
        }

        return Question(
            number = block.number,
            type = type,
            question = stem.ifBlank { return null },
            options = finalOptions,
            answer = normalizeAnswer(answer, type),
            analysis = analysisLines.joinToString("\n").trim()
        )
    }

    private fun inferType(
        stem: String,
        options: List<Option>,
        answer: List<String>
    ): QuestionType {
        if (Regex("简答|问答|名词解释|论述|说明").containsMatchIn(stem)) return QuestionType.SHORT
        if (Regex("填空|填入|补全").containsMatchIn(stem)) return QuestionType.BLANK

        val judgeOptions = options.size == 2 && options.all { it.text in listOf("正确", "错误", "对", "错") }
        if (judgeOptions && answer.any { it in listOf("A", "B", "正确", "错误") }) return QuestionType.JUDGE
        if (answer.any { it in listOf("正确", "错误") }) return QuestionType.JUDGE
        if (answer.size > 1) return QuestionType.MULTIPLE
        return if (options.isEmpty()) QuestionType.BLANK else QuestionType.SINGLE
    }

    private fun splitAnswer(raw: String): List<String> {
        val value = raw.trim()
        if (value.isBlank()) return emptyList()
        if (Regex("^(对|正确|是|√|True)$", RegexOption.IGNORE_CASE).matches(value)) return listOf("正确")
        if (Regex("^(错|错误|否|×|False)$", RegexOption.IGNORE_CASE).matches(value)) return listOf("错误")

        val compact = value.replace(Regex("[,，、;；/\\\\\\s]+"), "").uppercase()
        if (Regex("^[A-G]{2,7}$").matches(compact)) return compact.map { it.toString() }
        if (Regex("^[A-G]$").matches(compact)) return listOf(compact)
        if (Regex("^[1-9]{1,7}$").matches(compact)) return compact.map { it.toString() }

        return value.split(Regex("[,，、;；/\\\\]+"))
            .map { it.trim().uppercase() }
            .filter { it.isNotBlank() }
    }

    private fun normalizeAnswer(answer: List<String>, type: QuestionType): List<String> {
        return when (type) {
            QuestionType.JUDGE -> answer.mapNotNull {
                when (it.uppercase()) {
                    "正确", "对", "是", "√", "TRUE" -> "A"
                    "错误", "错", "否", "×", "FALSE" -> "B"
                    "A", "B" -> it.uppercase()
                    else -> null
                }
            }.distinct()

            QuestionType.SINGLE,
            QuestionType.MULTIPLE -> answer.map { it.uppercase() }.distinct()

            else -> answer.distinct()
        }
    }
}
