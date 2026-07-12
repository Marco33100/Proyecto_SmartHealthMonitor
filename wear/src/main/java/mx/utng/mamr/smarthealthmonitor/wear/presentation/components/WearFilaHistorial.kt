package mx.utng.mamr.smarthealthmonitor.wear.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import mx.utng.mamr.smarthealthmonitor.data.db.LecturaFC

@Composable
fun WearFilaHistorial(lectura: LecturaFC) {
    val color = if (lectura.estado == "Normal")
        MaterialTheme.colors.primary
    else
        MaterialTheme.colors.error

    Chip(
        label = { Text("${lectura.bpm} bpm",
            color = color) },
        secondaryLabel = { Text(lectura.hora) },
        onClick = { },
        colors = ChipDefaults.secondaryChipColors(),
        modifier = Modifier.fillMaxWidth()
    )
}

