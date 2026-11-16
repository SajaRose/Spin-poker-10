package com.sarajane.spinpoker10.model

/**
 * Mains de poker, Jacks or Better. payout = gain pour 1 crédit misé sur la ligne.
 */
enum class PokerHand(val label: String, val payout: Int) {
    ROYAL_FLUSH("Quinte Flush Royale", 250),
    STRAIGHT_FLUSH("Quinte Flush", 50),
    FOUR_KIND("Carré", 25),
    FULL_HOUSE("Full", 9),
    FLUSH("Couleur", 6),
    STRAIGHT("Quinte", 4),
    THREE_KIND("Brelan", 3),
    TWO_PAIR("Double Paire", 2),
    JACKS_OR_BETTER("Paire de Valets ou mieux", 1),
    NOTHING("Pas de gain", 0);

    companion object {
        /** Ordre décroissant pour l'affichage de la table des gains. */
        val PAYTABLE_ORDER = listOf(
            ROYAL_FLUSH, STRAIGHT_FLUSH, FOUR_KIND, FULL_HOUSE,
            FLUSH, STRAIGHT, THREE_KIND, TWO_PAIR, JACKS_OR_BETTER
        )
    }
}

object HandEvaluator {

    /**
     * Évalue une main de 5 cartes.
     * @throws IllegalArgumentException si la main ne contient pas 5 cartes.
     */
    fun evaluate(cards: List<Card>): PokerHand {
        require(cards.size == 5) { "Une main doit contenir 5 cartes" }
        val flush = cards.map { it.suit }.toSet().size == 1
        val straight = isStraight(cards.map { it.rank })
        val counts = cards.groupingBy { it.rank }.eachCount().values.sortedDescending()

        return when {
            flush && straight && isRoyal(cards) -> PokerHand.ROYAL_FLUSH
            flush && straight -> PokerHand.STRAIGHT_FLUSH
            counts[0] >= 4 -> PokerHand.FOUR_KIND
            counts[0] == 3 && counts.getOrNull(1) == 2 -> PokerHand.FULL_HOUSE
            flush -> PokerHand.FLUSH
            straight -> PokerHand.STRAIGHT
            counts[0] == 3 -> PokerHand.THREE_KIND
            counts[0] == 2 && counts.getOrNull(1) == 2 -> PokerHand.TWO_PAIR
            counts[0] == 2 && highPairRank(cards) -> PokerHand.JACKS_OR_BETTER
            else -> PokerHand.NOTHING
        }
    }

    private fun isStraight(ranks: List<Int>): Boolean {
        val sorted = ranks.sorted()
        if (sorted == listOf(2, 3, 4, 5, 14)) return true // quinte avec As bas
        return sorted.zipWithNext().all { (a, b) -> b - a == 1 }
    }

    private fun isRoyal(cards: List<Card>): Boolean =
        cards.map { it.rank }.containsAll(listOf(10, 11, 12, 13, 14))

    private fun highPairRank(cards: List<Card>): Boolean {
        val pairRank = cards.groupingBy { it.rank }
            .eachCount()
            .filterValues { it == 2 }
            .keys.firstOrNull()
        return pairRank != null && pairRank >= 11
    }
}
