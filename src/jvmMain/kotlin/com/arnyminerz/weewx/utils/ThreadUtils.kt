package com.arnyminerz.weewx.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Runs the given [block] of code in the main thread.
 */
suspend fun <R> ui(block: suspend CoroutineScope.() -> R): R = withContext(Dispatchers.Default, block)

/**
 * Runs the given [block] of code in a new thread in the IO scope.
 */
fun doAsync(block: suspend CoroutineScope.() -> Unit) = CoroutineScope(Dispatchers.IO).launch(block = block)

/**
 * Runs the given [block] of code in a new thread in the IO scope.
 */
fun async(block: suspend CoroutineScope.() -> Unit): () -> Unit = {
    CoroutineScope(Dispatchers.IO).launch(block = block)
}
