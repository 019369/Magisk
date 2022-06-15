package com.zzqy.shaper.core.download

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import androidx.core.net.toUri
import com.zzqy.shaper.core.Info
import com.zzqy.shaper.core.model.MagiskJson
import com.zzqy.shaper.core.model.StubJson
import com.zzqy.shaper.core.model.module.OnlineModule
import com.zzqy.shaper.core.utils.MediaStoreUtils
import com.zzqy.shaper.di.AppContext
import com.zzqy.shaper.ktx.cachedFile
import com.zzqy.shaper.ui.flash.FlashFragment
import com.zzqy.shaper.view.Notifications
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

private fun cachedFile(name: String) = AppContext.cachedFile(name).apply { delete() }.toUri()

enum class Action {
    Flash,
    Download
}

sealed class Subject : Parcelable {

    abstract val url: String
    abstract val file: Uri
    abstract val title: String
    abstract val notifyId: Int
    open val autoLaunch: Boolean get() = true
    open val postDownload: (() -> Unit)? get() = null

    abstract fun pendingIntent(context: Context): PendingIntent?

    @Parcelize
    class Module(
        val module: OnlineModule,
        val action: Action,
        override val notifyId: Int = Notifications.nextId()
    ) : Subject() {
        override val url: String get() = module.zipUrl
        override val title: String get() = module.downloadFilename
        override val autoLaunch: Boolean get() = action == Action.Flash

        @IgnoredOnParcel
        override val file by lazy {
            MediaStoreUtils.getFile(title).uri
        }

        override fun pendingIntent(context: Context) =
            FlashFragment.installIntent(context, file)
    }

    @Parcelize
    class App(
        private val json: MagiskJson = Info.remote.magisk,
        val stub: StubJson = Info.remote.stub,
        override val notifyId: Int = Notifications.nextId()
    ) : Subject() {
        override val title: String get() = "Magisk-${json.version}(${json.versionCode})"
        override val url: String get() = json.link

        @IgnoredOnParcel
        override val file by lazy {
            cachedFile("manager.apk")
        }

        @IgnoredOnParcel
        override var postDownload: (() -> Unit)? = null

        @IgnoredOnParcel
        var intent: Intent? = null
        override fun pendingIntent(context: Context) = intent?.toPending(context)
    }

    @SuppressLint("InlinedApi")
    protected fun Intent.toPending(context: Context): PendingIntent {
        return PendingIntent.getActivity(context, notifyId, this,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT)
    }
}
