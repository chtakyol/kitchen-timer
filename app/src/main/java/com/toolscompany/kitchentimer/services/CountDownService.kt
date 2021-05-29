package com.toolscompany.kitchentimer.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.toolscompany.kitchentimer.R
import com.toolscompany.kitchentimer.other.Constants.ACTION_PAUSE
import com.toolscompany.kitchentimer.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.toolscompany.kitchentimer.other.Constants.ACTION_STOP
import com.toolscompany.kitchentimer.other.Constants.NOTIFICATION_CHANNEL_ID
import com.toolscompany.kitchentimer.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.toolscompany.kitchentimer.other.Constants.NOTIFICATION_ID
import com.toolscompany.kitchentimer.other.Constants.TIMER_UPDATE_INTERVAL
import com.toolscompany.kitchentimer.other.Utilities
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class CountDownService : LifecycleService() {

    var isFirst = true
    var serviceKilled = false

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var curNotificationBuilder: NotificationCompat.Builder

    companion object{
        val isRunning = MutableLiveData<Boolean>()
        val durationInMillis = MutableLiveData<Long>()

    }

    private fun postInitialValues()
    {
        isRunning.postValue(false)
        durationInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        curNotificationBuilder = baseNotificationBuilder
        postInitialValues()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE -> {
                    durationInMillis.postValue(it.getLongExtra("duration", 0L))
                    startForegroundService()
                }

                ACTION_PAUSE -> {
                    pauseService()
                }

                ACTION_STOP -> {
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startCountdownTimer(){
        isRunning.postValue(true)
        CoroutineScope(Dispatchers.Main).launch{
            while (isRunning.value!!){
                if (durationInMillis.value!! > 0) {
                    delay(1000)
                    durationInMillis.postValue(durationInMillis.value!! - 1000)
                    Timber.d("Countdown" + durationInMillis.value!!)
                }
                else {
                    onTimerFinished()
                    killService()
                }
            }
        }
    }

    private fun onTimerFinished() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            createNotificationChannel(notificationManager)
        }
        val notification = curNotificationBuilder
                .setContentText("Ready!!")
        notificationManager.notify(NOTIFICATION_ID, notification.build())
    }

    private fun startForegroundService() {
        startCountdownTimer()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        durationInMillis.observe(this, Observer {
            if(!serviceKilled){
                val notification = curNotificationBuilder
                        .setContentText(Utilities.getFormattedStopWatchTime(it))
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun pauseService()
    {
        isRunning.postValue(false)

    }

    private fun killService()
    {
        isFirst = true
        serviceKilled = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }
}