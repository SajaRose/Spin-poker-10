package com.sarajane.spinpoker10.game

import com.sarajane.spinpoker10.model.Card
import kotlin.random.Random

/** Une colonne (rouleau) : 3 cartes visibles + indicateur GARDÉ. */
data class ReelColumn(val cards: List<Card>, val held: Boolean)

/**
 * Moteur de tirage. Chaque colonne tire 3 cartes sans remise
 * depuis un paquet de 52 cartes, comme un rouleau de Spin Poker.
 */
class GameEngine(private val rng: Random = Random.Default) {

    /** Première distribution : 5 colonnes de 3 cartes. */
    fun deal(): List<ReelColumn> = List(5) { ReelColumn(drawThree(), held = false) }

    /** Deuxième tirage : seules les colonnes non gardées sont relancées. */
    fun complete(columns: List<ReelColumn>): List<ReelColumn> =
        columns.map { if (it.held) it else ReelColumn(drawThree(), held = false) }

    private fun drawThree(): List<Card> {
        val deck = Card.DECK.toMutableList()
        deck.shuffle(rng)
        return deck.take(3)
    }
}
