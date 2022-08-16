#pragma once

#include <string>

// shaperinit will hex patch this constant,
// appending \0 to prevent the compiler from reusing the string for "1"
#define MAIN_SOCKET  "d30138f2310a9fb9c54a3e0c21f58591\0"
#define JAVA_PACKAGE_NAME "com.zzqy.shaper"
#define LOGFILE         "/cache/shaper.log"
#define UNBLOCKFILE     "/dev/.shaper_unblock"
#define SECURE_DIR      "/data/adb"
#define MODULEROOT      SECURE_DIR "/modules"
#define MODULEUPGRADE   SECURE_DIR "/modules_update"
#define DATABIN         SECURE_DIR "/shaper"
#define SHAPERDB        SECURE_DIR "/shaper.db"

// tmpfs paths
extern std::string  SHAPERTMP;
#define INTLROOT    ".shaper"
#define MIRRDIR     INTLROOT "/mirror"
#define RULESDIR    MIRRDIR "/sepolicy.rules"
#define BLOCKDIR    INTLROOT "/block"
#define MODULEMNT   INTLROOT "/modules"
#define BBPATH      INTLROOT "/busybox"
#define ROOTOVL     INTLROOT "/rootdir"
#define SHELLPTS    INTLROOT "/pts"
#define ROOTMNT     ROOTOVL "/.mount_list"
#define ZYGISKBIN   INTLROOT "/zygisk"
#define SELINUXMOCK INTLROOT "/selinux"

constexpr const char *applet_names[] = { "yu", "resetprop", nullptr };

#define POST_FS_DATA_WAIT_TIME       40
#define POST_FS_DATA_SCRIPT_MAX_TIME 35

extern int SDK_INT;
#define APP_DATA_DIR (SDK_INT >= 24 ? "/data/user_de" : "/data/user")

// Multi-call entrypoints
int shaper_main(int argc, char *argv[]);
int su_client_main(int argc, char *argv[]);
int resetprop_main(int argc, char *argv[]);
int app_process_main(int argc, char *argv[]);
int zygisk_main(int argc, char *argv[]);
