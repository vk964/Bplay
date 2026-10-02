package com.bel9ja.boardclub.launch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal sealed class LaunchState {
    object Idle : LaunchState()
    object Checking : LaunchState()
    object App : LaunchState()
    data class Web(val url: String, val resume: Boolean) : LaunchState()
}

/** Survives rotation so the launch check runs only once per launch. */
internal class LaunchViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow<LaunchState>(LaunchState.Idle)
    val state: StateFlow<LaunchState> = _state

    /** WebView screen has already been started for this decision. */
    var webHandedOff: Boolean = false

    val isIdle: Boolean get() = _state.value == LaunchState.Idle

    fun openAppDirectly() {
        _state.value = LaunchState.App
    }

    fun startIfNeeded() {
        if (_state.value != LaunchState.Idle) return
        _state.value = LaunchState.Checking
        viewModelScope.launch {
            _state.value = try {
                when (val d = LaunchRouter(getApplication()).decide()) {
                    Destination.App -> LaunchState.App
                    is Destination.Web -> LaunchState.Web(d.url, d.resume)
                }
            } catch (e: Exception) {
                LaunchState.App // any technical error -> app functionality
            }
        }
    }
}
