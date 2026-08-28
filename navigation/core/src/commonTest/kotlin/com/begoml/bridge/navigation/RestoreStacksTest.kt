package com.begoml.bridge.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private data class TestRoute(override val key: String) : Route

private class PrefixCodec(private val prefix: String) : RouteCodec {

    override fun decode(key: String): Route? = TestRoute(key).takeIf { key.startsWith(prefix) }
}

class RestoreStacksTest {

    @Test
    fun `keys come back as routes, in the order and the tab they were saved in`() {
        val saved = listOf(listOf("home"), listOf("squad", "player:7"))

        val restored = restoreStacks(
            saved = saved,
            codecs = listOf(PrefixCodec("home"), PrefixCodec("squad"), PrefixCodec("player:")),
            onUnknownKey = { error("nothing should be unknown here") },
        )

        assertEquals(saved, restored.map { stack -> stack.map { it.key } })
    }

    @Test
    fun `a key no codec knows is reported, not silently dropped`() {
        val unknown = mutableListOf<String>()

        val restored = restoreStacks(
            saved = listOf(listOf("squad", "ghost:1")),
            codecs = listOf(PrefixCodec("squad")),
            onUnknownKey = { key -> unknown += key },
        )

        assertEquals(listOf("ghost:1"), unknown)
        assertEquals(listOf(listOf("squad")), restored.map { stack -> stack.map { it.key } })
    }

    @Test
    fun `the first codec that recognises a key decides it`() {
        val restored = restoreStacks(
            saved = listOf(listOf("player:7")),
            codecs = listOf(PrefixCodec("nothing"), PrefixCodec("player:")),
            onUnknownKey = { error("player: is decodable") },
        )

        assertTrue(restored.single().single() is TestRoute)
    }
}
