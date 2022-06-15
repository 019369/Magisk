package com.zzqy.shaper.events.dialog

import androidx.lifecycle.lifecycleScope
import com.zzqy.shaper.BuildConfig
import com.zzqy.shaper.R
import com.zzqy.shaper.core.Info
import com.zzqy.shaper.core.base.BaseActivity
import com.zzqy.shaper.core.tasks.MagiskInstaller
import com.zzqy.shaper.ui.home.HomeViewModel
import com.zzqy.shaper.view.MagiskDialog
import kotlinx.coroutines.launch

class EnvFixDialog(private val vm: HomeViewModel) : DialogEvent() {

    override fun build(dialog: MagiskDialog) {
        dialog.apply {
            setTitle(R.string.env_fix_title)
            setMessage(R.string.env_fix_msg)
            setButton(MagiskDialog.ButtonType.POSITIVE) {
                text = android.R.string.ok
                doNotDismiss = true
                onClick {
                    dialog.apply {
                        setTitle(R.string.setup_title)
                        setMessage(R.string.setup_msg)
                        resetButtons()
                        setCancelable(false)
                    }
                    (dialog.ownerActivity as BaseActivity).lifecycleScope.launch {
                        MagiskInstaller.FixEnv {
                            dialog.dismiss()
                        }.exec()
                    }
                }
            }
            setButton(MagiskDialog.ButtonType.NEGATIVE) {
                text = android.R.string.cancel
            }
        }

        if (Info.env.versionCode != BuildConfig.VERSION_CODE ||
            Info.env.versionString != BuildConfig.VERSION_NAME) {
            dialog.setButton(MagiskDialog.ButtonType.POSITIVE) {
                text = android.R.string.ok
                onClick {
                    vm.onMagiskPressed()
                    dialog.dismiss()
                }
            }
        }
    }
}
