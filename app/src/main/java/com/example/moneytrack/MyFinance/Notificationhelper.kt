//NotificationHelper
package com.example.moneytrack.ui

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.moneytrack.R

/**
 * แจ้งเตือนในระบบ (บันทึกรายการสำเร็จ + แจ้งเตือนการใช้เงิน)
 * ต้องมีใน AndroidManifest.xml:
 *   <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
 */
object NotificationHelper {
    private const val CHANNEL_SAVED = "transaction_saved"
    private const val CHANNEL_ALERT = "spending_alerts"
    const val PERMISSION = "android.permission.POST_NOTIFICATIONS"

    /** Android 13+ ต้องขอสิทธิ์ก่อน รุ่นต่ำกว่านั้นถือว่าได้สิทธิ์เสมอ */
    fun hasPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(context, PERMISSION) ==
                PackageManager.PERMISSION_GRANTED

    fun needsRuntimePermission(): Boolean = Build.VERSION.SDK_INT >= 33

    /** แจ้งเตือนเมื่อบันทึกรายการสำเร็จ */
    fun notifySaved(context: Context, title: String, text: String) {
        post(
            context = context,
            channelId = CHANNEL_SAVED,
            channelName = "แจ้งเตือนการบันทึกรายการ",
            channelDesc = "แจ้งเตือนเมื่อบันทึกรายรับ-รายจ่ายสำเร็จ",
            notificationId = System.currentTimeMillis().toInt(),
            title = title,
            text = text
        )
    }

    /** แจ้งเตือนการใช้เงิน เช่น ใช้ครบวงเงินรายวัน / เงินเหลือน้อย */
    fun notifyAlert(context: Context, id: Int, title: String, text: String) {
        post(
            context = context,
            channelId = CHANNEL_ALERT,
            channelName = "แจ้งเตือนการใช้เงิน",
            channelDesc = "เตือนเมื่อใช้เงินครบกำหนดหรือเงินเหลือน้อย",
            notificationId = id,
            title = title,
            text = text
        )
    }

    // แก้ error: lint มองไม่เห็นว่าเช็คสิทธิ์ผ่าน hasPermission() แล้ว
    // จึงต้อง suppress (เช็คจริงอยู่บรรทัดแรกของฟังก์ชัน + มี try/catch กันไว้อีกชั้น)
    @SuppressLint("MissingPermission")
    private fun post(
        context: Context,
        channelId: String,
        channelName: String,
        channelDesc: String,
        notificationId: Int,
        title: String,
        text: String
    ) {
        if (!hasPermission(context)) return

        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = channelDesc }
            context.getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = launchIntent?.let {
            PendingIntent.getActivity(
                context,
                0,
                it,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setColor(0xFFE65E8C.toInt())
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // ผู้ใช้ปิดสิทธิ์แจ้งเตือนไว้ ข้ามไป
        }
    }
}