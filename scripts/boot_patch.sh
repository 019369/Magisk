#!/system/bin/sh
#######################################################################################
# Shaper Boot Image Patcher
#######################################################################################
#
# Usage: boot_patch.sh <bootimage>
#
# The following flags can be set in environment variables:
# KEEPVERITY, KEEPFORCEENCRYPT, RECOVERYMODE
#
# This script should be placed in a directory with the following files:
#
# File name          Type      Description
#
# boot_patch.sh      script    A script to patch boot image for Shaper.
#                  (this file) The script will use files in its same
#                              directory to complete the patching process
# util_functions.sh  script    A script which hosts all functions required
#                              for this script to work properly
# shaperinit         binary    The binary to replace /init
# shaper(32/64)      binary    The shaper binaries
# shaperboot         binary    A tool to manipulate boot images
# chromeos           folder    This folder includes the utility and keys to sign
#                  (optional)  chromeos boot images. Only used for Pixel C.
#
#######################################################################################

############
# Functions
############

# Pure bash dirname implementation
getdir() {
  case "$1" in
    */*)
      dir=${1%/*}
      if [ -z $dir ]; then
        echo "/"
      else
        echo $dir
      fi
    ;;
    *) echo "." ;;
  esac
}

#################
# Initialization
#################

if [ -z $SOURCEDMODE ]; then
  # Switch to the location of the script file
  cd "$(getdir "${BASH_SOURCE:-$0}")"
  # Load utility functions
  . ./util_functions.sh
  # Check if 64-bit
  api_level_arch_detect
fi

BOOTIMAGE="$1"
[ -e "$BOOTIMAGE" ] || abort "$BOOTIMAGE does not exist!"

# Dump image for MTD/NAND character device boot partitions
if [ -c "$BOOTIMAGE" ]; then
  nanddump -f boot.img "$BOOTIMAGE"
  BOOTNAND="$BOOTIMAGE"
  BOOTIMAGE=boot.img
fi

# Flags
[ -z $KEEPVERITY ] && KEEPVERITY=false
[ -z $KEEPFORCEENCRYPT ] && KEEPFORCEENCRYPT=false
[ -z $PATCHVBMETAFLAG ] && PATCHVBMETAFLAG=false
[ -z $RECOVERYMODE ] && RECOVERYMODE=false
export KEEPVERITY
export KEEPFORCEENCRYPT
export PATCHVBMETAFLAG

chmod -R 755 .

#########
# Unpack
#########

CHROMEOS=false

ui_print "- Unpacking boot image"
./shaperboot unpack "$BOOTIMAGE"

case $? in
  0 ) ;;
  1 )
    abort "! Unsupported/Unknown image format"
    ;;
  2 )
    ui_print "- ChromeOS boot image detected"
    CHROMEOS=true
    ;;
  * )
    abort "! Unable to unpack boot image"
    ;;
esac

###################
# Ramdisk Restores
###################

# Test patch status and do restore
ui_print "- Checking ramdisk status"
if [ -e ramdisk.cpio ]; then
  ./shaperboot cpio ramdisk.cpio test
  STATUS=$?
else
  # Stock A only legacy SAR, or some Android 13 GKIs
  STATUS=0
fi
case $((STATUS & 3)) in
  0 )  # Stock boot
    ui_print "- Stock boot image detected"
    SHA1=$(./shaperboot sha1 "$BOOTIMAGE" 2>/dev/null)
    cat $BOOTIMAGE > stock_boot.img
    cp -af ramdisk.cpio ramdisk.cpio.orig 2>/dev/null
    ;;
  1 )  # Shaper patched
    ui_print "- Shaper patched boot image detected"
    # Find SHA1 of stock boot image
    [ -z $SHA1 ] && SHA1=$(./shaperboot cpio ramdisk.cpio sha1 2>/dev/null)
    ./shaperboot cpio ramdisk.cpio restore
    cp -af ramdisk.cpio ramdisk.cpio.orig
    rm -f stock_boot.img
    ;;
  2 )  # Unsupported
    ui_print "! Boot image patched by unsupported programs"
    abort "! Please restore back to stock boot image"
    ;;
esac

# Work around custom legacy Sony /init -> /(s)bin/init_sony : /init.real setup
INIT=init
if [ $((STATUS & 4)) -ne 0 ]; then
  INIT=init.real
fi

##################
# Ramdisk Patches
##################

ui_print "- Patching ramdisk"

echo "KEEPVERITY=$KEEPVERITY" > config
echo "KEEPFORCEENCRYPT=$KEEPFORCEENCRYPT" >> config
echo "PATCHVBMETAFLAG=$PATCHVBMETAFLAG" >> config
echo "RECOVERYMODE=$RECOVERYMODE" >> config
[ ! -z $SHA1 ] && echo "SHA1=$SHA1" >> config

# Compress to save precious ramdisk space
SKIP32="#"
SKIP64="#"
if [ -f shaper32 ]; then
  ./shaperboot compress=xz shaper32 shaper32.xz
  unset SKIP32
fi
if [ -f shaper64 ]; then
  ./shaperboot compress=xz shaper64 shaper64.xz
  unset SKIP64
fi

./shaperboot cpio ramdisk.cpio \
"add 0750 $INIT shaperinit" \
"mkdir 0750 overlay.d" \
"mkdir 0750 overlay.d/sbin" \
"$SKIP32 add 0644 overlay.d/sbin/shaper32.xz shaper32.xz" \
"$SKIP64 add 0644 overlay.d/sbin/shaper64.xz shaper64.xz" \
"patch" \
"backup ramdisk.cpio.orig" \
"mkdir 000 .backup" \
"add 000 .backup/.shaper config"

rm -f ramdisk.cpio.orig config shaper*.xz

#################
# Binary Patches
#################

for dt in dtb kernel_dtb extra; do
  [ -f $dt ] && ./shaperboot dtb $dt patch && ui_print "- Patch fstab in $dt"
done

if [ -f kernel ]; then
  # Remove Samsung RKP
  ./shaperboot hexpatch kernel \
  49010054011440B93FA00F71E9000054010840B93FA00F7189000054001840B91FA00F7188010054 \
  A1020054011440B93FA00F7140020054010840B93FA00F71E0010054001840B91FA00F7181010054

  # Remove Samsung defex
  # Before: [mov w2, #-221]   (-__NR_execve)
  # After:  [mov w2, #-32768]
  ./shaperboot hexpatch kernel 821B8012 E2FF8F12

  # Force kernel to load rootfs
  # skip_initramfs -> want_initramfs
  ./shaperboot hexpatch kernel \
  736B69705F696E697472616D667300 \
  77616E745F696E697472616D667300
fi

#################
# Repack & Flash
#################

ui_print "- Repacking boot image"
./shaperboot repack "$BOOTIMAGE" || abort "! Unable to repack boot image"

# Sign chromeos boot
$CHROMEOS && sign_chromeos

# Restore the original boot partition path
[ -e "$BOOTNAND" ] && BOOTIMAGE="$BOOTNAND"

# Reset any error code
true
