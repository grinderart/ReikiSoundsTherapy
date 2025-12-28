package com.example.reikisoundstherapy

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

class SoundTherapyService : Service() {

    private var musicPlayer: MediaPlayer? = null
    private var binauralPlayer: MediaPlayer? = null
    private var bellPlayer: MediaPlayer? = null

    private val handler = Handler(Looper.getMainLooper())
    private var bellRunnable: Runnable? = null
    private var bellUri: Uri? = null
    private var bellVolume = 1.0f

    companion object {
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

                startForegroundService()
                startPlayback(musicUri, binauralUri, intervalMinutes)
            }
            ACTION_STOP -> {
                stopPlayback()
                stopSelf()
            }
            ACTION_SET_VOLUME_MUSIC -> {
                val volume = intent.getFloatExtra(EXTRA_VOLUME, 0.7f)
                musicPlayer?.setVolume(volume, volume)
            }
            ACTION_SET_VOLUME_BINAURAL -> {
                val volume = intent.getFloatExtra(EXTRA_VOLUME, 0.5f)
                binauralPlayer?.setVolume(volume, volume)
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
            musicPlayer = MediaPlayer.create(this, musicUri)?.apply {
                isLooping = true
                setVolume(0.7f, 0.7f)
                start()
            }
        }

        if (binauralUri != null) {
            binauralPlayer = MediaPlayer.create(this, binauralUri)?.apply {
                isLooping = true
                setVolume(0.5f, 0.5f)
                start()
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
                bellPlayer?.release()
                bellPlayer = MediaPlayer.create(this@SoundTherapyService, bellUri)?.apply {
                    setVolume(bellVolume, bellVolume)
                    setOnCompletionListener { mp -> mp.release() }
                    start()
                }
                handler.postDelayed(this, intervalMs)
            }
        }
        bellRunnable?.let { handler.post(it) }
    }

    private fun stopPlayback() {
        musicPlayer?.stop()
        musicPlayer?.release()
        musicPlayer = null

        binauralPlayer?.stop()
        binauralPlayer?.release()
        binauralPlayer = null

        bellPlayer?.release()
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
                PendingIntent.getActivity(this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE)
            }

        val stopServiceIntent = Intent(this, SoundTherapyService::class.java).apply {
            action = ACTION_STOP
        }
        val stopServicePendingIntent: PendingIntent =
            PendingIntent.getService(this, 0, stopServiceIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notification: Notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Terapia de Sonido Activa")
            .setContentText("La sesión de meditación está en curso.")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Consider creating a specific icon for the notification
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Parar", stopServicePendingIntent)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        stopPlayback()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}