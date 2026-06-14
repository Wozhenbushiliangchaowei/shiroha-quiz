package com.yiqiu.shirohaquiz.importer.parser

import com.yiqiu.shirohaquiz.importer.model.ImportDiagnostics
import com.yiqiu.shirohaquiz.importer.model.ImportResult
import com.yiqiu.shirohaquiz.importer.model.ImportWarning
import com.yiqiu.shirohaquiz.importer.model.Question
import com.yiqiu.shirohaquiz.importer.model.QuestionType
import com.yiqiu.shirohaquiz.importer.model.WarningLevel
import com.yiqiu.shirohaquiz.importer.score.ImportStrategyScorer
import com.yiqiu.shirohaquiz.importer.validate.ImportValidator

object QuizImportParser {
    fun parseStandardText(raw: String): ImportResult {
        val normalized = QuestionTextNormalizer.normalize(raw)
        val candidates = mutableListOf<Candidate>()
        val hasAnswerSection = AnswerSectionParser.hasAnswerSection(normalized)
        val questionArea = if (hasAnswerSection) {
            AnswerSectionParser.splitSections(normalized).first
        } else {
            normalized
        }

        val rewrittenQuestionArea = SharedStemQuestionFallbackParser.rewrite(questionArea)
        val primaryQuestions = QuestionParser.parseStandardFirst(rewrittenQuestionArea ?: questionArea)
        val primaryCandidate = if (hasAnswerSection) {
            val answerEntries = AnswerSectionParser.parse(normalized)
            val merged = DualFileMerger.mergeAuto(primaryQuestions, answerEntries)
            buildCandidate(
                name = buildPrimaryStrategyName(
                    hasSharedStemRewrite = rewrittenQuestionArea != null,
                    hasAnswerSection = true,
                    mergeName = merged.name
                ),
                questions = merged.questions,
                extraWarnings = merged.warnings
            )
        } else {
            buildCandidate(
                name = buildPrimaryStrategyName(
                    hasSharedStemRewrite = rewrittenQuestionArea != null,
                    hasAnswerSection = false
                ),
                questions = primaryQuestions
            )
        }
        candidates += primaryCandidate

        val tableCandidate = ExcelQuestionTableParser.parse(normalized)
            .takeIf { it.isNotEmpty() }
            ?.let { buildCandidate("Excel/CSV 表格题库解析", it) }
            ?.also { candidates += it }

        val fullPaperCandidate = if (FullPaperFallbackStrategy.shouldTry(normalized, primaryCandidate.questions)) {
            FullPaperFallbackStrategy.parse(normalized)
                .takeIf { it.isNotEmpty() }
                ?.let { buildCandidate("整卷真题复杂兜底解析", it) }
                ?.also { candidates += it }
        } else {
            null
        }

        val best = chooseBestCandidate(
            normalized = normalized,
            primaryCandidate = primaryCandidate,
            tableCandidate = tableCandidate,
            fullPaperCandidate = fullPaperCandidate
        )

        return buildResult(
            normalized = normalized,
            candidates = candidates,
            best = best
        )
    }

    fun parseDualText(questionText: String, answerText: String): ImportResult {
        val normalizedQuestion = QuestionTextNormalizer.normalize(questionText)
        val normalizedAnswer = QuestionTextNormalizer.normalize(answerText)
        val questionCandidates = mutableListOf<Candidate>()

        val rewrittenQuestion = SharedStemQuestionFallbackParser.rewrite(normalizedQuestion)
        val primaryQuestionCandidate = buildCandidate(
            name = buildPrimaryStrategyName(
                hasSharedStemRewrite = rewrittenQuestion != null,
                hasAnswerSection = false
            ),
            questions = QuestionParser.parseStandardFirst(rewrittenQuestion ?: normalizedQuestion)
        )
        questionCandidates += primaryQuestionCandidate

        val tableQuestionCandidate = ExcelQuestionTableParser.parse(normalizedQuestion)
            .takeIf { it.isNotEmpty() }
            ?.let { buildCandidate("Excel/CSV 表格题目解析", it) }
            ?.also { questionCandidates += it }

        val fullPaperQuestionCandidate = if (
            FullPaperFallbackStrategy.shouldTry(normalizedQuestion, primaryQuestionCandidate.questions)
        ) {
            FullPaperFallbackStrategy.parse(normalizedQuestion)
                .takeIf { it.isNotEmpty() }
                ?.let { buildCandidate("整卷真题复杂题目兜底解析", it) }
                ?.also { questionCandidates += it }
        } else {
            null
        }

        val selectedQuestionCandidate = chooseBestCandidate(
            normalized = normalizedQuestion,
            primaryCandidate = primaryQuestionCandidate,
            tableCandidate = tableQuestionCandidate,
            fullPaperCandidate = fullPaperQuestionCandidate
        )

        val answerCandidates = buildList {
            val plainAnswers = AnswerParser.parse(normalizedAnswer)
            if (plainAnswers.isNotEmpty()) add("普通答案表" to plainAnswers)
            val sectionAnswers = AnswerSectionParser.parse(normalizedAnswer)
            if (sectionAnswers.isNotEmpty()) add("答案分区表" to sectionAnswers)
            val fullParsedAnswers = QuestionParser.parseStandardFirst(normalizedAnswer)
                .filter { it.answer.isNotEmpty() }
                .mapIndexed { index, question ->
                    ParsedAnswerEntry(
                        number = question.number,
                        answer = question.answer,
                        analysis = question.analysis,
                        type = question.type,
                        sequence = index
                    )
                }
            if (fullParsedAnswers.isNotEmpty()) add("完整题库答案兜底" to fullParsedAnswers)
            val mixed = (plainAnswers + sectionAnswers + fullParsedAnswers)
                .distinctBy { Triple(it.type, it.number, it.sequence) }
            if (mixed.isNotEmpty()) add("混合答案来源" to mixed)
        }

        val mergedCandidates = mutableListOf<Candidate>()
        answerCandidates.forEach { (answerStrategy, answers) ->
            val merged = DualFileMerger.mergeAuto(selectedQuestionCandidate.questions, answers)
            mergedCandidates += buildCandidate(
                name = "${selectedQuestionCandidate.name} + $answerStrategy/${merged.name}",
                questions = merged.questions,
                extraWarnings = merged.warnings
            )
        }

        val best = mergedCandidates.maxByOrNull { it.score } ?: selectedQuestionCandidate
        val diagnosticCandidates = questionCandidates + mergedCandidates

        return buildResult(
            normalized = normalizedQuestion + "\n" + normalizedAnswer,
            candidates = diagnosticCandidates,
            best = best
        )
    }

    private fun buildPrimaryStrategyName(
        hasSharedStemRewrite: Boolean,
        hasAnswerSection: Boolean,
        mergeName: String = ""
    ): String {
        val localFallback = if (hasSharedStemRewrite) {
            "标准优先解析（单题紧凑修复 + 共用题干局部兜底）"
        } else {
            "标准优先解析（单题紧凑修复）"
        }
        return if (hasAnswerSection) {
            "$localFallback + 答案集中区识别/$mergeName"
        } else {
            localFallback
        }
    }

    private fun chooseBestCandidate(
        normalized: String,
        primaryCandidate: Candidate,
        tableCandidate: Candidate?,
        fullPaperCandidate: Candidate?
    ): Candidate {
        var best = primaryCandidate

        if (tableCandidate != null && shouldUseSpecializedTable(primaryCandidate, tableCandidate)) {
            best = tableCandidate
        }

        if (
            fullPaperCandidate != null &&
            shouldUseFullPaper(normalized, best, fullPaperCandidate)
        ) {
            best = fullPaperCandidate
        }

        return best
    }

    private fun shouldUseSpecializedTable(primary: Candidate, table: Candidate): Boolean {
        if (primary.questions.isEmpty()) return true
        val primaryErrors = primary.warnings.count { it.level == WarningLevel.ERROR }
        val tableErrors = table.warnings.count { it.level == WarningLevel.ERROR }
        val countFloor = (primary.questions.size * 0.8).toInt().coerceAtLeast(1)
        val preservesMostQuestions = table.questions.size >= countFloor
        val materiallyCleaner = tableErrors < primaryErrors || table.score >= primary.score + 30
        return preservesMostQuestions && materiallyCleaner
    }

    private fun shouldUseFullPaper(
        normalized: String,
        primary: Candidate,
        fullPaper: Candidate
    ): Boolean {
        if (!FullPaperFallbackStrategy.looksLikeFullPaper(normalized)) return false
        if (fullPaper.questions.isEmpty()) return false
        if (primary.questions.isEmpty()) return true

        val primaryCount = primary.questions.size
        val fullCount = fullPaper.questions.size
        val primaryErrors = primary.warnings.count { it.level == WarningLevel.ERROR }
        val fullErrors = fullPaper.warnings.count { it.level == WarningLevel.ERROR }
        val primarySubjective = primary.questions.count {
            it.type == QuestionType.SHORT || it.type == QuestionType.BLANK
        }
        val primaryShortStem = primary.questions.count {
            it.question.trim().length <= 3 && it.options.isEmpty()
        }
        val primaryFrontMatter = primary.questions.count { question ->
            Regex("""^(?:说明|注意事项|密卷|绝密|祝各位考生|时间[:：]|考试时间[:：])""")
                .containsMatchIn(question.question.trim())
        }
        val primaryObjective = primary.questions.count {
            it.type == QuestionType.SINGLE || it.type == QuestionType.MULTIPLE || it.type == QuestionType.JUDGE
        }
        val fullObjective = fullPaper.questions.count {
            it.type == QuestionType.SINGLE || it.type == QuestionType.MULTIPLE || it.type == QuestionType.JUDGE
        }

        val primaryOverallUnreliable =
            primaryErrors >= (primaryCount / 5).coerceAtLeast(2) ||
                primarySubjective > primaryCount / 3 ||
                primaryShortStem >= 3 ||
                primaryFrontMatter > 0
        if (!primaryOverallUnreliable) return false

        val countIsReasonable = fullCount >= (primaryCount * 0.7).toInt().coerceAtLeast(3)
        val structureImproved =
            fullErrors < primaryErrors ||
                fullObjective > primaryObjective ||
                fullPaper.score >= primary.score + 30

        return countIsReasonable && structureImproved
    }

    private fun buildCandidate(
        name: String,
        questions: List<Question>,
        extraWarnings: List<ImportWarning> = emptyList()
    ): Candidate {
        val repairedQuestions = questions.map(::repairQuestionForDisplay)
        val warnings = ImportValidator.validate(repairedQuestions) + extraWarnings
        val score = ImportStrategyScorer.score(repairedQuestions, warnings)
        return Candidate(name, repairedQuestions, warnings, score)
    }

    private fun repairQuestionForDisplay(question: Question): Question {
        return if (question.type == QuestionType.JUDGE && question.options.isEmpty()) {
            question.copy(
                options = listOf(
                    com.yiqiu.shirohaquiz.importer.model.Option("A", "正确"),
                    com.yiqiu.shirohaquiz.importer.model.Option("B", "错误")
                )
            )
        } else {
            question
        }
    }

    private fun buildResult(
        normalized: String,
        candidates: List<Candidate>,
        best: Candidate
    ): ImportResult {
        return ImportResult(
            questions = best.questions,
            strategyName = best.name,
            warnings = best.warnings,
            diagnostics = ImportDiagnostics(
                normalizedLength = normalized.length,
                blockCount = QuestionBlockSplitter.split(normalized).size,
                answeredCount = best.questions.count { it.answer.isNotEmpty() },
                candidateCount = candidates.size,
                notes = buildDiagnosticNotes(best, candidates)
            )
        )
    }

    private fun buildDiagnosticNotes(best: Candidate, candidates: List<Candidate>): List<String> {
        val typeSummary = best.questions.groupingBy { it.type }.eachCount()
        val typeNote = "题型分布：单选${typeSummary[QuestionType.SINGLE] ?: 0} / 多选${typeSummary[QuestionType.MULTIPLE] ?: 0} / 判断${typeSummary[QuestionType.JUDGE] ?: 0} / 填空${typeSummary[QuestionType.BLANK] ?: 0} / 简答${typeSummary[QuestionType.SHORT] ?: 0}"
        val candidateNotes = candidates
            .distinctBy { it.name }
            .sortedByDescending { it.score }
            .take(5)
            .map { candidate ->
                "${candidate.name}：${candidate.questions.size}题 / 答案${candidate.questions.count { it.answer.isNotEmpty() }} / 分数${candidate.score}"
            }
        return listOf(typeNote) + candidateNotes
    }

    private data class Candidate(
        val name: String,
        val questions: List<Question>,
        val warnings: List<ImportWarning>,
        val score: Int
    )
}
