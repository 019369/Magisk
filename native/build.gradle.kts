plugins {
    id("com.android.library")
}

setupCommon()

android {
    namespace = "com.zzqy.shaper.native"

    externalNativeBuild {
        ndkBuild {
            path("jni/Android.mk")
        }
    }

    defaultConfig {
        externalNativeBuild {
            ndkBuild {
                // Pass arguments to ndk-build.
                arguments(
                    "B_SHAPER=1", "B_INIT=1", "B_BOOT=1", "B_TEST=1", "B_POLICY=1", "B_PRELOAD=1",
                    "SHAPER_DEBUG=1", "SHAPER_VERSION=debug", "SHAPER_VER_CODE=INT_MAX"
                )
            }
        }
    }
}
