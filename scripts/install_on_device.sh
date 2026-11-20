#!/system/bin/sh
set -e

NO_ZYGOTE=0
for arg in "$@"; do
    case "$arg" in
        --no-zygote) NO_ZYGOTE=1 ;;
    esac
done

BASE_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"

if [ "$(id -u)" -ne 0 ]; then
    echo "Root permissions required. Run with: su -mm -c \"sh $0\""
    exit 1
fi

REQUIRED_FILES="
apks/Gallery_15.9_MyFanCam.apk
apks/VideoEditor_OneUI9_MyFanCam.apk
libs/bind/libveframework.videoeditor.samsung.so
libs/bind/libcontextanalyzer_jni.media.samsung.so
libs/mca/libmcactxanalyzer.so
libs/mca/librefr.so
libs/mca/libvhlgt.so
libs/mca/libsbs.so
libs/mca/libtflgpu.so
libs/mca/c++.so
libs/mca/g.so
libs/mca/libsnap_tfl.so
models/sbs/audio_ae_intervals.conf
models/sbs/fastScanner.tflite
models/sbs/mss_v0.23.0_VMWO_2_fp32.sorione
"

for rel in $REQUIRED_FILES; do
    if [ ! -f "$BASE_DIR/$rel" ]; then
        echo "Missing required package file: $BASE_DIR/$rel"
        exit 1
    fi
done

TFL_HUMAN="/system/etc/style_transfer/db/12-26_PhotoEditor_StyleTransfer_v1.0.0_HumanDetector.tflite"
if [ ! -f "$TFL_HUMAN" ]; then
    echo "[WARN] Built-in TFLite HumanDetector not found at:"
    echo "       $TFL_HUMAN"
    echo "       Audio Eraser will work, but My FanCam person detection needs this TFLite model."
fi

RESETPROP=""
for cand in /data/adb/ksu/bin/resetprop /data/local/tmp/resetprop "$(command -v resetprop 2>/dev/null || true)"; do
    if [ -n "$cand" ] && [ -x "$cand" ]; then
        RESETPROP="$cand"
        break
    fi
done

if [ -n "$RESETPROP" ]; then
    echo "[*] Setting ro.build.official.release = false using $RESETPROP"
    "$RESETPROP" ro.build.official.release false 2>/dev/null || true
else
    echo "[WARN] resetprop binary not found; relying on CorePatch if -3001 occurs."
fi

HAS_NSENTER=0
if nsenter -t 1 -m -- true 2>/dev/null; then
    HAS_NSENTER=1
fi

bind_file() {
    SRC="$1"
    DST="$2"
    if [ "$HAS_NSENTER" -eq 1 ]; then
        nsenter -t 1 -m -- umount -l "$DST" 2>/dev/null || true
        nsenter -t 1 -m -- mount --bind "$SRC" "$DST"
    else
        umount -l "$DST" 2>/dev/null || true
        mount --bind "$SRC" "$DST"
    fi
}

mkdir -p /data/adb/fancam /data/local/tmp/mca/models /data/sbs

cp -f "$BASE_DIR/libs/bind/libveframework.videoeditor.samsung.so" /data/adb/fancam/
cp -f "$BASE_DIR/libs/bind/libcontextanalyzer_jni.media.samsung.so" /data/adb/fancam/
chmod 644 /data/adb/fancam/libveframework.videoeditor.samsung.so /data/adb/fancam/libcontextanalyzer_jni.media.samsung.so
chcon u:object_r:system_lib_file:s0 /data/adb/fancam/libveframework.videoeditor.samsung.so /data/adb/fancam/libcontextanalyzer_jni.media.samsung.so

cp -f "$BASE_DIR/libs/mca/"*.so /data/local/tmp/mca/
chmod 755 /data/local/tmp/mca /data/local/tmp/mca/*.so
chcon u:object_r:system_file:s0 /data/local/tmp/mca /data/local/tmp/mca/*.so

for dlc in Locator.dlc Reid.dlc Detection.dlc Pose.dlc Keyword.dlc; do
    printf 'SNAPTFL_PLACEHLD' > "/data/local/tmp/mca/models/$dlc"
done
chmod 755 /data/local/tmp/mca/models
chmod 644 /data/local/tmp/mca/models/*.dlc
chcon u:object_r:system_file:s0 /data/local/tmp/mca/models /data/local/tmp/mca/models/*.dlc

cp -f "$BASE_DIR/models/sbs/"* /data/sbs/
chmod 755 /data/sbs
chmod 644 /data/sbs/*
chcon u:object_r:system_file:s0 /data/sbs /data/sbs/*

if [ ! -f /data/adb/fancam/ff_backup.xml ]; then
    cp -f /system/etc/floating_feature.xml /data/adb/fancam/ff_backup.xml
    chmod 644 /data/adb/fancam/ff_backup.xml
fi

FF_TMP="/data/local/tmp/ff_s23.xml.tmp"
if ! grep -q '</SecFloatingFeatureSet>' /system/etc/floating_feature.xml; then
    echo "[ERROR] Unexpected format in /system/etc/floating_feature.xml (missing </SecFloatingFeatureSet>)."
    exit 1
fi

grep -v -E 'SEC_FLOATING_FEATURE_MMFW_SUPPORT_TARGET_TRACKING|SEC_FLOATING_FEATURE_MMFW_SUPPORT_MEDIA_CONTEXT_ANALYZER|SEC_FLOATING_FEATURE_MMFW_CONFIG_MEDIA_CONTEXT_ANALYZER_CORE|SEC_FLOATING_FEATURE_AUDIO_CONFIG_MULTISOURCE_SEPARATOR|</SecFloatingFeatureSet>' /system/etc/floating_feature.xml > "$FF_TMP"
printf '    <SEC_FLOATING_FEATURE_MMFW_SUPPORT_TARGET_TRACKING>TRUE</SEC_FLOATING_FEATURE_MMFW_SUPPORT_TARGET_TRACKING>\r\n' >> "$FF_TMP"
printf '    <SEC_FLOATING_FEATURE_MMFW_SUPPORT_MEDIA_CONTEXT_ANALYZER>TRUE</SEC_FLOATING_FEATURE_MMFW_SUPPORT_MEDIA_CONTEXT_ANALYZER>\r\n' >> "$FF_TMP"
printf '    <SEC_FLOATING_FEATURE_MMFW_CONFIG_MEDIA_CONTEXT_ANALYZER_CORE>NPU</SEC_FLOATING_FEATURE_MMFW_CONFIG_MEDIA_CONTEXT_ANALYZER_CORE>\r\n' >> "$FF_TMP"
printf '    <SEC_FLOATING_FEATURE_AUDIO_CONFIG_MULTISOURCE_SEPARATOR>{FastScanning_6, SourceSeparator_4, Version_1.3.0}</SEC_FLOATING_FEATURE_AUDIO_CONFIG_MULTISOURCE_SEPARATOR>\r\n' >> "$FF_TMP"
printf '</SecFloatingFeatureSet>\r\n' >> "$FF_TMP"

if [ ! -s "$FF_TMP" ] || ! grep -q '</SecFloatingFeatureSet>' "$FF_TMP" || ! grep -q 'SEC_FLOATING_FEATURE_MMFW_SUPPORT_TARGET_TRACKING' "$FF_TMP"; then
    rm -f "$FF_TMP"
    exit 1
fi

mv -f "$FF_TMP" /data/local/tmp/ff_s23.xml
chmod 644 /data/local/tmp/ff_s23.xml
chcon u:object_r:system_file:s0 /data/local/tmp/ff_s23.xml

FF_ACTIVE="/data/local/tmp/ff_s23.xml"
if [ -f /data/adb/ff_s23.xml ]; then
    cp -f /data/local/tmp/ff_s23.xml /data/adb/ff_s23.xml
    chmod 644 /data/adb/ff_s23.xml
    chcon u:object_r:system_file:s0 /data/adb/ff_s23.xml
    FF_ACTIVE="/data/adb/ff_s23.xml"
fi

cat > /data/local/tmp/mca/reapply.sh <<EOF
#!/system/bin/sh
NO_ZYGOTE=0
for arg in "\$@"; do
    case "\$arg" in
        --no-zygote) NO_ZYGOTE=1 ;;
    esac
done

RESETPROP=""
for cand in /data/adb/ksu/bin/resetprop /data/local/tmp/resetprop "\$(command -v resetprop 2>/dev/null || true)"; do
    if [ -n "\$cand" ] && [ -x "\$cand" ]; then
        RESETPROP="\$cand"
        break
    fi
done
[ -n "\$RESETPROP" ] && "\$RESETPROP" ro.build.official.release false 2>/dev/null || true

FF_SRC="$FF_ACTIVE"
[ ! -f "\$FF_SRC" ] && [ -f /data/local/tmp/ff_s23.xml ] && FF_SRC="/data/local/tmp/ff_s23.xml"

chmod 644 "\$FF_SRC" /data/adb/fancam/*.so /data/sbs/* /data/local/tmp/mca/models/*.dlc 2>/dev/null || true
chmod 755 /data/local/tmp/mca /data/local/tmp/mca/models /data/sbs /data/local/tmp/mca/*.so /data/local/tmp/mca/reapply.sh 2>/dev/null || true

chcon u:object_r:system_file:s0 "\$FF_SRC" /data/local/tmp/mca /data/local/tmp/mca/* /data/local/tmp/mca/models /data/local/tmp/mca/models/* /data/sbs /data/sbs/* 2>/dev/null || true
chcon u:object_r:system_lib_file:s0 /data/adb/fancam/*.so 2>/dev/null || true

bind_mount() {
    SRC="\$1"
    DST="\$2"
    if nsenter -t 1 -m -- true 2>/dev/null; then
        nsenter -t 1 -m -- umount -l "\$DST" 2>/dev/null || true
        nsenter -t 1 -m -- mount --bind "\$SRC" "\$DST"
    else
        umount -l "\$DST" 2>/dev/null || true
        mount --bind "\$SRC" "\$DST"
    fi
}

[ -f "\$FF_SRC" ] && bind_mount "\$FF_SRC" /system/etc/floating_feature.xml
bind_mount /data/adb/fancam/libveframework.videoeditor.samsung.so /system/lib64/libveframework.videoeditor.samsung.so
bind_mount /data/adb/fancam/libcontextanalyzer_jni.media.samsung.so /system/lib64/libcontextanalyzer_jni.media.samsung.so

if [ "\$NO_ZYGOTE" -eq 0 ]; then
    setprop ctl.restart zygote
fi
EOF
chmod 755 /data/local/tmp/mca/reapply.sh
chcon u:object_r:system_file:s0 /data/local/tmp/mca/reapply.sh

if [ -d /data/adb/post-fs-data.d ]; then
    cat > /data/adb/post-fs-data.d/99_z_fancam_audioeraser.sh <<'EOF'
#!/system/bin/sh
if [ -x /data/local/tmp/mca/reapply.sh ]; then
    sh /data/local/tmp/mca/reapply.sh --no-zygote
fi
EOF
    chmod 755 /data/adb/post-fs-data.d/99_z_fancam_audioeraser.sh
fi

sh /data/local/tmp/mca/reapply.sh --no-zygote

install_apk() {
    APK="$1"
    LABEL="$2"
    SIZE=$(stat -c%s "$APK")
    echo "[*] Installing $LABEL ($SIZE bytes)..."
    OUT=$(cat "$APK" | pm install -r -d -i com.sec.android.app.samsungapps -S "$SIZE" 2>&1) || {
        echo "[ERROR] Failed installing $LABEL: $OUT"
        echo "        Make sure LSPosed/Vector + CorePatch 4.9 are active (signature verification"
        echo "        & version downgrade enabled) and ro.build.official.release=false is set."
        exit 1
    }
    echo "    $OUT"
}

install_apk "$BASE_DIR/apks/Gallery_15.9_MyFanCam.apk" "Samsung Gallery (My FanCam enabled)"
install_apk "$BASE_DIR/apks/VideoEditor_OneUI9_MyFanCam.apk" "Samsung Video Editor (One UI 9 + VEKit 4.0.14)"

am force-stop com.sec.android.app.vepreload 2>/dev/null || true
am force-stop com.sec.android.gallery3d 2>/dev/null || true

echo "[*] Active bind-mounts:"
mount | grep -E 'floating_feature|libveframework|libcontextanalyzer_jni' || true

echo "My FanCam + Audio Eraser installation finished!"

if [ "$NO_ZYGOTE" -eq 0 ]; then
    echo "[*] Restarting zygote in 2 seconds"
    sleep 2
    setprop ctl.restart zygote
else
    echo "[INFO] --no-zygote set: restart zygote ('setprop ctl.restart zygote') if features do not appear yet."
fi
