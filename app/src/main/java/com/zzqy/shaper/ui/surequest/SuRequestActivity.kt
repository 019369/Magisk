package com.zzqy.shaper.ui.surequest

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import com.zzqy.shaper.R
import com.zzqy.shaper.arch.UIActivity
import com.zzqy.shaper.core.su.SuCallbackHandler
import com.zzqy.shaper.core.su.SuCallbackHandler.REQUEST
import com.zzqy.shaper.databinding.ActivityRequestBinding
import com.zzqy.shaper.di.viewModel
import com.zzqy.shaper.ui.theme.Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class SuRequestActivity : UIActivity<ActivityRequestBinding>() {

    override val layoutRes: Int = R.layout.activity_request
    override val viewModel: SuRequestViewModel by viewModel()

    override fun onBackPressed() {
        viewModel.denyPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        lockOrientation()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.setHideOverlayWindows(true)
        }
        setTheme(Theme.selected.themeRes)
        super.onCreate(savedInstanceState)

        if (intent.action == Intent.ACTION_VIEW) {
            val action = intent.getStringExtra("action")
            if (action == REQUEST) {
                viewModel.handleRequest(intent)
            } else {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        SuCallbackHandler.run(this@SuRequestActivity, action, intent.extras)
                    }
                    finish()
                }
            }
        } else {
            finish()
        }
    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        theme.applyStyle(R.style.Foundation_Floating, true)
        return theme
    }

    private fun lockOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
    }
}
