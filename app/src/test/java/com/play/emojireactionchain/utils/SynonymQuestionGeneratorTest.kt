package com.play.emojireactionchain.utils

import com.play.emojireactionchain.viewModel.BaseGameViewModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SynonymQuestionGeneratorTest {

    private val generator = SynonymQuestionGenerator()

    @Test
    fun generateQuestion_neverPlacesEquivalentSynonymAsDistractor() {
        val available = listOf("😀", "😊", "😄", "😁", "😆", "😅", "🍎", "🐶", "🚗", "⚽")

        repeat(100) {
            val (chain, answer, choices) = generator.generateQuestion(available, level = 5)
            assertTrue("chain should not be empty", chain.isNotEmpty())
            assertFalse("answer should be present in options once", choices.groupBy { it }[answer]!!.size > 1)

            val activeGroup = BaseGameViewModel.synonymPairs.firstOrNull { it.contains(answer) }
            assertTrue("answer should belong to a known synonym group", activeGroup != null)

            val invalidDistractor = choices
                .filter { it != answer }
                .any { activeGroup!!.contains(it) }

            assertFalse("choices should not contain another valid synonym answer", invalidDistractor)
        }
    }
}

