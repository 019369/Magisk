package com.zzqy.shaper.events.dialog

import android.app.ProgressDialog
import android.content.Context
import android.widget.Toast
import com.zzqy.shaper.R
import com.zzqy.shaper.arch.NavigationActivity
import com.zzqy.shaper.ui.flash.FlashFragment
import com.zzqy.shaper.utils.Utils
import com.zzqy.shaper.view.MagiskDialog
import com.topjohnwu.superuser.Shell

class UninstallDialog : DialogEvent() {

    override fun build(dialog: MagiskDialog) {
        dialog.apply {
            setTitle(R.string.uninstall_magisk_title)
            setMessage(R.string.uninstall_magisk_msg)
            setButton(MagiskDialog.ButtonType.POSITIVE) {
                text = R.string.restore_img
                onClick { restore(dialog.context) }
            }
            setButton(MagiskDialog.ButtonType.NEGATIVE) {
                text = R.string.complete_uninstall
                onClick { completeUninstall(dialog) }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun restore(context: Context) {
        val dialog = ProgressDialog(context).apply {
            setMessage(context.getString(R.string.restore_img_msg))
            show()
        }

        Shell.cmd("restore_imgs").submit { result ->
            dialog.dismiss()
            if (result.isSuccess) {
                Utils.toast(R.string.restore_done, Toast.LENGTH_SHORT)
            } else {
                Utils.toast(R.string.restore_fail, Toast.LENGTH_LONG)
            }
        }
    }

    private fun completeUninstall(dialog: MagiskDialog) {
        (dialog.ownerActivity as NavigationActivity<*>)
            .navigation.navigate(FlashFragment.uninstall())
    }

}
