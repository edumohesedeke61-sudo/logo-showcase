package ng.lightgrab.courier

import android.app.Application
import ng.lightgrab.courier.notifications.Channels
import ng.lightgrab.courier.notifications.Push
import org.osmdroid.config.Configuration

class LightGrabApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().userAgentValue = packageName
        Channels.createAll(this)
        Push.init(this)
        container = AppContainer(this)
    }
}
