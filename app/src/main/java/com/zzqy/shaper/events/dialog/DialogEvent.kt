package com.zzqy.shaper.events.dialog

import com.zzqy.shaper.arch.ActivityExecutor
import com.zzqy.shaper.arch.UIActivity
import com.zzqy.shaper.arch.ViewEvent
import com.zzqy.shaper.view.MagiskDialog

abstract class DialogEvent : ViewEvent(), ActivityExecutor {

    override fun invoke(activity: UIActivity<*>) {
        MagiskDialog(activity)
            .apply { setOwnerActivity(activity) }
            .apply(this::build).show()
    }

    abstract fun build(dialog: MagiskDialog)

}

typealias GenericDialogListener = () -> Unit
