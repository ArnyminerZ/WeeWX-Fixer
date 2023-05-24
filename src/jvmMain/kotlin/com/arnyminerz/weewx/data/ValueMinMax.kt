package com.arnyminerz.weewx.data

import com.arnyminerz.weewx.utils.map

abstract class ValueMinMax<T : Number>(
    val value: T,
    val min: T,
    val max: T
) {
    init {
        if (min compareTo max > 0) throw IllegalArgumentException("min ($min) cannot be greater than max ($max)")
    }

    /** The neutral value for the current number type. Usually simply 0 */
    protected abstract val neutral: T

    protected abstract infix fun T.compareTo(b: T): Int

    val constrained: Float get() = value.toDouble().map(min.toDouble(), max.toDouble(), 0.0, 1.0).toFloat()

    val isEmpty: Boolean get() = value compareTo neutral == 0 && min compareTo neutral == 0 && max compareTo neutral == 0

    val percent: Int get() = (constrained * 100).toInt()

    val percentString: String get() = "$percent % ($value / $max)"
}

class LongValueMinMax(
    value: Long,
    min: Long,
    max: Long
) : ValueMinMax<Long>(value, min, max) {
    override fun Long.compareTo(b: Long): Int = compareTo(b)

    override val neutral: Long = 0L
}

infix fun Long.inside(range: LongRange): LongValueMinMax = LongValueMinMax(this, range.first, range.last)
