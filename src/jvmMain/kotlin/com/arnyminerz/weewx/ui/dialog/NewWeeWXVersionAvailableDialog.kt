package com.arnyminerz.weewx.ui.dialog

import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NewWeeWXVersionAvailableDialog(
    currentVersion: String,
    latestVersion: String,
    onDismissRequest: () -> Unit,
    onUpdateRequested: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Nova versió disponible") },
        text = {
            Text(
                "Hi ha una nova versió de WeeWX disponible al servidor. Ara mateix està instal·lada la versió %s, i l'última disponible és la %s.\nVols actualitzar ara?"
                    .format(currentVersion, latestVersion)
            )
        },
        confirmButton = {
            TextButton(
                onClick = onUpdateRequested
            ) { Text("Actualitza") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) { Text("Cancel·lar") }
        }
    )
}
