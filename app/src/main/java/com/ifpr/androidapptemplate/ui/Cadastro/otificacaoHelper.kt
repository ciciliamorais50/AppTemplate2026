package com.ifpr.androidapptemplate.ui.cadastro

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ifpr.androidapptemplate.MainActivity
import com.ifpr.androidapptemplate.R

object NotificacaoHelper {

    private const val CHANNEL_ID = "spectra_aprovacao"
    private const val CHANNEL_NAME = "Aprovações de Material"

    fun criarCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificações de materiais pendentes de aprovação"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun notificarPendentes(context: Context, quantidade: Int) {
        if (quantidade == 0) return

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

        val notificacao = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_spectra)
            .setContentTitle("Spectra — Pendências")
            .setContentText(texto)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(1001, notificacao)
        } catch (e: SecurityException) {
            // Permissão não concedida
        }
    }
}