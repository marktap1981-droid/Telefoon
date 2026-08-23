package nl.voorraadbeheer.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import nl.voorraadbeheer.app.ui.navigation.AppRoot
import nl.voorraadbeheer.app.ui.theme.VoorraadbeheerTheme
import nl.voorraadbeheer.app.widget.LowStockWidget

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* geen actie nodig, gebruiker heeft gekozen */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        setContent {
            VoorraadbeheerTheme {
                AppRoot()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Widget verversen zodra de gebruiker de app verlaat, i.p.v. te wachten op de dagelijkse achtergrondcheck.
        lifecycleScope.launch { runCatching { LowStockWidget.updateAll(applicationContext) } }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
