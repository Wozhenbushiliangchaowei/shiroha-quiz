package com.yiqiu.shirohaquiz.importer.parser

import com.yiqiu.shirohaquiz.importer.model.Question
import com.yiqiu.shirohaquiz.importer.model.QuestionType

object QuestionParser {
    fun parseStandard(text: String): List<Question> {
        return StandardQuestionParser.parse(text)
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
        if (sections.isEmpty()) return StandardQuestionParser.parse(text)

        return sections.flatMap { section ->
            val source = if (CompactQuestionRepair.hasCompactPattern(section.body)) {
                CompactQuestionRepair.repair(section.body)
            } else {
                section.body
            }
            StandardQuestionParser.parse(
                text = source,
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
