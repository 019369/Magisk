package com.zzqy.shaper.events.dialog

import com.zzqy.shaper.R
import com.zzqy.shaper.core.download.Action
import com.zzqy.shaper.core.download.DownloadService
import com.zzqy.shaper.core.download.Subject
import com.zzqy.shaper.core.model.module.OnlineModule
import com.zzqy.shaper.di.ServiceLocator
import com.zzqy.shaper.view.MagiskDialog

class ModuleInstallDialog(private val item: OnlineModule) : MarkDownDialog() {

    private val svc get() = ServiceLocator.networkService

    override suspend fun getMarkdownText(): String {
        val str = svc.fetchString(item.changelog)
        return if (str.length > 1000) str.substring(0, 1000) else str
    }

    override fun build(dialog: MagiskDialog) {
        super.build(dialog)
        dialog.apply {

            fun download(install: Boolean) {
                val action = if (install) Action.Flash else Action.Download
                val subject = Subject.Module(item, action)
                DownloadService.start(context, subject)
            }

            val title = context.getString(R.string.repo_install_title,
                item.name, item.version, item.versionCode)

            setTitle(title)
            setCancelable(true)
            setButton(MagiskDialog.ButtonType.NEGATIVE) {
                text = R.string.download
                onClick { download(false) }
            }
            setButton(MagiskDialog.ButtonType.POSITIVE) {
                text = R.string.install
                onClick { download(true) }
            }
            setButton(MagiskDialog.ButtonType.NEUTRAL) {
                text = android.R.string.cancel
            }
        }
    }

}
