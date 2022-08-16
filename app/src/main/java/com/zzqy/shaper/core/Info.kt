package com.zzqy.shaper.core

import android.os.Build
import androidx.databinding.ObservableBoolean
import com.zzqy.shaper.StubApk
import com.zzqy.shaper.core.model.UpdateInfo
import com.zzqy.shaper.core.utils.net.NetworkObserver
import com.zzqy.shaper.data.repository.NetworkService
import com.zzqy.shaper.di.AppContext
import com.zzqy.shaper.ktx.getProperty
import com.topjohnwu.superuser.ShellUtils.fastCmd
import com.topjohnwu.superuser.internal.UiThreadHandler
import android.util.Log

val isRunningAsStub get() = Info.stub != null

object Info {

    var stub: StubApk.Data? = null

    val EMPTY_REMOTE = UpdateInfo()
    var remote = EMPTY_REMOTE
    suspend fun getRemote(svc: NetworkService): UpdateInfo? {
        return if (remote === EMPTY_REMOTE) {
            svc.fetchUpdate()?.apply { remote = this }
        } else remote
    }

    // Device state
    @JvmStatic val env by lazy { loadState() }
    @JvmField var isSAR = false
    var isAB = false
    @JvmField val isZygiskEnabled = System.getenv("ZYGISK_ENABLED") == "1"
    @JvmStatic val isFDE get() = crypto == "block"
    @JvmField var ramdisk = false
    @JvmField var vbmeta = false
    var crypto = ""
    var noDataExec = false
    var isRooted = false

    @JvmField var hasGMS = true
    val isSamsung = Build.MANUFACTURER.equals("samsung", ignoreCase = true)
    @JvmField val isEmulator =
        getProperty("ro.kernel.qemu", "0") == "1" ||
            getProperty("ro.boot.qemu", "0") == "1"

    val isConnected by lazy {
        ObservableBoolean(false).also { field ->
            NetworkObserver.observe(AppContext) {
                UiThreadHandler.run { field.set(it) }
            }
        }
    }

    private fun loadState() = Env(
        testCmd("shaper -v").split(":".toRegex())[0],
        runCatching { testCmd("shaper -V").toInt() }.getOrDefault(-1)
    )

    fun testCmd(cmd:String): String {
        var res = fastCmd(cmd)
        Log.i("fuckShaper","- testCmd :cmd = "+cmd+",res = "+res)
        Log.i("fuckShaper","- testCmd :isRooted = "+isRooted)
        Log.i("fuckShaper","- testCmd :Const.Version.MIN_VERCODE = "+Const.Version.MIN_VERCODE)
        return res
    }

    class Env(
        val versionString: String = "",
        code: Int = -1
    ) {
        val versionCode = when {
            code < Const.Version.MIN_VERCODE -> -1
            else -> if (isRooted) code else -1
        }
        val isUnsupported = code > 0 && code < Const.Version.MIN_VERCODE
        val isActive = versionCode >= 0
        val versionStr = versionString
        val pastCode = code
    }
}
