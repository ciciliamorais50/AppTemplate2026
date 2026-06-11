package com.ifpr.androidapptemplate.ui.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ifpr.androidapptemplate.MainActivity
import com.ifpr.androidapptemplate.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMsgService"
        private const val CHANNEL_ID = "spectra_aprovacao"

        fun monitorarPendentes(context: Context) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val db = FirebaseFirestore.getInstance()

            db.collection("usuarios").document(uid).get()
                .addOnSuccessListener { doc ->
                    val papel = doc.getString("papel") ?: "cadastrador"
                    if (papel != "aprovador") return@addOnSuccessListener

                    db.collection("materiais")
                        .whereEqualTo("status", "pendente")
                        .addSnapshotListener { snap, _ ->
                            val quantidade = snap?.documents?.size ?: 0
                            if (quantidade > 0) {
                                dispararNotificacao(context, quantidade)
                            }
                        }
                }
        }

        private fun dispararNotificacao(context: Context, quantidade: Int) {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("nav_to", "notifications")
            }

            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val texto = if (quantidade == 1)
                "1 material aguardando sua aprovação"
            else
                "$quantidade materiais aguardando sua aprovação"

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Aprovações Spectra",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Materiais pendentes de aprovação"
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val notificacao = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_spectra)
                .setContentTitle("Spectra — Material Pendente")
                .setContentText(texto)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            try {
                notificationManager.notify(2001, notificacao)
            } catch (e: SecurityException) {
                Log.e(TAG, "Permissão de notificação negada")
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        remoteMessage.notification?.let {
            dispararNotificacao(this, 1)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
    }
}