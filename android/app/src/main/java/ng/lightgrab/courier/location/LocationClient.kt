package ng.lightgrab.courier.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

data class Fix(val lat: Double, val lng: Double, val accuracyM: Float?)

class LocationClient(private val context: Context) {
    private val fused = LocationServices.getFusedLocationProviderClient(context)

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    /** Best-effort current position for status changes. Returns null rather than blocking the courier. */
    @SuppressLint("MissingPermission")
    suspend fun currentFix(timeoutMs: Long = 6000): Fix? {
        if (!hasPermission()) return null
        return runCatching {
            withTimeoutOrNull(timeoutMs) {
                val cts = CancellationTokenSource()
                fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token).await()
            }?.let { Fix(it.latitude, it.longitude, if (it.hasAccuracy()) it.accuracy else null) }
        }.getOrNull()
    }
}
