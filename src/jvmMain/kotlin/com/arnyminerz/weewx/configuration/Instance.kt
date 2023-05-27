package com.arnyminerz.weewx.configuration

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.arnyminerz.weewx.data.Progress
import com.arnyminerz.weewx.data.ServerData
import com.arnyminerz.weewx.data.progress
import com.arnyminerz.weewx.remote.Client
import com.arnyminerz.weewx.utils.checksum
import com.arnyminerz.weewx.utils.watchForChanges
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.io.path.exists

class Instance(file: File): Config(file) {
    private val client: Client by lazy {
        Client(
            getValue("hostname"),
            getValue("username"),
            getValue("password"),
            getValue("port").toInt()
        )
    }

    private val remoteDatabaseFilePath = getValue("database")

    private val instanceDirectory: File get() = file.parentFile

    val databaseFile: File get() = File(instanceDirectory, "database.sqlite3")
    val checksumFile: File get() = File(instanceDirectory, "database.checksum")

    private val databaseExistsMutable: MutableState<Boolean> = mutableStateOf(databaseFile.exists())
    val databaseExists: State<Boolean> get() = databaseExistsMutable

    private val databaseFileHashMutable: MutableState<String?> = mutableStateOf(databaseFile.checksum())
    val databaseFileHash: State<String?> get() = databaseFileHashMutable

    /** Provides the hash the database had just after downloading. */
    private val databaseHashMutable: MutableState<String?> = mutableStateOf(checksumFile.takeIf { it.exists() }?.readText())
    val databaseHash: State<String?> get() = databaseHashMutable

    val taskProgress = mutableStateOf<Progress?>(null)

    private val serverInfoMutable: MutableState<ServerData> = mutableStateOf(ServerData())
    val serverInfo: State<ServerData> get() = serverInfoMutable

    val error: MutableState<String?> = mutableStateOf(null)

    init {
        databaseFile.toPath().watchForChanges {
            println("Database ($databaseFile) updated! Refreshing checksum...")
            databaseExistsMutable.value = exists()
            databaseFileHashMutable.value = databaseFile.takeIf { it.exists() }?.checksum()
            databaseHashMutable.value = checksumFile.takeIf { it.exists() }?.readText()
        }
    }

    suspend fun downloadDatabase() {
        try {
            if (databaseFile.exists()) databaseFile.delete()

            println("Downloading database for $name into $databaseFile")
            taskProgress.value = Progress.INDETERMINATE // Set progress to indeterminate
            client.use {
                download(remoteDatabaseFilePath, databaseFile) { progress ->
                    taskProgress.value = progress?.progress
                    true
                }
            }

            println("Storing checksum for database...")
            taskProgress.value = Progress.INDETERMINATE // Set progress to indeterminate
            if (checksumFile.exists()) checksumFile.delete()
            if (databaseFile.exists()) checksumFile.writeText(databaseFile.checksum()!!)

            println("Database downloaded.")
        } catch (e: Exception) {
            error.value = "No s'ha pogut descarregar. Error: ${e.message}"
        } finally {
            taskProgress.value = null
        }
    }

    suspend fun uploadDatabase() {
        try {
            println("Uploading database for $name into $remoteDatabaseFilePath")
            taskProgress.value = Progress.INDETERMINATE // Set progress to indeterminate
            client.use {
                upload(databaseFile, remoteDatabaseFilePath) { progress ->
                    taskProgress.value = progress?.progress
                    true
                }
            }

            println("Storing checksum for database...")
            taskProgress.value = Progress.INDETERMINATE // Set progress to indeterminate
            if (checksumFile.exists()) checksumFile.delete()
            if (databaseFile.exists()) checksumFile.writeText(databaseFile.checksum()!!)

            taskProgress.value = null
            println("Database uploaded.")
        } catch (e: Exception) {
            error.value = "No s'ha pogut carregar. Error: ${e.message}"
        } finally {
            taskProgress.value = null
        }
    }

    /**
     * Uses [client] to run the operations in [block], and updates [taskProgress] accordingly.
     */
    private suspend fun <R> performOperation(block: suspend Client.() -> R): R? =
        try {
            taskProgress.value = Progress.INDETERMINATE
            client.use(block)
        } catch (e: Exception) {
            error.value = e.message
            null
        }  finally {
            taskProgress.value = null
        }

    /**
     * Fetches data from the server to check compatibility, as well as information about WeeWX.
     * @see ServerData.isServerDistroUnsupported
     */
    suspend fun updateServerData() {
        performOperation {
            val osRelease = ReadonlyConfig(run("cat /etc/os-release"))
            val idLike = osRelease["ID_LIKE"] ?: osRelease["ID"]

            serverInfoMutable.value = serverInfoMutable.value.copy(
                isServerDistroUnsupported = idLike.takeUnless {
                    it.equals("ubuntu", true) || it.equals("debian", true)
                }
            )
            serverInfoMutable.value = serverInfoMutable.value.copy(
                weeWXVersion = run("wee_config --version")
            )
            serverInfoMutable.value = serverInfoMutable.value.copy(
                isWeeWXRunning = run("sudo systemctl is-active weewx") == "active"
            )
        }
    }

    suspend fun stopWeeWX() {
        performOperation {
            run("sudo systemctl stop weewx")

            val isActive = run("sudo systemctl is-active weewx")
            serverInfoMutable.value = serverInfoMutable.value.copy(
               isWeeWXRunning = isActive == "active"
            )
        }
    }

    suspend fun startWeeWX() {
        performOperation {
            run("sudo systemctl start weewx")

            val isActive = run("sudo systemctl is-active weewx")
            serverInfoMutable.value = serverInfoMutable.value.copy(
                isWeeWXRunning = isActive == "active"
            )
        }
    }

    suspend fun repairData(from: Date, to: Date) {
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        performOperation {
            run("sudo wee_database --rebuild-daily --from=${formatter.format(from)} --to=${formatter.format(to)}")
            run("sudo wee_database --calc-missing --from=${formatter.format(from)} --to=${formatter.format(to)}")
        }
    }

    suspend fun upgradeWeeWX() {
        performOperation {
            taskProgress.value = Progress.INDETERMINATE("Actualitzant repos")
            run("sudo apt-get update -y")
            taskProgress.value = Progress.INDETERMINATE("Parant WeeWX")
            run("sudo systemctl stop weewx")
            taskProgress.value = Progress.INDETERMINATE("Actualitzant WeeWX")
            run("sudo apt-get install weewx -y")
            taskProgress.value = Progress.INDETERMINATE("Arrancant WeeWX")
            run("sudo systemctl start weewx")
        }
    }
}