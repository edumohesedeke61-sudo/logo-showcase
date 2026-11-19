package ng.lightgrab.courier

import android.app.Application
import android.content.Intent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import ng.lightgrab.courier.calling.CallClient
import ng.lightgrab.courier.calling.LiveKitCallClient
import ng.lightgrab.courier.data.CourierRepository
import ng.lightgrab.courier.data.DemoCourierRepository
import ng.lightgrab.courier.data.SupabaseCourierRepository
import ng.lightgrab.courier.location.DeliveryTrackingService
import ng.lightgrab.courier.location.LocationClient
import ng.lightgrab.courier.maps.MapProvider
import ng.lightgrab.courier.maps.OsmMapProvider

/** Simple app-wide dependency container (one instance, created in LightGrabApp). */
class AppContainer(private val app: Application) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val backendConfigured: Boolean =
        BuildConfig.SUPABASE_URL.startsWith("https://") && BuildConfig.SUPABASE_PUBLISHABLE_KEY.isNotEmpty()

    private val supabase: SupabaseClient? = if (!backendConfigured) null else createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY,
    ) {
        defaultSerializer = KotlinXSerializer(Json { ignoreUnknownKeys = true; encodeDefaults = true })
        install(Auth) {
            scheme = "lightgrab"
            host = "auth"
        }
        install(Postgrest)
        install(Realtime)
        install(Storage)
        install(Functions)
    }

    val repository: CourierRepository = supabase?.let { SupabaseCourierRepository(it) } ?: DemoCourierRepository()
    val location = LocationClient(app)
    val maps: MapProvider = OsmMapProvider()
    val calls: CallClient = LiveKitCallClient(app)

    private val _accessRefresh = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val accessRefresh: SharedFlow<Unit> = _accessRefresh
    fun requestAccessRefresh() { _accessRefresh.tryEmit(Unit) }

    /** True after the courier opens a password-reset link: show "Set a new password". */
    val recoveryMode = MutableStateFlow(false)

    /** lightgrab://order/{id} or lightgrab://notifications waiting to be opened. */
    val pendingDeepLink = MutableStateFlow<String?>(null)

    fun handleIntent(intent: Intent) {
        val data = intent.data ?: return
        if (data.scheme != "lightgrab") return
        if (data.host == "auth") {
            supabase?.handleDeeplinks(intent)
            if (data.path?.contains("recovery") == true || data.fragment?.contains("type=recovery") == true) recoveryMode.value = true
        } else {
            pendingDeepLink.value = data.toString()
        }
    }

    /** Called on suspension or sign-out: nothing keeps running on the device. */
    fun stopActiveWork() {
        DeliveryTrackingService.stop(app)
        scope.launch { calls.hangUp("Call ended") }
    }
}
