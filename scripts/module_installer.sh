#!/sbin/sh

#################
# Initialization
#################

umask 022

# echo before loading util_functions
ui_print() { echo "$1"; }

require_new_shaper() {
  ui_print "*******************************"
  ui_print " Please install shaper v20.4+! "
  ui_print "*******************************"
  exit 1
}

#########################
# Load util_functions.sh
#########################

OUTFD=$2
ZIPFILE=$3

mount /data 2>/dev/null

[ -f /data/adb/shaper/util_functions.sh ] || require_new_shaper
. /data/adb/shaper/util_functions.sh
[ $SHAPER_VER_CODE -lt 20400 ] && require_new_shaper

install_module
exit 0
