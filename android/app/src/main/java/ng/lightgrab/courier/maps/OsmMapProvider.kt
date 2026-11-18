package ng.lightgrab.courier.maps

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

/** Native OpenStreetMap view (osmdroid). Shows the destination pin and, with permission, the courier. */
class OsmMapProvider : MapProvider {
    override val name = "OpenStreetMap"
    override val supportsRoutes = false

    @Composable
    override fun MiniMap(destination: Destination, showCourier: Boolean, modifier: Modifier) {
        val context = LocalContext.current
        val mapView = remember {
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(16.0)
            }
        }
        DisposableEffect(mapView) {
            mapView.onResume()
            onDispose { mapView.onPause(); mapView.onDetach() }
        }
        Box(modifier.clip(RoundedCornerShape(16.dp))) {
            AndroidView(
                factory = { mapView },
                update = { view ->
                    view.overlays.clear()
                    val point = GeoPoint(destination.lat, destination.lng)
                    view.overlays.add(
                        Marker(view).apply {
                            position = point
                            title = destination.label
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        },
                    )
                    if (showCourier) {
                        view.overlays.add(MyLocationNewOverlay(GpsMyLocationProvider(view.context), view).apply { enableMyLocation() })
                    }
                    view.controller.setCenter(point)
                    view.invalidate()
                },
            )
        }
    }
}
