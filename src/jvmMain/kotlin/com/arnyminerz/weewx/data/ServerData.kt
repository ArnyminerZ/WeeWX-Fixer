package com.arnyminerz.weewx.data

data class ServerData(
    val isWeeWXRunning: Boolean?,
    val weeWXVersion: String?,
    val isServerDistroUnsupported: String?
) {
    constructor(): this(null, null, null)

    val isEmpty: Boolean get() = isWeeWXRunning == null && weeWXVersion == null && isServerDistroUnsupported == null
}
