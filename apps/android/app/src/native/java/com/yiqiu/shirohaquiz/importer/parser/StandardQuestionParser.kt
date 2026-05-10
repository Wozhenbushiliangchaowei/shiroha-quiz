package com.yiqiu.shirohaquiz.importer.parser

import com.yiqiu.shirohaquiz.importer.model.Option
import com.yiqiu.shirohaquiz.importer.model.Question
import com.yiqiu.shirohaquiz.importer.model.QuestionType

object StandardQuestionParser {
    private val optionRegex = Regex("""^\s*([A-Ga-g])[.、．:：)]\s*(.+)$""")
    private val answerLineRegex = Regex("""^\s*(?:答案|正确答案|参考答案|标准答案)\s*[:：]\s*(.+)$""")
    private val analysisLineRegex = Regex("""^\s*(?:解析|说明)\s*[:：]?\s*(.*)$""")
    private val trailingChoiceAnswerRegex = Regex("""\(([A-Ga-g](?:[A-Ga-g]|[,，、/\s]+[A-Ga-g])*)\)\s*$""")
    private val trailingJudgeAnswerRegex = Regex("""\((对|错|正确|错误|√|×|True|False)\)\s*$""", RegexOption.IGNORE_CASE)
    private val shortKeywords = Regex("""(简答|问答|名词解释|论述|说明原因|谈谈)""")
    private val blankKeywords = Regex("""(填空|填入|补全|补充完整)""")

    fun parse(text: String): List<Question> {
        return QuestionBlockSplitter.split(text).mapNotNull(::parseBlock)
    }

    private fun parseBlock(block: QuestionBlock): Question? {
        if (block.lines.isEmpty()) return null

        val options = mutableListOf<Option>()
        val stemLines = mutableListOf<String>()
        val analysisLines = mutableListOf<String>()
        var answerText = ""
        var inAnalysis = false

        block.lines.forEach { rawLine ->
            val line = rawLine.trim()
            when {
                line.isBlank() -> Unit
                inAnalysis -> analysisLines += line
                analysisLineRegex.matches(line) -> {
                    inAnalysis = true
                    analysisLines += analysisLineRegex.find(line)?.groupValues?.get(1).orEmpty()
                }
                answerLineRegex.matches(line) -> {
                    answerText = answerLineRegex.find(line)?.groupValues?.get(1)?.trim().orEmpty()
                }
                optionRegex.matches(line) -> {
                    val match = optionRegex.find(line) ?: return@forEach
                    options += Option(match.groupValues[1].uppercase(), match.groupValues[2].trim())
                }
                options.isNotEmpty() -> {
                    val last = options.removeLast()
                    options += last.copy(text = "${last.text} $line".replace(Regex("""\s+"""), " ").trim())
                }
                else -> stemLines += line
            }
        }

        var stem = stemLines.joinToString(" ").replace(Regex("""\s+"""), " ").trim()
        if (stem.isBlank()) return null

        trailingJudgeAnswerRegex.find(stem)?.let { hit ->
            if (answerText.isBlank()) answerText = hit.groupValues[1]
            stem = stem.removeRange(hit.range).trim()
        }
        trailingChoiceAnswerRegex.find(stem)?.let { hit ->
            if (answerText.isBlank()) answerText = hit.groupValues[1]
            stem = stem.removeRange(hit.range).trim()
        }

        val type = inferType(stem = stem, options = options, answerText = answerText)
        val answer = normalizeAnswer(answerText, type)

        return Question(
            number = block.number,
            type = type,
            question = stem,
            options = options,
            answer = answer,
            analysis = analysisLines.joinToString("\n").trim()
        )
    }

    private fun inferType(stem: String, options: List<Option>, answerText: String): QuestionType {
        if (shortKeywords.containsMatchIn(stem)) return QuestionType.SHORT
        if (blankKeywords.containsMatchIn(stem)) return QuestionType.BLANK

        if (options.isEmpty()) {
            if (AnswerTokenParser.isObjectiveAnswerText(answerText)) {
                val tokens = AnswerTokenParser.parseObjectiveAnswers(answerText)
                if (tokens.all { it == "A" || it == "B" } &&
                    Regex("""(对|错|正确|错误|判断|是非|√|×)""").containsMatchIn(stem + answerText)
                ) {
                    return QuestionType.JUDGE
                }
                return if (tokens.size > 1) QuestionType.MULTIPLE else QuestionType.BLANK
            }
            return QuestionType.SHORT
        }

        val optionKeys = options.map { it.key.uppercase() }
        val looksLikeJudgePair = optionKeys == listOf("A", "B") &&
            options.all { it.text in listOf("正确", "错误", "对", "错", "是", "否", "√", "×", "True", "False") }

        val hasChoiceBeyondB = optionKeys.any { it in listOf("C", "D", "E", "F", "G") }
        if (hasChoiceBeyondB || options.size >= 3) {
            val tokens = AnswerTokenParser.parseObjectiveAnswers(answerText)
            return if (tokens.size > 1) QuestionType.MULTIPLE else QuestionType.SINGLE
        }

        if (looksLikeJudgePair) {
            return QuestionType.SINGLE
        }

        val tokens = AnswerTokenParser.parseObjectiveAnswers(answerText)
        return if (tokens.size > 1) QuestionType.MULTIPLE else QuestionType.SINGLE
    }

    private fun normalizeAnswer(answerText: String, type: QuestionType): List<String> {
        if (answerText.isBlank()) return emptyList()
        return when (type) {
            QuestionType.SINGLE, QuestionType.MULTIPLE -> AnswerTokenParser.parseObjectiveAnswers(answerText)
            QuestionType.JUDGE -> {
                val normalized = AnswerTokenParser.parseObjectiveAnswers(answerText)
                when {
                    normalized.isNotEmpty() -> normalized
                    Regex("""^(对|正确|是|√|true)$""", RegexOption.IGNORE_CASE).matches(answerText.trim()) -> listOf("A")
                    Regex("""^(错|错误|否|×|false)$""", RegexOption.IGNORE_CASE).matches(answerText.trim()) -> listOf("B")
                    else -> emptyList()
                }
            }
            QuestionType.BLANK, QuestionType.SHORT -> AnswerTokenParser.parseTextAnswer(answerText)
        }
    }
}
