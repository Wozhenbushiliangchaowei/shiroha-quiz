package com.yiqiu.shirohaquiz.importer.parser

import com.yiqiu.shirohaquiz.importer.model.Question
import com.yiqiu.shirohaquiz.importer.model.QuestionType

object QuestionParser {
    fun parseStandard(text: String): List<Question> {
        return StandardQuestionParser.parse(text)
    }

    /**
     * 标准解析作为主路径；只有单个题块出现明确紧凑格式信号，且修复结果结构更完整时，
     * 才替换该题块。正常标准题不会再与整份紧凑解析结果竞争。
     */
    fun parseStandardFirst(
        text: String,
        forcedType: QuestionType? = null,
        category: String = "",
        allowUnnumbered: Boolean = true
    ): List<Question> {
        val blocks = QuestionBlockSplitter.split(
            text = text,
            forcedType = forcedType,
            category = category,
            allowUnnumbered = allowUnnumbered
        )

        return blocks.mapNotNull(::parseBlockStandardFirst)
    }

    fun parseCompact(text: String): List<Question> {
        val preprocessed = CompactQuestionRepair.repair(text)
        return StandardQuestionParser.parse(preprocessed)
    }

    fun looksCompact(text: String): Boolean {
        return CompactQuestionRepair.hasCompactPattern(text)
    }

    fun parseSectioned(text: String): List<Question> {
        val sections = splitBySections(text)
        if (sections.isEmpty()) return parseStandardFirst(text)

        return sections.flatMap { section ->
            parseStandardFirst(
                text = section.body,
                forcedType = section.forcedType,
                category = section.title
            )
        }
    }

    fun looksSectioned(text: String): Boolean {
        return text.lineSequence().any { line ->
            val section = SectionTitleParser.parse(line.trim())
            section != null && !section.isAnswerSection
        }
    }

    private fun parseBlockStandardFirst(block: QuestionBlock): Question? {
        val standard = StandardQuestionParser.parseBlock(block)
        val rawBlock = block.lines.joinToString("\n")
        if (!CompactQuestionRepair.hasCompactPattern(rawBlock)) return standard

        val repaired = CompactQuestionRepair.repair(rawBlock)
        if (repaired == rawBlock) return standard

        val repairedLines = repaired.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toList()
        if (repairedLines.isEmpty()) return standard

        val fallback = StandardQuestionParser.parseBlock(block.copy(lines = repairedLines))
        val expectedOptionKeys = CompactQuestionRepair.compactOptionKeys(rawBlock)

        return if (questionStructureScore(fallback, expectedOptionKeys) > questionStructureScore(standard, expectedOptionKeys)) {
            fallback
        } else {
            standard
        }
    }

    private fun questionStructureScore(question: Question?, expectedOptionKeys: Set<String>): Int {
        question ?: return Int.MIN_VALUE / 4
        if (question.question.isBlank()) return Int.MIN_VALUE / 4

        var score = 100
        val optionKeys = question.options.map { it.key.uppercase() }.toSet()
        val isObjective = question.type == QuestionType.SINGLE ||
            question.type == QuestionType.MULTIPLE ||
            question.type == QuestionType.JUDGE

        if (expectedOptionKeys.isNotEmpty()) {
            val covered = expectedOptionKeys.count { it in optionKeys }
            score += covered * 35
            score -= (expectedOptionKeys.size - covered) * 55
            score += if (isObjective) 80 else -180
        } else if (isObjective) {
            score += if (question.options.size >= 2) 60 else -100
        }

        if (isObjective) {
            score += when {
                question.options.size >= 2 -> 40
                question.options.isEmpty() -> -100
                else -> -60
            }
            if (question.answer.isNotEmpty()) {
                score += if (question.answer.all { it.uppercase() in optionKeys }) 35 else -90
            }
        } else if (expectedOptionKeys.isNotEmpty()) {
            score -= 120
        }

        if (question.analysis.isNotBlank()) score += 5
        return score
    }

    private data class SectionChunk(
        val title: String,
        val forcedType: QuestionType?,
        val body: String
    )

    private fun splitBySections(text: String): List<SectionChunk> {
        val chunks = mutableListOf<SectionChunk>()
        var currentTitle = ""
        var currentType: QuestionType? = null
        val currentLines = mutableListOf<String>()
        var sawSection = false

        fun flush() {
            val body = currentLines.joinToString("\n").trim()
            if (body.isNotBlank()) {
                chunks += SectionChunk(currentTitle, currentType, body)
            }
            currentLines.clear()
        }

        text.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isBlank()) {
                currentLines += rawLine
                return@forEach
            }

            val section = SectionTitleParser.parse(line)
            if (section != null && !section.isAnswerSection) {
                flush()
                sawSection = true
                currentTitle = section.title
                currentType = section.forcedType
            } else {
                currentLines += rawLine
            }
        }
        flush()

        return if (sawSection) chunks else emptyList()
    }
}
