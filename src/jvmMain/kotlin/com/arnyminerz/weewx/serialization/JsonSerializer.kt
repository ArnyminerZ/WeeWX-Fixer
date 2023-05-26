package com.arnyminerz.weewx.serialization

import org.json.JSONObject

interface JsonSerializer <T: Any> {
    fun fromJson(json: JSONObject): T
}