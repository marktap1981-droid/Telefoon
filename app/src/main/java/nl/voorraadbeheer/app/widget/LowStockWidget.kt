package nl.voorraadbeheer.app.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import nl.voorraadbeheer.app.data.model.InventoryItem
import nl.voorraadbeheer.app.util.daysUntil

class LowStockWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val (lowStock, expiringSoon) = fetchCounts()
        provideContent {
            Column(
                modifier = androidx.glance.GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(androidx.compose.ui.graphics.Color(0xFF2E7D32)))
                    .padding(12.dp),
            ) {
                Text(
                    "Voorraadbeheer",
                    style = TextStyle(color = ColorProvider(androidx.compose.ui.graphics.Color.White), fontWeight = FontWeight.Bold),
                )
                Text(
                    "Lage voorraad: $lowStock",
                    style = TextStyle(color = ColorProvider(androidx.compose.ui.graphics.Color.White)),
                )
                Text(
                    "Bijna verlopen: $expiringSoon",
                    style = TextStyle(color = ColorProvider(androidx.compose.ui.graphics.Color.White)),
                )
            }
        }
    }

    private suspend fun fetchCounts(): Pair<Int, Int> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return 0 to 0
        val firestore = FirebaseFirestore.getInstance()
        val householdId = runCatching {
            firestore.collection("users").document(uid).get().await().getString("householdId")
        }.getOrNull() ?: return 0 to 0
        val snapshot = runCatching {
            firestore.collection("households").document(householdId).collection("inventoryItems")
                .get().await()
        }.getOrNull() ?: return 0 to 0

        val items = snapshot.toObjects(InventoryItem::class.java)
        val lowStock = items.count { it.isLowStock }
        val expiringSoon = items.count { item ->
            val expiry = item.expiryDate ?: return@count false
            expiry.daysUntil() in 0..3
        }
        return lowStock to expiringSoon
    }

    companion object {
        suspend fun updateAll(context: Context) {
            val widget = LowStockWidget()
            val manager = GlanceAppWidgetManager(context)
            manager.getGlanceIds(LowStockWidget::class.java).forEach { id ->
                widget.update(context, id)
            }
        }
    }
}
