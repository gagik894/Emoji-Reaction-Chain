package com.play.emojireactionchain.utils

import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StickerRewardEngineTest {

    @Test
    fun nextSticker_returnsFirstUncollectedSticker() {
        val engine = StickerRewardEngine(listOf("🌟", "🎈", "🦄"))

        val next = engine.nextSticker(setOf("🌟"))

        assertTrue(next == "🎈")
    }

    @Test
    fun nextSticker_returnsNullWhenEverythingCollected() {
        val engine = StickerRewardEngine(listOf("🌟", "🎈"))

        val next = engine.nextSticker(setOf("🌟", "🎈"))

        assertNull(next)
    }
}

