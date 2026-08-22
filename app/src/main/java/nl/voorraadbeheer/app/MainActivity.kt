package nl.voorraadbeheer.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import nl.voorraadbeheer.app.ui.navigation.AppRoot
import nl.voorraadbeheer.app.ui.theme.VoorraadbeheerTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoorraadbeheerTheme {
                AppRoot()
            }
        }
    }
}
