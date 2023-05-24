package com.arnyminerz.weewx.remote

import com.arnyminerz.weewx.data.inside
import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import com.jcraft.jsch.SftpProgressMonitor
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.net.ConnectException

class Client(
    private val hostname: String,
    private val username: String,
    private val password: String,
    private val port: Int
) {
    private var session: Session? = null

    /**
     * Initializes the connection with the server given. Remember to call [disconnect] after performing all the actions
     * desired.
     */
    fun connect() {
        // If already connected, return
        if (session?.isConnected == true) return

        println("Connecting to $hostname:$port...")
        session = JSch().getSession(username, hostname, port).apply {
            setPassword(password)
            setConfig("StrictHostKeyChecking", "no")
        }
        session?.connect()
        println("Connected to $hostname:$port!")
    }

    /**
     * Disconnects from the currently connected server, if any.
     */
    fun disconnect() {
        session?.disconnect()
        session = null
        println("Disconnected from $hostname:$port!")
    }

    /**
     * Initializes the connection with the server, performs the actions in [block], and closes the connection.
     */
    suspend fun use(exceptionHandler: suspend (Exception) -> Unit, block: suspend Client.() -> Unit) {
        try {
            connect()
            block()
        } catch (e: Exception) {
            exceptionHandler(e)
        } finally {
            disconnect()
        }
    }

    /**
     * Initializes the connection with the server, performs the actions in [block], and closes the connection.
     */
    suspend fun <R> use(block: suspend Client.() -> R): R =
        try {
            connect()
            block()
        } finally {
            disconnect()
        }

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T: Channel, R> openChannel(
        type: String,
        config: suspend T.() -> Unit = {},
        block: suspend T.() -> R
    ): R {
        val session = session ?: throw IllegalStateException("Session not initialized. Please, run connect() before.")
        if (!session.isConnected) {
            this.session = null
            throw ConnectException("Connection with server lost.")
        }

        var channel: T? = null
        try {
            channel = session.openChannel(type) as T
            config(channel)
            channel.connect()

            // Wait until the channel is connected
            while (!channel.isConnected) {
                delay(100)
            }

            return block(channel)
        } finally {
            channel?.disconnect()
        }
    }

    /**
     * Runs the specified command in the SSH session. [connect] must be called before this.
     * @param command The command to be run.
     * @return The response the server gave.
     * @throws IllegalStateException If the server is still not connected.
     * @throws ConnectException If the connection to the server was lost before running the command.
     * @throws JSchException If there was an error while running the command.
     */
    suspend fun run(command: String): String {
        println("Running command \"$command\" on $hostname:$port...")
        var stream: InputStreamReader? = null
        return openChannel<ChannelExec, String>("exec", {
            setCommand(command)
            stream = inputStream.reader()
            setErrStream(System.err, true)
        }) {
            val buffer = CharArray(128)
            val output = StringBuilder()
            var read: Int? = stream?.read(buffer, 0, buffer.size)
            while (read != null && read >= 0) {
                output.appendRange(buffer, 0, read)
                read = stream?.read(buffer, 0, buffer.size)
            }
            stream?.close()

            // Return the collected output, removing trailing line breaks
            output.toString().trimEnd('\n')
        }
    }

    private suspend fun <R> sftp(block: suspend ChannelSftp.() -> R): R = openChannel("sftp", block = block)

    suspend fun download(path: String, target: File, progress: ProgressCallback? = null) = sftp {
        println("Fetching $path from $hostname:$port")
        get(path, target.absolutePath, object : SftpProgressMonitor {
            private lateinit var range: LongRange

            private var accumulator: Long = 0

            override fun init(op: Int, src: String?, dest: String?, max: Long) {
                range = 0L until max
                println("Started download for $path: total = ${range.last}")
                progress?.callback(0L inside range)
            }

            override fun count(count: Long): Boolean {
                accumulator += count
                println("Download progress for $path: $accumulator / ${range.last}")
                return progress?.callback(accumulator inside range) ?: true
            }

            override fun end() { progress?.callback(null) }
        })
    }

    suspend fun upload(source: File, target: String, progress: ProgressCallback? = null) = sftp {
        println("Uploading $source into $target of $hostname:$port...")
        put(source.absolutePath, target, object : SftpProgressMonitor {
            private lateinit var range: LongRange

            private var accumulator: Long = 0

            override fun init(op: Int, src: String?, dest: String?, max: Long) {
                range = 0L until max
                println("Started upload for $source: total = ${range.last}")
                progress?.callback(0L inside range)
            }

            override fun count(count: Long): Boolean {
                accumulator += count
                println("Upload progress for $source: $accumulator / ${range.last}")
                return progress?.callback(accumulator inside range) ?: true
            }

            override fun end() { progress?.callback(null) }
        })
    }
}