package com.toolscompany.kitchentimer.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.toolscompany.kitchentimer.R
import com.toolscompany.kitchentimer.other.Constants.ACTION_PAUSE
import com.toolscompany.kitchentimer.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.toolscompany.kitchentimer.other.Utilities
import com.toolscompany.kitchentimer.services.CountDownService
import com.toolscompany.kitchentimer.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_counter.*
import timber.log.Timber

@AndroidEntryPoint
class CounterFragment : Fragment(R.layout.fragment_counter) {

    private var menu: Menu? = null

    private var isRunning = false
    private var mins = 1L
    private var previousPos = 0.0
    private var curDurationInMillis = 999L

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        circularSlider.setBorderColor(R.color.purple_500)
        circularSlider.setThumbColor(R.color.purple_700)
        circularSlider.setStartAngle(3.14)
        circularSlider.setAngle(3.14)
        circularSlider.setOnSliderMovedListener {
            if (it < 0.0)
            {
                if (it % 0.6 <= 0.1 && previousPos < it && mins > 1){
                    previousPos = it
                    mins--
                    minVal.text = Utilities.getFormattedStopWatchTime((mins * 60L * 1000L))
                }
                else if (it % 0.6 <= 0.1 && previousPos > it && mins > 0){
                    previousPos = it
                    mins++
                    minVal.text = Utilities.getFormattedStopWatchTime((mins * 60L * 1000L))
                }
            }
        }

        button1.setOnClickListener {
            toggleButton()
        }

        subscribeToObserver()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_menu, menu)
        this.menu = menu
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings ->
                navHostFragment.findNavController().navigate(R.id.action_counterFragment_to_settingsFragment)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun sendCommandToService(action: String, duration:Long)
    {
        Intent(requireContext(), CountDownService::class.java).also {
            it.action = action
            it.putExtra("duration", duration)
            requireContext().startService(it)
        }
    }

    private fun subscribeToObserver() {
        CountDownService.isRunning.observe(viewLifecycleOwner, Observer {
            updateIsRunning(it)
        })

        CountDownService.durationInMillis.observe(viewLifecycleOwner, Observer {
            curDurationInMillis = it
            val formattedTime = Utilities.getFormattedStopWatchTime(curDurationInMillis)
            minVal.text = formattedTime
        })
    }

    private fun updateIsRunning(isRunning: Boolean) {
        this.isRunning = isRunning
    }

    private fun toggleButton(){
        if (isRunning){
            sendCommandToService(ACTION_PAUSE, mins * 60L * 1000L)
            button1.text = "Pause"
        }
        else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE, mins * 60L * 1000L)
            button1.text = "Start"
        }
    }
}