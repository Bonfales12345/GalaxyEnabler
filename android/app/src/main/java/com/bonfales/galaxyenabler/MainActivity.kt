@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
)

package com.bonfales.galaxyenabler

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ManageSearch
import androidx.compose.material.icons.rounded.Assistant
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.InstallMobile
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Tonality
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.VideoSettings
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bonfales.galaxyenabler.ui.theme.GalaxyEnablerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private fun applyPresetToMap(
    preset: AppDefaultPreset,
    target: MutableMap<FeatureGroup, Boolean>,
) {
    FeatureGroup.entries.forEach { group ->
        target[group] = when (preset) {
            AppDefaultPreset.RECOMMENDED -> group.inDefaultProfile
            AppDefaultPreset.MINIMAL -> group == FeatureGroup.CORE
            AppDefaultPreset.ALL -> true
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val initialSettings = AppPrefsStore.load(this)
        setContent {
            val context = LocalContext.current
            var appSettings by remember { mutableStateOf(initialSettings) }
            var showSetupWizard by remember { mutableStateOf(!initialSettings.setupCompleted) }
            var setupInitialStep by remember { mutableIntStateOf(0) }
            var presetApplyNonce by remember { mutableIntStateOf(0) }

            val haptics = rememberPixelHaptics(appSettings.hapticsEnabled)

            fun updateSettings(next: AppSettings) {
                appSettings = next
                AppPrefsStore.save(context, next)
            }

            GalaxyEnablerTheme(settings = appSettings) {
                AnimatedContent(
                    targetState = showSetupWizard,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "root_flow",
                ) { inSetup ->
                    if (inSetup) {
                        PixelSetupScreen(
                            settings = appSettings,
                            initialStep = setupInitialStep,
                            isReconfiguring = appSettings.setupCompleted,
                            haptics = haptics,
                            onUpdateSettings = ::updateSettings,
                            onCancel = { showSetupWizard = false },
                            onFinish = { finalSettings ->
                                val completed = finalSettings.copy(setupCompleted = true)
                                updateSettings(completed)
                                presetApplyNonce++
                                showSetupWizard = false
                            },
                        )
                    } else {
                        GalaxyEnablerApp(
                            settings = appSettings,
                            haptics = haptics,
                            presetApplyNonce = presetApplyNonce,
                            onOpenSetup = { step ->
                                setupInitialStep = step
                                showSetupWizard = true
                            },
                        )
                    }
                }
            }
        }
    }
}

private enum class AppTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Rounded.Home),
    FEATURES("Features", Icons.Rounded.Tune),
    LOG("Log", Icons.Rounded.Terminal),
}

private enum class DialogState {
    NONE,
    CONFIRM_APPLY,
    OFFER_SOFT_REBOOT,
    MEDIA_PACK_POPUP,
    CREATIVE_STUDIO_POPUP,
    APPLE_SHARE_POPUP,
    NOTIFICATION_HIGHLIGHTS_POPUP,
    HORIZONTAL_LOCK_POPUP,
    AICORE_POPUP,
    AOD_SPOOF_DONE,
    PHOTOS_DONE,
}

private val APK_MIME_TYPES = arrayOf(
    "application/vnd.android.package-archive",
    "application/octet-stream",
    "*/*",
)

private val AICORE_MIME_TYPES = arrayOf(
    "application/vnd.android.package-archive",
    "application/zip",
    "application/octet-stream",
    "*/*",
)

private val ZIP_MIME_TYPES = arrayOf(
    "application/zip",
    "application/x-zip-compressed",
    "application/octet-stream",
    "*/*",
)

private fun openExternalUrl(context: Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }.onFailure {
        Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun GalaxyEnablerApp(
    settings: AppSettings,
    haptics: PixelHaptics,
    presetApplyNonce: Int,
    onOpenSetup: (Int) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentTab by remember { mutableStateOf(AppTab.HOME) }
    var status by remember { mutableStateOf(SystemStatus()) }
    var busy by remember { mutableStateOf(false) }
    var dialogState by remember { mutableStateOf(DialogState.NONE) }
    var logText by remember { mutableStateOf("Ready.") }

    var enableOfficialSpoof by remember { mutableStateOf(true) }
    var aicoreSpoofModelInstall by remember { mutableStateOf(true) }
    var aicoreKeepModelSpoof by remember { mutableStateOf(false) }

    var pendingApplyAfterPopup by remember { mutableStateOf(false) }
    var popupInstallModels by remember { mutableStateOf(true) }
    var popupInstallShims by remember { mutableStateOf(true) }
    var popupInstallApks by remember { mutableStateOf(false) }
    var popupApplySpoof by remember { mutableStateOf(true) }

    val enabledGroups = remember {
        mutableStateMapOf<FeatureGroup, Boolean>().apply {
            applyPresetToMap(settings.defaultPreset, this)
        }
    }

    LaunchedEffect(presetApplyNonce) {
        if (presetApplyNonce > 0) {
            applyPresetToMap(settings.defaultPreset, enabledGroups)
        }
    }

    suspend fun appendLog(line: String) {
        withContext(Dispatchers.Main) {
            logText = if (logText.isEmpty() || logText == "Ready.") line else "$logText\n$line"
        }
    }

    suspend fun refreshStatus(logOutput: Boolean = false) {
        status = RootHelper.queryStatus(onLog = if (logOutput) ::appendLog else null)
    }

    fun runAction(block: suspend () -> Unit) {
        if (busy) return
        scope.launch {
            busy = true
            try {
                block()
            } finally {
                busy = false
            }
        }
    }

    fun collectSelectedTags(): LinkedHashMap<String, String> {
        val tags = linkedMapOf<String, String>()
        FeatureGroup.entries.forEach { group ->
            if (enabledGroups[group] == true) {
                tags.putAll(group.tags)
            }
        }
        return tags
    }

    fun openMediaPackDialog(fromApply: Boolean) {
        val audioOn = enabledGroups[FeatureGroup.AUDIO] == true
        val fancamOn = enabledGroups[FeatureGroup.FANCAM] == true
        val mp = status.mediaPack
        popupInstallModels = if (fromApply) audioOn && !mp.audioModelsReady else !mp.audioModelsReady
        popupInstallShims = if (fromApply) fancamOn && !mp.fancamShimsReady else !mp.fancamShimsReady
        popupApplySpoof = enableOfficialSpoof
        pendingApplyAfterPopup = fromApply
        dialogState = DialogState.MEDIA_PACK_POPUP
    }

    val zipPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val resumeApply = pendingApplyAfterPopup
        pendingApplyAfterPopup = false
        dialogState = DialogState.NONE
        currentTab = AppTab.LOG
        runAction {
            val ok = RootHelper.installMediaPackFromZip(
                context = context,
                zipUri = uri,
                installModels = popupInstallModels,
                installShims = popupInstallShims,
                installApks = popupInstallApks,
                applySpoof = popupApplySpoof,
                onLog = ::appendLog,
            )
            refreshStatus(logOutput = false)
            if (ok && resumeApply) {
                dialogState = DialogState.CONFIRM_APPLY
            }
        }
    }

    val apkPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        dialogState = DialogState.NONE
        currentTab = AppTab.LOG
        runAction {
            RootHelper.installApkWithSpoof(
                context = context,
                apkUri = uri,
                label = "Selected Samsung APK",
                restoreOfficialPropAfter = true,
                onLog = ::appendLog,
            )
            refreshStatus(logOutput = false)
        }
    }

    val aicorePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        dialogState = DialogState.NONE
        currentTab = AppTab.LOG
        runAction {
            RootHelper.installAicorePackageWithSpoof(
                context = context,
                uri = uri,
                spoofModelDuringInstall = aicoreSpoofModelInstall,
                keepModelSpoofAfter = aicoreKeepModelSpoof,
                onLog = ::appendLog,
            )
            refreshStatus(logOutput = false)
        }
    }

    LaunchedEffect(Unit) {
        refreshStatus(logOutput = false)
    }

    val enabledCount = enabledGroups.values.count { it }
    val allEnabled = enabledCount == FeatureGroup.entries.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentTab) {
                            AppTab.HOME -> "GalaxyEnabler"
                            AppTab.FEATURES -> "Features"
                            AppTab.LOG -> "Log"
                        },
                        style = MaterialTheme.typography.titleLargeEmphasized,
                    )
                },
                subtitle = {
                    val stateLabel = if (status.mountActive) "Active" else "Stock"
                    val homeSubtitle = if (status.rootGranted && status.device.displayLabel.isNotBlank()) {
                        "${status.device.displayLabel} · $stateLabel"
                    } else {
                        stateLabel
                    }
                    Text(
                        text = when (currentTab) {
                            AppTab.HOME -> homeSubtitle
                            AppTab.FEATURES -> "$enabledCount of ${FeatureGroup.entries.size} enabled"
                            AppTab.LOG -> "Root · ${status.device.rootMode}"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                actions = {
                    if (busy) {
                        ContainedLoadingIndicator(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(36.dp),
                        )
                    } else {
                        when (currentTab) {
                            AppTab.HOME -> {
                                FilledTonalIconButton(
                                    onClick = {
                                        haptics.click()
                                        onOpenSetup(1)
                                    },
                                    shapes = IconButtonDefaults.shapes(),
                                ) {
                                    Icon(Icons.Rounded.Palette, contentDescription = "Customize")
                                }
                                Spacer(Modifier.width(4.dp))
                                FilledTonalIconButton(
                                    onClick = {
                                        haptics.click()
                                        runAction { refreshStatus(logOutput = true) }
                                    },
                                    shapes = IconButtonDefaults.shapes(),
                                ) {
                                    Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
                                }
                            }
                            AppTab.FEATURES -> {
                                FilledTonalButton(
                                    onClick = {
                                        haptics.tick()
                                        applyPresetToMap(settings.defaultPreset, enabledGroups)
                                    },
                                    shapes = ButtonDefaults.shapes(),
                                ) {
                                    Text("Default")
                                }
                                Spacer(Modifier.width(4.dp))
                                TextButton(
                                    onClick = {
                                        haptics.tick()
                                        val target = !allEnabled
                                        FeatureGroup.entries.forEach { enabledGroups[it] = target }
                                    },
                                    shapes = ButtonDefaults.shapes(),
                                ) {
                                    Text(if (allEnabled) "Clear" else "All")
                                }
                            }
                            AppTab.LOG -> {
                                FilledTonalIconButton(
                                    onClick = {
                                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        cm.setPrimaryClip(ClipData.newPlainText("GalaxyEnabler Log", logText))
                                        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                                    },
                                    shapes = IconButtonDefaults.shapes(),
                                ) {
                                    Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy")
                                }
                                IconButton(
                                    onClick = { logText = "Ready." },
                                    shapes = IconButtonDefaults.shapes(),
                                ) {
                                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Clear")
                                }
                            }
                        }
                    }
                },
            )
        },
        bottomBar = {
            ShortNavigationBar {
                AppTab.entries.forEach { tab ->
                    ShortNavigationBarItem(
                        selected = currentTab == tab,
                        onClick = {
                            if (currentTab != tab) haptics.tick()
                            currentTab = tab
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (currentTab) {
                AppTab.HOME -> HomeTab(
                    status = status,
                    heroShape = settings.heroShape,
                    enabledCount = enabledCount,
                    totalCount = FeatureGroup.entries.size,
                    busy = busy,
                    onApply = {
                        haptics.click()
                        val audioOn = enabledGroups[FeatureGroup.AUDIO] == true
                        val fancamOn = enabledGroups[FeatureGroup.FANCAM] == true
                        if (status.rootGranted && status.mediaPack.needsSetup(audioOn, fancamOn)) {
                            openMediaPackDialog(fromApply = true)
                        } else {
                            dialogState = DialogState.CONFIRM_APPLY
                        }
                    },
                    onPhotosMethod = {
                        haptics.click()
                        currentTab = AppTab.LOG
                        runAction {
                            val ok = RootHelper.applyPhotosMethod(
                                selectedTags = collectSelectedTags(),
                                enableOfficialSpoof = enableOfficialSpoof,
                                onLog = ::appendLog,
                            )
                            refreshStatus(logOutput = false)
                            if (ok) {
                                dialogState = DialogState.PHOTOS_DONE
                            }
                        }
                    },
                    onAodWallpaperSpoof = {
                        haptics.click()
                        currentTab = AppTab.LOG
                        runAction {
                            val ok = RootHelper.applyAodWallpaperSpoof(
                                selectedTags = collectSelectedTags(),
                                enableOfficialSpoof = enableOfficialSpoof,
                                onLog = ::appendLog,
                            )
                            refreshStatus(logOutput = false)
                            if (ok) {
                                dialogState = DialogState.AOD_SPOOF_DONE
                            }
                        }
                    },
                    onRestore = {
                        haptics.click()
                        runAction {
                            val ok = RootHelper.restoreStock(::appendLog)
                            refreshStatus(logOutput = false)
                            if (ok) dialogState = DialogState.OFFER_SOFT_REBOOT
                        }
                    },
                    onOpenAppearance = {
                        haptics.click()
                        onOpenSetup(0)
                    },
                    onOpenNotificationHighlightsPopup = { dialogState = DialogState.NOTIFICATION_HIGHLIGHTS_POPUP },
                    onOpenHorizontalLockPopup = { dialogState = DialogState.HORIZONTAL_LOCK_POPUP },
                    onOpenAicorePopup = { dialogState = DialogState.AICORE_POPUP },
                    onOpenCreativeStudio = { dialogState = DialogState.CREATIVE_STUDIO_POPUP },
                    onOpenAppleSharePopup = { dialogState = DialogState.APPLE_SHARE_POPUP },
                    onOpenMediaSetup = { openMediaPackDialog(fromApply = false) },
                    onPickApkSpoof = { apkPickerLauncher.launch(APK_MIME_TYPES) },
                    onOnboarding = { runAction { RootHelper.launchOnboarding(::appendLog) } },
                    onNudgeSettings = { runAction { RootHelper.openNudgeSettings(::appendLog) } },
                    onAodSettings = { runAction { RootHelper.openAodSettings(::appendLog) } },
                    onCheckAiVersion = {
                        haptics.tick()
                        runAction {
                            val ver = RootHelper.checkAiVersion(
                                ffTarget = status.device.ffPath,
                                onLog = ::appendLog,
                            )
                            status = status.copy(aiVersion = ver)
                        }
                    },
                )

                AppTab.FEATURES -> FeaturesTab(
                    enabledGroups = enabledGroups,
                    enableOfficialSpoof = enableOfficialSpoof,
                    busy = busy,
                    onToggle = { group, checked ->
                        haptics.tick()
                        enabledGroups[group] = checked
                    },
                    onToggleOfficialSpoof = {
                        haptics.tick()
                        enableOfficialSpoof = it
                    },
                    onOpenNotificationHighlightsPopup = { dialogState = DialogState.NOTIFICATION_HIGHLIGHTS_POPUP },
                    onOpenAppleSharePopup = { dialogState = DialogState.APPLE_SHARE_POPUP },
                    onOpenHorizontalLockPopup = { dialogState = DialogState.HORIZONTAL_LOCK_POPUP },
                    onOpenAicorePopup = { dialogState = DialogState.AICORE_POPUP },
                )

                AppTab.LOG -> LogTab(logText = logText)
            }
        }
    }

    if (dialogState == DialogState.CONFIRM_APPLY) {
        val selectedTags = collectSelectedTags()
        AlertDialog(
            onDismissRequest = { dialogState = DialogState.NONE },
            title = { Text("Apply & Soft Reboot?", style = MaterialTheme.typography.headlineSmallEmphasized) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WarningAmber,
                                contentDescription = "Warning",
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(20.dp),
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Save all app data before continuing",
                                    style = MaterialTheme.typography.titleSmallEmphasized,
                                )
                                Text(
                                    text = "Applying this patch will immediately soft-reboot Zygote and restart all running apps and services. Any unsaved work will be lost.",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }

                    Text(
                        text = "Applies ${selectedTags.size} feature flags and soft-reboots Zygote.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        dialogState = DialogState.NONE
                        currentTab = AppTab.LOG
                        runAction {
                            logText = ""
                            val ok = RootHelper.applyFeatures(
                                selectedTags = selectedTags,
                                enableOfficialSpoof = enableOfficialSpoof,
                                onLog = ::appendLog,
                            )
                            if (!ok) refreshStatus(logOutput = false)
                        }
                    },
                    shapes = ButtonDefaults.shapes(),
                ) {
                    Text("Apply & Reboot")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { dialogState = DialogState.NONE },
                    shapes = ButtonDefaults.shapes(),
                ) {
                    Text("Cancel")
                }
            },
        )
    }

    if (dialogState == DialogState.MEDIA_PACK_POPUP) {
        val mp = status.mediaPack
        AlertDialog(
            onDismissRequest = {
                dialogState = DialogState.NONE
                pendingApplyAfterPopup = false
            },
            title = { Text("Audio Eraser & FanCam Setup", style = MaterialTheme.typography.headlineSmallEmphasized) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = if (pendingApplyAfterPopup) {
                            "Required Audio Eraser or FanCam files are missing. Install from the release ZIP or continue with flags only."
                        } else {
                            "Install Audio Eraser models, FanCam libraries, and downgraded Samsung APKs."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            StatusCheckRow("Audio Models", if (mp.audioModelsReady) "Ready" else "Missing")
                            StatusCheckRow(
                                "FanCam Shims",
                                when {
                                    mp.fancamBindActive -> "Active"
                                    mp.fancamShimsReady -> "Staged"
                                    else -> "Missing"
                                },
                            )
                            StatusCheckRow(
                                "Style Transfer",
                                if (mp.styleTransferModelPresent) "Present" else "Missing",
                            )
                            StatusCheckRow(
                                "Spoof Tool",
                                if (mp.resetpropAvailable) "Ready (${mp.officialRelease})" else "Missing",
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    PopupCheckboxRow(
                        label = "Audio Eraser models",
                        checked = popupInstallModels,
                        onCheckedChange = { popupInstallModels = it },
                    )
                    PopupCheckboxRow(
                        label = "FanCam libraries",
                        checked = popupInstallShims,
                        onCheckedChange = { popupInstallShims = it },
                    )
                    PopupCheckboxRow(
                        label = "Gallery & Video Editor APKs",
                        checked = popupInstallApks,
                        onCheckedChange = { popupInstallApks = it },
                    )
                    PopupCheckboxRow(
                        label = "Official release spoof",
                        checked = popupApplySpoof,
                        onCheckedChange = { popupApplySpoof = it },
                    )

                    Button(
                        onClick = {
                            val resumeApply = pendingApplyAfterPopup
                            pendingApplyAfterPopup = false
                            dialogState = DialogState.NONE
                            currentTab = AppTab.LOG
                            runAction {
                                val ok = RootHelper.installMediaPackFromDownloads(
                                    context = context,
                                    installModels = popupInstallModels,
                                    installShims = popupInstallShims,
                                    installApks = popupInstallApks,
                                    applySpoof = popupApplySpoof,
                                    onLog = ::appendLog,
                                )
                                refreshStatus(logOutput = false)
                                if (ok && resumeApply) {
                                    dialogState = DialogState.CONFIRM_APPLY
                                }
                            }
                        },
                        enabled = !busy && (popupInstallModels || popupInstallShims || popupInstallApks),
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Install from /sdcard/Download")
                    }

                    FilledTonalButton(
                        onClick = { zipPickerLauncher.launch(ZIP_MIME_TYPES) },
                        enabled = !busy && (popupInstallModels || popupInstallShims || popupInstallApks),
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Pick Release ZIP")
                    }

                    OutlinedButton(
                        onClick = { openExternalUrl(context, RootHelper.RELEASES_URL) },
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Download Release ZIP")
                    }
                }
            },
            confirmButton = {
                if (pendingApplyAfterPopup) {
                    TextButton(
                        onClick = {
                            pendingApplyAfterPopup = false
                            dialogState = DialogState.CONFIRM_APPLY
                        },
                        shapes = ButtonDefaults.shapes(),
                    ) {
                        Text("Continue Flags Only")
                    }
                } else {
                    TextButton(
                        onClick = { dialogState = DialogState.NONE },
                        shapes = ButtonDefaults.shapes(),
                    ) {
                        Text("Close")
                    }
                }
            },
            dismissButton = {
                if (pendingApplyAfterPopup) {
                    TextButton(
                        onClick = {
                            pendingApplyAfterPopup = false
                            dialogState = DialogState.NONE
                        },
                        shapes = ButtonDefaults.shapes(),
                    ) {
                        Text("Cancel")
                    }
                }
            },
        )
    }

    if (dialogState == DialogState.CREATIVE_STUDIO_POPUP) {
        val mp = status.mediaPack
        AlertDialog(
            onDismissRequest = { dialogState = DialogState.NONE },
            title = { Text("Creative Studio", style = MaterialTheme.typography.headlineSmallEmphasized) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            StatusCheckRow("Status", mp.creativeStudioVersion.ifBlank { "Not installed" })
                        }
                    }

                    if (mp.creativeStudioInstalled) {
                        Button(
                            onClick = {
                                dialogState = DialogState.NONE
                                runAction { RootHelper.launchCreativeStudio(::appendLog) }
                            },
                            shapes = ButtonDefaults.shapes(),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Launch Creative Studio")
                        }
                    }

                    FilledTonalButton(
                        onClick = {
                            dialogState = DialogState.NONE
                            currentTab = AppTab.LOG
                            runAction {
                                RootHelper.installCreativeStudioFromDownloads(::appendLog)
                                refreshStatus(logOutput = false)
                            }
                        },
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Install from /sdcard/Download")
                    }

                    OutlinedButton(
                        onClick = { apkPickerLauncher.launch(APK_MIME_TYPES) },
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Pick Creative Studio APK (Spoof)")
                    }

                    OutlinedButton(
                        onClick = { openExternalUrl(context, RootHelper.CREATIVE_STUDIO_URL) },
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Download from APKMirror")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { dialogState = DialogState.NONE },
                    shapes = ButtonDefaults.shapes(),
                ) {
                    Text("Close")
                }
            },
        )
    }

    if (dialogState == DialogState.APPLE_SHARE_POPUP) {
        val mp = status.mediaPack
        AlertDialog(
            onDismissRequest = { dialogState = DialogState.NONE },
            title = { Text("Share with Apple", style = MaterialTheme.typography.headlineSmallEmphasized) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            StatusCheckRow(
                                "Quick Share (13.8.51.27+)",
                                mp.quickShareVersion.ifBlank { "Missing" },
                            )
                            StatusCheckRow(
                                "Connectivity (1.5.13.15+)",
                                mp.quickShareConnectivityVersion.ifBlank { "Missing" },
                            )
                            StatusCheckRow("Play Services", "26.11+")
                        }
                    }

                    Text(
                        text = "On the Apple device, set AirDrop to \"Everyone for 10 minutes\".",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Button(
                        onClick = {
                            dialogState = DialogState.NONE
                            currentTab = AppTab.LOG
                            runAction {
                                RootHelper.applyShareWithApple(
                                    selectedTags = collectSelectedTags(),
                                    enableOfficialSpoof = enableOfficialSpoof,
                                    onLog = ::appendLog,
                                )
                                refreshStatus(logOutput = false)
                            }
                        },
                        enabled = !busy,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Enable & Open Quick Share Settings")
                    }

                    FilledTonalButton(
                        onClick = {
                            dialogState = DialogState.NONE
                            currentTab = AppTab.LOG
                            runAction {
                                RootHelper.installQuickShareFromDownloads(::appendLog)
                                refreshStatus(logOutput = false)
                            }
                        },
                        enabled = !busy,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Install from Downloads")
                    }

                    FilledTonalButton(
                        onClick = { apkPickerLauncher.launch(APK_MIME_TYPES) },
                        enabled = !busy,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Pick APK")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = { openExternalUrl(context, RootHelper.QUICK_SHARE_URL) },
                            shapes = ButtonDefaults.shapes(),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Quick Share", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        OutlinedButton(
                            onClick = { openExternalUrl(context, RootHelper.QUICK_SHARE_CONN_URL) },
                            shapes = ButtonDefaults.shapes(),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Connectivity", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { dialogState = DialogState.NONE },
                    shapes = ButtonDefaults.shapes(),
                ) {
                    Text("Close")
                }
            },
        )
    }

    if (dialogState == DialogState.NOTIFICATION_HIGHLIGHTS_POPUP) {
        val mp = status.mediaPack
        AlertDialog(
            onDismissRequest = { dialogState = DialogState.NONE },
            title = { Text("Notification Highlights", style = MaterialTheme.typography.headlineSmallEmphasized) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            StatusCheckRow(
                                "Language Core (3.2.40.5+)",
                                mp.languageCoreVersion.ifBlank { "Missing / Stock" },
                            )
                            StatusCheckRow(
                                "One UI",
                                status.device.oneUiLabel.ifBlank { "—" },
                            )
                        }
                    }

                    Text(
                        text = "Enables on-device AI Notification Summaries and Prioritize Notifications via floating_feature & Samsung Language Core.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Button(
                        onClick = {
                            dialogState = DialogState.NONE
                            currentTab = AppTab.LOG
                            runAction {
                                RootHelper.applyNotificationHighlights(
                                    selectedTags = collectSelectedTags(),
                                    enableOfficialSpoof = enableOfficialSpoof,
                                    onLog = ::appendLog,
                                )
                                refreshStatus(logOutput = false)
                            }
                        },
                        enabled = !busy,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Enable & Open Notification Settings")
                    }

                    FilledTonalButton(
                        onClick = {
                            dialogState = DialogState.NONE
                            currentTab = AppTab.LOG
                            runAction {
                                RootHelper.installLanguageCoreFromDownloads(::appendLog)
                                refreshStatus(logOutput = false)
                            }
                        },
                        enabled = !busy,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Install Language Core from Downloads")
                    }

                    FilledTonalButton(
                        onClick = { apkPickerLauncher.launch(APK_MIME_TYPES) },
                        enabled = !busy,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Pick APK")
                    }

                    OutlinedButton(
                        onClick = { openExternalUrl(context, RootHelper.LANGUAGE_CORE_URL) },
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Download Language Core (APKMirror)")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { dialogState = DialogState.NONE },
                    shapes = ButtonDefaults.shapes(),
                ) {
                    Text("Close")
                }
            },
        )
    }

    if (dialogState == DialogState.HORIZONTAL_LOCK_POPUP) {
        val mp = status.mediaPack
        AlertDialog(
            onDismissRequest = { dialogState = DialogState.NONE },
            title = { Text("Horizontal Lock (Camera)", style = MaterialTheme.typography.headlineSmallEmphasized) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            StatusCheckRow(
                                "Camera Version",
                                mp.cameraVersion.ifBlank { "—" },
                            )
                            StatusCheckRow(
                                "Camera Feature XML",
                                mp.cameraFeaturePath.ifBlank { "Not found" },
                            )
                            StatusCheckRow(
                                "Overlay Status",
                                if (mp.horizontalLockMountActive) "Active" else "Stock",
                            )
                        }
                    }

                    Text(
                        text = "Bind-mounts camera-feature.xml + floating_feature.xml in RAM (Knox 0x0). 360° Horizon Lock requires One UI 9 / S26-capable Camera APK & EIS HAL — disable below if Super Steady closes on older HALs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Button(
                        onClick = {
                            dialogState = DialogState.NONE
                            currentTab = AppTab.LOG
                            runAction {
                                RootHelper.applyHorizontalLock(
                                    selectedTags = collectSelectedTags(),
                                    enableOfficialSpoof = enableOfficialSpoof,
                                    onLog = ::appendLog,
                                )
                                refreshStatus(logOutput = false)
                            }
                        },
                        enabled = !busy,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Enable & Test in Camera (No Reboot)")
                    }

                    FilledTonalButton(
                        onClick = {
                            dialogState = DialogState.NONE
                            currentTab = AppTab.LOG
                            runAction {
                                RootHelper.disableHorizontalLock(::appendLog)
                                refreshStatus(logOutput = false)
                            }
                        },
                        enabled = !busy,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Disable Horizontal Lock")
                    }

                    OutlinedButton(
                        onClick = { apkPickerLauncher.launch(APK_MIME_TYPES) },
                        enabled = !busy,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Pick Camera APK (Spoof)")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { dialogState = DialogState.NONE },
                    shapes = ButtonDefaults.shapes(),
                ) {
                    Text("Close")
                }
            },
        )
    }

    if (dialogState == DialogState.AICORE_POPUP) {
        val mp = status.mediaPack
        AlertDialog(
            onDismissRequest = { dialogState = DialogState.NONE },
            title = { Text("AICore & Gemini Nano", style = MaterialTheme.typography.headlineSmallEmphasized) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            StatusCheckRow(
                                "Google AICore",
                                when {
                                    mp.aicoreFullInstalled -> "Ready (${mp.aicoreVersion})"
                                    mp.aicoreStubOnly -> "Stub (${mp.aicoreVersion})"
                                    else -> "Missing"
                                },
                            )
                            StatusCheckRow(
                                "Sysconfig Feature",
                                if (mp.aicoreFeatureDeclared) "Declared" else "Needs Reboot / Apply",
                            )
                            StatusCheckRow(
                                "Private Compute",
                                mp.pcsVersion.ifBlank { "Missing / Stock" },
                            )
                        }
                    }

                    Text(
                        text = "Injects com.google.android.feature.AICORE via sysconfig bind-mount, sets DeviceConfig aicore flags, and spoofs Play Store + S26 Ultra model to install single APKs or .apkm split bundles.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    PopupCheckboxRow(
                        label = "Spoof S26 Ultra (SM-S948B) during install",
                        checked = aicoreSpoofModelInstall,
                        onCheckedChange = { aicoreSpoofModelInstall = it },
                    )
                    PopupCheckboxRow(
                        label = "Keep S26 Ultra model spoof for model download",
                        checked = aicoreKeepModelSpoof,
                        onCheckedChange = { aicoreKeepModelSpoof = it },
                    )

                    Button(
                        onClick = {
                            dialogState = DialogState.NONE
                            currentTab = AppTab.LOG
                            runAction {
                                val ok = RootHelper.applyAicoreSetup(
                                    selectedTags = collectSelectedTags(),
                                    enableOfficialSpoof = enableOfficialSpoof,
                                    persistModelSpoof = aicoreKeepModelSpoof,
                                    onLog = ::appendLog,
                                )
                                refreshStatus(logOutput = false)
                                if (ok && !status.mediaPack.aicoreFeatureDeclared) {
                                    dialogState = DialogState.OFFER_SOFT_REBOOT
                                }
                            }
                        },
                        enabled = !busy,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Enable AICore Flags & Sysconfig")
                    }

                    FilledTonalButton(
                        onClick = {
                            dialogState = DialogState.NONE
                            currentTab = AppTab.LOG
                            runAction {
                                RootHelper.installAicoreFromDownloads(
                                    context = context,
                                    spoofModelDuringInstall = aicoreSpoofModelInstall,
                                    keepModelSpoofAfter = aicoreKeepModelSpoof,
                                    onLog = ::appendLog,
                                )
                                refreshStatus(logOutput = false)
                            }
                        },
                        enabled = !busy,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Install AICore from /sdcard/Download")
                    }

                    FilledTonalButton(
                        onClick = { aicorePickerLauncher.launch(AICORE_MIME_TYPES) },
                        enabled = !busy,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Pick AICore APK / Bundle (.apkm)")
                    }

                    OutlinedButton(
                        onClick = { openExternalUrl(context, RootHelper.AICORE_APKMIRROR_URL) },
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Download Google AICore (APKMirror)")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { dialogState = DialogState.NONE },
                    shapes = ButtonDefaults.shapes(),
                ) {
                    Text("Close")
                }
            },
        )
    }

    if (dialogState == DialogState.AOD_SPOOF_DONE) {
        AlertDialog(
            onDismissRequest = { dialogState = DialogState.NONE },
            title = { Text("AOD Wallpaper Active", style = MaterialTheme.typography.headlineSmallEmphasized) },
            text = {
                Text("AOD fullscreen wallpaper enabled and SystemUI restarted.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        dialogState = DialogState.NONE
                        runAction { RootHelper.openAodSettings(::appendLog) }
                    },
                    shapes = ButtonDefaults.shapes(),
                ) {
                    Text("AOD Settings")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { dialogState = DialogState.NONE },
                    shapes = ButtonDefaults.shapes(),
                ) {
                    Text("Done")
                }
            },
        )
    }

    if (dialogState == DialogState.PHOTOS_DONE) {
        AlertDialog(
            onDismissRequest = { dialogState = DialogState.NONE },
            title = { Text("Photos Method Applied", style = MaterialTheme.typography.headlineSmallEmphasized) },
            text = {
                Text("Gallery and Photo Editor were restarted.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        dialogState = DialogState.NONE
                        runAction { RootHelper.openSamsungGallery(::appendLog) }
                    },
                    shapes = ButtonDefaults.shapes(),
                ) {
                    Text("Open Gallery")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { dialogState = DialogState.NONE },
                    shapes = ButtonDefaults.shapes(),
                ) {
                    Text("Done")
                }
            },
        )
    }

    if (dialogState == DialogState.OFFER_SOFT_REBOOT) {
        AlertDialog(
            onDismissRequest = { dialogState = DialogState.NONE },
            title = { Text("Stock Restored", style = MaterialTheme.typography.headlineSmallEmphasized) },
            text = {
                Text("All overlays removed. Soft-reboot Zygote now?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        dialogState = DialogState.NONE
                        runAction { RootHelper.softRebootZygote(::appendLog) }
                    },
                    shapes = ButtonDefaults.shapes(),
                ) {
                    Text("Soft Reboot")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { dialogState = DialogState.NONE },
                    shapes = ButtonDefaults.shapes(),
                ) {
                    Text("Later")
                }
            },
        )
    }
}

private data class ToolActionItem(
    val icon: ImageVector,
    val title: String,
    val onClick: () -> Unit,
)

@Composable
private fun HomeTab(
    status: SystemStatus,
    heroShape: AppHeroShape,
    enabledCount: Int,
    totalCount: Int,
    busy: Boolean,
    onApply: () -> Unit,
    onPhotosMethod: () -> Unit,
    onAodWallpaperSpoof: () -> Unit,
    onRestore: () -> Unit,
    onOpenAppearance: () -> Unit,
    onOpenNotificationHighlightsPopup: () -> Unit,
    onOpenHorizontalLockPopup: () -> Unit,
    onOpenAicorePopup: () -> Unit,
    onOpenCreativeStudio: () -> Unit,
    onOpenAppleSharePopup: () -> Unit,
    onOpenMediaSetup: () -> Unit,
    onPickApkSpoof: () -> Unit,
    onOnboarding: () -> Unit,
    onNudgeSettings: () -> Unit,
    onAodSettings: () -> Unit,
    onCheckAiVersion: () -> Unit,
) {
    val toolItems = listOf(
        ToolActionItem(
            icon = Icons.Rounded.Palette,
            title = "Appearance & Setup",
            onClick = onOpenAppearance,
        ),
        ToolActionItem(
            icon = Icons.Rounded.Memory,
            title = "AICore & Gemini Nano",
            onClick = onOpenAicorePopup,
        ),
        ToolActionItem(
            icon = Icons.Rounded.NotificationsActive,
            title = "Notification Highlights",
            onClick = onOpenNotificationHighlightsPopup,
        ),
        ToolActionItem(
            icon = Icons.Rounded.ScreenRotation,
            title = "Horizontal Lock (Camera)",
            onClick = onOpenHorizontalLockPopup,
        ),
        ToolActionItem(
            icon = Icons.Rounded.Share,
            title = "Share with Apple",
            onClick = onOpenAppleSharePopup,
        ),
        ToolActionItem(
            icon = Icons.Rounded.Brush,
            title = "Creative Studio",
            onClick = onOpenCreativeStudio,
        ),
        ToolActionItem(
            icon = Icons.Rounded.VideoSettings,
            title = "Audio Eraser & FanCam",
            onClick = onOpenMediaSetup,
        ),
        ToolActionItem(
            icon = Icons.Rounded.InstallMobile,
            title = "Install APK",
            onClick = onPickApkSpoof,
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val heroContainer = if (status.mountActive) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        }
        val heroContent = if (status.mountActive) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        }
        Card(
            colors = CardDefaults.cardColors(
                containerColor = heroContainer,
                contentColor = heroContent,
            ),
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Surface(
                        shape = heroPolygonFor(heroShape).toShape(),
                        color = if (status.mountActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        },
                        contentColor = if (status.mountActive) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        modifier = Modifier.size(48.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (status.mountActive) {
                                    Icons.Rounded.Verified
                                } else {
                                    Icons.Rounded.PhoneAndroid
                                },
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = status.device.displayName,
                            style = MaterialTheme.typography.titleLargeEmphasized,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        val uiLabel = status.device.oneUiLabel.ifBlank { "One UI" }
                        val stateLabel = when {
                            status.mountActive -> "Enabled"
                            status.rootGranted -> "Stock"
                            else -> "Root required"
                        }
                        Text(
                            text = "$uiLabel · $stateLabel",
                            style = MaterialTheme.typography.labelLarge,
                            color = heroContent.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                HorizontalDivider(color = heroContent.copy(alpha = 0.14f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatusMetric(
                        label = "AI Version",
                        value = status.aiVersion,
                        modifier = Modifier.weight(1f),
                    )
                    StatusMetric(
                        label = "Now Nudge",
                        value = status.nudgeVersion,
                        modifier = Modifier.weight(1f),
                    )
                    StatusMetric(
                        label = "Selected",
                        value = "$enabledCount/$totalCount",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onApply,
                enabled = !busy && enabledCount > 0,
                shapes = ButtonDefaults.shapesFor(ButtonDefaults.MediumContainerHeight),
                contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = ButtonDefaults.MediumContainerHeight),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bolt,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Enable & Reboot ($enabledCount)",
                    style = MaterialTheme.typography.titleMediumEmphasized,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FilledTonalButton(
                    onClick = onPhotosMethod,
                    enabled = !busy,
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Photos", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                FilledTonalButton(
                    onClick = onAodWallpaperSpoof,
                    enabled = !busy,
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Smartphone,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("AOD", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle("Modules")
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                toolItems.forEachIndexed { index, item ->
                    SegmentedListItem(
                        onClick = item.onClick,
                        shapes = ListItemDefaults.segmentedShapes(index = index, count = toolItems.size),
                        colors = ListItemDefaults.segmentedColors(),
                        enabled = !busy,
                        leadingContent = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                            )
                        },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                    ) {
                        Text(item.title, style = MaterialTheme.typography.titleSmallEmphasized)
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle("Shortcuts")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilledTonalButton(
                        onClick = onOnboarding,
                        enabled = !busy,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Onboarding", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    FilledTonalButton(
                        onClick = onNudgeSettings,
                        enabled = !busy,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Nudge Settings", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilledTonalButton(
                        onClick = onAodSettings,
                        enabled = !busy,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("AOD Settings", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    FilledTonalButton(
                        onClick = onCheckAiVersion,
                        enabled = !busy,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Check Version", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onRestore,
            enabled = !busy,
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Rounded.RestartAlt,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text("Restore Stock")
        }
    }
}

private fun featureGroupIcon(group: FeatureGroup): ImageVector = when (group) {
    FeatureGroup.CORE -> Icons.Rounded.Stars
    FeatureGroup.AICORE -> Icons.Rounded.Memory
    FeatureGroup.NOTIFICATION_HIGHLIGHTS -> Icons.Rounded.NotificationsActive
    FeatureGroup.AI_SELECT -> Icons.Rounded.AutoAwesome
    FeatureGroup.NEXT_GEN_BIXBY -> Icons.Rounded.Assistant
    FeatureGroup.NATURAL_AI_SEARCH -> Icons.AutoMirrored.Rounded.ManageSearch
    FeatureGroup.LIVE_AUDIO_ERASER -> Icons.Rounded.RecordVoiceOver
    FeatureGroup.NOTE_HEALTH_ASSIST -> Icons.Rounded.MonitorHeart
    FeatureGroup.CREATIVE_STUDIO -> Icons.Rounded.Brush
    FeatureGroup.AOD_WALLPAPER -> Icons.Rounded.Smartphone
    FeatureGroup.PHOTO_AMBIENT_WALLPAPER -> Icons.Rounded.Wallpaper
    FeatureGroup.VIVIDNESS_SLIDER -> Icons.Rounded.Palette
    FeatureGroup.ADAPTIVE_COLOR_TONE -> Icons.Rounded.Tonality
    FeatureGroup.SHARE_WITH_APPLE -> Icons.Rounded.Share
    FeatureGroup.SCAM_CALL_SCREENING -> Icons.Rounded.VerifiedUser
    FeatureGroup.CALL_BRIEF -> Icons.Rounded.Call
    FeatureGroup.PHOTO_ASSIST -> Icons.Rounded.PhotoLibrary
    FeatureGroup.SUITE -> Icons.Rounded.EditNote
    FeatureGroup.AUDIO -> Icons.Rounded.GraphicEq
    FeatureGroup.FANCAM -> Icons.Rounded.Videocam
    FeatureGroup.PRO_VIDEO_LOG_APV -> Icons.Rounded.HighQuality
    FeatureGroup.HORIZONTAL_LOCK -> Icons.Rounded.ScreenRotation
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmallEmphasized,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

@Composable
private fun FeaturesTab(
    enabledGroups: Map<FeatureGroup, Boolean>,
    enableOfficialSpoof: Boolean,
    busy: Boolean,
    onToggle: (FeatureGroup, Boolean) -> Unit,
    onToggleOfficialSpoof: (Boolean) -> Unit,
    onOpenNotificationHighlightsPopup: () -> Unit,
    onOpenAppleSharePopup: () -> Unit,
    onOpenHorizontalLockPopup: () -> Unit,
    onOpenAicorePopup: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle("Features")
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                val groups = FeatureGroup.entries
                groups.forEachIndexed { index, group ->
                    val checked = enabledGroups[group] == true
                    SegmentedListItem(
                        checked = checked,
                        onCheckedChange = { onToggle(group, it) },
                        shapes = ListItemDefaults.segmentedShapes(index = index, count = groups.size),
                        colors = ListItemDefaults.segmentedColors(),
                        enabled = !busy,
                        leadingContent = {
                            Icon(
                                imageVector = featureGroupIcon(group),
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                            )
                        },
                        supportingContent = {
                            Text(
                                text = group.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        trailingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                val setupAction: (() -> Unit)? = when (group) {
                                    FeatureGroup.AICORE -> onOpenAicorePopup
                                    FeatureGroup.NOTIFICATION_HIGHLIGHTS -> onOpenNotificationHighlightsPopup
                                    FeatureGroup.SHARE_WITH_APPLE -> onOpenAppleSharePopup
                                    FeatureGroup.HORIZONTAL_LOCK -> onOpenHorizontalLockPopup
                                    else -> null
                                }
                                if (setupAction != null) {
                                    FilledTonalButton(
                                        onClick = setupAction,
                                        enabled = !busy,
                                        shapes = ButtonDefaults.shapes(),
                                    ) {
                                        Text("Setup")
                                    }
                                }
                                Switch(
                                    checked = checked,
                                    onCheckedChange = null,
                                    enabled = !busy,
                                )
                            }
                        },
                    ) {
                        Text(group.title, style = MaterialTheme.typography.titleSmallEmphasized)
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle("Options")
            SegmentedListItem(
                checked = enableOfficialSpoof,
                onCheckedChange = onToggleOfficialSpoof,
                shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
                colors = ListItemDefaults.segmentedColors(),
                enabled = !busy,
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.Security,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                    )
                },
                trailingContent = {
                    Switch(
                        checked = enableOfficialSpoof,
                        onCheckedChange = null,
                        enabled = !busy,
                    )
                },
            ) {
                Text("Official Release Spoof", style = MaterialTheme.typography.titleSmallEmphasized)
            }
        }
    }
}

@Composable
private fun StatusCheckRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun PopupCheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun LogTab(logText: String) {
    val logScroll = rememberScrollState()
    LaunchedEffect(logText) {
        logScroll.animateScrollTo(logScroll.maxValue)
    }
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        SelectionContainer {
            Text(
                text = logText,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.5.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(logScroll)
                    .padding(18.dp),
            )
        }
    }
}

@Composable
private fun StatusMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmallEmphasized,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
