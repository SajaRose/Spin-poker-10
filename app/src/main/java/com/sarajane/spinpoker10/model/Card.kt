package com.sarajane.spinpoker10.model

/**
 * Une carte de poker. rank : 2..14 (14 = As), suit : 0=♠ 1=♥ 2=♦ 3=♣.
 */
data class Card(val rank: Int, val suit: Int) {

    val rankLabel: String
        get() = when (rank) {
            14 -> "A"
            13 -> "K"
            12 -> "Q"
            11 -> "J"
            else -> rank.toString()
        }

    val suitSymbol: String
        get() = SUITS[suit]

    val isRed: Boolean
        get() = suit == 1 || suit == 2

    companion object {
        val SUITS = listOf("♠", "♥", "♦", "♣")
        val DECK: List<Card> = (2..14).flatMap { r -> (0..3).map { s -> Card(r, s) } }
        /** Carte neutre utilisée pour le dos de carte / état vide. */
        val BACK = Card(0, -1)
    }
}
