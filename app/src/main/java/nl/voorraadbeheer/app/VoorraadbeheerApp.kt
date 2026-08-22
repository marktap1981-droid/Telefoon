package nl.voorraadbeheer.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import nl.voorraadbeheer.app.notifications.NotificationHelper
import nl.voorraadbeheer.app.notifications.StockCheckWorker
import javax.inject.Inject

@HiltAndroidApp
class VoorraadbeheerApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        StockCheckWorker.schedule(this)
    }
}
