package com.arnyminerz.weewx.configuration

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.arnyminerz.weewx.data.LongValueMinMax.Companion.INDETERMINATE
import com.arnyminerz.weewx.data.ValueMinMax
import com.arnyminerz.weewx.data.inside
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

    val downloadProgress = mutableStateOf<ValueMinMax<Long>?>(null)

    private val isWeewxRunningMutable: MutableState<Boolean?> = mutableStateOf(null)
    val isWeewxRunning: State<Boolean?> get() = isWeewxRunningMutable

    private val weewxVersionMutable: MutableState<String?> = mutableStateOf(null)
    val weewxVersion: State<String?> get() = weewxVersionMutable

    private val isLoadingMutable: MutableState<Boolean> = mutableStateOf(false)
    val isLoading: State<Boolean> get() = isLoadingMutable

    private val isServerDistroUnsupportedMutable: MutableState<String?> = mutableStateOf(null)
    val isServerDistroUnsupported: State<String?> get() = isServerDistroUnsupportedMutable

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
            isLoadingMutable.value = true

            if (databaseFile.exists()) databaseFile.delete()

            println("Downloading database for $name into $databaseFile")
            downloadProgress.value = INDETERMINATE // Set progress to indeterminate
            client.use {
                download(remoteDatabaseFilePath, databaseFile) { progress ->
                    downloadProgress.value = progress
                    true
                }
            }

            println("Storing checksum for database...")
            downloadProgress.value = INDETERMINATE // Set progress to indeterminate
            if (checksumFile.exists()) checksumFile.delete()
            if (databaseFile.exists()) checksumFile.writeText(databaseFile.checksum()!!)

            downloadProgress.value = null
            println("Database downloaded.")
        } finally {
            isLoadingMutable.value = false
        }
    }

    suspend fun uploadDatabase() {
        try {
            isLoadingMutable.value = true

            println("Uploading database for $name into $remoteDatabaseFilePath")
            downloadProgress.value = 0L inside (0..0L) // Set progress to indeterminate
            client.use {
                upload(databaseFile, remoteDatabaseFilePath) { progress ->
                    downloadProgress.value = progress
                    true
                }
            }

            println("Storing checksum for database...")
            downloadProgress.value = INDETERMINATE // Set progress to indeterminate
            if (checksumFile.exists()) checksumFile.delete()
            if (databaseFile.exists()) checksumFile.writeText(databaseFile.checksum()!!)

            downloadProgress.value = null
            println("Database uploaded.")
        } finally {
            isLoadingMutable.value = false
        }
    }

    /**
     * Uses [client] to run the operations in [block], and updates [isLoadingMutable] accordingly.
     */
    private suspend fun <R> performOperation(block: suspend Client.() -> R): R {
        try {
            isLoadingMutable.value = true
            return client.use(block)
        } finally {
            isLoadingMutable.value = false
        }
    }

    /**
     * Checks whether WeeWX is running in the remote server or not.
     */
    suspend fun updateWeeWXStatus() {
        isWeewxRunningMutable.value = null

        performOperation {
            weewxVersionMutable.value = run("wee_config --version")
            isWeewxRunningMutable.value = run("sudo systemctl is-active weewx") == "active"
        }
    }

    /**
     * Fetches data from the server to check compatibility among other things.
     * @see isServerDistroUnsupported
     */
    suspend fun updateServerData() {
        performOperation {
            val osRelease = ReadonlyConfig(run("cat /etc/os-release"))
            val idLike = osRelease["ID_LIKE"] ?: osRelease["ID"]
            isServerDistroUnsupportedMutable.value = idLike.takeUnless {
                it.equals("ubuntu", true) || it.equals("debian", true)
            }
        }
    }

    suspend fun stopWeeWX() {
        performOperation {
            run("sudo systemctl stop weewx")

            val isActive = run("sudo systemctl is-active weewx")
            isWeewxRunningMutable.value = isActive == "active"
        }
    }

    suspend fun startWeeWX() {
        performOperation {
            run("sudo systemctl start weewx")

            val isActive = run("sudo systemctl is-active weewx")
            isWeewxRunningMutable.value = isActive == "active"
        }
    }

    suspend fun repairData(from: Date, to: Date) {
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        performOperation {
            run("sudo wee_database --rebuild-daily --from=${formatter.format(from)} --to=${formatter.format(to)}")
            run("sudo wee_database --calc-missing --from=${formatter.format(from)} --to=${formatter.format(to)}")
        }
    }
}