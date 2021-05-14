package com.joshsoftware.core.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
//import com.google.firebase.crashlytics.ktx.crashlytics
import kotlinx.coroutines.launch
import timber.log.Timber


open class BaseViewModel<T>(): ViewModel() {
    protected var _result = MutableLiveData<T>()
    protected var _error = MutableLiveData<String>()
    protected var _spinner = MutableLiveData<Boolean>()

    val result: LiveData<T>
        get() = _result

    val error: LiveData<String>
        get() = _error

    val spinner: LiveData<Boolean>
        get() = _spinner

    fun executeRoutine(execute: suspend () -> Unit) {
        _spinner.value = true
        viewModelScope.launch {
            try {
                execute()
            } catch (ex: Exception) {
                logException(ex)
            } finally {
                _spinner.value = false
            }
        }
    }

    fun logException(ex: Exception) {
//        Firebase.crashlytics.recordException(ex as Throwable)
        Timber.e(ex)
        _error.value = ex.localizedMessage
    }
}