package com.arnyminerz.weewx.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.arnyminerz.weewx.configuration.Instance
import com.arnyminerz.weewx.ui.dialog.NewWeeWXVersionAvailableDialog
import com.arnyminerz.weewx.ui.dialog.ProgressDialog
import com.arnyminerz.weewx.ui.dialog.UnsupportedDistroDialog
import com.arnyminerz.weewx.updates.UpdateChecker
import com.arnyminerz.weewx.updates.WeeWX
import com.arnyminerz.weewx.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ColumnScope.InstanceScreen(
    instance: Instance,
    snackbarHostState: SnackbarHostState,
    isLoading: Boolean,
    setLoading: (Boolean) -> Unit,
    onCloseRequested: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val error by instance.error
    val progress by instance.taskProgress
    val isInstanceLoading = progress != null

    val databaseExists by instance.databaseExists
    val databaseHash by instance.databaseHash
    val databaseFileHash by instance.databaseFileHash

    val serverInfo by instance.serverInfo

    val newVersionAvailable by UpdateChecker.newVersionAvailable
    val latestWeeWXRelease by WeeWX.latestRelease

    ProgressDialog(progress, "Carregant...")

    LaunchedEffect(Unit) {
        doAsync { instance.updateServerData() }
    }

    serverInfo.isServerDistroUnsupported?.let {
        UnsupportedDistroDialog(it, onCloseRequested)
    }

    LaunchedEffect(error) {
        snapshotFlow { error }
            .distinctUntilChanged()
            .filterNotNull()
            .collect { message ->
                snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Long)
                instance.error.value = null
            }
    }

    if (serverInfo.isEmpty) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var shouldDisplayNewWeeWXVersionDialog by remember { mutableStateOf(true) }
    if (newVersionAvailable != null && shouldDisplayNewWeeWXVersionDialog) {
        NewWeeWXVersionAvailableDialog(
            currentVersion = serverInfo.weeWXVersion ?: ".",
            latestVersion = latestWeeWXRelease?.version?.value ?: ".",
            { shouldDisplayNewWeeWXVersionDialog = false }
        ) { doAsync { instance.upgradeWeeWX() } }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Pas 1. Detindre el servei",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Per a assegurar-nos que no hi han dades en trànsit quan fem les modificacions, primer hem de detindre el servei de WeeWX en execució. Fes click al botó de baix per a detindre el servei. A la dreta s'indica si s'està executant o no, pots fer clic per a actualizar.",
            style = MaterialTheme.typography.bodyLarge
        )
        Row(Modifier.fillMaxWidth()) {
            OutlinedButton(
                modifier = Modifier.weight(1f).padding(end = 4.dp),
                enabled = !isLoading && !isInstanceLoading,
                onClick = async { instance.stopWeeWX() }
            ) { Text("Detindre servei") }
            IconButton(
                enabled = !isLoading && !isInstanceLoading,
                onClick = async { instance.updateServerData() }
            ) {
                serverInfo.isWeeWXRunning?.let {
                    Icon(if (it) Icons.Rounded.Done else Icons.Rounded.Close, null)
                } ?: CircularProgressIndicator()
            }
        }

        Text(
            text = "Pas 2. Descarregar base de dades",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "El primer pas és sempre descarregar l'última versió de la base de dades. Per a fer-ho simplement fes clic al botó de \"Descarregar db\". Un diàleg es mostrarà mentre es descarrega. Una vegada descarregada, pots fer clic sobre el botó \"Obrir db\", i s'obrirà la base de dades al programa assignat per defecte.\nUna vegada obert al programa, és moment de fer les modificacions necessàries. Quan acabes, desa els canvis, aquest programa ho detectarà automàticament.",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Possibles errors:",
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            text = buildAnnotatedString {
                append("- \"")
                withStyle(SpanStyle(fontWeight = FontWeight.Medium)) { append("Cap aplicació suportada") }
                append("\": Vol dir que no tens ")
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append("DB Browser for SQLite") }
                append(" descarregat, o no és l'opció predeterminada per a obrir arxius.")
            },
            style = MaterialTheme.typography.bodyLarge
        )
        Row(Modifier.fillMaxWidth().padding(top = 12.dp)) {
            OutlinedButton(
                modifier = Modifier.weight(1f).padding(end = 4.dp),
                enabled = !isLoading && !isInstanceLoading,
                onClick = {
                    setLoading(true)
                    doAsync {
                        instance.downloadDatabase()
                        val result = withContext(Dispatchers.Main) {
                            setLoading(false)
                            snackbarHostState.showSnackbar(
                                "Base de dades descarregada",
                                "Obrir",
                                duration = SnackbarDuration.Long
                            )
                        }
                        if (result == SnackbarResult.ActionPerformed)
                            DesktopUtils.open(instance.databaseFile) {
                                scope.launch { snackbarHostState.showSnackbar("No s'ha pogut obrir l'arxiu. Cap aplicació suportada.") }
                            }
                    }
                }
            ) { Text("Descarregar db") }

            OutlinedButton(
                modifier = Modifier.weight(1f).padding(start = 4.dp),
                enabled = !isLoading && !isInstanceLoading && databaseExists,
                onClick = {
                    DesktopUtils.open(instance.databaseFile) {
                        scope.launch { snackbarHostState.showSnackbar("No s'ha pogut obrir l'arxiu. Cap aplicació suportada.") }
                    }
                }
            ) { Text("Obrir db") }

            IconButton(
                onClick = { instance.databaseFile.delete() }
            ) { Icon(Icons.Outlined.DeleteForever, null) }
        }

        Text(
            text = "Pas 3. Pujar les modificacions al servidor",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Una vegada hages completat les modificacions, toca pujar-les al servidor. Això és tant fàcil com prémer el següent botó.",
            style = MaterialTheme.typography.bodyLarge
        )
        Row(Modifier.fillMaxWidth()) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                enabled = !isLoading && !isInstanceLoading && databaseHash != databaseFileHash,
                onClick = async {
                    instance.uploadDatabase()
                    withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar(
                            "Base de dades carregada!"
                        )
                    }
                }
            ) { Text("Carregar modificacions") }
        }

        Text(
            text = "Pas 4. Recalcular dades",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Ara WeeWX ha de tornar a processar les dades, per a incloure les teues modificacions. Per a que aquest procès dure menys, pots establir un rang de dates, per a evitar recalcular tot l'historial. Tot i això, aquest procés pot tardar una bona estona.",
            style = MaterialTheme.typography.bodyLarge
        )
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd") }
            var startDate by remember { mutableStateOf("") }
            var endDate by remember { mutableStateOf("") }

            OutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                isError = !dateFormatter.isValidDate(startDate),
                label = { Text("Data d'inici") },
                placeholder = { Text("yyyy-MM-dd") },
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            OutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it },
                isError = !dateFormatter.isValidDate(endDate),
                label = { Text("Data final") },
                placeholder = { Text("yyyy-MM-dd") },
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            OutlinedButton(
                enabled = !isLoading && !isInstanceLoading && dateFormatter.isValidDate(startDate) && dateFormatter.isValidDate(endDate),
                onClick = async {
                    withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar("S'ha iniciat la operació!", duration = SnackbarDuration.Short)
                    }
                    instance.repairData(
                        from = dateFormatter.parse(startDate),
                        to = dateFormatter.parse(endDate)
                    )
                    withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar(
                            "Base de dades carregada!"
                        )
                    }
                }
            ) { Text("Recalcular") }
        }

        Text(
            text = "Pas 5. Reconnectar el servei",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Ja hem acabat, ara simplement hem de tornar a arrancar el servei.",
            style = MaterialTheme.typography.bodyLarge
        )
        Row(Modifier.fillMaxWidth()) {
            OutlinedButton(
                modifier = Modifier.weight(1f).padding(end = 4.dp),
                enabled = !isLoading && !isInstanceLoading,
                onClick = async { instance.startWeeWX() }
            ) { Text("Arrancar servei") }
            IconButton(
                enabled = !isLoading && !isInstanceLoading,
                onClick = async { instance.updateServerData() }
            ) {
                serverInfo.isWeeWXRunning?.let {
                    Icon(if (it) Icons.Rounded.Done else Icons.Rounded.Close, null)
                } ?: CircularProgressIndicator()
            }
        }
    }

    Text(
        text = "Hash server: ${databaseHash ?: "cap"}\nHash actual: ${databaseFileHash ?: "cap"}",
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.fillMaxWidth().padding(4.dp)
    )
    Row(Modifier.fillMaxWidth().padding(4.dp)) {
        val latestReleaseStr = latestWeeWXRelease?.version?.let { latest ->
            val current = serverInfo.weeWXVersion?.semVer
            if (current != null)
                if (latest > current)
                    " (Nova versió disponible)"
                else
                    " (Actualizada)"
            else
                ""
        }
        val weewxRunningString = when (serverInfo.isWeeWXRunning) {
            true -> " - En execució"
            false -> " - Detingut"
            else -> ""
        }
        val newVersionAvailableString = when(newVersionAvailable) {
            null -> ""
            else -> " (Nova versió disponible)"
        }
        Text(
            text = "Versió de WeeWX: ${serverInfo.weeWXVersion ?: "Carregant..."}${latestReleaseStr}${weewxRunningString}",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Versió de WeeWX-Fixer: ${UpdateChecker.appVersion()}${newVersionAvailableString}",
            style = MaterialTheme.typography.labelSmall
        )
    }
}
