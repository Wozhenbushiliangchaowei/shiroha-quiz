package com.yiqiu.shirohaquiz.importer.score

import com.yiqiu.shirohaquiz.importer.model.ImportWarning
import com.yiqiu.shirohaquiz.importer.model.Question
import com.yiqiu.shirohaquiz.importer.model.QuestionType
import com.yiqiu.shirohaquiz.importer.model.WarningLevel

object ImportStrategyScorer {
    fun score(questions: List<Question>, warnings: List<ImportWarning>): Int {
        if (questions.isEmpty()) return Int.MIN_VALUE / 2
        val answeredCount = questions.count { it.answer.isNotEmpty() }
        val hardErrors = warnings.count { it.level == WarningLevel.ERROR }
        val softWarnings = warnings.count { it.level == WarningLevel.WARNING }
        val answerCoverage = answeredCount.toDouble() / questions.size.toDouble()
        val objectiveCount = questions.count {
            it.type == QuestionType.SINGLE || it.type == QuestionType.MULTIPLE || it.type == QuestionType.JUDGE
        }
        val optionCoverage = if (objectiveCount == 0) 1.0 else {
            questions.count {
                (it.type == QuestionType.SINGLE || it.type == QuestionType.MULTIPLE || it.type == QuestionType.JUDGE) && it.options.size >= 2
            }.toDouble() / objectiveCount.toDouble()
        }
        val sectionBonus = if (questions.any { it.category.isNotBlank() }) 20 else 0

        return questions.size * 5 +
            answeredCount * 12 +
            (answerCoverage * 150).toInt() +
            (optionCoverage * 80).toInt() +
            sectionBonus -
            hardErrors * 35 -
            softWarnings * 6
    }
}
