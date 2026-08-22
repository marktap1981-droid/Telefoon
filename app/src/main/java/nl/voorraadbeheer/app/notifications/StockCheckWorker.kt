package nl.voorraadbeheer.app.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import nl.voorraadbeheer.app.data.repository.InventoryRepository
import nl.voorraadbeheer.app.data.repository.UserPreferencesRepository
import nl.voorraadbeheer.app.util.daysUntil
import nl.voorraadbeheer.app.widget.LowStockWidget
import java.util.concurrent.TimeUnit

@HiltWorker
class StockCheckWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val inventoryRepository: InventoryRepository,
    private val preferencesRepository: UserPreferencesRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val prefs = preferencesRepository.preferences.first()
        if (!prefs.notificationsEnabled) return Result.success()

        val items = runCatching { inventoryRepository.observeAllItems().first() }.getOrNull()
            ?: return Result.success()

        val lowStockCount = items.count { it.isLowStock }
        val expiringSoonCount = items.count { item ->
            val expiry = item.expiryDate ?: return@count false
            expiry.daysUntil() in 0..prefs.expiryReminderDays.toLong()
        }

        NotificationHelper.showLowStockNotification(applicationContext, lowStockCount)
        NotificationHelper.showExpiryNotification(applicationContext, expiringSoonCount)
        runCatching { LowStockWidget.updateAll(applicationContext) }

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "stock_check_worker"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = PeriodicWorkRequestBuilder<StockCheckWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
