package io.iskopasi.githubobserverclient.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.iskopasi.githubobserverclient.MainActivity
import io.iskopasi.githubobserverclient.R
import io.iskopasi.githubobserverclient.models.SearchStatus
import io.iskopasi.githubobserverclient.repo.Repo
import io.iskopasi.simplymotion.utils.CommunicatorCallback
import io.iskopasi.simplymotion.utils.ServiceCommunicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class DLService() : LifecycleService() {
    @Inject
    lateinit var repo: Repo

    private val pendingIntent by lazy {
        PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    private val commandHandler: CommunicatorCallback = { data, obj, comm ->
    }
    private val serviceCommunicator = ServiceCommunicator("Service", commandHandler)

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)

        return serviceCommunicator.onBind()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val owner = intent?.getStringExtra("owner") ?: ""
        val repoName = intent?.getStringExtra("repoName") ?: ""
        val defaultBranch = intent?.getStringExtra("defaultBranch") ?: ""

        showNotification(repoName)

        lifecycleScope.launch(Dispatchers.IO) {
            serviceCommunicator.sendMsg(SearchStatus.Loading.name)
            val result = repo.downloadZip(owner, repoName, defaultBranch)

            // Parse response
            serviceCommunicator.sendMsg(SearchStatus.Idle.name)

            stopSelf()
        }.invokeOnCompletion { handler ->
            if (handler != null) {
                handler.printStackTrace()
                serviceCommunicator.sendMsg(
                    SearchStatus.Error.name,
                    "DL Error -> ${handler.message}"
                )
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun showNotification(repoName: String) {
        val notificationId = 123
        val channelId = getChannel()
        val notification = NotificationCompat.Builder(this, channelId).apply {
            setSmallIcon(R.mipmap.ic_launcher_round)
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setCategory(NotificationCompat.CATEGORY_SERVICE)
            setContentTitle("Downloading repository...")
            setContentText("Downloading $repoName...")
            setContentIntent(pendingIntent)
            setOngoing(true)
            setAutoCancel(false)
        }.build()

        ServiceCompat.startForeground(
            this,
            notificationId,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else 0
        )
    }

    private fun getChannel(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "go_channel"
            val channelName = "go_channel"

            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                lightColor = Color.RED
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            ContextCompat.getSystemService(this, NotificationManager::class.java)!!
                .createNotificationChannel(channel)

            channelId
        } else {
            ""
        }
    }
}