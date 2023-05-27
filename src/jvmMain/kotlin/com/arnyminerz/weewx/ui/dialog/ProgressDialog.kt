package com.arnyminerz.weewx.ui.dialog

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import com.arnyminerz.weewx.data.Progress

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProgressDialog(
    progress: Progress?,
    title: String
) {
    progress?.let {
        AlertDialog(
            onDismissRequest = {},
            text = {
                Column(
                    modifier = Modifier.widthIn(min = 200.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                    if (it.isEmpty)
                        CircularProgressIndicator()
                    else {
                        CircularProgressIndicator(progress = it.constrained)
                        Text(
                            text = it.percentString,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 32.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    if (it.msg != null)
                        Text(
                            text = it.msg,
                            textAlign = TextAlign.Center,
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
fun ProgressDialog_Preview_Indeterminate_NoMessage() {
    ProgressDialog(Progress(0, 0, null), "Loading")
}

@Preview
@Composable
fun ProgressDialog_Preview_Indeterminate_Message() {
    ProgressDialog(Progress(0, 0, "Loading msg"), "Loading")
}
