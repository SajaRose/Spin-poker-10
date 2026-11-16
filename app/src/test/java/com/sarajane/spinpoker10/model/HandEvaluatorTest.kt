package com.sarajane.spinpoker10.model

import org.junit.Assert.assertEquals
import org.junit.Test

class HandEvaluatorTest {

    private fun c(rank: Int, suit: Int) = Card(rank, suit)

    @Test
    fun `royal flush`() {
        val hand = listOf(c(10, 0), c(11, 0), c(12, 0), c(13, 0), c(14, 0))
        assertEquals(PokerHand.ROYAL_FLUSH, HandEvaluator.evaluate(hand))
    }

    @Test
    fun `straight flush`() {
        val hand = listOf(c(5, 1), c(6, 1), c(7, 1), c(8, 1), c(9, 1))
        assertEquals(PokerHand.STRAIGHT_FLUSH, HandEvaluator.evaluate(hand))
    }

    @Test
    fun `four of a kind`() {
        val hand = listOf(c(9, 0), c(9, 1), c(9, 2), c(9, 3), c(3, 0))
        assertEquals(PokerHand.FOUR_KIND, HandEvaluator.evaluate(hand))
    }

    @Test
    fun `full house`() {
        val hand = listOf(c(7, 0), c(7, 1), c(7, 2), c(4, 3), c(4, 0))
        assertEquals(PokerHand.FULL_HOUSE, HandEvaluator.evaluate(hand))
    }

    @Test
    fun `flush`() {
        val hand = listOf(c(2, 2), c(5, 2), c(9, 2), c(11, 2), c(13, 2))
        assertEquals(PokerHand.FLUSH, HandEvaluator.evaluate(hand))
    }

    @Test
    fun `straight`() {
        val hand = listOf(c(8, 0), c(9, 1), c(10, 2), c(11, 3), c(12, 0))
        assertEquals(PokerHand.STRAIGHT, HandEvaluator.evaluate(hand))
    }

    @Test
    fun `ace low straight`() {
        val hand = listOf(c(14, 0), c(2, 1), c(3, 2), c(4, 3), c(5, 0))
        assertEquals(PokerHand.STRAIGHT, HandEvaluator.evaluate(hand))
    }

    @Test
    fun `three of a kind`() {
        val hand = listOf(c(6, 0), c(6, 1), c(6, 2), c(2, 3), c(9, 0))
        assertEquals(PokerHand.THREE_KIND, HandEvaluator.evaluate(hand))
    }

    @Test
    fun `two pair`() {
        val hand = listOf(c(5, 0), c(5, 1), c(12, 2), c(12, 3), c(2, 0))
        assertEquals(PokerHand.TWO_PAIR, HandEvaluator.evaluate(hand))
    }

    @Test
    fun `jacks or better`() {
        val hand = listOf(c(11, 0), c(11, 1), c(3, 2), c(7, 3), c(14, 0))
        assertEquals(PokerHand.JACKS_OR_BETTER, HandEvaluator.evaluate(hand))
    }

    @Test
    fun `low pair is nothing`() {
        val hand = listOf(c(10, 0), c(10, 1), c(3, 2), c(7, 3), c(14, 0))
        assertEquals(PokerHand.NOTHING, HandEvaluator.evaluate(hand))
    }

    @Test
    fun `garbage hand`() {
        val hand = listOf(c(2, 0), c(5, 1), c(8, 2), c(10, 3), c(13, 0))
        assertEquals(PokerHand.NOTHING, HandEvaluator.evaluate(hand))
    }

    @Test
    fun `paylines constant matches definitions`() {
        assertEquals(10, Paylines.LINES.size)
        assertEquals(10, Paylines.COUNT)
        Paylines.LINES.forEach { line ->
            assertEquals(5, line.size)
            line.forEach { row -> assert(row in 1..3) }
        }
    }
}
