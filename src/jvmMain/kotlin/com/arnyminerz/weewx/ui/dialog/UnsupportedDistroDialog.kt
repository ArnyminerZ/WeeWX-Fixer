package com.arnyminerz.weewx.ui.dialog

import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
@ExperimentalMaterialApi
fun UnsupportedDistroDialog(distro: String, onCloseRequested: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCloseRequested,
        title = { Text("Distribució no suportada") },
        text = { Text("La distribució que executa el servidor ($distro) no està suportada.") },
        confirmButton = {
            TextButton(
                onClick = onCloseRequested
            ) { Text("Canviar perfil") }
        }
    )
}
