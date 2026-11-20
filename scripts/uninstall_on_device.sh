#!/system/bin/sh
set -e

NO_ZYGOTE=0
for arg in "$@"; do
    case "$arg" in
        --no-zygote) NO_ZYGOTE=1 ;;
    esac
done

if [ "$(id -u)" -ne 0 ]; then
    echo "[ERROR] Root permissions required. Run with: su -mm -c \"sh $0\""
    exit 1
fi


HAS_NSENTER=0
if nsenter -t 1 -m -- true 2>/dev/null; then
    HAS_NSENTER=1
fi

unbind_file() {
    DST="$1"
    if [ "$HAS_NSENTER" -eq 1 ]; then
        nsenter -t 1 -m -- umount -l "$DST" 2>/dev/null || true
    fi
    umount -l "$DST" 2>/dev/null || true
}

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

unbind_file /system/lib64/libveframework.videoeditor.samsung.so
unbind_file /system/lib64/libcontextanalyzer_jni.media.samsung.so

if [ -f /data/adb/fancam/ff_backup.xml ]; then
    FF_BACKUP="/data/adb/fancam/ff_backup.xml"
    if grep -q 'SEC_FLOATING_FEATURE_FRAMEWORK_SUPPORT_NOW_BRIEF' "$FF_BACKUP"; then
        cp -f "$FF_BACKUP" /data/local/tmp/ff_s23.xml
        chmod 644 /data/local/tmp/ff_s23.xml
        chcon u:object_r:system_file:s0 /data/local/tmp/ff_s23.xml
        FF_RESTORE="/data/local/tmp/ff_s23.xml"
        if [ -f /data/adb/ff_s23.xml ]; then
            cp -f "$FF_BACKUP" /data/adb/ff_s23.xml
            chmod 644 /data/adb/ff_s23.xml
            chcon u:object_r:system_file:s0 /data/adb/ff_s23.xml
            FF_RESTORE="/data/adb/ff_s23.xml"
        fi
        bind_file "$FF_RESTORE" /system/etc/floating_feature.xml
    else
        unbind_file /system/etc/floating_feature.xml
    fi
else
    for ff in /data/local/tmp/ff_s23.xml /data/adb/ff_s23.xml; do
        if [ -f "$ff" ]; then
            sed -i '/SEC_FLOATING_FEATURE_MMFW_SUPPORT_TARGET_TRACKING/d' "$ff"
            sed -i '/SEC_FLOATING_FEATURE_MMFW_SUPPORT_MEDIA_CONTEXT_ANALYZER/d' "$ff"
            sed -i '/SEC_FLOATING_FEATURE_MMFW_CONFIG_MEDIA_CONTEXT_ANALYZER_CORE/d' "$ff"
            sed -i '/SEC_FLOATING_FEATURE_AUDIO_CONFIG_MULTISOURCE_SEPARATOR/d' "$ff"
        fi
    done
fi

rm -f /data/adb/post-fs-data.d/99_z_fancam_audioeraser.sh
rm -rf /data/local/tmp/mca /data/sbs /data/adb/fancam

am force-stop com.sec.android.app.vepreload 2>/dev/null || true
am force-stop com.sec.android.gallery3d 2>/dev/null || true


if [ "$NO_ZYGOTE" -eq 0 ]; then
    echo "[*] Restarting zygote in 2 seconds"
    sleep 2
    setprop ctl.restart zygote
fi
