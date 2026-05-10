package com.yiqiu.shirohaquiz.importer.parser

data class QuestionBlock(
    val number: String,
    val lines: List<String>
)

object QuestionBlockSplitter {
    private val questionStartRegex = Regex(
        """^\s*(?:第\s*)?(\d{1,4})\s*(?:题)?(?:[.、．:：]|[)）])\s*(.*)$"""
    )

    fun split(text: String): List<QuestionBlock> {
        val blocks = mutableListOf<QuestionBlock>()
        var currentNumber: String? = null
        var currentLines = mutableListOf<String>()

        text.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            val match = questionStartRegex.find(line)
            if (match != null) {
                currentNumber?.let { blocks += QuestionBlock(it, currentLines.toList()) }
                currentNumber = match.groupValues[1]
                currentLines = mutableListOf<String>().apply {
                    val remainder = match.groupValues[2].trim()
                    if (remainder.isNotBlank()) add(remainder)
                }
            } else if (currentNumber != null && line.isNotBlank()) {
                currentLines += line
            }
        }

        currentNumber?.let { blocks += QuestionBlock(it, currentLines.toList()) }
        return blocks
    }
}
