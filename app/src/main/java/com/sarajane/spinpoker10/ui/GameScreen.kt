package com.sarajane.spinpoker10.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sarajane.spinpoker10.R
import com.sarajane.spinpoker10.game.GameViewModel
import com.sarajane.spinpoker10.game.Phase
import com.sarajane.spinpoker10.model.Card
import com.sarajane.spinpoker10.model.PokerHand
import com.sarajane.spinpoker10.sound.SoundManager
import com.sarajane.spinpoker10.ui.theme.CasinoBackground
import com.sarajane.spinpoker10.ui.theme.Gold
import com.sarajane.spinpoker10.ui.theme.HeldGreen
import com.sarajane.spinpoker10.ui.theme.WinGold
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun GameScreen(viewModel: GameViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val sound = remember { SoundManager(context) }
    DisposableEffect(Unit) { onDispose { sound.release() } }
    var showPaytable by remember { mutableStateOf(false) }

    LaunchedEffect(state.phase) {
        when (state.phase) {
            Phase.HOLD -> sound.play("reel_stop")
            Phase.RESULT -> {
                if (state.lastWin >= state.betPerLine * 50) sound.play("bigwin")
                else if (state.lastWin > 0) sound.play("win")
                else sound.play("lose")
            }
            else -> {}
        }
    }
    LaunchedEffect(state.lastWin) {
        if (state.lastWin > 0) sound.play("coin")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CasinoBackground)
            .padding(8.dp)
    ) {
        HeaderBar()
        InfoBar(state.credits, state.betPerLine, state.totalBet, state.lastWin)
        Spacer(Modifier.height(8.dp))

        Row(Modifier.weight(1f)) {
            LineLegend(state.wins.map { it.line }.toSet())
            Spacer(Modifier.width(4.dp))
            ReelGrid(state, viewModel, sound)
        }

        Spacer(Modifier.height(8.dp))
        StatusLine(state)
        Controls(state, viewModel, sound) { showPaytable = true }
    }

    if (showPaytable) {
        PaytableDialog(state.betPerLine) { showPaytable = false }
    }
}

@Composable
private fun HeaderBar() {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.game_title),
            color = Gold,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Text(
            text = stringResource(R.string.game_subtitle),
            color = Color(0xFF9FB3C8),
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun InfoBar(credits: Int, betPerLine: Int, totalBet: Int, lastWin: Int) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF15263A))
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        InfoCell(stringResource(R.string.credits), credits.toString(), Gold)
        InfoCell(stringResource(R.string.bet_per_line), betPerLine.toString(), Color.White)
        InfoCell(stringResource(R.string.bet_total), totalBet.toString(), Color.White)
        InfoCell(stringResource(R.string.win), lastWin.toString(), WinGold)
    }
}

@Composable
private fun InfoCell(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color(0xFF9FB3C8), fontSize = 10.sp)
        Text(value, color = valueColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LineLegend(winningLines: Set<Int>) {
    Column(
        Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        (1..10).forEach { index ->
            val isWinning = (index - 1) in winningLines
            Text(
                text = index.toString(),
                color = if (isWinning) CasinoBackground else Color(0xFF62788C),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isWinning) WinGold else Color(0xFF1E3450))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun ReelGrid(
    state: com.sarajane.spinpoker10.game.GameState,
    viewModel: GameViewModel,
    sound: SoundManager
) {
    val winningCells = remember(state.wins) {
        state.wins.flatMap { w ->
            com.sarajane.spinpoker10.model.Paylines.LINES[w.line]
                .mapIndexed { col, row -> col to (row - 1) }
        }.toSet()
    }

    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        state.columns.forEachIndexed { col, column ->
            val spinning = state.phase == Phase.SPINNING ||
                (state.phase == Phase.DRAWING && !column.held)
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0F2233))
                    .padding(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                repeat(3) { row ->
                    val card = column.cards[row]
                    val highlighted = (col to row) in winningCells
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(2.dp)
                            .clickable(enabled = state.phase == Phase.HOLD) {
                                sound.play("hold")
                                viewModel.toggleHold(col)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (spinning) {
                            SpinningCard()
                        } else {
                            PlayingCard(card, highlighted)
                        }
                        if (column.held && state.phase != Phase.IDLE) {
                            HeldBadge(Modifier.align(Alignment.BottomCenter))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeldBadge(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.hold),
        color = Color.White,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(HeldGreen)
            .padding(horizontal = 6.dp, vertical = 1.dp)
    )
}

@Composable
private fun PlayingCard(card: Card, highlighted: Boolean) {
    val faceColor = if (card.rank == 0) Color(0xFF1E4E6E) else Color(0xFFF7F5F0)
    val shape = RoundedCornerShape(6.dp)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(0.72f, matchHeightConstraintsFirst = true)
            .clip(shape)
            .background(faceColor)
            .border(
                width = if (highlighted) 3.dp else 1.dp,
                color = if (highlighted) WinGold else Color(0xFFC8CDD3),
                shape = shape
            )
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (card.rank == 0) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("SP10", color = Color(0xFF7FA8C9), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Text(
                text = card.suitSymbol,
                color = if (card.isRed) Color(0xFFC62828) else Color(0xFF212121),
                fontSize = 16.sp
            )
            Text(
                text = card.rankLabel,
                color = if (card.isRed) Color(0xFFC62828) else Color(0xFF212121),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun SpinningCard() {
    val card by produceState(Card.BACK) {
        while (true) {
            delay(60)
            value = Card(Random.nextInt(2, 15), Random.nextInt(0, 4))
        }
    }
    PlayingCard(card.value, highlighted = false)
}

@Composable
private fun StatusLine(state: com.sarajane.spinpoker10.game.GameState) {
    val text = when {
        state.message == GameViewModel.MSG_INSUFFICIENT -> stringResource(R.string.insufficient_credits)
        state.phase == Phase.HOLD -> stringResource(R.string.hold_hint)
        state.wins.isNotEmpty() -> state.wins.joinToString(" · ") { w ->
            w.hand.label + " L" + (w.line + 1) + " : +" + w.amount
        }
        else -> " "
    }
    Text(
        text = text,
        color = if (state.wins.isNotEmpty()) WinGold else Color(0xFFB0C4D8),
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp),
        maxLines = 2
    )
}

@Composable
private fun Controls(
    state: com.sarajane.spinpoker10.game.GameState,
    viewModel: GameViewModel,
    sound: SoundManager,
    onShowPaytable: () -> Unit
) {
    val mainLabel = when (state.phase) {
        Phase.HOLD -> stringResource(R.string.draw)
        Phase.SPINNING, Phase.DRAWING -> "..."
        else -> stringResource(R.string.deal)
    }
    val mainAction = {
        when (state.phase) {
            Phase.HOLD -> viewModel.draw()
            Phase.SPINNING, Phase.DRAWING -> {}
            else -> viewModel.spin()
        }
    }
    Column {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedButton(
                onClick = { sound.play("click"); viewModel.changeBet() },
                enabled = state.phase == Phase.IDLE,
                modifier = Modifier.weight(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.bet_per_line), fontSize = 9.sp)
                    Text(state.betPerLine.toString() + " ✦", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            Button(
                onClick = { sound.play("deal"); viewModel.spin(maxBet = true) },
                enabled = state.phase == Phase.IDLE || state.phase == Phase.RESULT,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA)),
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.bet_max), fontSize = 12.sp, maxLines = 1) }
            OutlinedButton(
                onClick = { sound.play("click"); onShowPaytable() },
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.paytable), fontSize = 10.sp, maxLines = 1) }
        }
        Spacer(Modifier.height(6.dp))
        Button(
            onClick = {
                sound.play("deal")
                mainAction()
            },
            enabled = state.phase != Phase.SPINNING && state.phase != Phase.DRAWING,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFC62828),
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(mainLabel, fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }
        if (state.credits < state.totalBet) {
            TextButton(
                onClick = { sound.play("coin"); viewModel.refillCredits() },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.free_credits), color = Gold) }
        }
    }
}

@Composable
private fun PaytableDialog(betPerLine: Int, onDismiss: () -> Unit) {
    Surface(
        color = Color(0xFF15263A),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.paytable),
                color = Gold,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            PokerHand.PAYTABLE_ORDER.forEach { hand ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(hand.label, color = Color.White, fontSize = 13.sp)
                    Text(
                        (hand.payout * betPerLine).toString() + " ✦",
                        color = WinGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                "Mise / ligne : " + betPerLine + " ✦ · 10 lignes",
                color = Color(0xFF9FB3C8),
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) { Text(stringResource(R.string.close)) }
        }
    }
}
