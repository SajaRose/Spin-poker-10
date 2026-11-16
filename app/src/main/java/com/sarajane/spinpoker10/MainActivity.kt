package com.sarajane.spinpoker10

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import com.sarajane.spinpoker10.ui.GameScreen
import com.sarajane.spinpoker10.ui.theme.SpinPokerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpinPokerTheme {
                Surface(color = Color(0xFF0D1B2A)) {
                    GameScreen()
                }
            }
        }
    }
}
