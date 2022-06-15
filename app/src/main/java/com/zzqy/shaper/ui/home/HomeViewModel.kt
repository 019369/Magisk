package com.zzqy.shaper.ui.home

import android.content.Context
import androidx.core.net.toUri
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import com.zzqy.shaper.BuildConfig
import com.zzqy.shaper.R
import com.zzqy.shaper.arch.*
import com.zzqy.shaper.core.Config
import com.zzqy.shaper.core.Info
import com.zzqy.shaper.core.download.Subject
import com.zzqy.shaper.core.download.Subject.App
import com.zzqy.shaper.data.repository.NetworkService
import com.zzqy.shaper.databinding.itemBindingOf
import com.zzqy.shaper.databinding.set
import com.zzqy.shaper.events.SnackbarEvent
import com.zzqy.shaper.events.dialog.EnvFixDialog
import com.zzqy.shaper.events.dialog.ManagerInstallDialog
import com.zzqy.shaper.events.dialog.UninstallDialog
import com.zzqy.shaper.ktx.await
import com.zzqy.shaper.utils.Utils
import com.zzqy.shaper.utils.asText
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.launch
import me.tatarka.bindingcollectionadapter2.BR
import kotlin.math.roundToInt

enum class MagiskState {
    NOT_INSTALLED, UP_TO_DATE, OBSOLETE, LOADING
}

class HomeViewModel(
    private val svc: NetworkService
) : BaseViewModel() {

    val magiskTitleBarrierIds =
        intArrayOf(R.id.home_magisk_icon, R.id.home_magisk_title, R.id.home_magisk_button)
    val magiskDetailBarrierIds =
        intArrayOf(R.id.home_magisk_installed_version, R.id.home_device_details_ramdisk)
    val appTitleBarrierIds =
        intArrayOf(R.id.home_manager_icon, R.id.home_manager_title, R.id.home_manager_button)

    @get:Bindable
    var isNoticeVisible = Config.safetyNotice
        set(value) = set(value, field, { field = it }, BR.noticeVisible)

    val stateMagisk
        get() = when {
            !Info.env.isActive -> MagiskState.NOT_INSTALLED
            Info.env.versionCode < BuildConfig.VERSION_CODE -> MagiskState.OBSOLETE
            else -> MagiskState.UP_TO_DATE
        }

    @get:Bindable
    var stateManager = MagiskState.LOADING
        set(value) = set(value, field, { field = it }, BR.stateManager)

    val magiskInstalledVersion
        get() = Info.env.run {
            if (isActive)
                "$versionString ($versionCode)".asText()
            else
                R.string.not_available.asText()
        }

    @get:Bindable
    var managerRemoteVersion = R.string.loading.asText()
        set(value) = set(value, field, { field = it }, BR.managerRemoteVersion)

    val managerInstalledVersion
        get() = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})" +
            Info.stub?.let { " (${it.version})" }.orEmpty()

    @get:Bindable
    var stateManagerProgress = 0
        set(value) = set(value, field, { field = it }, BR.stateManagerProgress)

    val itemBinding = itemBindingOf<IconLink> {
        it.bindExtra(BR.viewModel, this)
    }

    companion object {
        private var checkedEnv = false
    }

    override fun refresh() = viewModelScope.launch {
        state = State.LOADING
        Info.getRemote(svc)?.apply {
            state = State.LOADED

            stateManager = when {
                BuildConfig.VERSION_CODE < magisk.versionCode -> MagiskState.OBSOLETE
                else -> MagiskState.UP_TO_DATE
            }

            managerRemoteVersion =
                "${magisk.version} (${magisk.versionCode}) (${stub.versionCode})".asText()
        } ?: run {
            state = State.LOADING_FAILED
            managerRemoteVersion = R.string.not_available.asText()
        }
        ensureEnv()
    }

    val showTest = false

    fun onTestPressed() = object : ViewEvent(), ActivityExecutor {
        override fun invoke(activity: UIActivity<*>) {
            /* Entry point to trigger test events within the app */
        }
    }.publish()

    fun onProgressUpdate(progress: Float, subject: Subject) {
        if (subject is App)
            stateManagerProgress = progress.times(100f).roundToInt()
    }

    fun onLinkPressed(link: String) = object : ViewEvent(), ContextExecutor {
        override fun invoke(context: Context) = Utils.openLink(context, link.toUri())
    }.publish()

    fun onDeletePressed() = UninstallDialog().publish()

    fun onManagerPressed() = when (state) {
        State.LOADED -> withExternalRW {
            withInstallPermission {
                ManagerInstallDialog().publish()
            }
        }
        State.LOADING -> SnackbarEvent(R.string.loading).publish()
        else -> SnackbarEvent(R.string.no_connection).publish()
    }

    fun onMagiskPressed() = withExternalRW {
        HomeFragmentDirections.actionHomeFragmentToInstallFragment().navigate()
    }

    fun hideNotice() {
        Config.safetyNotice = false
        isNoticeVisible = false
    }

    private suspend fun ensureEnv() {
        if (MagiskState.NOT_INSTALLED == stateMagisk || checkedEnv) return
        val cmd = "env_check ${Info.env.versionString} ${Info.env.versionCode}"
        if (!Shell.cmd(cmd).await().isSuccess) {
            EnvFixDialog(this).publish()
        }
        checkedEnv = true
    }

}
