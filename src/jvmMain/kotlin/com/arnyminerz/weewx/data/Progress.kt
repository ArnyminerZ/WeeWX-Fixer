package com.arnyminerz.weewx.data

data class Progress(override val value: Long, override val max: Long, val msg: String?): LongValueMinMax(value, 0L, max) {
    companion object {
        val INDETERMINATE = Progress(0, 0, null)

        @Suppress("FunctionName")
        fun INDETERMINATE(msg: String) = Progress(0, 0, msg)
    }

    constructor(value: LongValueMinMax): this(value.value, value.max, null)

    constructor(value: LongValueMinMax, msg: String): this(value.value, value.max, msg)
}

val LongValueMinMax.progress: Progress
    get() = Progress(this)

fun LongValueMinMax.progress(msg: String): Progress = Progress(this, msg)
