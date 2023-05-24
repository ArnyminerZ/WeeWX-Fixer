package com.arnyminerz.weewx.utils

fun Double.map(inMin: Double, inMax: Double, outMin: Double, outMax: Double): Double =
    (this - inMin) * (outMax - outMin) / (inMax - inMin) + outMin
