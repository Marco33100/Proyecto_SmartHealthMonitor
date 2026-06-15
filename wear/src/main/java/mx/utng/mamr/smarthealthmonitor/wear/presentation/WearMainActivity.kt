package mx.utng.mamr.smarthealthmonitor.wear.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import kotlinx.coroutines.launch
import mx.utng.mamr.smarthealthmonitor.wear.HealthDataService
import mx.utng.mamr.smarthealthmonitor.wear.R
import mx.utng.mamr.smarthealthmonitor.wear.presentation.theme.SmartHealthMonitorTheme
import androidx.activity.compose.setContent

class WearMainActivity : ComponentActivity() {

    private val permissions = arrayOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.ACTIVITY_RECOGNITION,
        "android.permission.health.READ_HEART_RATE"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SmartHealthMonitorTheme {
                // TODO Ej.02: reemplazar con WearNavGraph
                WearDashboardScreen()
            }
        }
    }

    fun registrarSalud() {
        lifecycleScope.launch {
            try {
                HealthDataService.registrar(applicationContext)
                Log.d("WearMainActivity", "Servicio de salud registrado con éxito")
            } catch (e: Exception) {
                Log.e("WearMainActivity", "Error al registrar salud: ${e.message}")
            }
        }
    }
}

@Composable
fun WearApp(greetingName: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? WearMainActivity
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.all { it }) {
            activity?.registrarSalud()
        } else {
            Toast.makeText(context, "Se requieren permisos para funcionar", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        val missingPermissions = activity?.let { act ->
            arrayOf(
                Manifest.permission.BODY_SENSORS,
                Manifest.permission.ACTIVITY_RECOGNITION,
                "android.permission.health.READ_HEART_RATE"
            ).filter {
                ContextCompat.checkSelfPermission(act, it) != PackageManager.PERMISSION_GRANTED
            }.toTypedArray()
        } ?: emptyArray()

        if (missingPermissions.isEmpty()) {
            activity?.registrarSalud()
        } else {
            launcher.launch(missingPermissions)
        }
    }

    SmartHealthMonitorTheme {
        AppScaffold {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(
                scrollState = listState,
                edgeButton = {
                    EdgeButton(
                        onClick = { /*TODO*/ },
                        colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    ) {
                        Text("More")
                    }
                },
            ) { contentPadding ->
                TransformingLazyColumn(contentPadding = contentPadding, state = listState) {
                    item {
                        ListHeader(
                            modifier =
                            Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(text = "SmartHealth Watch")
                        }
                    }
                    item {
                        Button(
                            onClick = { 
                                activity?.registrarSalud()
                                scope.launch {
                                    val bpmSimulado = (70..130).random()
                                    mx.utng.mamr.smarthealthmonitor.wear.WearDataSender(context).enviarFC(bpmSimulado)
                                    Toast.makeText(context, "Medición: $bpmSimulado bpm", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("MEDIR PULSO")
                        }
                    }
                }
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}