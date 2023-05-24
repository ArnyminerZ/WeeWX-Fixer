package com.arnyminerz.weewx.ui.reusable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity

@Composable
@ExperimentalMaterial3Api
fun Spinner(
    selectedText: String,
    options: List<String>,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onOptionSelected: (index: Int, value: String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var dropDownWidth by remember { mutableStateOf(0) }

    val rotation by animateFloatAsState(if (expanded) 180f else 0f)

    Column(modifier) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { dropDownWidth = it.width }
                .clickable(enabled) { expanded = true },
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    Icons.Filled.ArrowDropDown,
                    "contentDescription",
                    Modifier.clickable(enabled) { expanded = !expanded }.rotate(rotation)
                )
            },
            readOnly = true,
            enabled = false,
            singleLine = true,
            colors = if (enabled) TextFieldDefaults.outlinedTextFieldColors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                // For Icons
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) else TextFieldDefaults.outlinedTextFieldColors()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current) { dropDownWidth.toDp() })
        ) {
            options.forEachIndexed { index, item ->
                DropdownMenuItem(onClick = { onOptionSelected(index, item) }, enabled = enabled) {
                    Text(text = item)
                }
            }
        }
    }
}
