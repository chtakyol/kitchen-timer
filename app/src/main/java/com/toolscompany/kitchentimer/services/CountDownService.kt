package com.toolscompany.kitchentimer.services

import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.toolscompany.kitchentimer.other.Constants.ACTION_PAUSE
import com.toolscompany.kitchentimer.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.toolscompany.kitchentimer.other.Constants.ACTION_STOP
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class CountDownService : LifecycleService() {

    companion object{
        val isRunning = MutableLiveData<Boolean>()
        val durationInMillis = MutableLiveData<Long>()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE -> {
                    durationInMillis.postValue(it.getLongExtra("duration", 0L))
                    Timber.d("Service Start!" + durationInMillis.toString())
                }

                ACTION_PAUSE -> {

                }

                ACTION_STOP -> {

                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

}