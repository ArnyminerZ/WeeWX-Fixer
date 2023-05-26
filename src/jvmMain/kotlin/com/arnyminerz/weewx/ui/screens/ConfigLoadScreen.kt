package com.arnyminerz.weewx.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arnyminerz.weewx.configuration.AppConfigProvider
import com.arnyminerz.weewx.configuration.Config
import com.arnyminerz.weewx.ui.reusable.Spinner
import com.arnyminerz.weewx.updates.UpdateChecker
import com.arnyminerz.weewx.utils.DesktopUtils
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ConfigLoadScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedInstanceIndex by remember { mutableStateOf(0) }
    val instances = AppConfigProvider.listInstances()
    val instance = instances.getOrNull(selectedInstanceIndex)

    var loading by remember { mutableStateOf(false) }

    var showingFilePicker by remember { mutableStateOf(false) }
    FilePicker(showingFilePicker, fileExtensions = listOf("properties")) { path ->
        showingFilePicker = false
        // do something with path
        if (path == null) return@FilePicker

        val file = File(path.path)
        val config = Config(file)
        try {
            AppConfigProvider.importInstance(config)
        } catch (e: IllegalArgumentException) {
            scope.launch { snackbarHostState.showSnackbar("Fitxer de configuració invàlid.\nError: ${e.message}") }
        }
    }

    val newVersionAvailable by UpdateChecker.newVersionAvailable
    newVersionAvailable?.let { version ->
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                TextButton(onClick = { DesktopUtils.browse(version.url) }) {
                    Text("Descarregar")
                }
            },
            title = { Text("Nova versió disponible") },
            text = { Text("Hi ha una nova versió de l'app disponible. Per favor, actualitza.") }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            Spinner(
                instance?.name ?: "Cap",
                instances.map { it.name },
                "Instància",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                enabled = !loading
            ) { index, _ -> selectedInstanceIndex = index }

            AnimatedVisibility(instances.isEmpty()) {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { showingFilePicker = true }
                ) {
                    Text(
                        text = "No hi ha cap instància creada. Fes click per a importar",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            instance?.let { i -> InstanceScreen(i, snackbarHostState, loading) { loading = it } }
        }
    }
}

@Preview
@Composable
fun ConfigLoadScreen_Preview() {
    ConfigLoadScreen()
}
