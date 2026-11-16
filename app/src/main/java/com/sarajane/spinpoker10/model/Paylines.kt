package com.sarajane.spinpoker10.model

/**
 * Définition des 10 lignes de paiement sur la grille 5 colonnes x 3 rangées.
 * Chaque ligne associe, pour chaque colonne (index 0..4), la rangée utilisée (1=haut, 2=milieu, 3=bas).
 * Les 5 cartes sélectionnées forment une main de poker évaluée en Jacks or Better.
 */
object Paylines {

    val LINES: List<List<Int>> = listOf(
        listOf(1, 1, 1, 1, 1), // L1 : rangée du haut
        listOf(2, 2, 2, 2, 2), // L2 : rangée du milieu
        listOf(3, 3, 3, 3, 3), // L3 : rangée du bas
        listOf(2, 1, 1, 1, 2), // L4 : V pointe en haut
        listOf(2, 3, 3, 3, 2), // L5 : V pointe en bas
        listOf(1, 2, 2, 2, 1), // L6 : pont haut
        listOf(3, 2, 2, 2, 3), // L7 : pont bas
        listOf(1, 2, 1, 2, 1), // L8 : zigzag haut
        listOf(3, 2, 3, 2, 3), // L9 : zigzag bas
        listOf(1, 2, 3, 2, 1)  // L10 : chevron
    )

    const val COUNT = 10

    /** Cartes de la ligne donnée sur la grille de colonnes. */
    fun lineCards(lineIndex: Int, columns: List<List<Card>>): List<Card> {
        val line = LINES[lineIndex]
        return line.mapIndexed { col, row -> columns[col][row - 1] }
    }
}
