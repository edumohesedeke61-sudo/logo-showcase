package ng.lightgrab.courier.maps

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

data class Destination(val lat: Double, val lng: Double, val label: String)

/**
 * Replaceable map contract. The app only talks to this interface, so the
 * provider (OpenStreetMap today; Google Maps or MapLibre later) can change
 * without touching any screen.
 */
interface MapProvider {
    val name: String

    /** True only when the provider returns real routes. The app never draws invented routes or distances. */
    val supportsRoutes: Boolean

    @Composable
    fun MiniMap(destination: Destination, showCourier: Boolean, modifier: Modifier)

    /** Hands off turn-by-turn navigation to an installed maps app. */
    fun navigate(context: Context, destination: Destination) {
        val nav = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=${destination.lat},${destination.lng}"))
        val geo = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("geo:${destination.lat},${destination.lng}?q=${destination.lat},${destination.lng}(${Uri.encode(destination.label)})"),
        )
        try {
            context.startActivity(nav.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: ActivityNotFoundException) {
            try { context.startActivity(geo.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: ActivityNotFoundException) { }
        }
    }
}
