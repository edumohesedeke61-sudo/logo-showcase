package ng.lightgrab.courier.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ng.lightgrab.courier.AppContainer
import ng.lightgrab.courier.core.designsystem.Brand
import ng.lightgrab.courier.core.model.CourierProfile
import ng.lightgrab.courier.feature.call.CallScreen
import ng.lightgrab.courier.feature.dashboard.DashboardScreen
import ng.lightgrab.courier.feature.delivery.HandoverScreen
import ng.lightgrab.courier.feature.delivery.ReportIssueScreen
import ng.lightgrab.courier.feature.notifications.NotificationsScreen
import ng.lightgrab.courier.feature.orders.HistoryScreen
import ng.lightgrab.courier.feature.orders.OrderDetailScreen
import ng.lightgrab.courier.feature.profile.ProfileScreen
import ng.lightgrab.courier.notifications.Notifier
import ng.lightgrab.courier.notifications.Push

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val TABS = listOf(
    Tab("home", "Home", Icons.Filled.Home),
    Tab("history", "History", Icons.Filled.History),
    Tab("alerts", "Alerts", Icons.Filled.Notifications),
    Tab("profile", "Profile", Icons.Filled.Person),
)

/** Everything an approved, active courier can reach. */
@Composable
fun CourierApp(container: AppContainer, courier: CourierProfile, onSignOut: () -> Unit) {
    val context = LocalContext.current
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    var unread by remember { mutableIntStateOf(0) }
    val deepLink by container.pendingDeepLink.collectAsStateWithLifecycle()

    val notifPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        Push.token(context)?.let { runCatching { container.repository.registerDeviceToken(it) } }
        unread = runCatching { container.repository.notifications().count { !it.read } }.getOrDefault(0)
    }
    // Live inbox. When push is configured the push message already shows the alert.
    LaunchedEffect(Unit) {
        runCatching {
            container.repository.newNotifications().collect { n ->
                unread++
                if (!Push.configured) Notifier.show(context, n.id, n.kind, n.title, n.body, n.assignmentId)
            }
        }
    }
    LaunchedEffect(deepLink) {
        val link = deepLink ?: return@LaunchedEffect
        container.pendingDeepLink.value = null
        when {
            link.startsWith("lightgrab://order/") -> nav.navigate("order/${link.removePrefix("lightgrab://order/").substringBefore('#')}")
            link.startsWith("lightgrab://notifications") -> nav.navigate("alerts")
        }
    }

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            NavHost(nav, startDestination = "home") {
                composable("home") {
                    DashboardScreen(container, courier, onOpenOrder = { nav.navigate("order/$it") })
                }
                composable("history") { HistoryScreen(container) }
                composable("alerts") {
                    NotificationsScreen(container, onOpenOrder = { nav.navigate("order/$it") }, onRead = { unread = (unread - 1).coerceAtLeast(0) })
                }
                composable("profile") { ProfileScreen(container, courier, onSignOut) }
                composable("order/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) { e ->
                    val id = e.arguments?.getString("id").orEmpty()
                    OrderDetailScreen(
                        container, id,
                        onBack = { nav.popBackStack() },
                        onCall = { nav.navigate("call/$id") },
                        onHandover = { nav.navigate("handover/$id") },
                        onReportIssue = { nav.navigate("issue/$id") },
                    )
                }
                composable("handover/{id}") { e ->
                    HandoverScreen(container, e.arguments?.getString("id").orEmpty(), onBack = { nav.popBackStack() }, onDone = {
                        nav.popBackStack("home", inclusive = false)
                    })
                }
                composable("issue/{id}") { e ->
                    ReportIssueScreen(container, e.arguments?.getString("id").orEmpty(), onBack = { nav.popBackStack() }, onDone = {
                        nav.popBackStack("home", inclusive = false)
                    })
                }
                composable("call/{id}") { e ->
                    CallScreen(container, e.arguments?.getString("id").orEmpty(), onClose = { nav.popBackStack() })
                }
            }
        }
        if (TABS.any { it.route == route }) {
            NavigationBar(containerColor = Brand.White) {
                TABS.forEach { tab ->
                    NavigationBarItem(
                        selected = route == tab.route,
                        onClick = {
                            nav.navigate(tab.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            if (tab.route == "alerts" && unread > 0) BadgedBox(badge = { Badge { Text("$unread") } }) { Icon(tab.icon, null) }
                            else Icon(tab.icon, null)
                        },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Brand.Navy, selectedTextColor = Brand.Navy, indicatorColor = Brand.Leaf,
                            unselectedIconColor = Brand.Muted, unselectedTextColor = Brand.Muted,
                        ),
                    )
                }
            }
        }
    }
}
