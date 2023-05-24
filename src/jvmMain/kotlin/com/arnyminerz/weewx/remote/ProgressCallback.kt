package com.arnyminerz.weewx.remote

import com.arnyminerz.weewx.data.ValueMinMax

fun interface ProgressCallback {
    /**
     * Gets called whenever the progress of the operation is updated.
     * @param progress `null` if the operation has finished. Contains the current progress of the operation.
     * @return `true` if the transfer should go on, `false` if the transfer should be cancelled.
     */
    fun callback(progress: ValueMinMax<Long>?): Boolean
}
