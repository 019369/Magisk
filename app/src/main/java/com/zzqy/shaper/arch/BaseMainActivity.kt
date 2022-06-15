package com.zzqy.shaper.arch

import android.Manifest.permission.REQUEST_INSTALL_PACKAGES
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import com.zzqy.shaper.BuildConfig.APPLICATION_ID
import com.zzqy.shaper.R
import com.zzqy.shaper.core.Config
import com.zzqy.shaper.core.Const
import com.zzqy.shaper.core.JobService
import com.zzqy.shaper.core.isRunningAsStub
import com.zzqy.shaper.core.tasks.HideAPK
import com.zzqy.shaper.core.utils.RootUtils
import com.zzqy.shaper.di.ServiceLocator
import com.zzqy.shaper.ui.theme.Theme
import com.zzqy.shaper.utils.Utils
import com.zzqy.shaper.view.MagiskDialog
import com.zzqy.shaper.view.Notifications
import com.zzqy.shaper.view.Shortcuts
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.launch

abstract class BaseMainActivity<Binding : ViewDataBinding> : NavigationActivity<Binding>() {

    companion object {
        private var doPreload = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Theme.selected.themeRes)

        if (isRunningAsStub && doPreload) {
            // Manually apply splash theme for stub
            theme.applyStyle(R.style.StubSplashTheme, true)
        }

        super.onCreate(savedInstanceState)

        if (!isRunningAsStub) {
            val splashScreen = installSplashScreen()
            splashScreen.setKeepOnScreenCondition { doPreload }
        }

        if (doPreload) {
            Shell.getShell(Shell.EXECUTOR) {
                if (isRunningAsStub && !it.isRoot) {
                    showInvalidStateMessage()
                    return@getShell
                }
                preLoad()
                runOnUiThread {
                    doPreload = false
                    if (isRunningAsStub) {
                        // Re-launch main activity without splash theme
                        relaunch()
                    } else {
                        showMainUI(savedInstanceState)
                    }
                }
            }
        } else {
            showMainUI(savedInstanceState)
        }
    }

    abstract fun showMainUI(savedInstanceState: Bundle?)

    @SuppressLint("InlinedApi")
    private fun showInvalidStateMessage(): Unit = runOnUiThread {
        MagiskDialog(this).apply {
            setTitle(R.string.unsupport_nonroot_stub_title)
            setMessage(R.string.unsupport_nonroot_stub_msg)
            setButton(MagiskDialog.ButtonType.POSITIVE) {
                text = R.string.install
                onClick {
                    withPermission(REQUEST_INSTALL_PACKAGES) {
                        if (!it) {
                            Utils.toast(R.string.install_unknown_denied, Toast.LENGTH_SHORT)
                            showInvalidStateMessage()
                        } else {
                            lifecycleScope.launch {
                                HideAPK.restore(this@BaseMainActivity)
                            }
                        }
                    }
                }
            }
            setCancelable(false)
            show()
        }
    }

    private fun preLoad() {
        val prevPkg = intent.getStringExtra(Const.Key.PREV_PKG)

        Config.load(prevPkg)
        handleRepackage(prevPkg)
        Notifications.setup(this)
        JobService.schedule(this)
        Shortcuts.setupDynamic(this)

        // Pre-fetch network services
        ServiceLocator.networkService

        // Wait for root service
        RootUtils.Connection.await()
    }

    private fun handleRepackage(pkg: String?) {
        if (packageName != APPLICATION_ID) {
            runCatching {
                // Hidden, remove com.zzqy.shaper if exist as it could be malware
                packageManager.getApplicationInfo(APPLICATION_ID, 0)
                Shell.cmd("(pm uninstall $APPLICATION_ID)& >/dev/null 2>&1").exec()
            }
        } else {
            if (Config.suManager.isNotEmpty())
                Config.suManager = ""
            pkg ?: return
            if (!Shell.cmd("(pm uninstall $pkg)& >/dev/null 2>&1").exec().isSuccess) {
                // Uninstall through Android API
                uninstallAndWait(pkg)
            }
        }
    }

}
