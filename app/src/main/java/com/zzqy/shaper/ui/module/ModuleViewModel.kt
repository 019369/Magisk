package com.zzqy.shaper.ui.module

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zzqy.shaper.BR
import com.zzqy.shaper.R
import com.zzqy.shaper.arch.BaseViewModel
import com.zzqy.shaper.core.Info
import com.zzqy.shaper.core.base.ContentResultCallback
import com.zzqy.shaper.core.model.module.LocalModule
import com.zzqy.shaper.core.model.module.OnlineModule
import com.zzqy.shaper.databinding.RvItem
import com.zzqy.shaper.databinding.diffListOf
import com.zzqy.shaper.databinding.itemBindingOf
import com.zzqy.shaper.events.GetContentEvent
import com.zzqy.shaper.events.SnackbarEvent
import com.zzqy.shaper.events.dialog.ModuleInstallDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import me.tatarka.bindingcollectionadapter2.collections.MergeObservableList

class ModuleViewModel : BaseViewModel() {

    val bottomBarBarrierIds = intArrayOf(R.id.module_update, R.id.module_remove)

    private val itemsInstalled = diffListOf<LocalModuleRvItem>()

    val items = MergeObservableList<RvItem>()
    val itemBinding = itemBindingOf<RvItem> {
        it.bindExtra(BR.viewModel, this)
    }

    val data get() = uri

    init {
        if (Info.env.isActive) {
            items.insertItem(InstallModule)
                .insertList(itemsInstalled)
        }
    }

    override fun refresh(): Job {
        return viewModelScope.launch {
            state = State.LOADING
            loadInstalled()
            state = State.LOADED
            loadUpdateInfo()
        }
    }

    private suspend fun loadInstalled() {
        val installed = LocalModule.installed().map { LocalModuleRvItem(it) }
        val diff = withContext(Dispatchers.Default) {
            itemsInstalled.calculateDiff(installed)
        }
        itemsInstalled.update(installed, diff)
    }

    private suspend fun loadUpdateInfo() {
        withContext(Dispatchers.IO) {
            itemsInstalled.forEach {
                if (it.item.fetch())
                    it.fetchedUpdateInfo()
            }
        }
    }

    fun downloadPressed(item: OnlineModule?) =
        if (item != null && isConnected.get()) {
            withExternalRW { ModuleInstallDialog(item).publish() }
        } else {
            SnackbarEvent(R.string.no_connection).publish()
        }

    fun installPressed() = withExternalRW {
        GetContentEvent("application/zip", UriCallback()).publish()
    }

    @Parcelize
    class UriCallback : ContentResultCallback {
        override fun onActivityResult(result: Uri) {
            uri.value = result
        }
    }

    companion object {
        private val uri = MutableLiveData<Uri?>()
    }
}
