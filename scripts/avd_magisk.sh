#!/usr/bin/env bash
#####################################################################
#   AVD Shaper Setup
#####################################################################
#
# Support emulator ABI: x86_64 and arm64
# Support API level: 23 - 31 (21 and 22 images do not have SELinux)
#
# With an emulator booted and accessible via ADB, usage:
# ./build.py emulator
#
# This script will stop zygote, simulate the Shaper start up process
# that would've happened before zygote was started, and finally
# restart zygote. This is useful for setting up the emulator for
# developing Shaper, testing modules, and developing root apps using
# the official Android emulator (AVD) instead of a real device.
#
# This only covers the "core" features of Shaper. For testing
# shaperinit, please checkout avd_patch.sh.
#
#####################################################################

mount_sbin() {
  mount -t tmpfs -o 'mode=0755' tmpfs /sbin
  chcon u:object_r:rootfs:s0 /sbin
}

if [ ! -f /system/build.prop ]; then
  # Running on PC
  echo 'Please run `./build.py emulator` instead of directly executing the script!'
  exit 1
fi

cd /data/local/tmp
chmod 755 busybox

if [ -z "$FIRST_STAGE" ]; then
  export FIRST_STAGE=1
  export ASH_STANDALONE=1
  if [ $(./busybox id -u) -ne 0 ]; then
    # Re-exec script with root
    exec /system/xbin/su 0 ./busybox sh $0
  else
    # Re-exec script with busybox
    exec ./busybox sh $0
  fi
fi

pm install -r $(pwd)/app-debug.apk

# Extract files from APK
unzip -oj app-debug.apk 'assets/util_functions.sh'
. ./util_functions.sh

api_level_arch_detect

unzip -oj app-debug.apk "lib/$ABI/*" "lib/$ABI32/libshaper32.so" -x "lib/$ABI/libbusybox.so"
for file in lib*.so; do
  chmod 755 $file
  mv "$file" "${file:3:${#file}-6}"
done

# Stop zygote (and previous setup if exists)
shaper --stop 2>/dev/null
stop
if [ -d /dev/avd-shaper ]; then
  umount -l /dev/avd-shaper 2>/dev/null
  rm -rf /dev/avd-shaper 2>/dev/null
fi

# SELinux stuffs
if [ -f /vendor/etc/selinux/precompiled_sepolicy ]; then
  ./shaperpolicy --load /vendor/etc/selinux/precompiled_sepolicy --live --magisk 2>&1
elif [ -f /sepolicy ]; then
  ./shaperpolicy --load /sepolicy --live --magisk 2>&1
else
  ./shaperpolicy --live --magisk 2>&1
fi

SHAPERTMP=/sbin

# Setup bin overlay
if mount | grep -q rootfs; then
  # Legacy rootfs
  mount -o rw,remount /
  rm -rf /root
  mkdir /root
  chmod 750 /root
  ln /sbin/* /root
  mount -o ro,remount /
  mount_sbin
  ln -s /root/* /sbin
elif [ -e /sbin ]; then
  # Legacy SAR
  mount_sbin
  mkdir -p /dev/sysroot
  block=$(mount | grep ' / ' | awk '{ print $1 }')
  [ $block = "/dev/root" ] && block=/dev/block/dm-0
  mount -o ro $block /dev/sysroot
  for file in /dev/sysroot/sbin/*; do
    [ ! -e $file ] && break
    if [ -L $file ]; then
      cp -af $file /sbin
    else
      sfile=/sbin/$(basename $file)
      touch $sfile
      mount -o bind $file $sfile
    fi
  done
  umount -l /dev/sysroot
  rm -rf /dev/sysroot
else
  # Android Q+ without sbin
  SHAPERTMP=/dev/avd-shaper
  mkdir /dev/avd-shaper
  mount -t tmpfs -o 'mode=0755' tmpfs /dev/avd-shaper
fi

# Shaper stuff
mkdir -p SHAPERBIN 2>/dev/null
unzip -oj app-debug.apk 'assets/*' -x 'assets/chromeos/*' \
-x 'assets/bootctl' -x 'assets/main.jar' -d SHAPERBIN
mkdir $NVBASE/modules 2>/dev/null
mkdir $POSTFSDATAD 2>/dev/null
mkdir $SERVICED 2>/dev/null

for file in shaper32 shaper64 shaperpolicy; do
  chmod 755 ./$file
  cp -af ./$file $SHAPERTMP/$file
  cp -af ./$file SHAPERBIN/$file
done
cp -af ./shaperboot SHAPERBIN/shaperboot
cp -af ./shaperinit SHAPERBIN/shaperinit
cp -af ./busybox SHAPERBIN/busybox

ln -s ./shaper64 $SHAPERTMP/shaper
ln -s ./shaper $SHAPERTMP/su
ln -s ./shaper $SHAPERTMP/resetprop
ln -s ./shaper $SHAPERTMP/shaperhide
ln -s ./shaperpolicy $SHAPERTMP/supolicy

./shaperinit -x manager $SHAPERTMP/stub.apk

mkdir -p $SHAPERTMP/.shaper/mirror
mkdir $SHAPERTMP/.shaper/block
touch $SHAPERTMP/.shaper/config

# Boot up
$SHAPERTMP/shaper --post-fs-data
while [ ! -f /dev/.shaper_unblock ]; do sleep 1; done
rm /dev/.shaper_unblock
start
$SHAPERTMP/shaper --service
