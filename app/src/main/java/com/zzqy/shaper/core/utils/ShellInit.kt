package com.zzqy.shaper.core.utils

import android.content.Context
import com.zzqy.shaper.R
import com.zzqy.shaper.StubApk
import com.zzqy.shaper.core.Config
import com.zzqy.shaper.core.Const
import com.zzqy.shaper.core.Info
import com.zzqy.shaper.core.isRunningAsStub
import com.zzqy.shaper.ktx.cachedFile
import com.zzqy.shaper.ktx.deviceProtectedContext
import com.zzqy.shaper.ktx.rawResource
import com.zzqy.shaper.ktx.writeTo
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ShellUtils
import java.io.File
import java.util.jar.JarFile
import android.util.Log


class ShellInit : Shell.Initializer() {
    override fun onInit(context: Context, shell: Shell): Boolean {

        Log.i("fuckShaper","- onInit :shell.isRoot = "+shell.isRoot)
        Log.i("fuckShaper","- onInit :"+Log.getStackTraceString(Throwable()))
        if (shell.isRoot) {
            Info.isRooted = true
            RootUtils.bindTask?.let { shell.execTask(it) }
            RootUtils.bindTask = null
        }
        shell.newJob().apply {
            add("export ASH_STANDALONE=1")

            val localBB: File
            if (isRunningAsStub) {
                if (!shell.isRoot)
                    return true
                val jar = JarFile(StubApk.current(context))
                val bb = jar.getJarEntry("lib/${Const.CPU_ABI}/libbusybox.so")
                localBB = context.deviceProtectedContext.cachedFile("busybox")
                localBB.delete()
                jar.getInputStream(bb).writeTo(localBB)
                localBB.setExecutable(true)
            } else {
                localBB = File(context.applicationInfo.nativeLibraryDir, "libbusybox.so")
            }

            if (shell.isRoot) {
                add("export SHAPERTMP=\$(shaper --path)/.shaper")
                // Test if we can properly execute stuff in /data
                Info.noDataExec = !shell.newJob().add("$localBB sh -c \"$localBB true\"").exec().isSuccess
            }

            if (Info.noDataExec) {
                // Copy it out of /data to workaround Samsung bullshit
                add(
                    "if [ -x \$SHAPERTMP/busybox/busybox ]; then",
                    "  cp -af $localBB \$SHAPERTMP/busybox/busybox",
                    "  exec \$SHAPERTMP/busybox/busybox sh",
                    "else",
                    "  cp -af $localBB /dev/.busybox",
                    "  exec /dev/.busybox sh",
                    "fi"
                )
            } else {
                // Directly execute the file
                add("exec $localBB sh")
            }

            add(context.rawResource(R.raw.manager))
            if (shell.isRoot) {
                add(context.assets.open("util_functions.sh"))
            }
            add("app_init")
        }.exec()

        fun fastCmd(cmd: String) = ShellUtils.fastCmd(shell, cmd)
        fun getVar(name: String) = fastCmd("echo \$$name")
        fun getBool(name: String) = getVar(name).toBoolean()

        Const.SHAPERTMP = getVar("SHAPERTMP")
        Info.isSAR = getBool("SYSTEM_ROOT")
        Info.ramdisk = getBool("RAMDISKEXIST")
        Info.vbmeta = getBool("VBMETAEXIST")
        Info.isAB = getBool("ISAB")
        Info.crypto = getVar("CRYPTOTYPE")

        // Default presets
        Config.recovery = getBool("RECOVERYMODE")
        Config.keepVerity = getBool("KEEPVERITY")
        Config.keepEnc = getBool("KEEPFORCEENCRYPT")
        Config.patchVbmeta = getBool("PATCHVBMETAFLAG")

        return true
    }
}
