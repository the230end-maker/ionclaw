package com.ionclaw.lib

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicInteger

// bridges the native invoke_platform tool to android platform features.
// the agent triggers local-notification.send and the core forwards it here via the async jni callback.
// each request must be answered exactly once through ionclaw_platform_respond.
object IonClawPlatform {
    private const val CHANNEL_ID = "ionclaw_notifications"
    private const val TIMEOUT_SECONDS = 30

    private lateinit var appContext: Context
    private val notificationId = AtomicInteger(1)

    // registers the async platform handler with the native core. safe to call once at launch.
    fun register(context: Context) {
        appContext = context.applicationContext
        createNotificationChannel()
        IonClawNative.nativeSetPlatformHandler(TIMEOUT_SECONDS)
    }

    // entry point invoked from the native jni bridge on a core thread.
    @JvmStatic
    fun dispatch(requestId: Long, function: String, paramsJson: String) {
        when (function) {
            "local-notification.send" -> sendLocalNotification(requestId, paramsJson)
            else -> respond(requestId, "Error: '$function' is not implemented on android.")
        }
    }

    @SuppressLint("MissingPermission")
    private fun sendLocalNotification(requestId: Long, paramsJson: String) {
        val manager = NotificationManagerCompat.from(appContext)

        // the user can revoke notifications at any time, so the request must fail cleanly
        if (!manager.areNotificationsEnabled()) {
            respond(requestId, "Error: notifications not authorized by the user")
            return
        }

        val params = JSONObject(paramsJson)
        val title = params.optString("title", "IonClaw")
        val body = params.optString("message", "")

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .build()

        manager.notify(notificationId.getAndIncrement(), notification)
        respond(requestId, "OK")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            CHANNEL_ID,
            "IonClaw",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        appContext.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun respond(requestId: Long, result: String) {
        IonClawNative.nativePlatformRespond(requestId, result)
    }
}
