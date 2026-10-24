package com.example.reikisoundstherapy

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat

class SoundTherapyService : Service(), AudioManager.OnAudioFocusChangeListener {

    private var musicPlayer: MediaPlayer? = null
    private var binauralPlayer: MediaPlayer? = null
    private var bellPlayer: MediaPlayer? = null

    private val handler = Handler(Looper.getMainLooper())
    private var bellRunnable: Runnable? = null
    private var bellUri: Uri? = null
    private var bellVolume = 1.0f

    private var musicVolume = 0.7f
    private var binauralVolume = 0.5f

    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    companion object {
        private const val TAG = "SoundTherapyService"
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_SET_VOLUME_MUSIC = "ACTION_SET_VOLUME_MUSIC"
        const val ACTION_SET_VOLUME_BINAURAL = "ACTION_SET_VOLUME_BINAURAL"
        const val ACTION_SET_VOLUME_BELL = "ACTION_SET_VOLUME_BELL"
        const val EXTRA_MUSIC_URI = "EXTRA_MUSIC_URI"
        const val EXTRA_BINAURAL_URI = "EXTRA_BINAURAL_URI"
        const val EXTRA_BELL_URI = "EXTRA_BELL_URI"
        const val EXTRA_INTERVAL = "EXTRA_INTERVAL"
        const val EXTRA_VOLUME = "EXTRA_VOLUME"
        const val NOTIFICATION_CHANNEL_ID = "SoundTherapyChannel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val musicUriString = intent.getStringExtra(EXTRA_MUSIC_URI)
                val binauralUriString = intent.getStringExtra(EXTRA_BINAURAL_URI)
                val bellUriString = intent.getStringExtra(EXTRA_BELL_URI)

                val musicUri: Uri? = musicUriString?.let { Uri.parse(it) }
                val binauralUri: Uri? = binauralUriString?.let { Uri.parse(it) }

                if (bellUriString != null) {
                    bellUri = Uri.parse(bellUriString)
                }

                val intervalMinutes = intent.getLongExtra(EXTRA_INTERVAL, 5)

                requestAudioFocus()
                startForegroundService()
                startPlayback(musicUri, binauralUri, intervalMinutes)
            }
            ACTION_STOP -> {
                stopPlayback()
                abandonAudioFocus()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_SET_VOLUME_MUSIC -> {
                musicVolume = intent.getFloatExtra(EXTRA_VOLUME, 0.7f)
                musicPlayer?.setVolume(musicVolume, musicVolume)
            }
            ACTION_SET_VOLUME_BINAURAL -> {
                binauralVolume = intent.getFloatExtra(EXTRA_VOLUME, 0.5f)
                binauralPlayer?.setVolume(binauralVolume, binauralVolume)
            }
            ACTION_SET_VOLUME_BELL -> {
                bellVolume = intent.getFloatExtra(EXTRA_VOLUME, 1.0f)
            }
        }
        return START_NOT_STICKY
    }

    private fun startPlayback(musicUri: Uri?, binauralUri: Uri?, interval: Long) {
        stopPlayback()

        if (musicUri != null) {
            try {
                musicPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(this@SoundTherapyService, musicUri)
                    isLooping = true
                    setVolume(musicVolume, musicVolume)
                    prepare()
                    start()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting musicPlayer: ${e.message}", e)
            }
        }

        if (binauralUri != null) {
            try {
                binauralPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(this@SoundTherapyService, binauralUri)
                    isLooping = true
                    setVolume(binauralVolume, binauralVolume)
                    prepare()
                    start()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting binauralPlayer: ${e.message}", e)
            }
        }

        if (bellUri != null) {
            scheduleBell(interval * 60 * 1000)
        }
    }

    private fun scheduleBell(intervalMs: Long) {
        bellRunnable?.let { handler.removeCallbacks(it) }
        bellRunnable = object : Runnable {
            override fun run() {
                try {
                    bellPlayer?.release()
                    if (bellUri != null) {
                        bellPlayer = MediaPlayer().apply {
                            setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .build()
                            )
                            setDataSource(this@SoundTherapyService, bellUri!!)
                            setVolume(bellVolume, bellVolume)
                            setOnCompletionListener { mp -> mp.release() }
                            prepare()
                            start()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error playing bell: ${e.message}", e)
                }
                handler.postDelayed(this, intervalMs)
            }
        }
        bellRunnable?.let { handler.post(it) }
    }

    private fun stopPlayback() {
        try {
            musicPlayer?.stop()
            musicPlayer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping musicPlayer: ${e.message}")
        }
        musicPlayer = null

        try {
            binauralPlayer?.stop()
            binauralPlayer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping binauralPlayer: ${e.message}")
        }
        binauralPlayer = null

        try {
            bellPlayer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing bellPlayer: ${e.message}")
        }
        bellPlayer = null

        bellRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Canal Terapia de Sonido",
                NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(
                    this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

        val stopServiceIntent = Intent(this, SoundTherapyService::class.java).apply {
            action = ACTION_STOP
        }
        val stopServicePendingIntent: PendingIntent =
            PendingIntent.getService(
                this, 0, stopServiceIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        val notification: Notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Terapia de Sonido Activa")
            .setContentText("La sesión de armonización está en curso.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Detener", stopServicePendingIntent)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(this)
                .build()

            audioManager?.requestAudioFocus(audioFocusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(this)
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                stopPlayback()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                musicPlayer?.pause()
                binauralPlayer?.pause()
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                musicPlayer?.start()
                binauralPlayer?.start()
            }
        }
    }

    override fun onDestroy() {
        stopPlayback()
        abandonAudioFocus()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
