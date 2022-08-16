package com.zzqy.shaper.core

import android.os.Build
import android.os.Process
import com.zzqy.shaper.BuildConfig

@Suppress("DEPRECATION")
object Const {

    val CPU_ABI: String get() = Build.SUPPORTED_ABIS[0]

    // Null if 32-bit only or 64-bit only
    val CPU_ABI_32 =
        if (Build.SUPPORTED_64_BIT_ABIS.isEmpty()) null
        else Build.SUPPORTED_32_BIT_ABIS.firstOrNull()

    // Paths
    lateinit var SHAPERTMP: String
    val MAGISK_PATH get() = "$SHAPERTMP/modules"
    const val TMPDIR = "/dev/tmp"
    const val SHAPER_LOG = "/cache/shaper.log"

    // Misc
    val USER_ID = Process.myUid() / 100000
    val APP_IS_CANARY get() = Version.isCanary(BuildConfig.VERSION_CODE)

    object Version {
        const val MIN_VERSION = "v21.0"
        const val MIN_VERCODE = 21000

        fun atLeast_21_2() = Info.env.versionCode >= 21200 || isCanary()
        fun atLeast_24_0() = Info.env.versionCode >= 24000 || isCanary()
        fun isCanary() = isCanary(Info.env.versionCode)

        fun isCanary(ver: Int) = ver > 0 && ver % 100 != 0
    }

    object ID {
        const val JOB_SERVICE_ID = 7
    }

    object Url {
        const val PATREON_URL = "https://www.patreon.com/topjohnwu"
        const val SOURCE_CODE_URL = "https://github.com/topjohnwu/Magisk"

        val CHANGELOG_URL = if (APP_IS_CANARY) Info.remote.magisk.note
        else "https://topjohnwu.github.io/Magisk/releases/${BuildConfig.VERSION_CODE}.md"

        const val GITHUB_RAW_URL = "https://raw.githubusercontent.com/"
        const val GITHUB_API_URL = "https://api.github.com/"
        const val GITHUB_PAGE_URL = "https://topjohnwu.github.io/magisk-files/"
        const val JS_DELIVR_URL = "https://cdn.jsdelivr.net/gh/"
    }

    object Key {
        // intents
        const val OPEN_SECTION = "section"
        const val PREV_PKG = "prev_pkg"
    }

    object Value {
        const val FLASH_ZIP = "flash"
        const val PATCH_FILE = "patch"
        const val FLASH_MAGISK = "shaper"
        const val FLASH_INACTIVE_SLOT = "slot"
        const val UNINSTALL = "uninstall"
    }

    object Nav {
        const val HOME = "home"
        const val SETTINGS = "settings"
        const val MODULES = "modules"
        const val SUPERUSER = "superuser"
    }
}