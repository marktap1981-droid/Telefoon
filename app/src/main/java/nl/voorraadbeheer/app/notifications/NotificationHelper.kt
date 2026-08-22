package nl.voorraadbeheer.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import nl.voorraadbeheer.app.MainActivity
import nl.voorraadbeheer.app.R

object NotificationHelper {
    const val CHANNEL_ID = "voorraad_meldingen"
    private const val LOW_STOCK_NOTIFICATION_ID = 1001
    private const val EXPIRY_NOTIFICATION_ID = 1002

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.widget_low_stock_label),
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    fun showLowStockNotification(context: Context, count: Int) {
        if (count <= 0) return
        show(
            context,
            id = LOW_STOCK_NOTIFICATION_ID,
            title = context.getString(R.string.dashboard_low_stock),
            text = "$count product(en) hebben een lage voorraad.",
        )
    }

    fun showExpiryNotification(context: Context, count: Int) {
        if (count <= 0) return
        show(
            context,
            id = EXPIRY_NOTIFICATION_ID,
            title = context.getString(R.string.dashboard_expiring_soon),
            text = "$count product(en) zijn bijna houdbaar tot.",
        )
    }

    private fun show(context: Context, id: Int, title: String, text: String) {
        val intent = android.content.Intent(context, MainActivity::class.java)
        val pendingIntent = android.app.PendingIntent.getActivity(
            context, id, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS,
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(id, notification)
        }
    }
}
