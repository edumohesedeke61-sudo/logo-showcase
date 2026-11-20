package ng.lightgrab.courier.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ng.lightgrab.courier.AppContainer
import ng.lightgrab.courier.core.designsystem.Brand
import ng.lightgrab.courier.core.designsystem.DemoBanner
import ng.lightgrab.courier.core.designsystem.LoadingScreen
import ng.lightgrab.courier.core.model.AccessState
import ng.lightgrab.courier.feature.auth.ErrorScreen
import ng.lightgrab.courier.feature.auth.PendingScreen
import ng.lightgrab.courier.feature.auth.ResetPasswordScreen
import ng.lightgrab.courier.feature.auth.SetNewPasswordScreen
import ng.lightgrab.courier.feature.auth.SignInScreen
import ng.lightgrab.courier.feature.auth.SuspendedScreen
import ng.lightgrab.courier.feature.onboarding.ApplyScreen
import ng.lightgrab.courier.feature.onboarding.CreateAccountScreen

@Composable
fun AppRoot(container: AppContainer) {
    val vm: AppViewModel = viewModel { AppViewModel(container) }
    val access by vm.access.collectAsStateWithLifecycle()
    val recovery by container.recoveryMode.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(Brand.Navy).statusBarsPadding()) {
        if (container.repository.isDemo) DemoBanner()
        Box(Modifier.weight(1f).background(Brand.Cream).navigationBarsPadding().imePadding()) {
            when (val a = access) {
                AccessState.Loading -> LoadingScreen()
                AccessState.SignedOut -> SignedOutFlow(container)
                AccessState.NeedsApplication -> ApplyScreen(container.repository, onSubmitted = vm::refresh, onSignOut = vm::signOut)
                is AccessState.InReview -> PendingScreen(a, onRefresh = vm::refresh, onSignOut = vm::signOut)
                is AccessState.Suspended -> SuspendedScreen(a.reason, onSignOut = vm::signOut)
                is AccessState.Error -> ErrorScreen(a.message, onRetry = vm::refresh, onSignOut = vm::signOut)
                is AccessState.Active ->
                    if (recovery) SetNewPasswordScreen(container.repository) { container.recoveryMode.value = false }
                    else CourierApp(container, a.courier, onSignOut = vm::signOut)
            }
        }
    }
}

@Composable
private fun SignedOutFlow(container: AppContainer) {
    var screen by rememberSaveable { mutableStateOf("sign_in") }
    BackHandler(enabled = screen != "sign_in") { screen = "sign_in" }
    when (screen) {
        "reset" -> ResetPasswordScreen(container.repository) { screen = "sign_in" }
        "apply" -> CreateAccountScreen(container.repository) { screen = "sign_in" }
        else -> SignInScreen(container.repository, onForgot = { screen = "reset" }, onApply = { screen = "apply" })
    }
}
