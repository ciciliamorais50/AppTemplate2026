package com.ifpr.androidapptemplate.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ifpr.androidapptemplate.MainActivity
import com.ifpr.androidapptemplate.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val status = remoteMessage.data["status"] ?: return
        sendStatusNotification(status)
    }

    private fun sendNotification(message: String?) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val channelId = "status_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Status do Pedido", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle("Nova mensagem")
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(1000, builder.build())
    }

    private fun sendStatusNotification(status: String) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val channelId = "status_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Status do Pedido", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val targetProgress = when (status) {
            "em preparo" -> 33
            "saindo para entrega" -> 66
            "entregue" -> 100
            else -> 0
        }

        val remoteViews = RemoteViews(packageName, R.layout.custom_notification)
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setCustomContentView(remoteViews)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)

        val notificationId = 1001
        var currentProgress = 0

        val handler = android.os.Handler(mainLooper)
        val updateRunnable = object : Runnable {
            override fun run() {
                if (currentProgress <= targetProgress) {
                    remoteViews.setTextViewText(R.id.status_text, "Pedido: $status")
                    remoteViews.setProgressBar(R.id.progress_bar, 100, currentProgress, false)
                    notificationManager.notify(notificationId, builder.build())
                    currentProgress++
                    handler.postDelayed(this, 50L)
                }
            }
        }

        handler.post(updateRunnable)

        if (targetProgress == 100) {
            handler.postDelayed({
                notificationManager.cancel(notificationId)
            }, 3000)
        }
    }
}