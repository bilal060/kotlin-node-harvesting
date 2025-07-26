package com.devicesync.app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.devicesync.app.R

class SyncService : Service() {
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        startForeground(1, NotificationCompat.Builder(this, "sync_channel")
            .setContentTitle("Device Sync")
            .setContentText("Syncing device data...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build())
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle sync logic here
        return START_NOT_STICKY
    }
} 