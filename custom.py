
'''
magisk = shaper

magiskinit = shaperinit
magiskboot = shaperboot
magiskpolicy = shaperpolicy
magisk32 = shaper32
magisk64 = shaper64
.magisk = .shaper
com.topjohnwu.magisk = com.zzqy.shaper
MAGISKBIN = SHAPERBIN
Magisk = Shaper
MAGISKTMP = SHAPERTMP
MAGISK = SHAPER
MAGISK_VER = SHAPER_VER

B_MAGISK = B_SHAPER
MAGISK_DEBUG = SHAPER_DEBUG
MAGISK_VERSION = SHAPER_VERSION
MAGISK_VER_CODE = SHAPER_VER_CODE
MAGISK_FULL_VER = SHAPER_FULL_VER
MAGISK_VERSION_STUB = SHAPER_VERSION_STUB

su = yu

magisk_env = shaper_env
magisk-tmp = shaper-tmp
/magisk = /shaper
magisk_node = shaper_node
inject_magisk_bins = inject_shaper_bins
magisk_rules = shaper_rules
magisk_main = shaper_main
magisk_unblock = shaper_unblock
magisk.db = shaper.db
MAGISKDB = SHAPERDB

init.magisk.rc = init.shaper.rc
magisk.log = shaper.log
MAGISK_LOG = SHAPER_LOG

0
----java
    buildSrc/src/main/java/Plugin.kt
    buildSrc/src/main/java/Codegen.kt
    buildSrc/src/main/java/Steup.kt

    com.zzqy.shaper.core.tasks.HideAPK
    com.zzqy.shaper.core.tasks.MagiskInstallImpl
    com.zzqy.shaper.core.Const
    com.zzqy.shaper.core.utils.ShellInit
    com.zzqy.shaper.core.model.module.LocalModule
    com.zzqy.shaper.data.repository.LogRepository

----c
    native/jni/base/logging.cpp
    native/jni/core/package.cpp
    native/jni/sepolicy/package.cpp
    native/jni/zygisk/hook.cpp

    native/jni/core/bootstages.cpp
    native/jni/init/getinfo.cpp
    native/jni/init/twostage.cpp
    native/jni/init/rootdir.cpp
    native/jni/core/module.cpp
    native/jni/init/mount.cpp
    native/jni/init/selinux.cpp
    native/jni/sepolicy/rules.cpp
    native/jni/core/applests.cpp
    native/jni/core/db.cpp
    native/jni/boot/remdisk.cpp
    native/jni/zygisk/entry.cpp
    native/jni/zygisk/zygisk.hpp
    native/jni/su/su_daemon.cpp
    native/jni/core/package.cpp
    native/jni/core/scripting.cpp
    native/jni/zygisk/deny/cli.cpp
    native/jni/init/init.cpp
    native/jni/zygisk/main.cpp



----sh
    scripts/addon.d.sh
    scripts/avd_magisk.sh
    scripts/avd_patch.sh
    scripts/avd_test.sh
    scripts/boot_patch.sh
    scripts/flash_script.sh
    scripts/module_installer.sh
    scripts/uninstaller.sh
    scripts/util_functions.sh

    app/src/main/res/raw/manager.sh

    app/src/main/assets/addon.d.sh
    app/src/main/assets/boot_patch.sh
    app/src/main/assets/module_installer.sh
    app/src/main/assets/uninstaller.sh
    app/src/main/assets/util_functions.sh

    app/src/main/resources/META-INF/com/google/android/updater-script

----so
    native/jni/Android.mk
        magisk
    native/jni/core/magisk.cpp
    native/jni/core/daemon.cpp
    native/jni/su/su.cpp

----package name
    settings.gradle.kts
    app/build.gradle.kts


----
    build.py
    gradle.properties

    native/build.gradle.kts
    native/jni/Android.mk





















'''







































