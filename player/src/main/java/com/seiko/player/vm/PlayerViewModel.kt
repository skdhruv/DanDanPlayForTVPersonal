package com.seiko.player.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceDataStore
import com.seiko.player.data.model.Progress
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlin.math.abs

class PlayerViewModel : ViewModel() {


    private val _progress = MutableLiveData<Progress>().apply { value = Progress() }
    val progress: LiveData<Progress> = _progress

    private var lastTime = 0L

//    @ExperimentalCoroutinesApi
//    @ObsoleteCoroutinesApi
//    private val eventActor = viewModelScope.actor<MediaPlayer.Event>(capacity = Channel.UNLIMITED, start = CoroutineStart.UNDISPATCHED) {
//        for (event in channel) {
//            when(event.type) {
//                MediaPlayer.Event.LengthChanged -> {
//                    updateProgress(newLength = event.lengthChanged)
//                }
//                MediaPlayer.Event.TimeChanged -> {
//                    val time = event.timeChanged
//                    if (abs(time - lastTime) > 950L) {
//                        updateProgress(newTime = time)
//                        lastTime = time
//                    }
//                }
//            }
//        }
//    }

    private fun updateProgress(newTime: Long = -1L, newLength: Long = -1L) {
        _progress.value = _progress.value?.apply {
            if (newTime != -1L) time = newTime
            if (newLength != -1L) length = newLength
        }
    }
//
//    fun seekTo(position: Int) {
//        seekTo(position.toFloat() / 100)
//    }

}