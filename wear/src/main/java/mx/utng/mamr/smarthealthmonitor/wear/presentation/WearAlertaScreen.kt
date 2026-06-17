package mx.utng.mamr.smarthealthmonitor.wear.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close

@Composable
fun WearAlertaScreen(
    fc: Int,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp,
            Alignment.CenterVertically)
    ) {
        Text("FC: $fc bpm",
            style = MaterialTheme.typography.title3,
            color = MaterialTheme.colors.error)
        Text("¿Enviar alerta?",
            style = MaterialTheme.typography.body2)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Botón Confirmar (ícono ✓)
            Button(onClick = onConfirmar,
                modifier = Modifier.size(52.dp),
                colors = ButtonDefaults.primaryButtonColors(
                    backgroundColor = MaterialTheme.colors.error)) {
                Icon(Icons.Default.Check,
                    contentDescription = "Confirmar alerta")
            }
            // Botón Cancelar (ícono ✗)
            Button(onClick = onCancelar,
                modifier = Modifier.size(52.dp)) {
                Icon(Icons.Default.Close,
                    contentDescription = "Cancelar")
            }
        }
    }
}
