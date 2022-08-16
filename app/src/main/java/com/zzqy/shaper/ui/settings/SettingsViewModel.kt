package com.zzqy.shaper.ui.settings

import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.lifecycle.viewModelScope
import com.zzqy.shaper.BR
import com.zzqy.shaper.BuildConfig
import com.zzqy.shaper.R
import com.zzqy.shaper.arch.BaseViewModel
import com.zzqy.shaper.core.Const
import com.zzqy.shaper.core.Info
import com.zzqy.shaper.core.isRunningAsStub
import com.zzqy.shaper.core.tasks.HideAPK
import com.zzqy.shaper.databinding.adapterOf
import com.zzqy.shaper.databinding.itemBindingOf
import com.zzqy.shaper.di.AppContext
import com.zzqy.shaper.events.AddHomeIconEvent
import com.zzqy.shaper.events.RecreateEvent
import com.zzqy.shaper.events.SnackbarEvent
import com.zzqy.shaper.events.dialog.BiometricEvent
import com.zzqy.shaper.ktx.activity
import com.zzqy.shaper.utils.Utils
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.launch

class SettingsViewModel : BaseViewModel(), BaseSettingsItem.Handler {

    val adapter = adapterOf<BaseSettingsItem>()
    val itemBinding = itemBindingOf<BaseSettingsItem> { it.bindExtra(BR.handler, this) }
    val items = createItems()

    init {
        viewModelScope.launch {
            Language.loadLanguages(this)
        }
    }

    private fun createItems(): List<BaseSettingsItem> {
        val context = AppContext
        val hidden = context.packageName != BuildConfig.APPLICATION_ID

        // Customization
        val list = mutableListOf(
            Customization,
            Theme, Language
        )
        if (isRunningAsStub && ShortcutManagerCompat.isRequestPinShortcutSupported(context))
            list.add(AddShortcut)

        // Manager
        list.addAll(listOf(
            AppSettings,
            UpdateChannel, UpdateChannelUrl, DoHToggle, UpdateChecker, DownloadPath
        ))
        if (Info.env.isActive) {
            if (Const.USER_ID == 0) {
                if (hidden) list.add(Restore) else list.add(Hide)
            }
        }

        // Magisk
        if (Info.env.isActive) {
            list.addAll(listOf(
                Magisk,
                SystemlessHosts
            ))
            if (Const.Version.atLeast_24_0()) {
                list.addAll(listOf(Zygisk, DenyList, DenyListConfig))
            }
        }

        // Superuser
        if (Utils.showSuperUser()) {
            list.addAll(listOf(
                Superuser,
                Tapjack, Biometrics, AccessMode, MultiuserMode, MountNamespaceMode,
                AutomaticResponse, RequestTimeout, SUNotification
            ))
            if (Build.VERSION.SDK_INT < 23) {
                // Biometric is only available on 6.0+
                list.remove(Biometrics)
            }
            if (Build.VERSION.SDK_INT < 26) {
                // Re-authenticate is not feasible on 8.0+
                list.add(Reauthenticate)
            }
            if (Build.VERSION.SDK_INT >= 31) {
                // Can hide overlay windows on 12.0+
                list.remove(Tapjack)
            }
        }

        return list
    }

    override fun onItemPressed(view: View, item: BaseSettingsItem, andThen: () -> Unit) {
        when (item) {
            DownloadPath -> withExternalRW(andThen)
            Biometrics -> authenticate(andThen)
            Theme -> SettingsFragmentDirections.actionSettingsFragmentToThemeFragment().navigate()
            DenyListConfig -> SettingsFragmentDirections.actionSettingsFragmentToDenyFragment().navigate()
            SystemlessHosts -> createHosts()
            Hide, Restore -> withInstallPermission(andThen)
            AddShortcut -> AddHomeIconEvent().publish()
            else -> andThen()
        }
    }

    override fun onItemAction(view: View, item: BaseSettingsItem) {
        when (item) {
            Language -> RecreateEvent().publish()
            UpdateChannel -> openUrlIfNecessary(view)
            is Hide -> viewModelScope.launch { HideAPK.hide(view.activity, item.value) }
            Restore -> viewModelScope.launch { HideAPK.restore(view.activity) }
            Zygisk -> if (Zygisk.mismatch) SnackbarEvent(R.string.reboot_apply_change).publish()
            else -> Unit
        }
    }

    private fun openUrlIfNecessary(view: View) {
        UpdateChannelUrl.refresh()
        if (UpdateChannelUrl.isEnabled && UpdateChannelUrl.value.isBlank()) {
            UpdateChannelUrl.onPressed(view, this)
        }
    }

    private fun authenticate(callback: () -> Unit) {
        BiometricEvent {
            // allow the change on success
            onSuccess { callback() }
        }.publish()
    }

    private fun createHosts() {
        Shell.cmd("add_hosts_module").submit {
            Utils.toast(R.string.settings_hosts_toast, Toast.LENGTH_SHORT)
        }
    }
}