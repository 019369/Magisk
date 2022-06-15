package com.zzqy.shaper.events.dialog

import com.zzqy.shaper.arch.ActivityExecutor
import com.zzqy.shaper.arch.UIActivity
import com.zzqy.shaper.arch.ViewEvent
import com.zzqy.shaper.core.utils.BiometricHelper

class BiometricEvent(
    builder: Builder.() -> Unit
) : ViewEvent(), ActivityExecutor {

    private var listenerOnFailure: GenericDialogListener = {}
    private var listenerOnSuccess: GenericDialogListener = {}

    init {
        builder(Builder())
    }

    override fun invoke(activity: UIActivity<*>) {
        BiometricHelper.authenticate(
            activity,
            onError = listenerOnFailure,
            onSuccess = listenerOnSuccess
        )
    }

    inner class Builder internal constructor() {

        fun onFailure(listener: GenericDialogListener) {
            listenerOnFailure = listener
        }

        fun onSuccess(listener: GenericDialogListener) {
            listenerOnSuccess = listener
        }
    }

}
