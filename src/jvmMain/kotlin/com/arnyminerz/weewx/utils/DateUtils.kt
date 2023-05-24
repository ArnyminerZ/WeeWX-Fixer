package com.arnyminerz.weewx.utils

import java.text.DateFormat
import java.text.ParseException

fun DateFormat.isValidDate(date: String): Boolean =
    try {
        parse(date)
        true
    } catch (e: ParseException) {
        false
    }
