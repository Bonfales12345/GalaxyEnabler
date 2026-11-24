package com.bonfales.galaxyenabler

import android.content.Context
import android.net.Uri
import android.os.Process
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.StringReader
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

enum class FeatureGroup(
    val title: String,
    val subtitle: String,
    val inDefaultProfile: Boolean,
    val tags: Map<String, String>,
) {
    CORE(
        title = "Core AI & Now Brief",
        subtitle = "Now Brief, Now Nudge & on-device AI",
        inDefaultProfile = true,
        tags = linkedMapOf(
            "SEC_FLOATING_FEATURE_COMMON_CONFIG_AI_VERSION" to "20263",
            "SEC_FLOATING_FEATURE_GENAI_CONFIG_LLM_VERSION" to "1.70",
            "SEC_FLOATING_FEATURE_FRAMEWORK_SUPPORT_PERSONALIZED_DATA_CORE" to "TRUE",
            "SEC_FLOATING_FEATURE_FRAMEWORK_CONFIG_NOW_NUDGE_VERSION" to "2",
            "SEC_FLOATING_FEATURE_GENAI_SUPPORT_OFFLINE_LANGUAGEMODEL" to "TRUE",
        ),
    ),
    AICORE(
        title = "AICore & Gemini Nano",
        subtitle = "AICore sysconfig feature, DeviceConfig & on-device LLM flags",
        inDefaultProfile = true,
        tags = linkedMapOf(
            "SEC_FLOATING_FEATURE_GENAI_SUPPORT_AICORE" to "TRUE",
            "SEC_FLOATING_FEATURE_GENAI_SUPPORT_GEMINI_NANO" to "TRUE",
            "SEC_FLOATING_FEATURE_GENAI_SUPPORT_ON_DEVICE_LLM" to "TRUE",
            "SEC_FLOATING_FEATURE_GENAI_SUPPORT_OFFLINE_LANGUAGEMODEL" to "TRUE",
            "SEC_FLOATING_FEATURE_GENAI_CONFIG_LLM_VERSION" to "1.70",
            "SEC_FLOATING_FEATURE_COMMON_CONFIG_AI_VERSION" to "20263",
        ),
    ),
    NOTIFICATION_HIGHLIGHTS(
        title = "Notification Highlights",
        subtitle = "AI notification summaries & priority sorting",
        inDefaultProfile = true,
        tags = linkedMapOf(
            "SEC_FLOATING_FEATURE_SYSTEMUI_SUPPORT_NOTIFICATION_HIGHLIGHT" to "TRUE",
            "SEC_FLOATING_FEATURE_SYSTEMUI_SUPPORT_NOTIFICATION_SUMMARY" to "TRUE",
            "SEC_FLOATING_FEATURE_SYSTEMUI_SUPPORT_NOTIFICATION_PRIORITIZE" to "TRUE",
            "SEC_FLOATING_FEATURE_SETTINGS_SUPPORT_NOTIFICATION_HIGHLIGHT" to "TRUE",
            "SEC_FLOATING_FEATURE_GENAI_SUPPORT_OFFLINE_LANGUAGEMODEL" to "TRUE",
            "SEC_FLOATING_FEATURE_GENAI_CONFIG_LLM_VERSION" to "1.70",
            "SEC_FLOATING_FEATURE_COMMON_CONFIG_AI_VERSION" to "20263",
        ),
    ),
    AI_SELECT(
        title = "AI Select",
        subtitle = "S26 contextual screen selection, GIFs & smart actions",
        inDefaultProfile = true,
        tags = linkedMapOf(
            "SEC_FLOATING_FEATURE_SMARTCAPTURE_SUPPORT_AI_SELECT" to "TRUE",
            "SEC_FLOATING_FEATURE_FRAMEWORK_SUPPORT_AI_SELECT" to "TRUE",
            "SEC_FLOATING_FEATURE_AIRCOMMAND_SUPPORT_AI_SELECT" to "TRUE",
            "SEC_FLOATING_FEATURE_GENAI_SUPPORT_SMART_SELECT" to "TRUE",
        ),
    ),
    NEXT_GEN_BIXBY(
        title = "Next-Gen Bixby Agent",
        subtitle = "S26 conversational LLM device control & web agent",
        inDefaultProfile = true,
        tags = linkedMapOf(
            "SEC_FLOATING_FEATURE_BIXBY_SUPPORT_LLM_AGENT" to "TRUE",
            "SEC_FLOATING_FEATURE_BIXBY_SUPPORT_DEVICE_AGENT" to "TRUE",
            "SEC_FLOATING_FEATURE_BIXBY_SUPPORT_ON_DEVICE_AI" to "TRUE",
            "SEC_FLOATING_FEATURE_COMMON_SUPPORT_AI_AGENT" to "TRUE",
            "SEC_FLOATING_FEATURE_GENAI_CONFIG_LLM_VERSION" to "1.70",
        ),
    ),
    NATURAL_AI_SEARCH(
        title = "Semantic AI Search",
        subtitle = "S26 natural-language search in Settings, Finder & Gallery",
        inDefaultProfile = true,
        tags = linkedMapOf(
            "SEC_FLOATING_FEATURE_SETTINGS_SUPPORT_AI_SEARCH" to "TRUE",
            "SEC_FLOATING_FEATURE_SETTINGS_SUPPORT_LLM_SEARCH" to "TRUE",
            "SEC_FLOATING_FEATURE_GALLERY_SUPPORT_AI_SEARCH" to "TRUE",
            "SEC_FLOATING_FEATURE_GALLERY_SUPPORT_NATURAL_SEARCH" to "TRUE",
            "SEC_FLOATING_FEATURE_SFINDER_SUPPORT_AI_SEARCH" to "TRUE",
        ),
    ),
    LIVE_AUDIO_ERASER(
        title = "Live Audio Eraser",
        subtitle = "S26 real-time voice boost & noise removal in media apps",
        inDefaultProfile = true,
        tags = linkedMapOf(
            "SEC_FLOATING_FEATURE_AUDIO_SUPPORT_LIVE_AUDIO_ERASER" to "TRUE",
            "SEC_FLOATING_FEATURE_AUDIO_SUPPORT_REALTIME_NOISE_REDUCTION" to "TRUE",
            "SEC_FLOATING_FEATURE_SYSTEMUI_SUPPORT_LIVE_AUDIO_ERASER" to "TRUE",
            "SEC_FLOATING_FEATURE_AUDIO_SUPPORT_VOICE_BOOST" to "TRUE",
        ),
    ),
    NOTE_HEALTH_ASSIST(
        title = "Note & Health AI Assist",
        subtitle = "Notes AI summary, Math Solver & Health Energy Score",
        inDefaultProfile = true,
        tags = linkedMapOf(
            "SEC_FLOATING_FEATURE_NOTES_SUPPORT_AI_ASSIST" to "TRUE",
            "SEC_FLOATING_FEATURE_NOTES_SUPPORT_MATH_SOLVER" to "TRUE",
            "SEC_FLOATING_FEATURE_HEALTH_SUPPORT_AI_INSIGHT" to "TRUE",
            "SEC_FLOATING_FEATURE_HEALTH_SUPPORT_ENERGY_SCORE" to "TRUE",
        ),
    ),
    CREATIVE_STUDIO(
        title = "Creative Studio",
        subtitle = "Drawing Assist, Sketch & AI Stickers",
        inDefaultProfile = true,
        tags = linkedMapOf(
            "SEC_FLOATING_FEATURE_GENAI_SUPPORT_CREATIVE_STUDIO" to "TRUE",
            "SEC_FLOATING_FEATURE_GENAI_SUPPORT_DRAWING_ASSIST" to "TRUE",
            "SEC_FLOATING_FEATURE_GENAI_SUPPORT_SKETCH_TO_IMAGE" to "TRUE",
            "SEC_FLOATING_FEATURE_GALLERY_SUPPORT_SKETCH_TO_IMAGE" to "TRUE",
            "SEC_FLOATING_FEATURE_GENAI_SUPPORT_IMAGE_GENERATION" to "TRUE",
            "SEC_FLOATING_FEATURE_GENAI_SUPPORT_STYLE_TRANSFER" to "TRUE",
            "SEC_FLOATING_FEATURE_GALLERY_SUPPORT_AI_STICKER" to "TRUE",
            "SEC_FLOATING_FEATURE_CALENDAR_SUPPORT_CREATE_STICKER" to "TRUE",
        ),
    ),
    AOD_WALLPAPER(
        title = "AOD Wallpaper",
        subtitle = "Fullscreen Always On Display wallpaper",
        inDefaultProfile = true,
        tags = linkedMapOf(
            "SEC_FLOATING_FEATURE_LCD_CONFIG_AOD_FULLSCREEN" to "1",
            "SEC_FLOATING_FEATURE_LCD_CONFIG_AOD_BRIGHTNESS_ANIMATION" to "0",
            "SEC_FLOATING_FEATURE_LCD_CONFIG_AOD_REFRESH_RATE" to "0",
            "SEC_FLOATING_FEATURE_LOCKSCREEN_SUPPORT_AOD_WALLPAPER" to "TRUE",
        ),
    ),
    PHOTO_AMBIENT_WALLPAPER(
        title = "Photo Ambient Wallpaper",
        subtitle = "Dynamic weather & time-of-day lock screen wallpaper",
        inDefaultProfile = true,
        tags = linkedMapOf(
            "SEC_FLOATING_FEATURE_LOCKSCREEN_SUPPORT_PHOTO_AMBIENT_WALLPAPER" to "TRUE",
            "SEC_FLOATING_FEATURE_GENAI_SUPPORT_TIME_WEATHER_WALLPAPER" to "V4",
            "SEC_FLOATING_FEATURE_FRAMEWORK_SUPPORT_AI_WALLPAPER" to "TRUE",
            "SEC_FLOATING_FEATURE_WALLPAPER_SUPPORT_PHOTO_AMBIENT" to "TRUE",
            "SEC_FLOATING_FEATURE_GENAI_SUPPORT_IMAGE_GENERATION" to "TRUE",
        ),
    ),
    VIVIDNESS_SLIDER(
        title = "Vividness Slider",
        subtitle = "Screen mode vividness intensity slider",
        inDefaultProfile = true,
        tags = linkedMapOf(
            "SEC_FLOATING_FEATURE_LCD_CONFIG_VIVIDNESS" to "1",
            "SEC_FLOATING_FEATURE_LCD_CONFIG_VIVIDPLUS" to "1",
            "SEC_FLOATING_FEATURE_LCD_SUPPORT_VIVID_PLUS" to "TRUE",
            "SEC_FLOATING_FEATURE_LCD_SUPPORT_NATURAL_MODE" to "TRUE",
        ),
    ),
    ADAPTIVE_COLOR_TONE(
        title = "Adaptive Color Tone",
        subtitle = "Ambient white balance & color adaptation",
        inDefaultProfile = true,
        tags = linkedMapOf(
            "SEC_FLOATING_FEATURE_LCD_SUPPORT_EAD" to "TRUE",
            "SEC_FLOATING_FEATURE_LCD_CONFIG_EAD" to "1",
            "SEC_FLOATING_FEATURE_LCD_SUPPORT_ADAPTIVE_COLOR_TONE" to "TRUE",
            "SEC_FLOATING_FEATURE_LCD_CONFIG_ADAPTIVE_COLOR_TONE" to "TRUE",
        ),
    ),
    SHARE_WITH_APPLE(
        title = "Share with Apple",
        subtitle = "Quick Share to AirDrop",
        inDefaultProfile = true,
        tags = linkedMapOf(
            "SEC_FLOATING_FEATURE_QUICKSHARE_SUPPORT_APPLE_SHARE" to "TRUE",
            "SEC_FLOATING_FEATURE_QUICKSHARE_SUPPORT_CROSS_PLATFORM" to "TRUE",
            "SEC_FLOATING_FEATURE_ALLSHARE_SUPPORT_APPLE_AIRDROP" to "TRUE",
            "SEC_FLOATING_FEATURE_WIFI_SUPPORT_AWDL_COMPAT" to "TRUE",
            "SEC_FLOATING_FEATURE_COMMON_CONFIG_AI_VERSION" to "20263",
        ),
    ),
    SCAM_CALL_SCREENING(
        title = "Scam Detection & Call Screening",
        subtitle = "Built-in Phone scam alerts & AI call screening",
        inDefaultProfile = true,
        tags = linkedMapOf(
            "SEC_FLOATING_FEATURE_CALL_SUPPORT_CALL_SCREENING" to "TRUE",
            "SEC_FLOATING_FEATURE_CALL_SUPPORT_AI_CALL_SCREENING" to "TRUE",
            "SEC_FLOATING_FEATURE_CALL_SUPPORT_SCAM_DETECTION" to "TRUE",
            "SEC_FLOATING_FEATURE_CALL_SUPPORT_VOICE_PHISHING_DETECTION" to "TRUE",
            "SEC_FLOATING_FEATURE_CALL_SUPPORT_AI_CALL_ASSIST" to "TRUE",
            "SEC_FLOATING_FEATURE_COMMON_CONFIG_AI_VERSION" to "20263",
        ),
    ),
    CALL_BRIEF(
        title = "Call Brief",
        subtitle = "AI Call Assist & summaries",
        inDefaultProfile = true,
        tags = linkedMapOf(
            "SEC_FLOATING_FEATURE_CALL_SUPPORT_CALL_BRIEF" to "TRUE",
            "SEC_FLOATING_FEATURE_CALL_SUPPORT_AI_CALL_ASSIST" to "TRUE",
        ),
    ),
    PHOTO_ASSIST(
        title = "Photo Assist",
        subtitle = "Generative Edit, Portrait & Erasers",
        inDefaultProfile = true,
        tags = linkedMapOf(
            "SEC_FLOATING_FEATURE_GALLERY_SUPPORT_GENAI_EDIT" to "TRUE",
            "SEC_FLOATING_FEATURE_GALLERY_SUPPORT_SKETCH_TO_IMAGE" to "TRUE",
            "SEC_FLOATING_FEATURE_GALLERY_SUPPORT_PORTRAIT_STUDIO" to "TRUE",
            "SEC_FLOATING_FEATURE_GALLERY_SUPPORT_LIVE_EFFECT" to "TRUE",
            "SEC_FLOATING_FEATURE_GALLERY_SUPPORT_BEST_FACE" to "TRUE",
            "SEC_FLOATING_FEATURE_GALLERY_CONFIG_BEST_FACE_MODE" to "GENIMAGE",
            "SEC_FLOATING_FEATURE_GALLERY_SUPPORT_INSTANT_SLOWMO" to "TRUE",
            "SEC_FLOATING_FEATURE_GENAI_SUPPORT_STYLE_TRANSFER" to "TRUE",
            "SEC_FLOATING_FEATURE_GENAI_SUPPORT_OBJECT_ERASER" to "TRUE",
            "SEC_FLOATING_FEATURE_GENAI_SUPPORT_SHADOW_ERASER" to "TRUE",
            "SEC_FLOATING_FEATURE_GENAI_SUPPORT_REFLECTION_ERASER" to "TRUE",
            "SEC_FLOATING_FEATURE_GENAI_SUPPORT_IMAGE_CLIPPER" to "TRUE",
            "SEC_FLOATING_FEATURE_GENAI_SUPPORT_SMART_LASSO" to "TRUE",
            "SEC_FLOATING_FEATURE_GENAI_SUPPORT_SPOT_FIXER" to "TRUE",
        ),
    ),
    SUITE(
        title = "Writing & Browsing",
        subtitle = "Writing, Transcript & Browsing Assist",
        inDefaultProfile = true,
        tags = linkedMapOf(
            "SEC_FLOATING_FEATURE_COMMON_SUPPORT_AI_AGENT" to "TRUE",
            "SEC_FLOATING_FEATURE_SIP_SUPPORT_AI_WRITING_ASSIST" to "TRUE",
            "SEC_FLOATING_FEATURE_VOICERECORDER_SUPPORT_AI_TRANSCRIPT" to "TRUE",
            "SEC_FLOATING_FEATURE_INTERNET_SUPPORT_AI_BROWSING_ASSIST" to "TRUE",
        ),
    ),
    AUDIO(
        title = "Audio Eraser",
        subtitle = "Remove background noise in video",
        inDefaultProfile = false,
        tags = linkedMapOf(
            "SEC_FLOATING_FEATURE_MMFW_SUPPORT_AUDIO_ERASER" to "TRUE",
            "SEC_FLOATING_FEATURE_GALLERY_SUPPORT_AUDIO_ERASER" to "TRUE",
            "SEC_FLOATING_FEATURE_AUDIO_SUPPORT_AI_NOISE_REDUCTION" to "TRUE",
            "SEC_FLOATING_FEATURE_AUDIO_CONFIG_MULTISOURCE_SEPARATOR" to "{FastScanning_6, SourceSeparator_4, Version_1.3.0}",
        ),
    ),
    FANCAM(
        title = "FanCam",
        subtitle = "AI auto-framing & subject tracking",
        inDefaultProfile = false,
        tags = linkedMapOf(
            "SEC_FLOATING_FEATURE_GALLERY_SUPPORT_AI_FANCAM" to "TRUE",
            "SEC_FLOATING_FEATURE_CAMERA_SUPPORT_AUTO_FRAMING" to "TRUE",
            "SEC_FLOATING_FEATURE_MMFW_SUPPORT_TARGET_TRACKING" to "TRUE",
            "SEC_FLOATING_FEATURE_MMFW_SUPPORT_MEDIA_CONTEXT_ANALYZER" to "TRUE",
            "SEC_FLOATING_FEATURE_MMFW_CONFIG_MEDIA_CONTEXT_ANALYZER_CORE" to "NPU",
        ),
    ),
    PRO_VIDEO_LOG_APV(
        title = "Samsung Log & APV Video",
        subtitle = "S26 Log color profile & APV pro video flags",
        inDefaultProfile = false,
        tags = linkedMapOf(
            "SEC_FLOATING_FEATURE_CAMERA_SUPPORT_LOG_VIDEO" to "TRUE",
            "SEC_FLOATING_FEATURE_GALLERY_SUPPORT_LOG_VIDEO_LUT" to "TRUE",
            "SEC_FLOATING_FEATURE_MMFW_SUPPORT_APV_CODEC" to "TRUE",
            "SEC_FLOATING_FEATURE_CAMERA_SUPPORT_APV" to "TRUE",
        ),
    ),
    HORIZONTAL_LOCK(
        title = "Horizontal Lock",
        subtitle = "S26 360° Super Steady horizon lock (requires One UI 9 / S26 Camera HAL)",
        inDefaultProfile = false,
        tags = linkedMapOf(
            "SEC_FLOATING_FEATURE_CAMERA_SUPPORT_HORIZONTAL_LOCK" to "TRUE",
            "SEC_FLOATING_FEATURE_CAMERA_SUPPORT_SUPER_VIDEO_STABILIZATION" to "TRUE",
            "SEC_FLOATING_FEATURE_CAMERA_SUPPORT_VIDEO_AUTO_FPS" to "TRUE",
        ),
    ),
}

data class MediaPackStatus(
    val audioModelsReady: Boolean = false,
    val fancamShimsReady: Boolean = false,
    val fancamBindActive: Boolean = false,
    val styleTransferModelPresent: Boolean = false,
    val aodFullscreenActive: Boolean = false,
    val creativeStudioVersion: String = "",
    val quickShareVersion: String = "",
    val quickShareConnectivityVersion: String = "",
    val languageCoreVersion: String = "",
    val cameraVersion: String = "",
    val cameraFeaturePath: String = "",
    val horizontalLockMountActive: Boolean = false,
    val aicoreVersion: String = "",
    val aicoreFeatureDeclared: Boolean = false,
    val pcsVersion: String = "",
    val resetpropPath: String = "",
    val officialRelease: String = "—",
) {
    val resetpropAvailable: Boolean get() = resetpropPath.isNotBlank()
    val officialSpoofActive: Boolean get() = officialRelease == "false"
    val creativeStudioInstalled: Boolean get() = creativeStudioVersion.isNotBlank()
    val languageCoreInstalled: Boolean get() = languageCoreVersion.isNotBlank()
    val aicoreStubOnly: Boolean get() = aicoreVersion.contains("stub", ignoreCase = true)
    val aicoreFullInstalled: Boolean get() = aicoreVersion.isNotBlank() && !aicoreStubOnly

    fun needsSetup(audioEnabled: Boolean, fancamEnabled: Boolean): Boolean {
        if (audioEnabled && !audioModelsReady) return true
        if (fancamEnabled && !fancamShimsReady) return true
        return false
    }
}

enum class RootMode(val label: String) {
    SU_MM("su -mm"),
    SU_M("su -M"),
    NSENTER("nsenter"),
    SU_PLAIN("su"),
}

data class DeviceProfile(
    val model: String = "",
    val marketName: String = "",
    val soc: String = "",
    val socVendor: String = "",
    val oneUiRaw: Int = 0,
    val oneUiLabel: String = "",
    val ramGb: Int = 0,
    val rootMode: String = "su -mm",
    val ffPath: String = RootHelper.FF_TARGET,
) {
    val displayName: String
        get() = marketName.ifBlank { model }.ifBlank { "Galaxy" }

    val displayLabel: String
        get() = listOf(displayName, oneUiLabel).filter { it.isNotBlank() }.joinToString(" · ")
}

data class SystemStatus(
    val rootGranted: Boolean = false,
    val mountActive: Boolean = false,
    val aiVersion: String = "—",
    val nudgeVersion: String = "—",
    val suPath: String = "su",
    val mediaPack: MediaPackStatus = MediaPackStatus(),
    val device: DeviceProfile = DeviceProfile(),
)

object RootHelper {

    const val FF_TARGET = "/system/etc/floating_feature.xml"
    const val FF_PATCHED = "/data/local/tmp/ff_galaxyenabler_patched.xml"
    const val CAM_FEATURE_PATCHED = "/data/local/tmp/cam_feature_galaxyenabler_patched.xml"
    const val SYSCONFIG_AICORE_PATCHED = "/data/local/tmp/sysconfig_aicore_patched.xml"
    private const val FF_PATCHED_LEGACY = "/data/local/tmp/ff_galaxyai_patched.xml"
    private const val FF_PATCHED_S23U_LEGACY = "/data/local/tmp/ff_s23u_patched.xml"
    const val SIG_TARGET = "/system/etc/permissions/signature-permissions-downloadable.xml"
    const val SIG_PATCHED = "/data/local/tmp/sss_signature_patched.xml"
    const val RELEASES_URL = "https://github.com/Bonfales12345/GalaxyEnabler/releases"
    const val CREATIVE_STUDIO_URL =
        "https://www.apkmirror.com/apk/samsung-electronics-co-ltd/samsung-creative-studio/"
    const val QUICK_SHARE_URL =
        "https://www.apkmirror.com/apk/samsung-electronics-co-ltd/quick-share/"
    const val QUICK_SHARE_CONN_URL =
        "https://www.apkmirror.com/apk/samsung-electronics-co-ltd/quick-share-connectivity/"
    const val LANGUAGE_CORE_URL =
        "https://www.apkmirror.com/apk/samsung-electronics-co-ltd/samsung-language-core/"
    const val AICORE_APKMIRROR_URL =
        "https://www.apkmirror.com/apk/google-inc/android-aicore/"

    private const val DIR_FANCAM = "/data/adb/fancam"
    private const val DIR_MCA = "/data/local/tmp/mca"
    private const val DIR_MCA_MODELS = "/data/local/tmp/mca/models"
    private const val DIR_SBS = "/data/sbs"
    private const val FILE_ORIG_OFFICIAL = "/data/adb/fancam/orig_official"
    private const val FILE_ORIG_MODEL = "/data/adb/fancam/orig_model"
    private const val BIND_VEFRAMEWORK = "/system/lib64/libveframework.videoeditor.samsung.so"
    private const val BIND_CONTEXTANALYZER = "/system/lib64/libcontextanalyzer_jni.media.samsung.so"
    private const val STYLE_TRANSFER_TFLITE =
        "/system/etc/style_transfer/db/12-26_PhotoEditor_StyleTransfer_v1.0.0_HumanDetector.tflite"

    private val ffTargetCandidates = listOf(
        "/system/etc/floating_feature.xml",
        "/product/etc/floating_feature.xml",
        "/vendor/etc/floating_feature.xml",
    )

    private val camFeatureCandidates = listOf(
        "/system/cameradata/camera-feature.xml",
        "/product/cameradata/camera-feature.xml",
        "/vendor/cameradata/camera-feature.xml",
    )

    private val aicoreSysconfigCandidates = listOf(
        "/product/etc/sysconfig/google.xml",
        "/system/etc/sysconfig/google.xml",
        "/system/etc/permissions/platform.xml",
    )

    private val sigTargetCandidates = listOf(
        "/system/etc/permissions/signature-permissions-downloadable.xml",
        "/product/etc/permissions/signature-permissions-downloadable.xml",
    )

    private val sbsModelCandidates = listOf(
        "/data/sbs/fastScanner.tflite",
        "/system/etc/sbs/fastScanner.tflite",
        "/vendor/etc/sbs/fastScanner.tflite",
        "/system/etc/audio/sbs/fastScanner.tflite",
    )

    private const val PKG_SMART_SUGGESTIONS = "com.samsung.android.smartsuggestions"
    private const val PKG_GALLERY = "com.sec.android.gallery3d"
    private const val PKG_PHOTO_EDITOR = "com.sec.android.mimage.photoretouching"
    private const val PKG_VIDEO_EDITOR = "com.sec.android.app.vepreload"
    const val PKG_CREATIVE_STUDIO = "com.samsung.android.app.sketchbook"
    const val PKG_QUICK_SHARE = "com.samsung.android.app.sharelive"
    const val PKG_QUICK_SHARE_CONN = "com.samsung.android.aware.service"
    const val PKG_LANGUAGE_CORE = "com.samsung.android.offline.languagemodel"
    const val PKG_CAMERA = "com.sec.android.app.camera"
    const val PKG_GOOGLE_AICORE = "com.google.android.aicore"
    private const val PKG_SAMSUNG_AICORE = "com.samsung.android.aicore"
    private const val PKG_GOOGLE_PCS = "com.google.android.as.oss"
    private const val PKG_SMART_CAPTURE = "com.samsung.android.app.smartcapture"
    private const val PKG_GALAXY_FINDER = "com.samsung.android.app.galaxyfinder"
    private const val PKG_SCS = "com.samsung.android.scs"
    private const val PKG_AOD_SERVICE = "com.samsung.android.app.aodservice"
    private const val PKG_DIALER = "com.samsung.android.dialer"
    private const val PKG_INCALLUI = "com.samsung.android.incallui"
    private const val PKG_WALLPAPER_STYLE = "com.samsung.android.app.dressroom"
    private const val PKG_WEATHER_DAEMON = "com.sec.android.daemonapp"
    private const val PERM_BIOMETRIC_ADV = "android.permission.SET_BIOMETRIC_DIALOG_ADVANCED"

    private val PLACEHOLDER_DLCS = listOf(
        "Locator.dlc",
        "Reid.dlc",
        "Detection.dlc",
        "Pose.dlc",
        "Keyword.dlc",
    )

    private val COMPONENTS_TO_ENABLE = listOf(
        "com.samsung.android.smartsuggestions/.feature.aisuggestion.ui.appwidget.AiSuggestionAppWidgetReceiver",
        "com.samsung.android.smartsuggestions/.nudgesettings.NudgeMainSettingsActivity",
        "com.samsung.android.smartsuggestions/.nudgesettings.action.NudgeActionSettingsActivity",
        "com.samsung.android.smartsuggestions/.nudgesettings.recall.NudgeRecallSettingsActivity",
    )

    private val SETTINGS_TO_PUT = listOf(
        Triple("global", "sss_enabled", "1"),
        Triple("global", "nowbrief_enabled", "1"),
        Triple("global", "smart_suggestions_enabled", "1"),
        Triple("global", "now_nudge_enabled", "1"),
        Triple("global", "now_nudge_setting", "1"),
        Triple("system", "ai_info_confirmed", "1"),
    )

    private val suCandidates = listOf(
        "/data/adb/ksu/bin/su",
        "/data/adb/ap/bin/su",
        "/data/adb/magisk/su",
        "/debug_ramdisk/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/su/bin/su",
        "/vendor/bin/su",
    )

    private val resetpropCandidates = listOf(
        "/data/adb/ksu/bin/resetprop",
        "/data/adb/ap/bin/resetprop",
        "/data/adb/magisk/resetprop",
        "/debug_ramdisk/resetprop",
        "/sbin/resetprop",
        "/data/local/tmp/resetprop",
    )

    @Volatile
    private var cachedRootMode: RootMode? = null

    data class Result(
        val exitCode: Int,
        val output: String,
    ) {
        val isSuccess: Boolean get() = exitCode == 0
    }

    fun findSuBinary(): String {
        for (path in suCandidates) {
            val file = File(path)
            if (file.exists() && file.canExecute()) return path
        }
        return "su"
    }

    private fun runProbeArgs(vararg args: String): Boolean {
        return try {
            val proc = ProcessBuilder(*args).redirectErrorStream(true).start()
            proc.outputStream.close()
            val out = proc.inputStream.bufferedReader(StandardCharsets.UTF_8).readText().trim()
            proc.waitFor() == 0 && out == "0"
        } catch (_: Exception) {
            false
        }
    }

    fun detectRootMode(suBin: String = findSuBinary()): RootMode {
        cachedRootMode?.let { return it }
        val mode = when {
            runProbeArgs(suBin, "-mm", "-c", "id -u") -> RootMode.SU_MM
            runProbeArgs(suBin, "-M", "-c", "id -u") -> RootMode.SU_M
            runProbeArgs(suBin, "-c", "nsenter -t 1 -m -- id -u") -> RootMode.NSENTER
            else -> RootMode.SU_PLAIN
        }
        cachedRootMode = mode
        return mode
    }

    fun formatOneUiVersion(raw: Int): String {
        if (raw < 10000) return ""
        return "One UI ${raw / 10000}.${(raw % 10000) / 100}"
    }

    fun classifySocVendor(soc: String, hardware: String): String {
        val combined = "$soc $hardware".lowercase()
        return when {
            listOf("qcom", "sm", "kalama", "pineapple", "sun", "taro", "lahaina", "snapdragon")
                .any { combined.contains(it) } -> "Qualcomm"
            listOf("exynos", "s5e", "erd", "universal")
                .any { combined.contains(it) } -> "Exynos"
            listOf("mt", "dimensity", "mediatek")
                .any { combined.contains(it) } -> "MediaTek"
            else -> soc.ifBlank { hardware }
        }
    }

    fun isFlagshipNpuSoc(soc: String, hardware: String): Boolean {
        val combined = "$soc $hardware".lowercase()
        val qcomMatch = listOf("qcom", "sm", "kalama", "pineapple", "sun", "taro", "lahaina", "snapdragon")
            .any { combined.contains(it) }
        if (qcomMatch) return true
        val exynosFlagship = listOf("s5e99", "exynos22", "exynos24", "exynos25", "exynos26", "2200", "2400", "2500", "2600")
            .any { combined.contains(it) }
        if (exynosFlagship) return true
        val mtkFlagship = listOf("mt698", "mt699", "9300", "9400")
            .any { combined.contains(it) }
        return mtkFlagship
    }

    private fun stripBom(xml: String): String = xml.removePrefix("\uFEFF").trimStart()

    private fun buildSuCommandArgs(suBin: String, mode: RootMode, command: String): Array<String> = when (mode) {
        RootMode.SU_MM -> arrayOf(suBin, "-mm", "-c", command)
        RootMode.SU_M -> arrayOf(suBin, "-M", "-c", command)
        RootMode.NSENTER -> arrayOf(suBin, "-c", "nsenter -t 1 -m -- sh -c ${shellQuote(command)}")
        RootMode.SU_PLAIN -> arrayOf(suBin, "-c", command)
    }

    suspend fun runSuMm(
        command: String,
        stdinBytes: ByteArray? = null,
        logCommand: Boolean = true,
        onLog: (suspend (String) -> Unit)? = null,
    ): Result = withContext(Dispatchers.IO) {
        val suBin = findSuBinary()
        val mode = detectRootMode(suBin)
        if (logCommand) {
            onLog?.invoke("$ ${mode.label} -c ${shellQuote(command)}")
        }
        val sb = StringBuilder()
        try {
            val args = buildSuCommandArgs(suBin, mode, command)
            val proc = ProcessBuilder(*args)
                .redirectErrorStream(true)
                .start()

            if (stdinBytes != null) {
                proc.outputStream.use { out ->
                    out.write(stdinBytes)
                    out.flush()
                }
            } else {
                proc.outputStream.close()
            }

            BufferedReader(InputStreamReader(proc.inputStream, StandardCharsets.UTF_8)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val text = line.orEmpty()
                    sb.append(text).append('\n')
                    if (logCommand) {
                        onLog?.invoke(text)
                    }
                }
            }
            val code = proc.waitFor()
            if (logCommand && code != 0) {
                onLog?.invoke("[exit $code]")
            }
            Result(code, sb.toString().trim())
        } catch (e: Exception) {
            val err = "[ERROR] Failed executing $suBin (${mode.label}): ${e.message}"
            onLog?.invoke(err)
            Result(-1, err)
        }
    }

    private fun parseVersionTuple(text: String, prefix: String): List<Int>? {
        val match = Regex("${Regex.escape(prefix)}(\\d+(?:\\.\\d+)*)").find(text) ?: return null
        return match.groupValues[1].split('.').mapNotNull { it.toIntOrNull() }.ifEmpty { null }
    }

    private fun compareVersionTuples(a: List<Int>, b: List<Int>): Int {
        val maxLen = maxOf(a.size, b.size)
        for (i in 0 until maxLen) {
            val av = a.getOrElse(i) { 0 }
            val bv = b.getOrElse(i) { 0 }
            if (av != bv) return av.compareTo(bv)
        }
        return 0
    }

    @JvmOverloads
    fun patchFloatingFeatureXml(
        rawXml: String,
        tagsToApply: Map<String, String>,
        device: DeviceProfile? = null,
    ): String {
        val cleanXml = stripBom(rawXml)
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(InputSource(StringReader(cleanXml)))
        val root = doc.documentElement ?: error("Missing XML root element")
        if (root.tagName != "SecFloatingFeatureSet") {
            error("Unexpected root tag <${root.tagName}>; expected <SecFloatingFeatureSet>")
        }

        val existing = mutableMapOf<String, Element>()
        val children = root.childNodes
        for (i in 0 until children.length) {
            val node = children.item(i)
            if (node.nodeType == Node.ELEMENT_NODE) {
                val el = node as Element
                existing[el.tagName] = el
            }
        }

        val stockHasFullscreenAod =
            existing["SEC_FLOATING_FEATURE_LCD_CONFIG_AOD_FULLSCREEN"]?.textContent?.trim() == "1"

        for ((tag, value) in tagsToApply) {
            val el = existing[tag]
            val stockVal = el?.textContent?.trim().orEmpty()

            if (tag == "SEC_FLOATING_FEATURE_GENAI_SUPPORT_OFFLINE_LANGUAGEMODEL" &&
                device != null && device.ramGb in 1..5
            ) {
                continue
            }

            if (stockHasFullscreenAod &&
                (tag == "SEC_FLOATING_FEATURE_LCD_CONFIG_AOD_REFRESH_RATE" ||
                    tag == "SEC_FLOATING_FEATURE_LCD_CONFIG_AOD_BRIGHTNESS_ANIMATION") &&
                stockVal.isNotEmpty()
            ) {
                continue
            }

            val resolvedValue: String = when (tag) {
                "SEC_FLOATING_FEATURE_COMMON_CONFIG_AI_VERSION",
                "SEC_FLOATING_FEATURE_FRAMEWORK_CONFIG_NOW_NUDGE_VERSION",
                "SEC_FLOATING_FEATURE_LCD_CONFIG_VIVIDNESS",
                "SEC_FLOATING_FEATURE_LCD_CONFIG_VIVIDPLUS",
                "SEC_FLOATING_FEATURE_LCD_CONFIG_EAD", -> {
                    val curInt = stockVal.toIntOrNull()
                    val reqInt = value.toIntOrNull()
                    if (curInt != null && reqInt != null && curInt > reqInt) stockVal else value
                }
                "SEC_FLOATING_FEATURE_GENAI_CONFIG_LLM_VERSION" -> {
                    val curDbl = stockVal.toDoubleOrNull()
                    val reqDbl = value.toDoubleOrNull()
                    if (curDbl != null && reqDbl != null && curDbl > reqDbl) stockVal else value
                }
                "SEC_FLOATING_FEATURE_GENAI_SUPPORT_TIME_WEATHER_WALLPAPER" -> {
                    val curVer = Regex("^V(\\d+)$", RegexOption.IGNORE_CASE).find(stockVal)?.groupValues?.get(1)?.toIntOrNull()
                    val reqVer = Regex("^V(\\d+)$", RegexOption.IGNORE_CASE).find(value)?.groupValues?.get(1)?.toIntOrNull()
                    if (curVer != null && reqVer != null && curVer > reqVer) stockVal else value
                }
                "SEC_FLOATING_FEATURE_AUDIO_CONFIG_MULTISOURCE_SEPARATOR" -> {
                    val curVer = parseVersionTuple(stockVal, "Version_")
                    val reqVer = parseVersionTuple(value, "Version_")
                    if (curVer != null && reqVer != null && compareVersionTuples(curVer, reqVer) > 0) {
                        stockVal
                    } else {
                        value
                    }
                }
                "SEC_FLOATING_FEATURE_MMFW_CONFIG_MEDIA_CONTEXT_ANALYZER_CORE" -> {
                    when {
                        stockVal.isNotEmpty() -> stockVal
                        device != null && !isFlagshipNpuSoc(device.soc, device.socVendor) -> "CPU"
                        else -> value
                    }
                }
                else -> value
            }

            if (el != null) {
                el.textContent = resolvedValue
            } else {
                root.appendChild(doc.createTextNode("    "))
                val newEl = doc.createElement(tag)
                newEl.textContent = resolvedValue
                root.appendChild(newEl)
                root.appendChild(doc.createTextNode("\n"))
            }
        }

        val transformer = TransformerFactory.newInstance().newTransformer().apply {
            setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
        }
        val writer = StringWriter()
        transformer.transform(DOMSource(doc), StreamResult(writer))
        return writer.toString()
    }

    fun patchCameraFeatureXml(
        rawXml: String,
        localFlags: Map<String, String>,
    ): String {
        val cleanXml = stripBom(rawXml)
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(InputSource(StringReader(cleanXml)))
        val root = doc.documentElement ?: error("Missing XML root element")
        if (root.tagName != "resources") {
            error("Unexpected root tag <${root.tagName}>; expected <resources>")
        }

        val existing = mutableMapOf<String, Element>()
        val children = root.childNodes
        for (i in 0 until children.length) {
            val node = children.item(i)
            if (node.nodeType == Node.ELEMENT_NODE) {
                val el = node as Element
                if (el.tagName == "local") {
                    val nameAttr = el.getAttribute("name")
                    if (nameAttr.isNotBlank()) {
                        existing[nameAttr] = el
                    }
                }
            }
        }

        for ((name, value) in localFlags) {
            val el = existing[name]
            if (el != null) {
                el.setAttribute("value", value)
            } else {
                root.appendChild(doc.createTextNode("    "))
                val newEl = doc.createElement("local").apply {
                    setAttribute("name", name)
                    setAttribute("value", value)
                }
                root.appendChild(newEl)
                root.appendChild(doc.createTextNode("\n"))
            }
        }

        val transformer = TransformerFactory.newInstance().newTransformer().apply {
            setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
        }
        val writer = StringWriter()
        transformer.transform(DOMSource(doc), StreamResult(writer))
        return writer.toString()
    }

    fun patchSysconfigAicoreXml(rawXml: String): String {
        val cleanXml = stripBom(rawXml)
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(InputSource(StringReader(cleanXml)))
        val root = doc.documentElement ?: error("Missing XML root element")
        if (root.tagName != "config" && root.tagName != "permissions") {
            error("Unexpected root tag <${root.tagName}>; expected <config> or <permissions>")
        }

        val aicoreFeatureName = "com.google.android.feature.AICORE"
        val toRemove = mutableListOf<Node>()
        var hasFeature = false
        val children = root.childNodes
        for (i in 0 until children.length) {
            val node = children.item(i)
            if (node.nodeType == Node.ELEMENT_NODE) {
                val el = node as Element
                if (el.tagName == "unavailable-feature" && el.getAttribute("name") == aicoreFeatureName) {
                    toRemove.add(el)
                } else if (el.tagName == "feature" && el.getAttribute("name") == aicoreFeatureName) {
                    hasFeature = true
                }
            }
        }
        for (node in toRemove) {
            root.removeChild(node)
        }
        if (!hasFeature) {
            root.appendChild(doc.createTextNode("    "))
            val featEl = doc.createElement("feature").apply {
                setAttribute("name", aicoreFeatureName)
            }
            root.appendChild(featEl)
            root.appendChild(doc.createTextNode("\n"))
        }

        val transformer = TransformerFactory.newInstance().newTransformer().apply {
            setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
        }
        val writer = StringWriter()
        transformer.transform(DOMSource(doc), StreamResult(writer))
        return writer.toString()
    }

    fun patchSignaturePermissionsXml(rawXml: String): String? {
        val cleanXml = stripBom(rawXml)
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(InputSource(StringReader(cleanXml)))
        val root = doc.documentElement ?: error("Missing XML root element")
        if (root.tagName != "permissions") {
            error("Unexpected root tag <${root.tagName}>; expected <permissions>")
        }

        var pkgEl: Element? = null
        val children = root.childNodes
        for (i in 0 until children.length) {
            val node = children.item(i)
            if (node.nodeType == Node.ELEMENT_NODE) {
                val el = node as Element
                if (el.tagName == "signature-permissions" && el.getAttribute("package") == PKG_SMART_SUGGESTIONS) {
                    pkgEl = el
                    break
                }
            }
        }

        if (pkgEl != null) {
            val perms = pkgEl.childNodes
            for (i in 0 until perms.length) {
                val node = perms.item(i)
                if (node.nodeType == Node.ELEMENT_NODE) {
                    val el = node as Element
                    if (el.tagName == "permission" && el.getAttribute("name") == PERM_BIOMETRIC_ADV) {
                        return null
                    }
                }
            }
        } else {
            root.appendChild(doc.createTextNode("    "))
            pkgEl = doc.createElement("signature-permissions").apply {
                setAttribute("package", PKG_SMART_SUGGESTIONS)
            }
            root.appendChild(pkgEl)
            root.appendChild(doc.createTextNode("\n"))
        }

        val permEl = doc.createElement("permission").apply {
            setAttribute("name", PERM_BIOMETRIC_ADV)
        }
        pkgEl.appendChild(doc.createTextNode("\n        "))
        pkgEl.appendChild(permEl)
        pkgEl.appendChild(doc.createTextNode("\n    "))

        val transformer = TransformerFactory.newInstance().newTransformer().apply {
            setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
        }
        val writer = StringWriter()
        transformer.transform(DOMSource(doc), StreamResult(writer))
        return writer.toString()
    }

    private fun extractTagValue(grepOutput: String, tagName: String): String {
        val regex = Regex("<$tagName>([^<]*)</$tagName>")
        return regex.find(grepOutput)?.groupValues?.getOrNull(1)?.trim()?.ifEmpty { "—" } ?: "—"
    }

    suspend fun findResetprop(onLog: (suspend (String) -> Unit)? = null): String {
        val candidates = resetpropCandidates.joinToString(" ")
        val res = runSuMm(
            "for p in $candidates \"$(command -v resetprop 2>/dev/null || true)\"; do [ -n \"\$p\" ] && [ -x \"\$p\" ] && echo \"\$p\" && break; done",
            logCommand = onLog != null,
            onLog = onLog,
        )
        return res.output.lineSequence().firstOrNull()?.trim().orEmpty()
    }

    suspend fun applyOfficialReleaseSpoof(
        enable: Boolean = true,
        onLog: (suspend (String) -> Unit)? = null,
    ): Boolean {
        val rp = findResetprop()
        if (rp.isBlank()) {
            onLog?.invoke("[WARN] resetprop binary not found; cannot spoof ro.build.official.release.")
            return false
        }
        if (enable) {
            runSuMm(
                "mkdir -p $DIR_FANCAM; cur=\"$(getprop ro.build.official.release 2>/dev/null || true)\"; if [ ! -f $FILE_ORIG_OFFICIAL ] && [ -n \"\$cur\" ] && [ \"\$cur\" != \"false\" ]; then printf '%s' \"\$cur\" > $FILE_ORIG_OFFICIAL; fi",
                logCommand = false,
            )
            return runSuMm("${shellQuote(rp)} ro.build.official.release false", onLog = onLog).isSuccess
        } else {
            val origRes = runSuMm(
                "[ -f $FILE_ORIG_OFFICIAL ] && cat $FILE_ORIG_OFFICIAL || echo true",
                logCommand = false,
            )
            val restoreVal = origRes.output.trim().ifBlank { "true" }
            return runSuMm("${shellQuote(rp)} ro.build.official.release ${shellQuote(restoreVal)}", onLog = onLog).isSuccess
        }
    }

    suspend fun resolveFloatingFeatureTarget(): String {
        val candidates = ffTargetCandidates.joinToString(" ")
        val res = runSuMm(
            "for p in $candidates; do [ -f \"\$p\" ] && echo \"\$p\" && break; done",
            logCommand = false,
        )
        return res.output.lineSequence().firstOrNull()?.trim()?.ifBlank { FF_TARGET } ?: FF_TARGET
    }

    suspend fun resolveCameraFeatureTarget(): String? {
        val candidates = camFeatureCandidates.joinToString(" ")
        val res = runSuMm(
            "for p in $candidates; do [ -f \"\$p\" ] && echo \"\$p\" && break; done || ls /system/cameradata/camera-feature*.xml 2>/dev/null | head -n 1",
            logCommand = false,
        )
        return res.output.lineSequence().firstOrNull()?.trim()?.ifBlank { null }
    }

    suspend fun resolveAicoreSysconfigTarget(): String? {
        val candidates = aicoreSysconfigCandidates.joinToString(" ")
        val res = runSuMm(
            "for p in $candidates; do [ -f \"\$p\" ] && echo \"\$p\" && break; done || ls /product/etc/sysconfig/*.xml /system/etc/sysconfig/*.xml 2>/dev/null | head -n 1",
            logCommand = false,
        )
        return res.output.lineSequence().firstOrNull()?.trim()?.ifBlank { null }
    }

    private suspend fun resolveSignaturePermissionsTarget(): String? {
        val candidates = sigTargetCandidates.joinToString(" ")
        val res = runSuMm(
            "for p in $candidates; do [ -f \"\$p\" ] && echo \"\$p\" && break; done",
            logCommand = false,
        )
        return res.output.lineSequence().firstOrNull()?.trim()?.ifBlank { null }
    }

    suspend fun queryDeviceProfile(
        ffPath: String = FF_TARGET,
        rootMode: RootMode = detectRootMode(),
    ): DeviceProfile {
        val probeCmd = listOf(
            "echo \"MODEL=$(getprop ro.product.model 2>/dev/null || true)\"",
            "mkt=$(getprop ro.product.vendor.marketname 2>/dev/null || true); [ -z \"\$mkt\" ] && mkt=$(getprop ro.product.odm.marketname 2>/dev/null || true); echo \"MARKET=\$mkt\"",
            "soc=$(getprop ro.soc.model 2>/dev/null || true); [ -z \"\$soc\" ] && soc=$(getprop ro.hardware.chipname 2>/dev/null || true); [ -z \"\$soc\" ] && soc=$(getprop ro.board.platform 2>/dev/null || true); echo \"SOC=\$soc\"",
            "echo \"HW=$(getprop ro.hardware 2>/dev/null || true)\"",
            "echo \"ONEUI=$(getprop ro.build.version.oneui 2>/dev/null || echo 0)\"",
            "echo \"MEMKB=$(awk '/MemTotal:/ {print \$2; exit}' /proc/meminfo 2>/dev/null || echo 0)\"",
        ).joinToString("; ")
        val res = runSuMm(probeCmd, logCommand = false)
        val map = mutableMapOf<String, String>()
        res.output.lineSequence().forEach { line ->
            val idx = line.indexOf('=')
            if (idx > 0) {
                map[line.substring(0, idx).trim()] = line.substring(idx + 1).trim()
            }
        }
        val model = map["MODEL"].orEmpty()
        val marketName = map["MARKET"].orEmpty()
        val soc = map["SOC"].orEmpty()
        val hw = map["HW"].orEmpty()
        val oneUiRaw = map["ONEUI"]?.toIntOrNull() ?: 0
        val memKb = map["MEMKB"]?.toLongOrNull() ?: 0L
        val ramGb = if (memKb > 0L) ((memKb + 524288L) / 1048576L).toInt().coerceAtLeast(1) else 0
        return DeviceProfile(
            model = model,
            marketName = marketName,
            soc = soc,
            socVendor = classifySocVendor(soc, hw),
            oneUiRaw = oneUiRaw,
            oneUiLabel = formatOneUiVersion(oneUiRaw),
            ramGb = ramGb,
            rootMode = rootMode.label,
            ffPath = ffPath,
        )
    }

    suspend fun checkAiVersion(
        ffTarget: String = FF_TARGET,
        onLog: (suspend (String) -> Unit)? = null,
    ): String {
        val res = runSuMm("grep AI_VERSION $ffTarget", logCommand = onLog != null, onLog = onLog)
        return extractTagValue(res.output, "SEC_FLOATING_FEATURE_COMMON_CONFIG_AI_VERSION")
    }

    suspend fun queryMediaPackStatus(
        ffPath: String = FF_TARGET,
        onLog: (suspend (String) -> Unit)? = null,
    ): MediaPackStatus {
        val sbsChecks = sbsModelCandidates.joinToString(" -o ") { "-f $it" }
        val camTarget = resolveCameraFeatureTarget().orEmpty()
        val camMountCheck = if (camTarget.isNotBlank()) {
            "(awk '\$5 == \"$camTarget\" {print 1; exit}' /proc/1/mountinfo 2>/dev/null || awk '\$5 == \"$camTarget\" {print 1; exit}' /proc/self/mountinfo || echo 0)"
        } else {
            "echo 0"
        }
        val probeCmd = listOf(
            "echo \"MODELS=$([ $sbsChecks ] && echo 1 || echo 0)\"",
            "echo \"SHIMS=$([ -f $DIR_FANCAM/libveframework.videoeditor.samsung.so ] && [ -f $DIR_MCA/libsnap_tfl.so ] && echo 1 || echo 0)\"",
            "echo \"BIND=$(awk '\$5 == \"$BIND_VEFRAMEWORK\" {print 1; exit}' /proc/1/mountinfo 2>/dev/null || echo 0)\"",
            "echo \"STYLE=$([ -f $STYLE_TRANSFER_TFLITE ] && echo 1 || echo 0)\"",
            "echo \"AOD_FS=$(grep -q '<SEC_FLOATING_FEATURE_LCD_CONFIG_AOD_FULLSCREEN>1<' $ffPath 2>/dev/null && echo 1 || echo 0)\"",
            "echo \"CS_VER=$(dumpsys package $PKG_CREATIVE_STUDIO 2>/dev/null | sed -n 's/.*versionName=//p' | head -n 1 | tr -d '\\r')\"",
            "echo \"QS_VER=$(dumpsys package $PKG_QUICK_SHARE 2>/dev/null | grep -m1 versionName | sed 's/.*versionName=//' | tr -d '\\r ')\"",
            "echo \"QSC_VER=$(dumpsys package $PKG_QUICK_SHARE_CONN 2>/dev/null | grep -m1 versionName | sed 's/.*versionName=//' | tr -d '\\r ')\"",
            "echo \"LC_VER=$(dumpsys package $PKG_LANGUAGE_CORE 2>/dev/null | grep -m1 versionName | sed 's/.*versionName=//' | tr -d '\\r ')\"",
            "echo \"CAM_VER=$(dumpsys package $PKG_CAMERA 2>/dev/null | grep -m1 versionName | sed 's/.*versionName=//' | tr -d '\\r ')\"",
            "echo \"HL_MOUNT=$($camMountCheck)\"",
            "echo \"AIC_VER=$(dumpsys package $PKG_GOOGLE_AICORE 2>/dev/null | grep -m1 versionName | sed 's/.*versionName=//' | tr -d '\\r ')\"",
            "echo \"PCS_VER=$(dumpsys package $PKG_GOOGLE_PCS 2>/dev/null | grep -m1 versionName | sed 's/.*versionName=//' | tr -d '\\r ')\"",
            "echo \"AIC_FEAT=$(pm list features 2>/dev/null | grep -q 'com.google.android.feature.AICORE' && echo 1 || echo 0)\"",
            "echo \"OFFICIAL=$(getprop ro.build.official.release 2>/dev/null || echo —)\"",
        ).joinToString("; ")

        val probe = runSuMm(probeCmd, logCommand = onLog != null, onLog = onLog)
        val rp = findResetprop()

        val map = mutableMapOf<String, String>()
        probe.output.lineSequence().forEach { line ->
            val idx = line.indexOf('=')
            if (idx > 0) {
                map[line.substring(0, idx).trim()] = line.substring(idx + 1).trim()
            }
        }

        return MediaPackStatus(
            audioModelsReady = map["MODELS"] == "1",
            fancamShimsReady = map["SHIMS"] == "1",
            fancamBindActive = map["BIND"] == "1",
            styleTransferModelPresent = map["STYLE"] == "1",
            aodFullscreenActive = map["AOD_FS"] == "1",
            creativeStudioVersion = map["CS_VER"].orEmpty().trim(),
            quickShareVersion = map["QS_VER"].orEmpty().trim(),
            quickShareConnectivityVersion = map["QSC_VER"].orEmpty().trim(),
            languageCoreVersion = map["LC_VER"].orEmpty().trim(),
            cameraVersion = map["CAM_VER"].orEmpty().trim(),
            cameraFeaturePath = camTarget,
            horizontalLockMountActive = map["HL_MOUNT"] == "1",
            aicoreVersion = map["AIC_VER"].orEmpty().trim(),
            aicoreFeatureDeclared = map["AIC_FEAT"] == "1",
            pcsVersion = map["PCS_VER"].orEmpty().trim(),
            resetpropPath = rp,
            officialRelease = map["OFFICIAL"].orEmpty().ifBlank { "—" },
        )
    }

    suspend fun queryStatus(onLog: (suspend (String) -> Unit)? = null): SystemStatus {
        val suBin = findSuBinary()
        val idRes = runSuMm("id -u", logCommand = onLog != null, onLog = onLog)
        val rootGranted = idRes.isSuccess && idRes.output.trim() == "0"
        if (!rootGranted) {
            return SystemStatus(rootGranted = false, suPath = suBin)
        }

        val rootMode = detectRootMode(suBin)
        val ffPath = resolveFloatingFeatureTarget()
        val device = queryDeviceProfile(ffPath = ffPath, rootMode = rootMode)

        val mountCheck = runSuMm(
            "awk '\$5 == \"$ffPath\" {print \$5}' /proc/1/mountinfo 2>/dev/null || awk '\$5 == \"$ffPath\" {print \$5}' /proc/self/mountinfo",
            logCommand = onLog != null,
            onLog = onLog,
        )
        val mountActive = mountCheck.isSuccess && mountCheck.output.lineSequence().any { it.trim() == ffPath }

        val aiVer = checkAiVersion(ffTarget = ffPath, onLog = onLog)
        val nudgeRes = runSuMm("grep NOW_NUDGE_VERSION $ffPath", logCommand = onLog != null, onLog = onLog)
        val nudgeVer = extractTagValue(nudgeRes.output, "SEC_FLOATING_FEATURE_FRAMEWORK_CONFIG_NOW_NUDGE_VERSION")
        val mediaPack = queryMediaPackStatus(ffPath = ffPath, onLog = onLog)

        return SystemStatus(
            rootGranted = true,
            mountActive = mountActive,
            aiVersion = aiVer,
            nudgeVersion = nudgeVer,
            suPath = suBin,
            mediaPack = mediaPack,
            device = device,
        )
    }

    private suspend fun patchAndBindFloatingFeature(
        tagsToApply: Map<String, String>,
        device: DeviceProfile? = null,
        onLog: suspend (String) -> Unit,
    ): Boolean {
        val ffTarget = device?.ffPath?.ifBlank { resolveFloatingFeatureTarget() }
            ?: resolveFloatingFeatureTarget()
        val resolvedDevice = device ?: queryDeviceProfile(ffPath = ffTarget)

        onLog("[1/5] Checking existing mount state for ${resolvedDevice.displayName} ($ffTarget)...")
        val preMount = runSuMm(
            "awk '\$5 == \"$ffTarget\" {print}' /proc/1/mountinfo 2>/dev/null || awk '\$5 == \"$ffTarget\" {print}' /proc/self/mountinfo",
            onLog = onLog,
        )
        if (preMount.output.isNotBlank()) {
            onLog("[*] Unmounting existing $ffTarget overlay...")
            val umountRes = runSuMm(
                "umount $ffTarget || umount -l $ffTarget",
                onLog = onLog,
            )
            if (!umountRes.isSuccess) {
                onLog("[ERROR] Could not unmount existing overlay.")
                return false
            }
        }

        onLog("[2/5] Reading stock $ffTarget...")
        val stockRes = runSuMm("cat $ffTarget", logCommand = false, onLog = onLog)
        onLog("$ cat $ffTarget -> ${stockRes.output.length} bytes (exit ${stockRes.exitCode})")
        if (!stockRes.isSuccess || stockRes.output.isBlank()) {
            onLog("[ERROR] Failed to read stock $ffTarget.")
            return false
        }

        onLog("[3/5] Patching ${tagsToApply.size} tags (${resolvedDevice.socVendor.ifBlank { "Galaxy" }}, ${resolvedDevice.oneUiLabel.ifBlank { "One UI" }})...")
        val patchedXml = try {
            patchFloatingFeatureXml(stockRes.output, tagsToApply, resolvedDevice)
        } catch (e: Exception) {
            onLog("[ERROR] DOM XML patch failed: ${e.message}")
            return false
        }

        onLog("[4/5] Writing $FF_PATCHED and bind-mounting...")
        val writeRes = runSuMm(
            "cat > $FF_PATCHED && chown 0:0 $FF_PATCHED && chmod 644 $FF_PATCHED && (chcon --reference=$ffTarget $FF_PATCHED 2>/dev/null || chcon u:object_r:system_file:s0 $FF_PATCHED 2>/dev/null || true) && mount --bind $FF_PATCHED $ffTarget",
            stdinBytes = patchedXml.toByteArray(StandardCharsets.UTF_8),
            onLog = onLog,
        )
        if (!writeRes.isSuccess) {
            onLog("[ERROR] Failed writing/bind-mounting $FF_PATCHED.")
            return false
        }

        onLog("[5/5] Verifying mount...")
        val mode = detectRootMode()
        val mountInfoFile = if (mode == RootMode.SU_PLAIN) "/proc/self/mountinfo" else "/proc/1/mountinfo"
        val verifyRes = runSuMm(
            "awk '\$5 == \"$ffTarget\" {print}' $mountInfoFile 2>/dev/null || awk '\$5 == \"$ffTarget\" {print}' /proc/self/mountinfo",
            onLog = onLog,
        )
        if (!verifyRes.isSuccess || verifyRes.output.isBlank()) {
            onLog("[ERROR] Verification failed: $ffTarget not visible in mountinfo.")
            return false
        }
        val liveAiVer = checkAiVersion(ffTarget = ffTarget, onLog = onLog)
        onLog("[OK] Live AI_VERSION=$liveAiVer")
        return true
    }

    private suspend fun ensureBiometricSignatureAllowlist(onLog: suspend (String) -> Unit) {
        onLog("[*] Checking biometric signature allowlist...")
        val bioCheck = runSuMm(
            "dumpsys package $PKG_SMART_SUGGESTIONS 2>/dev/null | grep '$PERM_BIOMETRIC_ADV' | head -n 2",
            onLog = onLog,
        )
        if (bioCheck.output.contains(PERM_BIOMETRIC_ADV) && !bioCheck.output.contains("granted=true")) {
            val sigTarget = resolveSignaturePermissionsTarget()
            if (sigTarget != null) {
                runSuMm("umount $sigTarget || umount -l $sigTarget 2>/dev/null || true", onLog = onLog)
                val sigRaw = runSuMm("cat $sigTarget", logCommand = false)
                if (sigRaw.isSuccess && sigRaw.output.isNotBlank()) {
                    val patchedSig = runCatching { patchSignaturePermissionsXml(sigRaw.output) }.getOrNull()
                    if (patchedSig != null) {
                        runSuMm(
                            "cat > $SIG_PATCHED && chown 0:0 $SIG_PATCHED && chmod 644 $SIG_PATCHED && (chcon --reference=$sigTarget $SIG_PATCHED 2>/dev/null || chcon u:object_r:system_file:s0 $SIG_PATCHED 2>/dev/null || true) && mount --bind $SIG_PATCHED $sigTarget",
                            stdinBytes = patchedSig.toByteArray(StandardCharsets.UTF_8),
                            onLog = onLog,
                        )
                    }
                }
            }
        }
    }

    private suspend fun patchAndBindCameraFeature(
        localFlags: Map<String, String>,
        onLog: suspend (String) -> Unit,
    ): Boolean {
        val camTarget = resolveCameraFeatureTarget()
        if (camTarget.isNullOrBlank()) {
            onLog("[WARN] camera-feature.xml not found; skipping Camera XML patch.")
            return false
        }
        onLog("[*] Patching ${localFlags.size} flags in $camTarget...")
        runSuMm("umount $camTarget 2>/dev/null || umount -l $camTarget 2>/dev/null || true", logCommand = false)
        val stockRes = runSuMm("cat $camTarget", logCommand = false)
        if (!stockRes.isSuccess || stockRes.output.isBlank()) {
            onLog("[ERROR] Failed to read $camTarget.")
            return false
        }
        val patchedXml = try {
            patchCameraFeatureXml(stockRes.output, localFlags)
        } catch (e: Exception) {
            onLog("[ERROR] Camera XML patch failed: ${e.message}")
            return false
        }
        val writeRes = runSuMm(
            "cat > $CAM_FEATURE_PATCHED && chown 0:0 $CAM_FEATURE_PATCHED && chmod 644 $CAM_FEATURE_PATCHED && (chcon --reference=$camTarget $CAM_FEATURE_PATCHED 2>/dev/null || chcon u:object_r:system_file:s0 $CAM_FEATURE_PATCHED 2>/dev/null || true) && mount --bind $CAM_FEATURE_PATCHED $camTarget",
            stdinBytes = patchedXml.toByteArray(StandardCharsets.UTF_8),
            onLog = onLog,
        )
        if (!writeRes.isSuccess) {
            onLog("[ERROR] Failed bind-mounting $CAM_FEATURE_PATCHED onto $camTarget.")
            return false
        }
        runSuMm(
            "rm -rf /data/data/$PKG_CAMERA/cache/* 2>/dev/null || true; am force-stop $PKG_CAMERA 2>/dev/null || true",
            onLog = onLog,
        )
        onLog("[OK] Camera feature overlay active on $camTarget.")
        return true
    }

    private suspend fun unmountCameraFeatureOverlay(onLog: (suspend (String) -> Unit)? = null) {
        val camTarget = resolveCameraFeatureTarget()
        val targets = buildList {
            if (!camTarget.isNullOrBlank()) add(camTarget)
            addAll(camFeatureCandidates)
        }.distinct()
        val cmd = buildString {
            for (t in targets) {
                append("umount $t 2>/dev/null || umount -l $t 2>/dev/null || true; ")
            }
            append("rm -f $CAM_FEATURE_PATCHED")
        }
        runSuMm(cmd, logCommand = onLog != null, onLog = onLog)
    }

    private suspend fun patchAndBindAicoreSysconfig(onLog: suspend (String) -> Unit): Boolean {
        val sysTarget = resolveAicoreSysconfigTarget()
        if (sysTarget.isNullOrBlank()) {
            onLog("[WARN] No sysconfig XML found; skipping com.google.android.feature.AICORE injection.")
            return false
        }
        onLog("[*] Patching com.google.android.feature.AICORE in $sysTarget...")
        runSuMm("umount $sysTarget 2>/dev/null || umount -l $sysTarget 2>/dev/null || true", logCommand = false)
        val stockRes = runSuMm("cat $sysTarget", logCommand = false)
        if (!stockRes.isSuccess || stockRes.output.isBlank()) {
            onLog("[ERROR] Failed reading $sysTarget.")
            return false
        }
        val patchedXml = try {
            patchSysconfigAicoreXml(stockRes.output)
        } catch (e: Exception) {
            onLog("[ERROR] Sysconfig XML patch failed: ${e.message}")
            return false
        }
        val writeRes = runSuMm(
            "cat > $SYSCONFIG_AICORE_PATCHED && chown 0:0 $SYSCONFIG_AICORE_PATCHED && chmod 644 $SYSCONFIG_AICORE_PATCHED && (chcon --reference=$sysTarget $SYSCONFIG_AICORE_PATCHED 2>/dev/null || chcon u:object_r:system_file:s0 $SYSCONFIG_AICORE_PATCHED 2>/dev/null || true) && mount --bind $SYSCONFIG_AICORE_PATCHED $sysTarget",
            stdinBytes = patchedXml.toByteArray(StandardCharsets.UTF_8),
            onLog = onLog,
        )
        if (!writeRes.isSuccess) {
            onLog("[ERROR] Failed bind-mounting $SYSCONFIG_AICORE_PATCHED onto $sysTarget.")
            return false
        }
        onLog("[OK] Sysconfig AICore feature mounted on $sysTarget.")
        return true
    }

    private suspend fun unmountAicoreSysconfigOverlay(onLog: (suspend (String) -> Unit)? = null) {
        val sysTarget = resolveAicoreSysconfigTarget()
        val targets = buildList {
            if (!sysTarget.isNullOrBlank()) add(sysTarget)
            addAll(aicoreSysconfigCandidates)
        }.distinct()
        val cmd = buildString {
            for (t in targets) {
                append("umount $t 2>/dev/null || umount -l $t 2>/dev/null || true; ")
            }
            append("rm -f $SYSCONFIG_AICORE_PATCHED")
        }
        runSuMm(cmd, logCommand = onLog != null, onLog = onLog)
    }

    private suspend fun applyAicoreRuntimeFlags(onLog: suspend (String) -> Unit) {
        runSuMm(
            "pm enable --user 0 $PKG_GOOGLE_AICORE 2>/dev/null || true; " +
                "pm enable --user 0 $PKG_SAMSUNG_AICORE 2>/dev/null || true; " +
                "pm enable --user 0 $PKG_GOOGLE_PCS 2>/dev/null || true; " +
                "settings --user 0 put global device_config_sync_disabled 0 2>/dev/null || true; " +
                "settings --user 0 put global aicore_enabled 1 2>/dev/null || true; " +
                "settings --user 0 put secure aicore_enabled 1 2>/dev/null || true; " +
                "cmd device_config put aicore aicore_enabled true 2>/dev/null || true; " +
                "cmd device_config put aicore enable_aicore_service true 2>/dev/null || true; " +
                "cmd device_config put aicore enable_gemini_nano true 2>/dev/null || true; " +
                "cmd device_config put aicore enable_on_device_intelligence true 2>/dev/null || true; " +
                "cmd device_config put aicore protected_download_v2_safe_base_model_switch_enabled true 2>/dev/null || true",
            onLog = onLog,
        )
    }

    suspend fun applyModelSpoof(
        spoofModel: String?,
        onLog: (suspend (String) -> Unit)? = null,
    ): Boolean {
        val rp = findResetprop()
        if (rp.isBlank()) {
            onLog?.invoke("[WARN] resetprop binary not found; cannot spoof ro.product.model.")
            return false
        }
        val quotedRp = shellQuote(rp)
        if (spoofModel != null) {
            runSuMm(
                "mkdir -p $DIR_FANCAM; cur=\"$(getprop ro.product.model 2>/dev/null || true)\"; if [ ! -f $FILE_ORIG_MODEL ] && [ -n \"\$cur\" ] && [ \"\$cur\" != ${shellQuote(spoofModel)} ]; then printf '%s' \"\$cur\" > $FILE_ORIG_MODEL; fi",
                logCommand = false,
            )
            val qModel = shellQuote(spoofModel)
            return runSuMm(
                "$quotedRp ro.product.model $qModel && $quotedRp ro.product.system.model $qModel && $quotedRp ro.product.product.model $qModel",
                onLog = onLog,
            ).isSuccess
        } else {
            val origRes = runSuMm("[ -f $FILE_ORIG_MODEL ] && cat $FILE_ORIG_MODEL || true", logCommand = false)
            val origModel = origRes.output.trim()
            if (origModel.isBlank()) return true
            val qOrig = shellQuote(origModel)
            val ok = runSuMm(
                "$quotedRp ro.product.model $qOrig && $quotedRp ro.product.system.model $qOrig && $quotedRp ro.product.product.model $qOrig",
                onLog = onLog,
            ).isSuccess
            runSuMm("rm -f $FILE_ORIG_MODEL", logCommand = false)
            return ok
        }
    }

    private suspend fun bindStagedFancamShimsIfPresent(onLog: suspend (String) -> Unit) {
        val bindPairs = listOf(
            "$DIR_FANCAM/libveframework.videoeditor.samsung.so" to BIND_VEFRAMEWORK,
            "$DIR_FANCAM/libcontextanalyzer_jni.media.samsung.so" to BIND_CONTEXTANALYZER,
        )
        for ((src, dst) in bindPairs) {
            val bindCmd = buildString {
                append("if [ -f $src ] && [ -e $dst ]; then ")
                append("umount -l $dst 2>/dev/null || true; ")
                append("(chcon --reference=$dst $src 2>/dev/null || chcon u:object_r:system_lib_file:s0 $src 2>/dev/null || true); ")
                append("mount --bind $src $dst; ")
                append("fi")
            }
            runSuMm(bindCmd, onLog = onLog)
        }
    }

    suspend fun applyFeatures(
        selectedTags: Map<String, String>,
        enableOfficialSpoof: Boolean = true,
        onLog: suspend (String) -> Unit,
    ): Boolean {
        if (selectedTags.isEmpty()) {
            onLog("[ERROR] Select at least one feature group.")
            return false
        }

        val ffPath = resolveFloatingFeatureTarget()
        val device = queryDeviceProfile(ffPath = ffPath)

        if (!patchAndBindFloatingFeature(selectedTags, device, onLog)) {
            return false
        }

        if (selectedTags.containsKey("SEC_FLOATING_FEATURE_GALLERY_SUPPORT_AI_FANCAM")) {
            bindStagedFancamShimsIfPresent(onLog)
        }

        if (enableOfficialSpoof) {
            onLog("[*] Applying ro.build.official.release = false...")
            applyOfficialReleaseSpoof(enable = true, onLog = onLog)
        }

        ensureBiometricSignatureAllowlist(onLog)

        onLog("[*] Enabling Smart Suggestions components & settings...")
        for (comp in COMPONENTS_TO_ENABLE) {
            runSuMm("pm enable --user 0 $comp 2>/dev/null || true", onLog = onLog)
        }
        for ((ns, key, value) in SETTINGS_TO_PUT) {
            runSuMm("settings --user 0 put $ns $key $value 2>/dev/null || true", onLog = onLog)
        }
        if (selectedTags["SEC_FLOATING_FEATURE_LCD_CONFIG_AOD_FULLSCREEN"] == "1") {
            onLog("[*] Enabling AOD lockscreen wallpaper settings...")
            runSuMm(
                "settings --user 0 put system aod_show_lockscreen_wallpaper 1 2>/dev/null || true; settings --user 0 put system aod_mode 1 2>/dev/null || true",
                onLog = onLog,
            )
        }
        if (selectedTags["SEC_FLOATING_FEATURE_GENAI_SUPPORT_CREATIVE_STUDIO"] == "TRUE") {
            runSuMm("pm enable --user 0 $PKG_CREATIVE_STUDIO 2>/dev/null || true", onLog = onLog)
        }
        if (selectedTags["SEC_FLOATING_FEATURE_QUICKSHARE_SUPPORT_APPLE_SHARE"] == "TRUE") {
            onLog("[*] Enabling Share with Apple Devices settings for Quick Share...")
            enableQuickShareAppleSettings(onLog)
        }
        if (selectedTags["SEC_FLOATING_FEATURE_CALL_SUPPORT_CALL_SCREENING"] == "TRUE") {
            onLog("[*] Enabling Scam Detection & Call Screening settings...")
            runSuMm(
                "settings --user 0 put global call_screening_enabled 1 2>/dev/null || true; " +
                    "settings --user 0 put global scam_detection_enabled 1 2>/dev/null || true; " +
                    "settings --user 0 put system voice_phishing_detection_enabled 1 2>/dev/null || true; " +
                    "pm enable --user 0 $PKG_DIALER 2>/dev/null || true; " +
                    "pm enable --user 0 $PKG_INCALLUI 2>/dev/null || true; " +
                    "am force-stop $PKG_DIALER 2>/dev/null || true; " +
                    "am force-stop $PKG_INCALLUI 2>/dev/null || true",
                onLog = onLog,
            )
        }
        if (selectedTags["SEC_FLOATING_FEATURE_LOCKSCREEN_SUPPORT_PHOTO_AMBIENT_WALLPAPER"] == "TRUE") {
            onLog("[*] Enabling Photo Ambient Wallpaper...")
            runSuMm(
                "settings --user 0 put system photo_ambient_wallpaper_enabled 1 2>/dev/null || true; " +
                    "pm enable --user 0 $PKG_WALLPAPER_STYLE 2>/dev/null || true; " +
                    "am force-stop $PKG_WALLPAPER_STYLE 2>/dev/null || true; " +
                    "am force-stop $PKG_WEATHER_DAEMON 2>/dev/null || true",
                onLog = onLog,
            )
        }
        if (selectedTags["SEC_FLOATING_FEATURE_LCD_CONFIG_VIVIDNESS"] != null ||
            selectedTags["SEC_FLOATING_FEATURE_LCD_SUPPORT_EAD"] == "TRUE"
        ) {
            onLog("[*] Refreshing Display Settings for Vividness & Adaptive Color Tone...")
            runSuMm(
                "am force-stop com.android.settings 2>/dev/null || true",
                onLog = onLog,
            )
        }
        if (selectedTags["SEC_FLOATING_FEATURE_SYSTEMUI_SUPPORT_NOTIFICATION_HIGHLIGHT"] == "TRUE") {
            onLog("[*] Enabling Notification Highlights settings...")
            enableNotificationHighlightsSettings(onLog)
        }
        if (selectedTags["SEC_FLOATING_FEATURE_SMARTCAPTURE_SUPPORT_AI_SELECT"] == "TRUE") {
            onLog("[*] Enabling AI Select components...")
            runSuMm(
                "pm enable --user 0 $PKG_SMART_CAPTURE 2>/dev/null || true; settings --user 0 put system ai_select_enabled 1 2>/dev/null || true",
                onLog = onLog,
            )
        }
        if (selectedTags["SEC_FLOATING_FEATURE_SETTINGS_SUPPORT_AI_SEARCH"] == "TRUE") {
            onLog("[*] Enabling Semantic AI Search services...")
            runSuMm(
                "pm enable --user 0 $PKG_GALAXY_FINDER 2>/dev/null || true; pm enable --user 0 $PKG_SCS 2>/dev/null || true",
                onLog = onLog,
            )
        }
        val camFlags = linkedMapOf<String, String>()
        if (selectedTags["SEC_FLOATING_FEATURE_CAMERA_SUPPORT_HORIZONTAL_LOCK"] == "TRUE") {
            camFlags["SUPPORT_SUPER_VIDEO_STABILIZATION"] = "true"
            camFlags["SUPPORT_SUPER_VIDEO_STABILIZATION_HORIZONTAL_LOCK"] = "true"
            camFlags["SUPPORT_VIDEO_HORIZONTAL_LOCK"] = "true"
            camFlags["SUPPORT_HORIZONTAL_LOCK"] = "true"
        }
        if (selectedTags["SEC_FLOATING_FEATURE_CAMERA_SUPPORT_LOG_VIDEO"] == "TRUE") {
            camFlags["SUPPORT_LOG_VIDEO"] = "true"
            camFlags["SUPPORT_SUPER_HIGH_BITRATE_VIDEO"] = "true"
        }
        if (camFlags.isNotEmpty()) {
            patchAndBindCameraFeature(camFlags, onLog)
        } else {
            unmountCameraFeatureOverlay()
        }
        if (selectedTags["SEC_FLOATING_FEATURE_GENAI_SUPPORT_AICORE"] == "TRUE") {
            onLog("[*] Applying AICore sysconfig & DeviceConfig flags...")
            patchAndBindAicoreSysconfig(onLog)
            applyAicoreRuntimeFlags(onLog)
        } else {
            unmountAicoreSysconfigOverlay()
        }
        restartGalleryAndEditor(onLog)

        onLog("[*] Verified mount. Soft-rebooting Zygote...")
        val rebootRes = runSuMm("sync; setprop ctl.restart zygote", onLog = onLog)
        return rebootRes.isSuccess
    }

    private suspend fun enableNotificationHighlightsSettings(onLog: suspend (String) -> Unit) {
        runSuMm(
            "pm enable --user 0 $PKG_LANGUAGE_CORE 2>/dev/null || true; " +
                "settings --user 0 put global noti_intelligence_enabled 1 2>/dev/null || true; " +
                "settings --user 0 put secure notification_highlight_enabled 1 2>/dev/null || true; " +
                "settings --user 0 put secure notification_summary_enabled 1 2>/dev/null || true; " +
                "settings --user 0 put secure notification_prioritize_enabled 1 2>/dev/null || true",
            onLog = onLog,
        )
    }

    suspend fun applyNotificationHighlights(
        selectedTags: Map<String, String>,
        enableOfficialSpoof: Boolean = true,
        onLog: suspend (String) -> Unit,
    ): Boolean {
        onLog("[*] Applying Notification Highlights (AI Summaries & Prioritization)...")
        val mergedTags = linkedMapOf<String, String>().apply {
            putAll(selectedTags)
            put("SEC_FLOATING_FEATURE_COMMON_CONFIG_AI_VERSION", "20263")
            putAll(FeatureGroup.NOTIFICATION_HIGHLIGHTS.tags)
        }
        val device = queryDeviceProfile(ffPath = resolveFloatingFeatureTarget())
        if (!patchAndBindFloatingFeature(mergedTags, device, onLog)) {
            return false
        }
        if (enableOfficialSpoof) {
            applyOfficialReleaseSpoof(enable = true, onLog = onLog)
        }
        enableNotificationHighlightsSettings(onLog)
        runSuMm(
            "am force-stop com.android.settings 2>/dev/null || true; killall com.android.systemui 2>/dev/null || true",
            onLog = onLog,
        )
        runSuMm(
            "am start --user 0 -a android.settings.NOTIFICATION_SETTINGS >/dev/null 2>&1 || am start --user 0 -a android.settings.SETTINGS >/dev/null 2>&1",
            onLog = onLog,
        )
        onLog("[OK] Notification Highlights flags applied. Check Settings > Notifications.")
        return true
    }

    suspend fun installLanguageCoreFromDownloads(onLog: suspend (String) -> Unit): Boolean {
        onLog("[*] Scanning /sdcard/Download, /data/media/0/Download & /data/local/tmp for Samsung Language Core APK...")
        val globs = listOf("*languagemodel*.apk", "*LanguageCore*.apk", "*Language*Core*.apk", "*language-core*.apk")
        val patterns = buildList {
            for (g in globs) {
                add("/sdcard/Download/$g")
                add("/data/media/0/Download/$g")
                add("/data/local/tmp/$g")
            }
        }.joinToString(" ")
        val findRes = runSuMm("ls -1t $patterns 2>/dev/null | head -n 1", logCommand = false)
        val foundPath = findRes.output.lineSequence().firstOrNull()?.trim().orEmpty()
        if (foundPath.isBlank()) {
            onLog("[ERROR] Samsung Language Core APK not found in /sdcard/Download or /data/local/tmp.")
            return false
        }
        val sizeRes = runSuMm("stat -c%s ${shellQuote(foundPath)} 2>/dev/null || echo 0", logCommand = false)
        val size = sizeRes.output.trim().toLongOrNull() ?: 0L
        if (size <= 0L) {
            onLog("[ERROR] Selected file is empty: $foundPath")
            return false
        }
        return installApkFromDevicePathWithSpoof(
            apkPath = foundPath,
            size = size,
            label = "Samsung Language Core",
            restoreOfficialPropAfter = true,
            onLog = onLog,
        )
    }

    suspend fun applyHorizontalLock(
        selectedTags: Map<String, String>,
        enableOfficialSpoof: Boolean = true,
        onLog: suspend (String) -> Unit,
    ): Boolean {
        onLog("[*] Applying Horizontal Lock (S26 360° Super Steady) without reboot...")
        val mergedTags = linkedMapOf<String, String>().apply {
            putAll(selectedTags)
            put("SEC_FLOATING_FEATURE_COMMON_CONFIG_AI_VERSION", "20263")
            putAll(FeatureGroup.HORIZONTAL_LOCK.tags)
        }
        val device = queryDeviceProfile(ffPath = resolveFloatingFeatureTarget())
        if (!patchAndBindFloatingFeature(mergedTags, device, onLog)) {
            return false
        }
        if (enableOfficialSpoof) {
            applyOfficialReleaseSpoof(enable = true, onLog = onLog)
        }
        val camFlags = linkedMapOf(
            "SUPPORT_SUPER_VIDEO_STABILIZATION" to "true",
            "SUPPORT_SUPER_VIDEO_STABILIZATION_HORIZONTAL_LOCK" to "true",
            "SUPPORT_VIDEO_HORIZONTAL_LOCK" to "true",
            "SUPPORT_HORIZONTAL_LOCK" to "true",
        )
        patchAndBindCameraFeature(camFlags, onLog)
        runSuMm(
            "am start --user 0 -a android.media.action.VIDEO_CAPTURE >/dev/null 2>&1 || monkey -p $PKG_CAMERA -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1",
            onLog = onLog,
        )
        onLog("[OK] Horizontal Lock overlay applied & Camera opened.")
        return true
    }

    suspend fun disableHorizontalLock(onLog: suspend (String) -> Unit): Boolean {
        onLog("[*] Removing Horizontal Lock camera-feature.xml overlay...")
        unmountCameraFeatureOverlay(onLog)
        runSuMm(
            "rm -rf /data/data/$PKG_CAMERA/cache/* 2>/dev/null || true; am force-stop $PKG_CAMERA 2>/dev/null || true",
            onLog = onLog,
        )
        onLog("[OK] Stock camera-feature.xml restored.")
        return true
    }

    suspend fun applyAicoreSetup(
        selectedTags: Map<String, String>,
        enableOfficialSpoof: Boolean,
        persistModelSpoof: Boolean,
        onLog: suspend (String) -> Unit,
    ): Boolean {
        onLog("[*] Applying AICore sysconfig, floating_feature & DeviceConfig flags...")
        val mergedTags = linkedMapOf<String, String>().apply {
            putAll(selectedTags)
            putAll(FeatureGroup.AICORE.tags)
        }
        val device = queryDeviceProfile(ffPath = resolveFloatingFeatureTarget())
        if (!patchAndBindFloatingFeature(mergedTags, device, onLog)) {
            return false
        }
        patchAndBindAicoreSysconfig(onLog)
        if (enableOfficialSpoof) {
            applyOfficialReleaseSpoof(enable = true, onLog = onLog)
        }
        if (persistModelSpoof) {
            onLog("[*] Spoofing ro.product.model -> SM-S948B...")
            applyModelSpoof("SM-S948B", onLog)
        } else {
            applyModelSpoof(null, onLog)
        }
        applyAicoreRuntimeFlags(onLog)
        val featLive = runSuMm(
            "pm list features 2>/dev/null | grep -q 'com.google.android.feature.AICORE' && echo 1 || echo 0",
            logCommand = false,
        ).output.trim() == "1"
        if (featLive) {
            onLog("[OK] com.google.android.feature.AICORE is live in PackageManager.")
        } else {
            onLog("[INFO] Sysconfig overlay mounted; Soft Reboot once so PackageManager registers com.google.android.feature.AICORE.")
        }
        return true
    }

    suspend fun installAicoreFromDownloads(
        context: Context,
        spoofModelDuringInstall: Boolean,
        keepModelSpoofAfter: Boolean,
        onLog: suspend (String) -> Unit,
    ): Boolean {
        onLog("[*] Scanning /sdcard/Download, /data/media/0/Download & /data/local/tmp for AICore APK/bundle...")
        val globs = listOf("*com.google.android.aicore*", "*AICore*", "*aicore*.apk*", "*aicore*.zip")
        val patterns = buildList {
            for (g in globs) {
                add("/sdcard/Download/$g")
                add("/data/media/0/Download/$g")
                add("/data/local/tmp/$g")
            }
        }.joinToString(" ")
        val findRes = runSuMm("ls -1t $patterns 2>/dev/null | head -n 1", logCommand = false)
        val foundPath = findRes.output.lineSequence().firstOrNull()?.trim().orEmpty()
        if (foundPath.isBlank()) {
            onLog("[ERROR] No AICore APK or .apkm bundle found in /sdcard/Download or /data/local/tmp.")
            return false
        }
        onLog("[*] Found AICore package: $foundPath")
        val stagedFile = File(context.cacheDir, "aicore_dl_${System.currentTimeMillis()}.bin")
        return try {
            withContext(Dispatchers.IO) { stagedFile.writeBytes(byteArrayOf()) }
            val myUid = Process.myUid()
            val copyRes = runSuMm(
                "cp -f ${shellQuote(foundPath)} ${shellQuote(stagedFile.absolutePath)} && chown $myUid:$myUid ${shellQuote(stagedFile.absolutePath)} && chmod 600 ${shellQuote(stagedFile.absolutePath)}",
                onLog = onLog,
            )
            if (!copyRes.isSuccess || !stagedFile.exists() || stagedFile.length() == 0L) {
                onLog("[ERROR] Failed staging $foundPath.")
                false
            } else {
                installAicoreFileWithSpoof(
                    pkgFile = stagedFile,
                    spoofModelDuringInstall = spoofModelDuringInstall,
                    keepModelSpoofAfter = keepModelSpoofAfter,
                    onLog = onLog,
                )
            }
        } finally {
            stagedFile.delete()
        }
    }

    suspend fun installAicorePackageWithSpoof(
        context: Context,
        uri: Uri,
        spoofModelDuringInstall: Boolean,
        keepModelSpoofAfter: Boolean,
        onLog: suspend (String) -> Unit,
    ): Boolean {
        val stagedFile = File(context.cacheDir, "aicore_pick_${System.currentTimeMillis()}.bin")
        return try {
            withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(stagedFile).use { output ->
                        input.copyTo(output)
                    }
                } ?: error("Cannot open selected AICore file.")
            }
            installAicoreFileWithSpoof(
                pkgFile = stagedFile,
                spoofModelDuringInstall = spoofModelDuringInstall,
                keepModelSpoofAfter = keepModelSpoofAfter,
                onLog = onLog,
            )
        } catch (e: Exception) {
            onLog("[ERROR] Failed staging AICore file: ${e.message}")
            false
        } finally {
            stagedFile.delete()
        }
    }

    private suspend fun installAicoreFileWithSpoof(
        pkgFile: File,
        spoofModelDuringInstall: Boolean,
        keepModelSpoofAfter: Boolean,
        onLog: suspend (String) -> Unit,
    ): Boolean {
        val size = pkgFile.length()
        if (size <= 0L) {
            onLog("[ERROR] Selected AICore file is empty.")
            return false
        }

        patchAndBindAicoreSysconfig(onLog)

        val extractDir = File(pkgFile.parentFile, "aicore_splits_${System.currentTimeMillis()}")
        var isSingleApk = false
        val splitFiles = mutableListOf<File>()

        try {
            withContext(Dispatchers.IO) {
                extractDir.mkdirs()
                val canonRoot = extractDir.canonicalPath + File.separator
                pkgFile.inputStream().use { raw ->
                    ZipInputStream(raw).use { zis ->
                        var entry = zis.nextEntry
                        var splitIdx = 0
                        while (entry != null) {
                            if (!entry.isDirectory) {
                                if (entry.name == "AndroidManifest.xml") {
                                    isSingleApk = true
                                    break
                                }
                                if (entry.name.endsWith(".apk", ignoreCase = true)) {
                                    val safeName = "split_${splitIdx++}.apk"
                                    val outFile = File(extractDir, safeName)
                                    if (!outFile.canonicalPath.startsWith(canonRoot)) {
                                        error("Blocked unsafe archive path: ${entry.name}")
                                    }
                                    FileOutputStream(outFile).use { out ->
                                        zis.copyTo(out)
                                    }
                                    splitFiles.add(outFile)
                                }
                            }
                            zis.closeEntry()
                            entry = zis.nextEntry
                        }
                    }
                }
            }

            if (!isSingleApk && splitFiles.isEmpty()) {
                onLog("[ERROR] File is neither a valid APK nor a split APK bundle (.apkm/.apks/.zip).")
                return false
            }

            val rp = findResetprop()
            val oldOfficial = if (rp.isNotBlank()) {
                runSuMm("getprop ro.build.official.release 2>/dev/null || true", logCommand = false).output.trim()
            } else {
                ""
            }
            if (rp.isNotBlank()) {
                runSuMm("${shellQuote(rp)} ro.build.official.release false", onLog = onLog)
            }
            if (spoofModelDuringInstall) {
                onLog("[*] Spoofing device model (SM-S948B) & Play Store installer (com.android.vending)...")
                applyModelSpoof("SM-S948B", onLog)
            }

            var ok = false
            var rawOut = ""
            try {
                if (isSingleApk) {
                    val res = runSuMm(
                        "cat ${shellQuote(pkgFile.absolutePath)} | pm install -r -d -g -i com.android.vending -S $size 2>&1",
                        onLog = onLog,
                    )
                    rawOut = res.output
                    ok = res.isSuccess && res.output.contains("Success")
                } else {
                    onLog("[*] Installing ${splitFiles.size} split APKs via Play Store session spoof...")
                    val createRes = runSuMm(
                        "pm install-create -r -d -g -i com.android.vending 2>&1",
                        onLog = onLog,
                    )
                    val sessionId = Regex("\\[(\\d+)]").find(createRes.output)?.groupValues?.get(1)
                    if (sessionId.isNullOrBlank()) {
                        rawOut = createRes.output
                        ok = false
                    } else {
                        var writeFailed = false
                        for ((idx, split) in splitFiles.withIndex()) {
                            val splitLen = split.length()
                            val writeRes = runSuMm(
                                "cat ${shellQuote(split.absolutePath)} | pm install-write -S $splitLen $sessionId split_$idx.apk 2>&1",
                                onLog = onLog,
                            )
                            if (!writeRes.isSuccess) {
                                rawOut = writeRes.output
                                writeFailed = true
                                break
                            }
                        }
                        if (writeFailed) {
                            runSuMm("pm install-abandon $sessionId 2>/dev/null || true", logCommand = false)
                            ok = false
                        } else {
                            val commitRes = runSuMm("pm install-commit $sessionId 2>&1", onLog = onLog)
                            rawOut = commitRes.output
                            ok = commitRes.isSuccess && commitRes.output.contains("Success")
                            if (!ok) {
                                runSuMm("pm install-abandon $sessionId 2>/dev/null || true", logCommand = false)
                            }
                        }
                    }
                }
            } finally {
                if (rp.isNotBlank() && oldOfficial.isNotBlank()) {
                    runSuMm("${shellQuote(rp)} ro.build.official.release ${shellQuote(oldOfficial)}", logCommand = false)
                }
                if (!keepModelSpoofAfter) {
                    applyModelSpoof(null, onLog)
                }
            }

            if (ok) {
                applyAicoreRuntimeFlags(onLog)
                onLog("[OK] Google AICore installed & runtime flags applied.")
            } else if (rawOut.contains("INSTALL_FAILED_MISSING_FEATURE")) {
                onLog("[WARN] INSTALL_FAILED_MISSING_FEATURE: tap 'Enable & Reboot' once so PackageManager loads com.google.android.feature.AICORE, then install again.")
            } else {
                onLog("[ERROR] AICore install failed. Ensure LSPosed/CorePatch is enabled if signature/downgrade error occurred.")
            }
            return ok
        } catch (e: Exception) {
            onLog("[ERROR] AICore bundle install error: ${e.message}")
            return false
        } finally {
            extractDir.deleteRecursively()
        }
    }

    private suspend fun enableQuickShareAppleSettings(onLog: suspend (String) -> Unit) {
        runSuMm(
            "settings --user 0 put global quick_share_apple_devices_enabled 1 2>/dev/null || true; " +
                "settings --user 0 put system quick_share_ios_support 1 2>/dev/null || true; " +
                "settings --user 0 put secure quick_share_cross_platform 1 2>/dev/null || true; " +
                "pm enable --user 0 $PKG_QUICK_SHARE 2>/dev/null || true; " +
                "pm enable --user 0 $PKG_QUICK_SHARE_CONN 2>/dev/null || true; " +
                "am force-stop $PKG_QUICK_SHARE 2>/dev/null || true; " +
                "am force-stop $PKG_QUICK_SHARE_CONN 2>/dev/null || true",
            onLog = onLog,
        )
    }

    suspend fun applyShareWithApple(
        selectedTags: Map<String, String>,
        enableOfficialSpoof: Boolean = true,
        onLog: suspend (String) -> Unit,
    ): Boolean {
        onLog("[*] Applying Share with Apple Devices (Quick Share <-> AirDrop)...")
        val mergedTags = linkedMapOf<String, String>().apply {
            putAll(selectedTags)
            put("SEC_FLOATING_FEATURE_COMMON_CONFIG_AI_VERSION", "20263")
            putAll(FeatureGroup.SHARE_WITH_APPLE.tags)
        }
        val device = queryDeviceProfile(ffPath = resolveFloatingFeatureTarget())
        if (!patchAndBindFloatingFeature(mergedTags, device, onLog)) {
            return false
        }
        if (enableOfficialSpoof) {
            applyOfficialReleaseSpoof(enable = true, onLog = onLog)
        }
        enableQuickShareAppleSettings(onLog)
        openQuickShareSettings(onLog)
        onLog("[OK] Share with Apple Devices flags applied. Check Quick Share > Settings.")
        return true
    }

    suspend fun openQuickShareSettings(onLog: suspend (String) -> Unit): Boolean {
        return runSuMm(
            "am start --user 0 -n $PKG_QUICK_SHARE/.settings.QuickShareSettingsActivity >/dev/null 2>&1 || " +
                "am start --user 0 -a $PKG_QUICK_SHARE.action.QUICK_SHARE_SETTINGS >/dev/null 2>&1 || " +
                "monkey -p $PKG_QUICK_SHARE -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1 || " +
                "am start --user 0 -a android.settings.SETTINGS >/dev/null 2>&1",
            onLog = onLog,
        ).isSuccess
    }

    suspend fun installQuickShareFromDownloads(onLog: suspend (String) -> Unit): Boolean {
        onLog("[*] Scanning /sdcard/Download, /data/media/0/Download & /data/local/tmp for Quick Share APKs...")
        applyOfficialReleaseSpoof(enable = true, onLog = onLog)

        val targets = listOf(
            "Samsung Quick Share" to listOf("*sharelive*.apk", "*QuickShare*.apk", "*Quick*Share*.apk", "*quick-share*.apk"),
            "Quick Share Connectivity" to listOf("*aware.service*.apk", "*QuickShareConnectivity*.apk", "*quick-share-connectivity*.apk"),
        )

        var anyInstalled = false
        for ((label, globs) in targets) {
            val patterns = buildList {
                for (g in globs) {
                    add("/sdcard/Download/$g")
                    add("/data/media/0/Download/$g")
                    add("/data/local/tmp/$g")
                }
            }.joinToString(" ")
            val findRes = runSuMm("ls -1t $patterns 2>/dev/null | head -n 1", logCommand = false)
            val foundPath = findRes.output.lineSequence().firstOrNull()?.trim().orEmpty()
            if (foundPath.isBlank()) {
                onLog("[INFO] $label APK not found in /sdcard/Download or /data/local/tmp; skipping.")
                continue
            }
            val sizeRes = runSuMm("stat -c%s ${shellQuote(foundPath)} 2>/dev/null || echo 0", logCommand = false)
            val size = sizeRes.output.trim().toLongOrNull() ?: 0L
            if (size <= 0L) {
                onLog("[WARN] Empty APK skipped: $foundPath")
                continue
            }
            val ok = installApkFromDevicePathWithSpoof(
                apkPath = foundPath,
                size = size,
                label = label,
                restoreOfficialPropAfter = true,
                onLog = onLog,
            )
            if (!ok) {
                return false
            }
            anyInstalled = true
        }

        if (!anyInstalled) {
            onLog("[ERROR] No Quick Share or Quick Share Connectivity APK found in /sdcard/Download or /data/local/tmp.")
            return false
        }

        enableQuickShareAppleSettings(onLog)
        onLog("[OK] Quick Share packages installed and restarted.")
        return true
    }

    suspend fun applyAodWallpaperSpoof(
        selectedTags: Map<String, String>,
        enableOfficialSpoof: Boolean = true,
        onLog: suspend (String) -> Unit,
    ): Boolean {
        onLog("[*] Applying AOD with Wallpaper Spoof (AOD_FULLSCREEN=1)...")
        val mergedTags = linkedMapOf<String, String>().apply {
            putAll(selectedTags)
            put("SEC_FLOATING_FEATURE_COMMON_CONFIG_AI_VERSION", "20263")
            putAll(FeatureGroup.AOD_WALLPAPER.tags)
        }
        val device = queryDeviceProfile(ffPath = resolveFloatingFeatureTarget())
        if (!patchAndBindFloatingFeature(mergedTags, device, onLog)) {
            return false
        }
        if (enableOfficialSpoof) {
            applyOfficialReleaseSpoof(enable = true, onLog = onLog)
        }
        runSuMm(
            "settings --user 0 put system aod_show_lockscreen_wallpaper 1 && settings --user 0 put system aod_mode 1",
            onLog = onLog,
        )
        runSuMm(
            "am force-stop $PKG_AOD_SERVICE; killall com.android.systemui 2>/dev/null || true",
            onLog = onLog,
        )
        onLog("[OK] AOD Wallpaper spoof applied. Check Settings > Lock screen and AOD.")
        return true
    }

    suspend fun openAodSettings(onLog: suspend (String) -> Unit): Boolean {
        return runSuMm(
            "am start --user 0 -n $PKG_AOD_SERVICE/.settings.AODSettingsMainActivity || am start --user 0 -a android.settings.LOCK_SCREEN_SETTINGS",
            onLog = onLog,
        ).isSuccess
    }

    suspend fun launchCreativeStudio(onLog: suspend (String) -> Unit): Boolean {
        return runSuMm(
            "pm enable --user 0 $PKG_CREATIVE_STUDIO 2>/dev/null || true; monkey -p $PKG_CREATIVE_STUDIO -c android.intent.category.LAUNCHER 1",
            onLog = onLog,
        ).isSuccess
    }

    suspend fun installCreativeStudioFromDownloads(onLog: suspend (String) -> Unit): Boolean {
        onLog("[*] Scanning /sdcard/Download, /data/media/0/Download & /data/local/tmp for Creative Studio APK...")
        val globs = listOf("*sketchbook*.apk", "*CreativeStudio*.apk", "*Creative*Studio*.apk", "*creative-studio*.apk")
        val patterns = buildList {
            for (g in globs) {
                add("/sdcard/Download/$g")
                add("/data/media/0/Download/$g")
                add("/data/local/tmp/$g")
            }
        }.joinToString(" ")
        val findRes = runSuMm("ls -1t $patterns 2>/dev/null | head -n 1", logCommand = false)
        val foundPath = findRes.output.lineSequence().firstOrNull()?.trim().orEmpty()
        if (foundPath.isBlank()) {
            onLog("[ERROR] Creative Studio APK not found in /sdcard/Download or /data/local/tmp. Tap Download from APKMirror or pick manually.")
            return false
        }
        val sizeRes = runSuMm("stat -c%s ${shellQuote(foundPath)} 2>/dev/null || echo 0", logCommand = false)
        val size = sizeRes.output.trim().toLongOrNull() ?: 0L
        if (size <= 0L) {
            onLog("[ERROR] Selected file is empty: $foundPath")
            return false
        }
        return installApkFromDevicePathWithSpoof(
            apkPath = foundPath,
            size = size,
            label = "Samsung Creative Studio",
            restoreOfficialPropAfter = true,
            onLog = onLog,
        )
    }

    suspend fun applyPhotosMethod(
        selectedTags: Map<String, String>,
        enableOfficialSpoof: Boolean = true,
        onLog: suspend (String) -> Unit,
    ): Boolean {
        onLog("[*] Running Photos Method (live Gallery & Photo Editor AI, no reboot)...")
        val mergedTags = linkedMapOf<String, String>().apply {
            putAll(selectedTags)
            put("SEC_FLOATING_FEATURE_COMMON_CONFIG_AI_VERSION", "20263")
            putAll(FeatureGroup.PHOTO_ASSIST.tags)
        }

        val device = queryDeviceProfile(ffPath = resolveFloatingFeatureTarget())
        if (!patchAndBindFloatingFeature(mergedTags, device, onLog)) {
            return false
        }

        if (enableOfficialSpoof) {
            applyOfficialReleaseSpoof(enable = true, onLog = onLog)
        }

        runSuMm("settings --user 0 put system ai_info_confirmed 1", onLog = onLog)
        restartGalleryAndEditor(onLog)
        onLog("[OK] Photos Method applied. Open Samsung Gallery & test Photo Assist / Object Eraser.")
        return true
    }

    suspend fun openSamsungGallery(onLog: suspend (String) -> Unit): Boolean {
        return runSuMm(
            "monkey -p $PKG_GALLERY -c android.intent.category.LAUNCHER 1",
            onLog = onLog,
        ).isSuccess
    }

    suspend fun installApkWithSpoof(
        context: Context,
        apkUri: Uri,
        label: String,
        restoreOfficialPropAfter: Boolean = true,
        onLog: suspend (String) -> Unit,
    ): Boolean {
        val tempFile = File(context.cacheDir, "spoof_install_${System.currentTimeMillis()}.apk")
        return try {
            withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(apkUri)?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                } ?: error("Cannot open selected APK URI.")
            }
            installApkFileWithSpoof(tempFile, label, restoreOfficialPropAfter, onLog)
        } catch (e: Exception) {
            onLog("[ERROR] Failed staging $label: ${e.message}")
            false
        } finally {
            tempFile.delete()
        }
    }

    private suspend fun installApkFileWithSpoof(
        apkFile: File,
        label: String,
        restoreOfficialPropAfter: Boolean,
        onLog: suspend (String) -> Unit,
    ): Boolean {
        val size = apkFile.length()
        if (size <= 0L) {
            onLog("[ERROR] $label is empty.")
            return false
        }
        return installApkFromDevicePathWithSpoof(
            apkPath = apkFile.absolutePath,
            size = size,
            label = label,
            restoreOfficialPropAfter = restoreOfficialPropAfter,
            onLog = onLog,
        )
    }

    private suspend fun installApkFromDevicePathWithSpoof(
        apkPath: String,
        size: Long,
        label: String,
        restoreOfficialPropAfter: Boolean,
        onLog: suspend (String) -> Unit,
    ): Boolean {
        onLog("[*] Installing $label ($size bytes) via Samsung store installer spoof...")
        val rp = findResetprop()
        val oldOfficial = if (rp.isNotBlank()) {
            runSuMm("getprop ro.build.official.release 2>/dev/null || true", logCommand = false)
                .output.trim()
        } else {
            ""
        }

        if (rp.isNotBlank()) {
            runSuMm("${shellQuote(rp)} ro.build.official.release false", onLog = onLog)
        } else {
            onLog("[WARN] resetprop binary not found; relying on CorePatch if -3001 occurs.")
        }

        val installRes = runSuMm(
            "cat ${shellQuote(apkPath)} | pm install -r -d -i com.sec.android.app.samsungapps -S $size 2>&1",
            onLog = onLog,
        )

        if (restoreOfficialPropAfter && rp.isNotBlank() && oldOfficial.isNotBlank()) {
            runSuMm("${shellQuote(rp)} ro.build.official.release ${shellQuote(oldOfficial)}", onLog = onLog)
        }

        val ok = installRes.isSuccess && installRes.output.contains("Success")
        if (!ok) {
            onLog("[ERROR] Failed installing $label. Ensure LSPosed/CorePatch is active if -3001 appears.")
        }
        return ok
    }

    suspend fun installMediaPackFromZip(
        context: Context,
        zipUri: Uri,
        installModels: Boolean,
        installShims: Boolean,
        installApks: Boolean,
        applySpoof: Boolean,
        onLog: suspend (String) -> Unit,
    ): Boolean {
        val extractRoot = File(context.cacheDir, "media_pack_${System.currentTimeMillis()}")
        return try {
            withContext(Dispatchers.IO) {
                extractRoot.mkdirs()
                val rootCanon = extractRoot.canonicalPath + File.separator
                context.contentResolver.openInputStream(zipUri)?.use { rawIn ->
                    ZipInputStream(rawIn).use { zis ->
                        var entry = zis.nextEntry
                        while (entry != null) {
                            if (!entry.isDirectory) {
                                val outFile = File(extractRoot, entry.name)
                                if (!outFile.canonicalPath.startsWith(rootCanon)) {
                                    error("Blocked unsafe ZIP path: ${entry.name}")
                                }
                                outFile.parentFile?.mkdirs()
                                FileOutputStream(outFile).use { out ->
                                    zis.copyTo(out)
                                }
                            }
                            zis.closeEntry()
                            entry = zis.nextEntry
                        }
                    }
                } ?: error("Unable to open ZIP stream.")
            }

            val baseDir = findExtractedPackRoot(extractRoot)
            onLog("[*] Extracted pack root: ${baseDir.name}")
            installMediaPackFromDirectory(
                baseDir = baseDir,
                installModels = installModels,
                installShims = installShims,
                installApks = installApks,
                applySpoof = applySpoof,
                onLog = onLog,
            )
        } catch (e: Exception) {
            onLog("[ERROR] ZIP unpack failed: ${e.message}")
            false
        } finally {
            extractRoot.deleteRecursively()
        }
    }

    suspend fun installMediaPackFromDownloads(
        context: Context,
        installModels: Boolean,
        installShims: Boolean,
        installApks: Boolean,
        applySpoof: Boolean,
        onLog: suspend (String) -> Unit,
    ): Boolean {
        onLog("[*] Searching /sdcard/Download, /data/media/0/Download & /data/local/tmp for Audio Eraser & FanCam pack...")
        val findZip = runSuMm(
            "ls -1t /sdcard/Download/S23U_MyFanCam_AudioEraser*.zip /data/media/0/Download/S23U_MyFanCam_AudioEraser*.zip /data/local/tmp/S23U_MyFanCam_AudioEraser*.zip 2>/dev/null | head -n 1",
            onLog = onLog,
        )
        val foundZipPath = findZip.output.lineSequence().firstOrNull()?.trim().orEmpty()
        if (foundZipPath.isNotBlank()) {
            onLog("[*] Found archive: $foundZipPath")
            val stagedZip = File(context.cacheDir, "downloads_pack_${System.currentTimeMillis()}.zip")
            return try {
                withContext(Dispatchers.IO) { stagedZip.writeBytes(byteArrayOf()) }
                val myUid = Process.myUid()
                val copyRes = runSuMm(
                    "cp -f ${shellQuote(foundZipPath)} ${shellQuote(stagedZip.absolutePath)} && chown $myUid:$myUid ${shellQuote(stagedZip.absolutePath)} && chmod 600 ${shellQuote(stagedZip.absolutePath)} && restorecon ${shellQuote(stagedZip.absolutePath)} 2>/dev/null || true",
                    onLog = onLog,
                )
                if (!copyRes.isSuccess || !stagedZip.exists() || stagedZip.length() == 0L) {
                    onLog("[ERROR] Could not stage archive into app cache.")
                    false
                } else {
                    installMediaPackFromZip(
                        context = context,
                        zipUri = Uri.fromFile(stagedZip),
                        installModels = installModels,
                        installShims = installShims,
                        installApks = installApks,
                        applySpoof = applySpoof,
                        onLog = onLog,
                    )
                }
            } finally {
                stagedZip.delete()
            }
        }

        val findDir = runSuMm(
            "for d in /sdcard/Download/S23U_MyFanCam_AudioEraser* /data/media/0/Download/S23U_MyFanCam_AudioEraser* /data/local/tmp/S23U_MyFanCam_AudioEraser*; do [ -d \"\$d\" ] && echo \"\$d\" && break; done",
            onLog = onLog,
        )
        val foundDirPath = findDir.output.lineSequence().firstOrNull()?.trim().orEmpty()
        if (foundDirPath.isNotBlank()) {
            onLog("[*] Found extracted directory: $foundDirPath")
            return installMediaPackFromDeviceDirectory(
                deviceDirPath = foundDirPath,
                installModels = installModels,
                installShims = installShims,
                installApks = installApks,
                applySpoof = applySpoof,
                onLog = onLog,
            )
        }

        onLog("[ERROR] No S23U_MyFanCam_AudioEraser* archive or folder found in /sdcard/Download or /data/local/tmp.")
        return false
    }

    private fun findExtractedPackRoot(extractRoot: File): File {
        fun hasPackMarkers(dir: File): Boolean =
            File(dir, "models/sbs").isDirectory ||
                File(dir, "libs/bind").isDirectory ||
                File(dir, "libs/mca").isDirectory ||
                File(dir, "apks").isDirectory

        if (hasPackMarkers(extractRoot)) return extractRoot
        extractRoot.listFiles()?.firstOrNull { it.isDirectory && hasPackMarkers(it) }?.let { return it }
        return extractRoot
    }

    private suspend fun installMediaPackFromDirectory(
        baseDir: File,
        installModels: Boolean,
        installShims: Boolean,
        installApks: Boolean,
        applySpoof: Boolean,
        onLog: suspend (String) -> Unit,
    ): Boolean {
        return installMediaPackInternal(
            isDevicePath = false,
            basePath = baseDir.absolutePath,
            installModels = installModels,
            installShims = installShims,
            installApks = installApks,
            applySpoof = applySpoof,
            localBaseDir = baseDir,
            onLog = onLog,
        )
    }

    private suspend fun installMediaPackFromDeviceDirectory(
        deviceDirPath: String,
        installModels: Boolean,
        installShims: Boolean,
        installApks: Boolean,
        applySpoof: Boolean,
        onLog: suspend (String) -> Unit,
    ): Boolean {
        return installMediaPackInternal(
            isDevicePath = true,
            basePath = deviceDirPath,
            installModels = installModels,
            installShims = installShims,
            installApks = installApks,
            applySpoof = applySpoof,
            localBaseDir = null,
            onLog = onLog,
        )
    }

    private suspend fun installMediaPackInternal(
        isDevicePath: Boolean,
        basePath: String,
        installModels: Boolean,
        installShims: Boolean,
        installApks: Boolean,
        applySpoof: Boolean,
        localBaseDir: File?,
        onLog: suspend (String) -> Unit,
    ): Boolean {
        val quotedBase = shellQuote(basePath)
        var anyStepDone = false

        if (applySpoof) {
            applyOfficialReleaseSpoof(enable = true, onLog = onLog)
        }

        if (installModels) {
            val modelsDirExists = if (localBaseDir != null) {
                File(localBaseDir, "models/sbs").isDirectory
            } else {
                runSuMm("test -d $quotedBase/models/sbs && echo YES || echo NO", logCommand = false).output.contains("YES")
            }
            if (modelsDirExists) {
                onLog("[*] Installing Audio Eraser SBS models -> $DIR_SBS/...")
                val res = runSuMm(
                    "mkdir -p $DIR_SBS && cp -f $quotedBase/models/sbs/* $DIR_SBS/ && chown 0:0 $DIR_SBS/* && chmod 755 $DIR_SBS && chmod 644 $DIR_SBS/* && chcon u:object_r:system_file:s0 $DIR_SBS $DIR_SBS/* 2>/dev/null || true",
                    onLog = onLog,
                )
                if (!res.isSuccess) return false
                anyStepDone = true
            } else {
                onLog("[WARN] models/sbs not present in selected pack; skipping Audio Eraser models.")
            }
        }

        if (installShims) {
            val hasBind = if (localBaseDir != null) {
                File(localBaseDir, "libs/bind/libveframework.videoeditor.samsung.so").isFile
            } else {
                runSuMm("test -f $quotedBase/libs/bind/libveframework.videoeditor.samsung.so && echo YES || echo NO", logCommand = false).output.contains("YES")
            }
            val hasMca = if (localBaseDir != null) {
                File(localBaseDir, "libs/mca").isDirectory
            } else {
                runSuMm("test -d $quotedBase/libs/mca && echo YES || echo NO", logCommand = false).output.contains("YES")
            }

            if (hasBind || hasMca) {
                onLog("[*] Installing My FanCam native libraries, MCA shims & placeholder DLCs...")
                val setupCmd = buildString {
                    append("mkdir -p $DIR_FANCAM $DIR_MCA $DIR_MCA_MODELS")
                    if (hasBind) {
                        append(" && cp -f $quotedBase/libs/bind/*.so $DIR_FANCAM/")
                        append(" && chmod 644 $DIR_FANCAM/*.so")
                        append(" && (chcon u:object_r:system_lib_file:s0 $DIR_FANCAM/*.so 2>/dev/null || true)")
                    }
                    if (hasMca) {
                        append(" && cp -f $quotedBase/libs/mca/*.so $DIR_MCA/")
                        append(" && chmod 755 $DIR_MCA $DIR_MCA/*.so")
                        append(" && (chcon u:object_r:system_file:s0 $DIR_MCA $DIR_MCA/*.so 2>/dev/null || true)")
                    }
                    for (dlc in PLACEHOLDER_DLCS) {
                        append(" && printf 'SNAPTFL_PLACEHLD' > $DIR_MCA_MODELS/$dlc")
                    }
                    append(" && chmod 755 $DIR_MCA_MODELS && chmod 644 $DIR_MCA_MODELS/*.dlc")
                    append(" && (chcon u:object_r:system_file:s0 $DIR_MCA_MODELS $DIR_MCA_MODELS/*.dlc 2>/dev/null || true)")
                }
                if (!runSuMm(setupCmd, onLog = onLog).isSuccess) return false

                if (hasBind) {
                    bindStagedFancamShimsIfPresent(onLog)
                }
                anyStepDone = true
            } else {
                onLog("[WARN] libs/bind or libs/mca not present in selected pack; skipping shims.")
            }
        }

        if (installApks) {
            val apkRelPaths = listOf(
                "apks/Gallery_15.9_MyFanCam.apk" to "Samsung Gallery (My FanCam)",
                "apks/VideoEditor_OneUI9_MyFanCam.apk" to "Samsung Video Editor (My FanCam)",
            )
            for ((rel, label) in apkRelPaths) {
                if (!isDevicePath && localBaseDir != null) {
                    val f = File(localBaseDir, rel)
                    if (f.isFile) {
                        if (!installApkFileWithSpoof(f, label, restoreOfficialPropAfter = false, onLog = onLog)) {
                            return false
                        }
                        anyStepDone = true
                    } else {
                        onLog("[WARN] $rel not present in pack; skipping.")
                    }
                } else {
                    val check = runSuMm("test -f $quotedBase/$rel && stat -c%s $quotedBase/$rel || echo 0", logCommand = false)
                    val size = check.output.trim().toLongOrNull() ?: 0L
                    if (size > 0L) {
                        onLog("[*] Installing $label ($size bytes) from device directory...")
                        val rp = findResetprop()
                        if (rp.isNotBlank()) {
                            runSuMm("${shellQuote(rp)} ro.build.official.release false", onLog = onLog)
                        }
                        val out = runSuMm(
                            "cat $quotedBase/$rel | pm install -r -d -i com.sec.android.app.samsungapps -S $size 2>&1",
                            onLog = onLog,
                        )
                        if (!out.isSuccess || !out.output.contains("Success")) return false
                        anyStepDone = true
                    }
                }
            }
        }

        restartGalleryAndEditor(onLog)
        if (!anyStepDone) {
            onLog("[WARN] Selected components were not found inside the chosen package.")
            return false
        }
        onLog("[OK] Audio Eraser & My FanCam setup complete.")
        return true
    }

    suspend fun restoreStock(onLog: suspend (String) -> Unit): Boolean {
        onLog("[*] Unmounting overlays, native library binds & cleaning staged files...")
        val ffPath = resolveFloatingFeatureTarget()
        val umountFf = runSuMm(
            "umount $ffPath || umount -l $ffPath 2>/dev/null || umount -l $FF_TARGET 2>/dev/null || true",
            onLog = onLog,
        )
        val sigPath = resolveSignaturePermissionsTarget() ?: SIG_TARGET
        runSuMm(
            "umount $sigPath || umount -l $sigPath 2>/dev/null || umount -l $SIG_TARGET 2>/dev/null || true",
            onLog = onLog,
        )
        runSuMm(
            "umount -l $BIND_VEFRAMEWORK 2>/dev/null || true; umount -l $BIND_CONTEXTANALYZER 2>/dev/null || true",
            onLog = onLog,
        )
        unmountCameraFeatureOverlay(onLog)
        unmountAicoreSysconfigOverlay(onLog)
        applyModelSpoof(null, onLog)

        val mediaNow = queryMediaPackStatus(ffPath = ffPath)
        if (mediaNow.resetpropAvailable && mediaNow.officialSpoofActive) {
            applyOfficialReleaseSpoof(enable = false, onLog = onLog)
        }
        runSuMm(
            "settings --user 0 put system aod_show_lockscreen_wallpaper 0 2>/dev/null || true; am force-stop $PKG_AOD_SERVICE 2>/dev/null || true; am force-stop $PKG_CAMERA 2>/dev/null || true",
            onLog = onLog,
        )

        val rmRes = runSuMm(
            "rm -rf $FF_PATCHED $FF_PATCHED_LEGACY $FF_PATCHED_S23U_LEGACY $SIG_PATCHED $DIR_FANCAM $DIR_MCA $DIR_SBS /data/adb/post-fs-data.d/99_z_fancam_audioeraser.sh /su/su.d/99_z_fancam_audioeraser.sh",
            onLog = onLog,
        )
        restartGalleryAndEditor(onLog)
        onLog("[OK] Stock floating_feature.xml, bind-mounts & spoof props restored.")
        return umountFf.isSuccess && rmRes.isSuccess
    }

    suspend fun restartGalleryAndEditor(onLog: suspend (String) -> Unit): Boolean {
        return runSuMm(
            "am force-stop $PKG_GALLERY; am force-stop $PKG_PHOTO_EDITOR; am force-stop --user 0 $PKG_VIDEO_EDITOR 2>/dev/null || true",
            onLog = onLog,
        ).isSuccess
    }

    suspend fun softRebootZygote(onLog: suspend (String) -> Unit): Boolean {
        onLog("[*] Soft-rebooting Zygote...")
        return runSuMm("sync; setprop ctl.restart zygote", onLog = onLog).isSuccess
    }

    suspend fun launchOnboarding(onLog: suspend (String) -> Unit): Boolean {
        return runSuMm(
            "am start --user 0 -n com.samsung.android.smartsuggestions/.feature.aisuggestion.ui.onboarding.OnboardingActivity",
            onLog = onLog,
        ).isSuccess
    }

    suspend fun openNudgeSettings(onLog: suspend (String) -> Unit): Boolean {
        return runSuMm(
            "am start --user 0 -n com.samsung.android.smartsuggestions/.nudgesettings.NudgeMainSettingsActivity",
            onLog = onLog,
        ).isSuccess
    }

    fun shellQuote(value: String): String = "'" + value.replace("'", "'\\''") + "'"
}
