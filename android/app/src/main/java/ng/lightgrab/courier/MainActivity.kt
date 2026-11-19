package ng.lightgrab.courier

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import ng.lightgrab.courier.core.designsystem.Brand
import ng.lightgrab.courier.core.designsystem.LightGrabTheme
import ng.lightgrab.courier.ui.AppRoot

/** Single-activity, fully native Compose app. No WebView anywhere. */
class MainActivity : ComponentActivity() {
    private val container get() = (application as LightGrabApp).container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Brand.Cream.toArgb(), Brand.Cream.toArgb()),
        )
        container.handleIntent(intent)
        setContent { LightGrabTheme { AppRoot(container) } }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        container.handleIntent(intent)
    }
}
