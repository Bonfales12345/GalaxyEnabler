package com.bonfales.galaxyenabler

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

class PixelHaptics(
    context: Context,
    private val view: View,
    private val enabled: () -> Boolean,
) {
    private val vibrator: Vibrator? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }.getOrNull()?.takeIf { runCatching { it.hasVibrator() }.getOrDefault(false) }

    private fun playComposition(vararg steps: Pair<Int, Pair<Float, Int>>, fallbackConstant: Int) {
        if (!enabled()) return
        val vib = vibrator
        if (vib != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val ids = steps.map { it.first }.toIntArray()
            val supported = runCatching { vib.areAllPrimitivesSupported(*ids) }.getOrDefault(false)
            if (supported) {
                val ok = runCatching {
                    val comp = VibrationEffect.startComposition()
                    for ((primitive, scaleAndDelay) in steps) {
                        comp.addPrimitive(primitive, scaleAndDelay.first, scaleAndDelay.second)
                    }
                    vib.vibrate(comp.compose())
                }.isSuccess
                if (ok) return
            }
        }
        runCatching {
            view.performHapticFeedback(fallbackConstant)
        }
    }

    fun bounce() {
        val secondPrimitive = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibrationEffect.Composition.PRIMITIVE_THUD
        } else {
            VibrationEffect.Composition.PRIMITIVE_LOW_TICK
        }
        playComposition(
            VibrationEffect.Composition.PRIMITIVE_LOW_TICK to (0.72f to 0),
            secondPrimitive to (0.55f to 22),
            fallbackConstant = HapticFeedbackConstants.CLOCK_TICK,
        )
    }

    fun tick() {
        playComposition(
            VibrationEffect.Composition.PRIMITIVE_TICK to (0.60f to 0),
            fallbackConstant = HapticFeedbackConstants.CLOCK_TICK,
        )
    }

    fun click() {
        playComposition(
            VibrationEffect.Composition.PRIMITIVE_CLICK to (0.85f to 0),
            fallbackConstant = HapticFeedbackConstants.CONTEXT_CLICK,
        )
    }

    fun confirm() {
        val confirmFallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.CONFIRM
        } else {
            HapticFeedbackConstants.CONTEXT_CLICK
        }
        playComposition(
            VibrationEffect.Composition.PRIMITIVE_TICK to (0.60f to 0),
            VibrationEffect.Composition.PRIMITIVE_CLICK to (0.95f to 48),
            fallbackConstant = confirmFallback,
        )
    }
}

@Composable
fun rememberPixelHaptics(enabled: Boolean): PixelHaptics {
    val context = LocalContext.current
    val view = LocalView.current
    val enabledState = rememberUpdatedState(enabled)
    return remember(context, view) {
        PixelHaptics(context = context, view = view, enabled = { enabledState.value })
    }
}
