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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.reikisoundstherapy.ui.theme.ReikiSoundsTherapyTheme
import kotlin.math.roundToInt

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current

    // Default URIs from res/raw
    val defaultMusicUri = Uri.parse("android.resource://${context.packageName}/${R.raw.bosque_aves}")
    val defaultBinauralUri = Uri.parse("android.resource://${context.packageName}/${R.raw.theta_binaural}")
    val defaultBellUri = Uri.parse("android.resource://${context.packageName}/${R.raw.ring}")

    var musicUri by remember { mutableStateOf<Uri?>(defaultMusicUri) }
    var binauralUri by remember { mutableStateOf<Uri?>(defaultBinauralUri) }
    var bellUri by remember { mutableStateOf<Uri?>(defaultBellUri) }

    var musicFileName by remember { mutableStateOf("Bosque y Aves (Naturaleza)") }
    var binauralFileName by remember { mutableStateOf("Onda Theta 1Hz (Reiki / Frecuencia)") }
    var bellFileName by remember { mutableStateOf("Campana Tibetana (Cuenco)") }

    var musicVolume by remember { mutableStateOf(0.7f) }
    var binauralVolume by remember { mutableStateOf(0.5f) }
    var bellVolume by remember { mutableStateOf(1.0f) }

    val intervals = listOf(1L, 3L, 5L, 10L, 15L, 20L, 30L)
    var selectedInterval by remember { mutableStateOf(intervals[2]) } // Default to 5 mins

    val sessionDurations = listOf(0L, 15L, 30L, 45L, 60L, 90L)
    var selectedSessionDuration by remember { mutableStateOf(sessionDurations[0]) } // Default to 0 (Infinite)

    var isPlaying by remember { mutableStateOf(false) }

    val selectMusicLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            musicUri = it
            musicFileName = it.lastPathSegment ?: "Archivo personalizado"
        }
    }

    val selectBinauralLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            binauralUri = it
            binauralFileName = it.lastPathSegment ?: "Archivo personalizado"
        }
    }

    val selectBellLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            bellUri = it
            bellFileName = it.lastPathSegment ?: "Archivo personalizado"
        }
    }
    
    val startAudioService = {
        val serviceIntent = Intent(context, SoundTherapyService::class.java).apply {
            action = SoundTherapyService.ACTION_START
            putExtra(SoundTherapyService.EXTRA_MUSIC_URI, musicUri?.toString())
            putExtra(SoundTherapyService.EXTRA_BINAURAL_URI, binauralUri?.toString())
            putExtra(SoundTherapyService.EXTRA_BELL_URI, bellUri?.toString())
            putExtra(SoundTherapyService.EXTRA_INTERVAL, selectedInterval)
            putExtra(SoundTherapyService.EXTRA_SESSION_DURATION, selectedSessionDuration)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
        isPlaying = true
        Toast.makeText(context, "Sesión de Terapia Iniciada", Toast.LENGTH_SHORT).show()
    }

    val requestPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            startAudioService()
        } else {
            Toast.makeText(context, "Se requieren permisos de audio y notificaciones para continuar.", Toast.LENGTH_LONG).show()
        }
    }

    fun checkPermissionsAndStart() {
        if (bellUri == null && musicUri == null && binauralUri == null) {
            Toast.makeText(context, "Selecciona al menos un sonido o campana para tu sesión", Toast.LENGTH_LONG).show()
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Reiki Sound Therapy",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "Relajación, Solfeggio y Reiki",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isPlaying) {
                        Button(
                            onClick = { checkPermissionsAndStart() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Iniciar Sesión de Terapia", style = MaterialTheme.typography.titleMedium)
                        }
                    } else {
                        Button(
                            onClick = {
                                val serviceIntent = Intent(context, SoundTherapyService::class.java).apply {
                                    action = SoundTherapyService.ACTION_STOP
                                }
                                context.startService(serviceIntent)
                                isPlaying = false
                                Toast.makeText(context, "Sesión Finalizada", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Detener Sesión", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Banner if playing
            AnimatedVisibility(
                visible = isPlaying,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        val statusMsg = if (selectedSessionDuration > 0) {
                            "Sesión activa (Temporizador: ${selectedSessionDuration} min)..."
                        } else {
                            "Sesión de armonización continua en curso..."
                        }
                        Text(
                            statusMsg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // ==================== SECTION 1: MÚSICA DE FONDO & AMBIENTE ====================
            SectionHeader(
                icon = Icons.Default.Nature,
                title = "Música de Fondo y Entorno Natural"
            )

            // Quick ambient environment & custom music selector chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val isBosque = musicUri?.toString()?.contains("bosque_aves") == true
                val isAgua = musicUri?.toString()?.contains("agua_lluvia") == true
                val isFuego = musicUri?.toString()?.contains("fuego_lena") == true
                val isCustom = !isBosque && !isAgua && !isFuego && musicUri != null

                FilterChip(
                    selected = isBosque,
                    onClick = {
                        musicUri = Uri.parse("android.resource://${context.packageName}/${R.raw.bosque_aves}")
                        musicFileName = "Bosque y Aves (Naturaleza)"
                    },
                    label = { Text("🌲 Bosque") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = isAgua,
                    onClick = {
                        musicUri = Uri.parse("android.resource://${context.packageName}/${R.raw.agua_lluvia_alt}")
                        musicFileName = "Agua y Lluvia (Ambiente)"
                    },
                    label = { Text("💧 Agua") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = isFuego,
                    onClick = {
                        musicUri = Uri.parse("android.resource://${context.packageName}/${R.raw.fuego_lena}")
                        musicFileName = "Fuego de Leña (Cálido)"
                    },
                    label = { Text("🔥 Fuego") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = isCustom,
                    onClick = {
                        selectMusicLauncher.launch("audio/*")
                    },
                    label = { Text("🎵 Música") },
                    modifier = Modifier.weight(1f)
                )
            }

            SoundTrackCard(
                title = "Música de Fondo / Sonido Ambiental",
                description = "Elige un ambiente o sube tu propia música relajante",
                fileName = musicFileName,
                volume = musicVolume,
                icon = Icons.Default.Audiotrack,
                onSelectClick = { selectMusicLauncher.launch("audio/*") },
                onVolumeChange = {
                    musicVolume = it
                    setVolume(SoundTherapyService.ACTION_SET_VOLUME_MUSIC, it)
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ==================== SECTION 2: FRECUENCIAS REIKI & CAMPANA ====================
            SectionHeader(
                icon = Icons.Default.Spa,
                title = "Frecuencias Reiki y Atención Plena"
            )

            SoundTrackCard(
                title = "Ondas Binaurales / Solfeggio",
                description = "Frecuencia de sanación y ondas cerebrales",
                fileName = binauralFileName,
                volume = binauralVolume,
                icon = Icons.Default.Audiotrack,
                onSelectClick = { selectBinauralLauncher.launch("audio/*") },
                onVolumeChange = {
                    binauralVolume = it
                    setVolume(SoundTherapyService.ACTION_SET_VOLUME_BINAURAL, it)
                }
            )

            SoundTrackCard(
                title = "Campana / Cuenco Periódico",
                description = "Sonido de atención plena a intervalos",
                fileName = bellFileName,
                volume = bellVolume,
                icon = Icons.Default.NotificationsActive,
                onSelectClick = { selectBellLauncher.launch("audio/*") },
                onVolumeChange = {
                    bellVolume = it
                    setVolume(SoundTherapyService.ACTION_SET_VOLUME_BELL, it)
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ==================== SECTION 3: DURACIÓN & INTERVALOS ====================
            SectionHeader(
                icon = Icons.Default.Timer,
                title = "Duración y Tiempos de Meditación"
            )

            // Session Total Duration Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        "Duración Total de la Sesión",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Tiempo total antes de finalizar automáticamente la sesión",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        sessionDurations.forEach { duration ->
                            val selected = selectedSessionDuration == duration
                            val labelText = if (duration == 0L) "∞ Infinito" else "${duration}m"
                            FilterChip(
                                selected = selected,
                                onClick = { selectedSessionDuration = duration },
                                label = { Text(labelText) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Bell Interval Selector Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        "Intervalo de la Campana / Cuenco",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Frecuencia temporal con la que sonará la campana de aviso",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        intervals.forEach { interval ->
                            val selected = selectedInterval == interval
                            FilterChip(
                                selected = selected,
                                onClick = { selectedInterval = interval },
                                label = { Text("${interval}m") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // Extra padding for bottom bar
        }
    }
}

@Composable
fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SoundTrackCard(
    title: String,
    description: String,
    fileName: String,
    volume: Float,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onSelectClick: () -> Unit,
    onVolumeChange: (Float) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // File selection row
            OutlinedButton(
                onClick = onSelectClick,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = fileName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Volume Slider row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = volume,
                    onValueChange = onVolumeChange,
                    valueRange = 0f..1f,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(volume * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "MainScreen Preview", showSystemUi = true)
@Composable
fun MainScreenPreview() {
    ReikiSoundsTherapyTheme {
        MainScreen()
    }
}
