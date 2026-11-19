package ng.lightgrab.courier.notifications

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import ng.lightgrab.courier.BuildConfig

/** Firebase is optional: it is only started when its values are set in local.properties. */
object Push {
    val configured: Boolean get() = BuildConfig.FIREBASE_APP_ID.isNotEmpty() && BuildConfig.FIREBASE_API_KEY.isNotEmpty()

    fun init(context: Context) {
        if (!configured || FirebaseApp.getApps(context).isNotEmpty()) return
        val options = FirebaseOptions.Builder()
            .setApplicationId(BuildConfig.FIREBASE_APP_ID)
            .setApiKey(BuildConfig.FIREBASE_API_KEY)
            .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
            .setGcmSenderId(BuildConfig.FIREBASE_SENDER_ID)
            .build()
        FirebaseApp.initializeApp(context, options)
    }

    suspend fun token(context: Context): String? {
        if (!configured || FirebaseApp.getApps(context).isEmpty()) return null
        return runCatching { FirebaseMessaging.getInstance().token.await() }.getOrNull()
    }
}
