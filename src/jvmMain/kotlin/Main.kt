import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arnyminerz.weewx.ui.screens.ConfigLoadScreen
import com.arnyminerz.weewx.ui.theme.AppTheme

@Composable
@Preview
fun App() {
    AppTheme {
        ConfigLoadScreen()
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "WeeWX Fixer"
    ) {
        App()
    }
}
