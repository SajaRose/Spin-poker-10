package com.sarajane.spinpoker10.game

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sarajane.spinpoker10.model.Card
import com.sarajane.spinpoker10.model.HandEvaluator
import com.sarajane.spinpoker10.model.Paylines
import com.sarajane.spinpoker10.model.PokerHand
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Phase { IDLE, SPINNING, HOLD, DRAWING, RESULT }

data class LineWin(val line: Int, val hand: PokerHand, val amount: Int)

data class GameState(
    val credits: Int = 1000,
    val betPerLine: Int = 1,
    val columns: List<ReelColumn> = emptyColumns(),
    val phase: Phase = Phase.IDLE,
    val wins: List<LineWin> = emptyList(),
    val lastWin: Int = 0,
    val message: String = ""
) {
    val totalBet: Int get() = betPerLine * Paylines.COUNT
    companion object {
        fun emptyColumns(): List<ReelColumn> =
            List(5) { ReelColumn(List(3) { Card.BACK }, held = false) }
        val BET_STEPS = listOf(1, 2, 3, 5, 10)
    }
}

class GameViewModel(app: Application, private val engine: GameEngine) : AndroidViewModel(app) {

    constructor(app: Application) : this(app, GameEngine())

    private val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(
        GameState(credits = prefs.getInt(KEY_CREDITS, 1000))
    )
    val state: StateFlow<GameState> = _state.asStateFlow()

    /** Première étape : mise prélevée, rouleaux en rotation, puis distribution. */
    fun spin(maxBet: Boolean = false) {
        val s = _state.value
        if (s.phase != Phase.IDLE && s.phase != Phase.RESULT) return
        val bet = if (maxBet) GameState.BET_STEPS.last() else s.betPerLine
        val total = bet * Paylines.COUNT
        if (s.credits < total) {
            _state.update { it.copy(message = MSG_INSUFFICIENT) }
            return
        }
        _state.update {
            it.copy(
                credits = it.credits - total,
                betPerLine = bet,
                phase = Phase.SPINNING,
                wins = emptyList(),
                lastWin = 0,
                message = ""
            )
        }
        persist()
        viewModelScope.launch {
            delay(SPIN_DURATION_MS)
            _state.update { it.copy(columns = engine.deal(), phase = Phase.HOLD) }
        }
    }

    /** Étape HOLD : garde une colonne (le GARDÉ s'applique à tout le rouleau). */
    fun toggleHold(index: Int) {
        if (_state.value.phase != Phase.HOLD) return
        _state.update { st ->
            st.copy(columns = st.columns.mapIndexed { i, c ->
                if (i == index) c.copy(held = !c.held) else c
            })
        }
    }

    /** Deuxième étape : relance des rouleaux non gardés puis évaluation des 10 mains. */
    fun draw() {
        if (_state.value.phase != Phase.HOLD) return
        _state.update { it.copy(phase = Phase.DRAWING) }
        viewModelScope.launch {
            delay(DRAW_DURATION_MS)
            val cols = engine.complete(_state.value.columns)
            val bet = _state.value.betPerLine
            val wins = Paylines.LINES.indices.mapNotNull { i ->
                val hand = HandEvaluator.evaluate(Paylines.lineCards(i, cols.map { it.cards }))
                if (hand == PokerHand.NOTHING) null else LineWin(i, hand, hand.payout * bet)
            }
            val total = wins.sumOf { it.amount }
            _state.update {
                it.copy(columns = cols, wins = wins, lastWin = total,
                    credits = it.credits + total, phase = Phase.RESULT)
            }
            persist()
            delay(RESULT_DISPLAY_MS)
            _state.update {
                it.copy(
                    phase = Phase.IDLE,
                    columns = it.columns.map { c -> c.copy(held = false) },
                    wins = emptyList()
                )
            }
        }
    }

    /** Cycle de la mise par ligne. */
    fun changeBet() {
        if (_state.value.phase != Phase.IDLE) return
        _state.update { st ->
            val idx = GameState.BET_STEPS.indexOf(st.betPerLine)
            st.copy(betPerLine = GameState.BET_STEPS[(idx + 1) % GameState.BET_STEPS.size])
        }
    }

    /** Crédits gratuits : aucun argent réel, jeu de divertissement. */
    fun refillCredits() {
        _state.update { it.copy(credits = it.credits + 500, message = "") }
        persist()
    }

    fun clearMessage() {
        _state.update { it.copy(message = "") }
    }

    private fun persist() {
        prefs.edit().putInt(KEY_CREDITS, _state.value.credits).apply()
    }

    companion object {
        private const val PREFS = "spinpoker10"
        private const val KEY_CREDITS = "credits"
        const val SPIN_DURATION_MS = 1400L
        const val DRAW_DURATION_MS = 900L
        const val RESULT_DISPLAY_MS = 3000L
        const val MSG_INSUFFICIENT = "INSUFFICIENT"
    }
}
