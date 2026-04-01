package com.play.emojireactionchain.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SequentialQuestionGeneratorTest {

    private val generator = SequentialQuestionGenerator()

    @Test
    fun generateQuestion_withCategoryEmojis_returnsConsistentPayload() {
        val available = listOf("🍎", "🍌", "🍇", "🍓", "🍉", "🥝")

        repeat(100) {
            val (chain, answer, choices) = generator.generateQuestion(available, level = 4)

            assertTrue("chain should not be empty", chain.isNotEmpty())
            assertTrue("chain should be max 5", chain.size <= 5)
            assertTrue("chain should not contain duplicates", chain.distinct().size == chain.size)
            assertFalse("answer should not be blank", answer.isBlank())
            assertFalse("answer must not be in chain", chain.contains(answer))
            assertTrue("choices should contain answer", choices.contains(answer))
            assertTrue("choices should be at least 3", choices.size >= 3)
            assertTrue("choices should be unique", choices.distinct().size == choices.size)
        }
    }

    @Test
    fun generateQuestion_withTooFewEmojis_usesFallbackAndStillBuildsQuestion() {
        val available = listOf("🍎", "🍌")

        val (chain, answer, choices) = generator.generateQuestion(available, level = 1)

        assertTrue("fallback chain should not be empty", chain.isNotEmpty())
        assertFalse("fallback answer should not be blank", answer.isBlank())
        assertTrue("fallback choices should include answer", choices.contains(answer))
    }
}

