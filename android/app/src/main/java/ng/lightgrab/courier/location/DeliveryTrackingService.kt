package ng.lightgrab.courier.location

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch
import ng.lightgrab.courier.LightGrabApp
import ng.lightgrab.courier.R
import ng.lightgrab.courier.notifications.Channels

/**
 * Visible foreground service that shares the courier's location with Office
 * only while they are travelling to a destination. No background location.
 */
class DeliveryTrackingService : LifecycleService() {

    private val fused by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private var assignmentId: String? = null

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val id = assignmentId ?: return
            val loc = result.lastLocation ?: return
            val repo = (application as LightGrabApp).container.repository
            lifecycleScope.launch {
                runCatching { repo.recordLocation(id, loc.latitude, loc.longitude, if (loc.hasAccuracy()) loc.accuracy else null) }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent?.action == ACTION_STOP) {
            stopTracking(); return START_NOT_STICKY
        }
        val id = intent?.getStringExtra(EXTRA_ASSIGNMENT) ?: return START_NOT_STICKY
        val orderNumber = intent.getStringExtra(EXTRA_ORDER_NUMBER) ?: ""
        if (!LocationClient(this).hasPermission()) { stopSelf(); return START_NOT_STICKY }
        assignmentId = id
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION else 0
        ServiceCompat.startForeground(this, NOTIFICATION_ID, buildNotification(orderNumber), type)
        fused.removeLocationUpdates(callback)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 20_000L).setMinUpdateDistanceMeters(25f).build()
        fused.requestLocationUpdates(request, callback, Looper.getMainLooper())
        return START_NOT_STICKY
    }

    private fun buildNotification(orderNumber: String): Notification =
        NotificationCompat.Builder(this, Channels.TRACKING)
            .setSmallIcon(R.drawable.ic_stat_lightgrab)
            .setContentTitle("On delivery $orderNumber")
            .setContentText("Sharing your location with LightGrab Office until you arrive")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    private fun stopTracking() {
        fused.removeLocationUpdates(callback)
        assignmentId = null
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        fused.removeLocationUpdates(callback)
        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_ID = 4101
        private const val ACTION_STOP = "ng.lightgrab.courier.STOP_TRACKING"
        private const val EXTRA_ASSIGNMENT = "assignment_id"
        private const val EXTRA_ORDER_NUMBER = "order_number"

        fun start(context: Context, assignmentId: String, orderNumber: String) {
            if (!LocationClient(context).hasPermission()) return
            val i = Intent(context, DeliveryTrackingService::class.java)
                .putExtra(EXTRA_ASSIGNMENT, assignmentId)
                .putExtra(EXTRA_ORDER_NUMBER, orderNumber)
            ContextCompat.startForegroundService(context, i)
        }

        fun stop(context: Context) {
            context.startService(Intent(context, DeliveryTrackingService::class.java).setAction(ACTION_STOP))
        }
    }
}
