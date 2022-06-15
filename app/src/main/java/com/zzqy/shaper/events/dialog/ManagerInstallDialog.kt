package com.zzqy.shaper.events.dialog

import com.zzqy.shaper.R
import com.zzqy.shaper.core.Info
import com.zzqy.shaper.core.download.DownloadService
import com.zzqy.shaper.core.download.Subject
import com.zzqy.shaper.di.AppContext
import com.zzqy.shaper.di.ServiceLocator
import com.zzqy.shaper.view.MagiskDialog
import java.io.File

class ManagerInstallDialog : MarkDownDialog() {

    private val svc get() = ServiceLocator.networkService

    override suspend fun getMarkdownText(): String {
        val text = svc.fetchString(Info.remote.magisk.note)
        // Cache the changelog
        AppContext.cacheDir.listFiles { _, name -> name.endsWith(".md") }.orEmpty().forEach {
            it.delete()
        }
        File(AppContext.cacheDir, "${Info.remote.magisk.versionCode}.md").writeText(text)
        return text
    }

    override fun build(dialog: MagiskDialog) {
        super.build(dialog)
        dialog.apply {
            setCancelable(true)
            setButton(MagiskDialog.ButtonType.POSITIVE) {
                text = R.string.install
                onClick { DownloadService.start(context, Subject.App()) }
            }
            setButton(MagiskDialog.ButtonType.NEGATIVE) {
                text = android.R.string.cancel
            }
        }
    }

}
