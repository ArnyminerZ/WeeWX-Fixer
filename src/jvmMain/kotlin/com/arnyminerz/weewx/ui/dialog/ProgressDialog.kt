package com.arnyminerz.weewx.ui.dialog

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arnyminerz.weewx.data.ValueMinMax
import com.arnyminerz.weewx.data.inside

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T : Number> ProgressDialog(
    progress: ValueMinMax<T>?,
    title: String
) {
    progress?.let {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(title) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (it.isEmpty)
                        CircularProgressIndicator()
                    else
                        CircularProgressIndicator(progress = it.constrained)
                    Text(
                        text = it.percentString,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            confirmButton = {}
        )
    }
}

@Preview
@Composable
fun ProgressDialog_Preview() {
    ProgressDialog(50L inside (0L until 100L), "Loading")
}
