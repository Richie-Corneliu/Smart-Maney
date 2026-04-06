package com.kelompok4.smartmaney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.kelompok4.smartmaney.navigation.AppNavHost
import com.kelompok4.smartmaney.ui.theme.SmartManeyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartManeyTheme {
                AppNavHost(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
