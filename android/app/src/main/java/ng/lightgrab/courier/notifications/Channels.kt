package ng.lightgrab.courier.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ng.lightgrab.courier.MainActivity
import ng.lightgrab.courier.R
import ng.lightgrab.courier.core.designsystem.Brand

/** Separate Android channels so couriers can tune sound per type. */
object Channels {
    const val ASSIGNMENTS = "assignments"
    const val CUSTOMER_UPDATES = "customer_updates"
    const val INSTRUCTIONS = "instructions"
    const val ANNOUNCEMENTS = "announcements"
    const val ACCOUNT = "account"
    const val TRACKING = "tracking"

    fun createAll(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        listOf(
            NotificationChannel(ASSIGNMENTS, "New & changed assignments", NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel(CUSTOMER_UPDATES, "Customer updates", NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel(INSTRUCTIONS, "Delivery instructions", NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel(ANNOUNCEMENTS, "Announcements", NotificationManager.IMPORTANCE_LOW),
            NotificationChannel(ACCOUNT, "Account status", NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel(TRACKING, "Active delivery tracking", NotificationManager.IMPORTANCE_LOW),
        ).forEach { nm.createNotificationChannel(it) }
    }

    fun forKind(kind: String): String = when (kind) {
        "assignment", "assignment_changed", "assignment_withdrawn" -> ASSIGNMENTS
        "customer_update" -> CUSTOMER_UPDATES
        "instruction" -> INSTRUCTIONS
        "account" -> ACCOUNT
        else -> ANNOUNCEMENTS
    }
}

object Notifier {
    fun show(context: Context, id: String, kind: String, title: String, body: String, assignmentId: String?) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val link = if (assignmentId != null) "lightgrab://order/$assignmentId" else "lightgrab://notifications"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link), context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(context, id.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val n = NotificationCompat.Builder(context, Channels.forKind(kind))
            .setSmallIcon(R.drawable.ic_stat_lightgrab)
            .setColor(Brand.Forest.toArgb())
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(id.hashCode(), n) }
    }
}
