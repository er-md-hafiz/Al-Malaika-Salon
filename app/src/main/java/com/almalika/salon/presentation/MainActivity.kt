package com.almalika.salon.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.almalika.salon.presentation.main.MainAppShell
import com.almalika.salon.presentation.theme.AlMalaikaSalonTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlMalaikaSalonTheme {
                MainAppShell()
            }
        }
    }
}
