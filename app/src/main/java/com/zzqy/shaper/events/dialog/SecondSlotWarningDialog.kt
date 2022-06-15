package com.zzqy.shaper.events.dialog

import com.zzqy.shaper.R
import com.zzqy.shaper.view.MagiskDialog

class SecondSlotWarningDialog : DialogEvent() {

    override fun build(dialog: MagiskDialog) {
        dialog.apply {
            setTitle(android.R.string.dialog_alert_title)
            setMessage(R.string.install_inactive_slot_msg)
            setButton(MagiskDialog.ButtonType.POSITIVE) {
                text = android.R.string.ok
            }
            setCancelable(true)
        }
    }
}
