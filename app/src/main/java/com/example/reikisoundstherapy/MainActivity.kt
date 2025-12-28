package com.example.reikisoundstherapy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.reikisoundstherapy.ui.theme.ReikiSoundsTherapyTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReikiSoundsTherapyTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current

    var musicUri by remember { mutableStateOf<Uri?>(null) }
    var binauralUri by remember { mutableStateOf<Uri?>(null) }
    var bellUri by remember { mutableStateOf<Uri?>(null) }

    var musicFileName by remember { mutableStateOf("No seleccionado") }
    var binauralFileName by remember { mutableStateOf("No seleccionado") }
    var bellFileName by remember { mutableStateOf("No seleccionado") }

    var musicVolume by remember { mutableStateOf(0.7f) }
    var binauralVolume by remember { mutableStateOf(0.5f) }
    var bellVolume by remember { mutableStateOf(1.0f) }

    val intervals = listOf(3L, 5L, 10L, 15L)
    var selectedInterval by remember { mutableStateOf(intervals[1]) } // Default to 5 mins

    var isPlaying by remember { mutableStateOf(false) }

    val selectMusicLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            musicUri = it
            musicFileName = it.lastPathSegment ?: "Archivo seleccionado"
        }
    }

    val selectBinauralLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            binauralUri = it
            binauralFileName = it.lastPathSegment ?: "Archivo seleccionado"
        }
    }

    val selectBellLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            bellUri = it
            bellFileName = it.lastPathSegment ?: "Archivo seleccionado"
        }
    }
    
    val startAudioService = {
        val serviceIntent = Intent(context, SoundTherapyService::class.java).apply {
            action = SoundTherapyService.ACTION_START
            putExtra(SoundTherapyService.EXTRA_MUSIC_URI, musicUri?.toString())
            putExtra(SoundTherapyService.EXTRA_BINAURAL_URI, binauralUri?.toString())
            putExtra(SoundTherapyService.EXTRA_BELL_URI, bellUri.toString())
            putExtra(SoundTherapyService.EXTRA_INTERVAL, selectedInterval)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
        isPlaying = true
    }

    val requestPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            startAudioService()
        } else {
            Toast.makeText(context, "Se requieren permisos para leer audio y mostrar notificaciones.", Toast.LENGTH_LONG).show()
        }
    }

    fun checkPermissionsAndStart() {
        if (bellUri == null || (musicUri == null && binauralUri == null)) {
            Toast.makeText(context, "Debes seleccionar la campana y al menos un sonido de fondo (música o binaural)", Toast.LENGTH_LONG).show()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionsToRequest = mutableListOf<String>()
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }

            if (permissionsToRequest.isNotEmpty()) {
                requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
            } else {
                startAudioService()
            }
        } else {
            startAudioService()
        }
    }
    
    fun setVolume(action: String, volume: Float) {
        val serviceIntent = Intent(context, SoundTherapyService::class.java).apply {
            this.action = action
            putExtra(SoundTherapyService.EXTRA_VOLUME, volume)
        }
        context.startService(serviceIntent)
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🎵 Pistas de Audio", style = MaterialTheme.typography.headlineSmall)
            
            Spacer(modifier = Modifier.height(16.dp))

            AudioFileSelector("Seleccionar Música de Fondo", musicFileName) {
                selectMusicLauncher.launch("audio/*")
            }
            AudioFileSelector("Seleccionar Onda Binaural", binauralFileName) {
                selectBinauralLauncher.launch("audio/*")
            }
            AudioFileSelector("Seleccionar Sonido de Campana", bellFileName) {
                selectBellLauncher.launch("audio/*")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))

            Text("🎚️ Mezclador de Volumen", style = MaterialTheme.typography.headlineSmall)
            
            Spacer(modifier = Modifier.height(16.dp))

            VolumeSlider("Música de Fondo", musicVolume) { newVolume ->
                musicVolume = newVolume
                setVolume(SoundTherapyService.ACTION_SET_VOLUME_MUSIC, newVolume)
            }
            VolumeSlider("Ondas Binaurales", binauralVolume) { newVolume ->
                binauralVolume = newVolume
                setVolume(SoundTherapyService.ACTION_SET_VOLUME_BINAURAL, newVolume)
            }
            VolumeSlider("Campana", bellVolume) { newVolume ->
                bellVolume = newVolume
                setVolume(SoundTherapyService.ACTION_SET_VOLUME_BELL, newVolume)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))
            
            IntervalSelector(
                intervals = intervals,
                selectedInterval = selectedInterval,
                onIntervalSelected = { selectedInterval = it }
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { checkPermissionsAndStart() },
                    enabled = !isPlaying
                ) {
                    Text("▶ Iniciar")
                }
                Spacer(modifier = Modifier.width(20.dp))
                Button(
                    onClick = {
                        val serviceIntent = Intent(context, SoundTherapyService::class.java).apply {
                            action = SoundTherapyService.ACTION_STOP
                        }
                        context.startService(serviceIntent)
                        isPlaying = false
                    },
                    enabled = isPlaying
                ) {
                    Text("■ Detener")
                }
            }
        }
    }
}

@Composable
fun AudioFileSelector(buttonText: String, fileName: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text(buttonText)
        }
        Text(fileName, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun VolumeSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntervalSelector(
    intervals: List<Long>,
    selectedInterval: Long,
    onIntervalSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text("Intervalo de la campana (minutos)")
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedInterval.toString(),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                intervals.forEach { interval ->
                    DropdownMenuItem(
                        text = { Text(interval.toString()) },
                        onClick = {
                            onIntervalSelected(interval)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
