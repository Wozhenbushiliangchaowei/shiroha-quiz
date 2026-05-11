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

        val standardQuestions = QuestionParser.parseStandard(normalized)
        val standardCandidate = buildCandidate("标准优先解析", standardQuestions)
        candidates += standardCandidate

        if (FullPaperFallbackStrategy.shouldTry(normalized, standardCandidate.questions)) {
            val fullPaperQuestions = FullPaperFallbackStrategy.parse(normalized)
            if (fullPaperQuestions.isNotEmpty()) {
                candidates += buildCandidate("整卷真题复杂兜底解析", fullPaperQuestions)
            }
        }

        if (QuestionParser.looksCompact(normalized)) {
            val compactQuestions = QuestionParser.parseCompact(normalized)
            if (compactQuestions.size >= standardQuestions.size) {
                candidates += buildCandidate("紧凑排版拆分解析", compactQuestions)
            }
        }

        if (QuestionParser.looksSectioned(normalized)) {
            val sectionedQuestions = QuestionParser.parseSectioned(normalized)
            candidates += buildCandidate("分区题型继承解析", sectionedQuestions)
        }

        if (AnswerSectionParser.hasAnswerSection(normalized)) {
            val (questionArea, _) = AnswerSectionParser.splitSections(normalized)
            val baseQuestionCandidates = buildList {
                add("题目区标准解析" to QuestionParser.parseStandard(questionArea))
                if (QuestionParser.looksCompact(questionArea)) {
                    add("题目区紧凑解析" to QuestionParser.parseCompact(questionArea))
                }
                if (QuestionParser.looksSectioned(questionArea)) {
                    add("题目区分区解析" to QuestionParser.parseSectioned(questionArea))
                }
            }.filter { it.second.isNotEmpty() }

            val answerEntries = AnswerSectionParser.parse(normalized)
            baseQuestionCandidates.forEach { (questionStrategy, questions) ->
                val merged = DualFileMerger.mergeAuto(questions, answerEntries)
                candidates += buildCandidate(
                    name = "$questionStrategy + 答案集中区识别/${merged.name}",
                    questions = merged.questions,
                    extraWarnings = merged.warnings
                )
            }
        }

        val best = candidates.maxByOrNull { it.score } ?: Candidate(
            name = "标准优先解析",
            questions = emptyList(),
            warnings = listOf(ImportWarning(WarningLevel.ERROR, null, "未识别到任何题目")),
            score = Int.MIN_VALUE
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

        val questionCandidates = buildList {
            add("标准题目解析" to QuestionParser.parseStandard(normalizedQuestion))
            if (QuestionParser.looksCompact(normalizedQuestion)) {
                add("紧凑题目解析" to QuestionParser.parseCompact(normalizedQuestion))
            }
            if (QuestionParser.looksSectioned(normalizedQuestion)) {
                add("分区题目解析" to QuestionParser.parseSectioned(normalizedQuestion))
            }
        }.filter { it.second.isNotEmpty() }

        val answerCandidates = buildList {
            val plainAnswers = AnswerParser.parse(normalizedAnswer)
            if (plainAnswers.isNotEmpty()) add("普通答案表" to plainAnswers)
            val sectionAnswers = AnswerSectionParser.parse(normalizedAnswer)
            if (sectionAnswers.isNotEmpty()) add("答案分区表" to sectionAnswers)
            val fullParsedAnswers = QuestionParser.parseStandard(normalizedAnswer)
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
        questionCandidates.forEach { (questionStrategy, questions) ->
            answerCandidates.forEach { (answerStrategy, answers) ->
                val merged = DualFileMerger.mergeAuto(questions, answers)
                mergedCandidates += buildCandidate(
                    name = "$questionStrategy + $answerStrategy/${merged.name}",
                    questions = merged.questions,
                    extraWarnings = merged.warnings
                )
            }
        }

        val best = mergedCandidates.maxByOrNull { it.score } ?: Candidate(
            name = "双文件智能合并",
            questions = emptyList(),
            warnings = listOf(ImportWarning(WarningLevel.ERROR, null, "双文件导入未得到有效题目")),
            score = Int.MIN_VALUE
        )

        return buildResult(
            normalized = normalizedQuestion + "\n" + normalizedAnswer,
            candidates = mergedCandidates,
            best = best
        )
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
