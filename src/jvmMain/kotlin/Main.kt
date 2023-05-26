import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arnyminerz.weewx.ui.screens.ConfigLoadScreen
import com.arnyminerz.weewx.ui.theme.AppTheme
import com.arnyminerz.weewx.updates.UpdateChecker
import com.arnyminerz.weewx.updates.WeeWX
import com.arnyminerz.weewx.utils.doAsync

@Composable
@Preview
fun App() {
    AppTheme {
        ConfigLoadScreen()
    }
}

fun main() = application {
    doAsync {
        UpdateChecker.checkForUpdates()
        WeeWX.getReleasedVersions()
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "WeeWX Fixer",
        icon = painterResource("Weewx.png")
    ) {
        App()
    }
}
