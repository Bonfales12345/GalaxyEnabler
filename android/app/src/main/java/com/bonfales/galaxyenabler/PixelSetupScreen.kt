@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
)

package com.bonfales.galaxyenabler

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toPath
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import com.bonfales.galaxyenabler.ui.theme.palettePreviewColors
import com.bonfales.galaxyenabler.ui.theme.resolveDynamicColorScheme
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

fun heroPolygonFor(shape: AppHeroShape): RoundedPolygon = when (shape) {
    AppHeroShape.COOKIE -> MaterialShapes.Cookie9Sided
    AppHeroShape.CLOVER -> MaterialShapes.Clover4Leaf
    AppHeroShape.BURST -> MaterialShapes.SoftBurst
    AppHeroShape.FLOWER -> MaterialShapes.Flower
    AppHeroShape.GEM -> MaterialShapes.Gem
}

private data class BottomWaveProfile(
    val leftY: Float,
    val peakX: Float,
    val peakY: Float,
    val midX: Float,
    val midY: Float,
    val rightY: Float,
)

private val WELCOME_WAVE_PROFILES = listOf(
    BottomWaveProfile(leftY = 0.37f, peakX = 0.16f, peakY = 0.345f, midX = 0.48f, midY = 0.465f, rightY = 0.535f),
    BottomWaveProfile(leftY = 0.44f, peakX = 0.26f, peakY = 0.39f, midX = 0.54f, midY = 0.47f, rightY = 0.525f),
    BottomWaveProfile(leftY = 0.41f, peakX = 0.20f, peakY = 0.395f, midX = 0.50f, midY = 0.49f, rightY = 0.56f),
    BottomWaveProfile(leftY = 0.39f, peakX = 0.23f, peakY = 0.36f, midX = 0.52f, midY = 0.475f, rightY = 0.54f),
)

private val BANNER_WAVE_PROFILES = listOf(
    BottomWaveProfile(leftY = 0.58f, peakX = 0.20f, peakY = 0.53f, midX = 0.52f, midY = 0.66f, rightY = 0.73f),
    BottomWaveProfile(leftY = 0.64f, peakX = 0.28f, peakY = 0.57f, midX = 0.56f, midY = 0.65f, rightY = 0.70f),
    BottomWaveProfile(leftY = 0.60f, peakX = 0.22f, peakY = 0.56f, midX = 0.50f, midY = 0.68f, rightY = 0.75f),
    BottomWaveProfile(leftY = 0.56f, peakX = 0.25f, peakY = 0.52f, midX = 0.54f, midY = 0.66f, rightY = 0.72f),
)

private fun lerpFloat(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction

private fun smoothStep(t: Float): Float {
    val c = t.coerceIn(0f, 1f)
    return c * c * (3f - 2f * c)
}

private fun cubicBezierCoord(p0: Float, p1: Float, p2: Float, p3: Float, t: Float): Float {
    val u = 1f - t
    val uu = u * u
    val tt = t * t
    return uu * u * p0 + 3f * uu * t * p1 + 3f * u * tt * p2 + tt * t * p3
}

private fun solveCubicBezierYForX(
    targetX: Float,
    x0: Float,
    y0: Float,
    cx1: Float,
    cy1: Float,
    cx2: Float,
    cy2: Float,
    x3: Float,
    y3: Float,
): Float {
    var lo = 0f
    var hi = 1f
    repeat(8) {
        val mid = 0.5f * (lo + hi)
        if (cubicBezierCoord(x0, cx1, cx2, x3, mid) < targetX) lo = mid else hi = mid
    }
    return cubicBezierCoord(y0, cy1, cy2, y3, 0.5f * (lo + hi))
}

private class WaveControlPoints {
    var y0 = 0f
    var x1 = 0f
    var y1 = 0f
    var x2 = 0f
    var y2 = 0f
    var y3 = 0f
    var seg1C1x = 0f
    var seg1C1y = 0f
    var seg1C2x = 0f
    var seg1C2y = 0f
    var seg2C1x = 0f
    var seg2C1y = 0f
    var seg2C2x = 0f
    var seg2C2y = 0f
    var seg3C1x = 0f
    var seg3C1y = 0f
    var seg3C2x = 0f
    var seg3C2y = 0f

    fun populate(
        w: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        y3: Float,
    ) {
        this.y0 = y0
        this.x1 = x1
        this.y1 = y1
        this.x2 = x2
        this.y2 = y2
        this.y3 = y3

        seg1C1x = x1 * 0.45f
        seg1C1y = y0
        seg1C2x = x1 * 0.62f
        seg1C2y = y1

        val s2 = (y3 - y1) / (w - x1).coerceAtLeast(1f)
        seg2C1x = x1 + (x2 - x1) * 0.42f
        seg2C1y = y1
        val seg2InDx = (x2 - x1) * 0.32f
        seg2C2x = x2 - seg2InDx
        seg2C2y = y2 - s2 * seg2InDx

        val seg3OutDx = (w - x2) * 0.35f
        seg3C1x = x2 + seg3OutDx
        seg3C1y = y2 + s2 * seg3OutDx
        seg3C2x = x2 + (w - x2) * 0.68f
        seg3C2y = y3
    }
}

private fun evalWaveYAtX(
    x: Float,
    w: Float,
    y0: Float,
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float,
    y3: Float,
    scratch: WaveControlPoints = WaveControlPoints(),
): Float {
    scratch.populate(w = w, y0 = y0, x1 = x1, y1 = y1, x2 = x2, y2 = y2, y3 = y3)
    val clampedX = x.coerceIn(0f, w)
    return when {
        clampedX <= x1 -> solveCubicBezierYForX(
            clampedX,
            0f,
            scratch.y0,
            scratch.seg1C1x,
            scratch.seg1C1y,
            scratch.seg1C2x,
            scratch.seg1C2y,
            scratch.x1,
            scratch.y1,
        )
        clampedX <= x2 -> solveCubicBezierYForX(
            clampedX,
            scratch.x1,
            scratch.y1,
            scratch.seg2C1x,
            scratch.seg2C1y,
            scratch.seg2C2x,
            scratch.seg2C2y,
            scratch.x2,
            scratch.y2,
        )
        else -> solveCubicBezierYForX(
            clampedX,
            scratch.x2,
            scratch.y2,
            scratch.seg3C1x,
            scratch.seg3C1y,
            scratch.seg3C2x,
            scratch.seg3C2y,
            w,
            scratch.y3,
        )
    }
}

private const val PERIMETER_SAMPLES = 64
private const val WAVE_SAMPLES = 64

private fun waveDeformationNorm(
    normX: Float,
    dentCenterX: Float,
    waveDent: Float,
    waveRipple: Float,
): Float {
    val dx = (normX - dentCenterX) / 0.16f
    val g = exp(-0.5f * dx * dx)
    return waveDent * g - waveRipple * 0.30f * (1f - g)
}

private fun evalLiveWaveNormY(
    normX: Float,
    y0: Float,
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float,
    y3: Float,
    dentCenterX: Float,
    waveDent: Float,
    waveRipple: Float,
    scratch: WaveControlPoints,
): Float {
    val baseY = evalWaveYAtX(
        x = normX,
        w = 1f,
        y0 = y0,
        x1 = x1,
        y1 = y1,
        x2 = x2,
        y2 = y2,
        y3 = y3,
        scratch = scratch,
    )
    return baseY + waveDeformationNorm(
        normX = normX,
        dentCenterX = dentCenterX,
        waveDent = waveDent,
        waveRipple = waveRipple,
    )
}

private fun sampleUnitPerimeter(
    morph: Morph,
    morphFraction: Float,
    scratchPath: Path,
    pathMeasure: PathMeasure,
    outPx: FloatArray,
    outPy: FloatArray,
) {
    scratchPath.rewind()
    morph.toPath(morphFraction.coerceIn(0f, 1f), path = scratchPath)
    val center = scratchPath.getBounds().center
    scratchPath.translate(Offset(-center.x, -center.y))
    pathMeasure.setPath(scratchPath, false)
    val totalLen = pathMeasure.length
    if (totalLen <= 0f) {
        val count = outPx.size
        for (i in 0 until count) {
            val angle = (2.0 * PI * i / count).toFloat()
            outPx[i] = 0.5f * cos(angle)
            outPy[i] = 0.5f * sin(angle)
        }
        return
    }
    val count = outPx.size
    for (i in 0 until count) {
        val dist = totalLen * (i.toFloat() / count)
        val pos = pathMeasure.getPosition(dist)
        if (pos.isSpecified) {
            outPx[i] = pos.x
            outPy[i] = pos.y
        } else {
            val angle = (2.0 * PI * i / count).toFloat()
            outPx[i] = 0.5f * cos(angle)
            outPy[i] = 0.5f * sin(angle)
        }
    }
}

private class BouncePhysicsState(bannerMode: Boolean) {
    var initialized = false
    var stageWidth = 1080f
    var stageHeight = if (bannerMode) 540f else 2340f
    var posX = 0f
    var posY = 0f
    var velX = 0f
    var velY = 0f
    var rotation = 0f
    var angVel = 0f
    var scaleX = 1f
    var scaleY = 1f
    var waveDent = 0f
    var waveDentVel = 0f
    var waveRipple = 0f
    var waveRippleVel = 0f
    var dentCenterX = if (bannerMode) 0.62f else 0.65f
    var wasInContact = false
    var boostBounceActive = false
    var bounceCount = 0
    var topMorphT = 0f
    var isTopMorphing = false
    var bottomMorphT = 0f
    var isBottomMorphing = false
    var impactXTarget = 0f
    var lastContactCenterY = 0.5f

    val unitPx = FloatArray(PERIMETER_SAMPLES)
    val unitPy = FloatArray(PERIMETER_SAMPLES)
    var perimeterReady = false
}

@Composable
private fun PixelBouncingShapesStage(
    heroShape: AppHeroShape,
    isDark: Boolean,
    amoledBlack: Boolean,
    expressiveMotion: Boolean,
    haptics: PixelHaptics,
    bannerMode: Boolean = false,
    autoHaptics: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val topSequence = remember(heroShape) {
        val primary = heroPolygonFor(heroShape)
        val rest = listOf(
            MaterialShapes.Cookie9Sided,
            MaterialShapes.SoftBurst,
            MaterialShapes.Clover4Leaf,
            MaterialShapes.Flower,
            MaterialShapes.VerySunny,
            MaterialShapes.Cookie12Sided,
            MaterialShapes.Gem,
        ).filter { it != primary }
        listOf(primary) + rest
    }
    val profiles = if (bannerMode) BANNER_WAVE_PROFILES else WELCOME_WAVE_PROFILES

    val scheme = MaterialTheme.colorScheme
    val targetBackdropColor = when {
        isDark && amoledBlack -> Color.Black
        isDark -> lerp(Color(0xFF0A0B12), scheme.surfaceContainerLowest, 0.45f)
        else -> scheme.surfaceContainerLowest
    }
    val targetBottomShapeColor = if (isDark) {
        lerp(targetBackdropColor, lerp(scheme.primaryContainer, scheme.primary, 0.38f), 0.34f)
    } else {
        lerp(scheme.surface, scheme.primaryContainer, 0.50f)
    }
    val targetTopShapeColor = if (isDark) {
        lerp(targetBackdropColor, lerp(scheme.primaryContainer, scheme.primary, 0.46f), 0.58f)
    } else {
        lerp(scheme.primaryContainer, scheme.primary, 0.30f)
    }

    val backdropColor by animateColorAsState(
        targetValue = targetBackdropColor,
        animationSpec = tween(durationMillis = 280),
        label = "stage_backdrop",
    )
    val bottomShapeColor by animateColorAsState(
        targetValue = targetBottomShapeColor,
        animationSpec = tween(durationMillis = 280),
        label = "stage_bottom",
    )
    val topShapeColor by animateColorAsState(
        targetValue = targetTopShapeColor,
        animationSpec = tween(durationMillis = 280),
        label = "stage_top",
    )

    var topIndex by remember(heroShape) { mutableIntStateOf(0) }
    var bottomIndex by remember(bannerMode) { mutableIntStateOf(0) }

    val safeTopIndex = topIndex % topSequence.size
    val nextTopIndex = (safeTopIndex + 1) % topSequence.size
    val topMorph = remember(safeTopIndex, topSequence) {
        Morph(topSequence[safeTopIndex], topSequence[nextTopIndex])
    }

    val state = remember(heroShape, bannerMode) { BouncePhysicsState(bannerMode) }
    var frameTick by remember { mutableLongStateOf(0L) }

    val currentTopMorph by rememberUpdatedState(topMorph)
    val currentExpressiveMotion by rememberUpdatedState(expressiveMotion)
    val currentAutoHaptics by rememberUpdatedState(autoHaptics)

    val topPath = remember { Path() }
    val topMatrix = remember { Matrix() }
    val bottomPath = remember { Path() }
    val sampleScratchPath = remember { Path() }
    val sampleMeasure = remember { PathMeasure() }
    val physicsWaveScratch = remember { WaveControlPoints() }
    val drawWaveScratch = remember { WaveControlPoints() }
    val waveSampleY = remember { FloatArray(WAVE_SAMPLES + 1) }

    LaunchedEffect(heroShape, bannerMode) {
        val apexX = if (bannerMode) 0.68f else 0.78f
        val apexY = if (bannerMode) 0.23f else 0.065f
        val baseImpactX = if (bannerMode) 0.62f else 0.65f

        state.posX = apexX
        state.posY = apexY
        state.velX = (baseImpactX - apexX) * 1.6f
        state.velY = 0f
        state.rotation = 0f
        state.angVel = 28f
        state.scaleX = 1f
        state.scaleY = 1f
        state.waveDent = 0f
        state.waveDentVel = 0f
        state.waveRipple = 0f
        state.waveRippleVel = 0f
        state.dentCenterX = baseImpactX
        state.wasInContact = false
        state.boostBounceActive = false
        state.bounceCount = 0
        state.topMorphT = 0f
        state.isTopMorphing = false
        state.bottomMorphT = 0f
        state.isBottomMorphing = false
        state.impactXTarget = baseImpactX
        state.perimeterReady = false
        state.initialized = true

        var lastNanos = 0L
        while (isActive) {
            withFrameNanos { nowNanos ->
                if (lastNanos == 0L) {
                    lastNanos = nowNanos
                    return@withFrameNanos
                }
                val dt = ((nowNanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.028f)
                lastNanos = nowNanos

                val w = state.stageWidth.coerceAtLeast(1f)
                val h = state.stageHeight.coerceAtLeast(1f)
                val topDiameter = if (bannerMode) minOf(h * 0.40f, w * 0.26f) else minOf(w * 0.74f, h * 0.35f)
                val contactRadiusNorm = (topDiameter * 0.46f) / h
                val gravity = if (currentExpressiveMotion) 2.25f else 1.70f

                val morphFrac = if (state.isTopMorphing) smoothStep(state.topMorphT) else 0f
                sampleUnitPerimeter(
                    morph = currentTopMorph,
                    morphFraction = morphFrac,
                    scratchPath = sampleScratchPath,
                    pathMeasure = sampleMeasure,
                    outPx = state.unitPx,
                    outPy = state.unitPy,
                )
                state.perimeterReady = true

                val curWave = profiles[bottomIndex % profiles.size]
                val nextWave = profiles[(bottomIndex + 1) % profiles.size]
                val bm = if (state.isBottomMorphing) smoothStep(state.bottomMorphT) else 0f

                val normY0 = lerpFloat(curWave.leftY, nextWave.leftY, bm)
                val normX1 = lerpFloat(curWave.peakX, nextWave.peakX, bm)
                val normY1 = lerpFloat(curWave.peakY, nextWave.peakY, bm)
                val normX2 = lerpFloat(curWave.midX, nextWave.midX, bm)
                val normY2 = lerpFloat(curWave.midY, nextWave.midY, bm)
                val normY3 = lerpFloat(curWave.rightY, nextWave.rightY, bm)

                val subDt = dt * 0.5f
                repeat(2) {
                    val rad = (state.rotation * (PI / 180.0)).toFloat()
                    val cosR = cos(rad)
                    val sinR = sin(rad)
                    val cxPx = state.posX * w
                    val cyPx = state.posY * h

                    var maxPenNorm = Float.NEGATIVE_INFINITY
                    var deepestNormX = state.posX
                    for (k in 0 until PERIMETER_SAMPLES) {
                        val lx = state.unitPx[k] * topDiameter
                        val ly = state.unitPy[k] * topDiameter
                        val wx = cxPx + lx * cosR - ly * sinR
                        val wy = cyPx + lx * sinR + ly * cosR
                        val sampleNormX = (wx / w).coerceIn(0f, 1f)
                        val baseSurfaceNormY = evalWaveYAtX(
                            x = sampleNormX,
                            w = 1f,
                            y0 = normY0,
                            x1 = normX1,
                            y1 = normY1,
                            x2 = normX2,
                            y2 = normY2,
                            y3 = normY3,
                            scratch = physicsWaveScratch,
                        )
                        val pen = (wy / h) - baseSurfaceNormY
                        if (pen > maxPenNorm) {
                            maxPenNorm = pen
                            deepestNormX = sampleNormX
                        }
                    }

                    val contactNormX = deepestNormX.coerceIn(0.08f, 0.92f)
                    state.lastContactCenterY = (state.posY - maxPenNorm).coerceIn(0.08f, 0.92f)

                    val dentAcc: Float
                    val inContact = maxPenNorm > 0f
                    if (inContact) {
                        if (!state.wasInContact) {
                            state.dentCenterX = contactNormX
                        } else {
                            state.dentCenterX += (contactNormX - state.dentCenterX) * 0.25f
                        }

                        val accY = gravity - 860f * maxPenNorm
                        val prevVelY = state.velY
                        state.velY += accY * subDt
                        state.posY += state.velY * subDt

                        dentAcc = 600f * maxPenNorm - 190f * state.waveDent - 13f * state.waveDentVel

                        val yLeft = evalWaveYAtX(
                            x = (contactNormX - 0.015f).coerceAtLeast(0f),
                            w = 1f,
                            y0 = normY0,
                            x1 = normX1,
                            y1 = normY1,
                            x2 = normX2,
                            y2 = normY2,
                            y3 = normY3,
                            scratch = physicsWaveScratch,
                        )
                        val yRight = evalWaveYAtX(
                            x = (contactNormX + 0.015f).coerceAtMost(1f),
                            w = 1f,
                            y0 = normY0,
                            x1 = normX1,
                            y1 = normY1,
                            x2 = normX2,
                            y2 = normY2,
                            y3 = normY3,
                            scratch = physicsWaveScratch,
                        )
                        val slope = (yRight - yLeft) / 0.03f
                        state.velX += slope * 1.4f * subDt

                        if (prevVelY > 0f && state.velY <= 0f) {
                            if (currentAutoHaptics || state.boostBounceActive) {
                                haptics.bounce()
                            }

                            if (state.isTopMorphing) {
                                topIndex = (topIndex + 1) % topSequence.size
                            }
                            state.isTopMorphing = true
                            state.topMorphT = 0f

                            if (state.isBottomMorphing) {
                                bottomIndex = (bottomIndex + 1) % profiles.size
                            }
                            state.isBottomMorphing = true
                            state.bottomMorphT = 0f

                            state.bounceCount++
                            state.impactXTarget = baseImpactX + when (state.bounceCount % 3) {
                                0 -> -0.03f
                                1 -> 0.03f
                                else -> 0f
                            }
                            val leverArm = state.posX - contactNormX
                            state.angVel = (leverArm * 520f + slope * 95f + if (state.bounceCount % 2 == 0) 48f else -36f)
                                .coerceIn(-110f, 110f)
                        }
                        state.wasInContact = true
                    } else {
                        if (state.wasInContact && state.velY < 0f) {
                            val targetApex = if (state.boostBounceActive) {
                                (apexY - 0.045f).coerceAtLeast(0.02f)
                            } else {
                                apexY
                            }
                            val rise = (state.posY - targetApex).coerceAtLeast(0.08f)
                            state.velY = -sqrt(2f * gravity * rise)
                            state.boostBounceActive = false
                        }
                        state.wasInContact = false

                        state.velY += gravity * subDt
                        state.posY += state.velY * subDt

                        dentAcc = -190f * state.waveDent - 13f * state.waveDentVel
                    }

                    state.waveDentVel += dentAcc * subDt
                    state.waveDent += state.waveDentVel * subDt

                    val rippleAcc = 140f * (state.waveDent - state.waveRipple) - 11f * state.waveRippleVel
                    state.waveRippleVel += rippleAcc * subDt
                    state.waveRipple += state.waveRippleVel * subDt

                    val desiredX = if (state.velY < 0f) apexX else state.impactXTarget
                    val targetVelX = (desiredX - state.posX) * 2.4f
                    state.velX += (targetVelX - state.velX) * minOf(1f, subDt * 5.5f)
                    state.posX = (state.posX + state.velX * subDt).coerceIn(0.45f, 0.84f)

                    state.angVel *= (1f - 0.35f * subDt)
                    state.rotation = (state.rotation + state.angVel * subDt) % 360f

                    val targetSy = if (inContact) {
                        (1f - (maxPenNorm / contactRadiusNorm.coerceAtLeast(0.05f)) * 0.85f).coerceIn(0.73f, 1f)
                    } else {
                        1f + (abs(state.velY) / 1.35f).coerceIn(0f, 1f) * 0.075f
                    }
                    state.scaleY += (targetSy - state.scaleY) * minOf(1f, subDt * 42f)
                    state.scaleX = 1f / state.scaleY
                }

                if (state.isTopMorphing) {
                    state.topMorphT += dt / 0.46f
                    if (state.topMorphT >= 1f) {
                        state.topMorphT = 0f
                        state.isTopMorphing = false
                        topIndex = (topIndex + 1) % topSequence.size
                    }
                }
                if (state.isBottomMorphing) {
                    state.bottomMorphT += dt / 0.58f
                    if (state.bottomMorphT >= 1f) {
                        state.bottomMorphT = 0f
                        state.isBottomMorphing = false
                        bottomIndex = (bottomIndex + 1) % profiles.size
                    }
                }

                frameTick++
            }
        }
    }

    Canvas(
        modifier = modifier.pointerInput(bannerMode) {
            detectTapGestures { tapOffset ->
                val w = state.stageWidth.coerceAtLeast(1f)
                val h = state.stageHeight.coerceAtLeast(1f)
                state.boostBounceActive = true
                if (state.posY < state.lastContactCenterY) {
                    state.velY = maxOf(state.velY + 0.85f, 1.35f)
                }
                if ((tapOffset.y / h) >= state.lastContactCenterY - 0.05f) {
                    state.dentCenterX = (tapOffset.x / w).coerceIn(0.1f, 0.9f)
                    state.waveDentVel += 0.45f
                }
                haptics.tick()
            }
        },
    ) {
        if (frameTick < 0L) return@Canvas

        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas
        state.stageWidth = w
        state.stageHeight = h

        drawRect(color = backdropColor)

        val curWave = profiles[bottomIndex % profiles.size]
        val nextWave = profiles[(bottomIndex + 1) % profiles.size]
        val bm = if (state.isBottomMorphing) smoothStep(state.bottomMorphT) else 0f

        val normY0 = lerpFloat(curWave.leftY, nextWave.leftY, bm)
        val normX1 = lerpFloat(curWave.peakX, nextWave.peakX, bm)
        val normY1 = lerpFloat(curWave.peakY, nextWave.peakY, bm)
        val normX2 = lerpFloat(curWave.midX, nextWave.midX, bm)
        val normY2 = lerpFloat(curWave.midY, nextWave.midY, bm)
        val normY3 = lerpFloat(curWave.rightY, nextWave.rightY, bm)

        for (i in 0..WAVE_SAMPLES) {
            val nx = i.toFloat() / WAVE_SAMPLES
            waveSampleY[i] = evalLiveWaveNormY(
                normX = nx,
                y0 = normY0,
                x1 = normX1,
                y1 = normY1,
                x2 = normX2,
                y2 = normY2,
                y3 = normY3,
                dentCenterX = state.dentCenterX,
                waveDent = state.waveDent,
                waveRipple = state.waveRipple,
                scratch = drawWaveScratch,
            ) * h
        }

        val stepX = w / WAVE_SAMPLES
        bottomPath.rewind()
        bottomPath.moveTo(0f, h)
        bottomPath.lineTo(0f, waveSampleY[0])
        for (i in 0 until WAVE_SAMPLES) {
            val p0y = waveSampleY[(i - 1).coerceAtLeast(0)]
            val p1y = waveSampleY[i]
            val p2y = waveSampleY[i + 1]
            val p3y = waveSampleY[(i + 2).coerceAtMost(WAVE_SAMPLES)]

            val x1p = i * stepX
            val x2p = (i + 1) * stepX
            val c1x = x1p + stepX / 3f
            val c1y = p1y + (p2y - p0y) / 6f
            val c2x = x2p - stepX / 3f
            val c2y = p2y - (p3y - p1y) / 6f
            bottomPath.cubicTo(c1x, c1y, c2x, c2y, x2p, p2y)
        }
        bottomPath.lineTo(w, h)
        bottomPath.close()
        drawPath(path = bottomPath, color = bottomShapeColor)

        val topDiameter = if (bannerMode) minOf(h * 0.40f, w * 0.26f) else minOf(w * 0.74f, h * 0.35f)
        val morphFraction = if (state.isTopMorphing) smoothStep(state.topMorphT) else 0f

        topPath.rewind()
        topMorph.toPath(morphFraction, path = topPath)
        topMatrix.reset()
        topMatrix.scale(topDiameter, topDiameter)
        topPath.transform(topMatrix)
        val topCenter = topPath.getBounds().center
        topPath.translate(Offset(-topCenter.x, -topCenter.y))

        val sx = state.scaleX
        val sy = state.scaleY
        val cx = (if (state.initialized) state.posX else if (bannerMode) 0.68f else 0.78f) * w
        val cy = (if (state.initialized) state.posY else if (bannerMode) 0.23f else 0.065f) * h

        val drawCy = if (state.perimeterReady) {
            val rad = (state.rotation * (PI / 180.0)).toFloat()
            val cosR = cos(rad)
            val sinR = sin(rad)
            var maxOverlapPx = Float.NEGATIVE_INFINITY
            for (k in 0 until PERIMETER_SAMPLES) {
                val lx = state.unitPx[k] * topDiameter
                val ly = state.unitPy[k] * topDiameter
                val rx = lx * cosR - ly * sinR
                val ry = lx * sinR + ly * cosR
                val wx = cx + rx * sx
                val wy = cy + ry * sy

                val fIdx = ((wx / w).coerceIn(0f, 1f) * WAVE_SAMPLES)
                val i0 = fIdx.toInt().coerceIn(0, WAVE_SAMPLES - 1)
                val frac = fIdx - i0
                val liveWaveY = lerpFloat(waveSampleY[i0], waveSampleY[i0 + 1], frac)
                val overlap = wy - liveWaveY
                if (overlap > maxOverlapPx) {
                    maxOverlapPx = overlap
                }
            }
            val minGapPx = 1.2f
            if (state.wasInContact || maxOverlapPx + minGapPx > 0f) {
                cy - (maxOverlapPx + minGapPx)
            } else {
                cy
            }
        } else {
            cy
        }

        withTransform({
            translate(left = cx, top = drawCy)
            scale(scaleX = sx, scaleY = sy, pivot = Offset.Zero)
            rotate(degrees = state.rotation, pivot = Offset.Zero)
        }) {
            drawPath(path = topPath, color = topShapeColor)
        }
    }
}

@Composable
fun PixelBouncingShapesHero(
    heroShape: AppHeroShape,
    haptics: PixelHaptics,
    compact: Boolean = false,
    isDark: Boolean = isSystemInDarkTheme(),
    amoledBlack: Boolean = false,
    expressiveMotion: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(28.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (compact) 180.dp else 208.dp)
            .clip(cardShape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.55f),
                shape = cardShape,
            ),
    ) {
        PixelBouncingShapesStage(
            heroShape = heroShape,
            isDark = isDark,
            amoledBlack = amoledBlack,
            expressiveMotion = expressiveMotion,
            haptics = haptics,
            bannerMode = true,
            autoHaptics = false,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
fun PixelSetupScreen(
    settings: AppSettings,
    initialStep: Int = 0,
    isReconfiguring: Boolean = false,
    haptics: PixelHaptics,
    onUpdateSettings: (AppSettings) -> Unit,
    onCancel: () -> Unit,
    onFinish: (AppSettings) -> Unit,
) {
    var step by remember { mutableIntStateOf(initialStep.coerceIn(0, 3)) }
    var lastWizardStep by remember { mutableIntStateOf(initialStep.coerceIn(1, 3)) }
    if (step > 0) {
        lastWizardStep = step
    }

    AnimatedContent(
        targetState = step == 0,
        transitionSpec = {
            val forward = !targetState
            val slideIn = slideInHorizontally(
                animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
            ) { width -> if (forward) width / 3 else -width / 3 }
            val slideOut = slideOutHorizontally(
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
            ) { width -> if (forward) -width / 3 else width / 3 }
            (slideIn + fadeIn()) togetherWith (slideOut + fadeOut())
        },
        label = "pixel_setup_root",
    ) { isWelcome ->
        if (isWelcome) {
            PixelWelcomeStep(
                settings = settings,
                isReconfiguring = isReconfiguring,
                haptics = haptics,
                onSkipOrCancel = {
                    if (isReconfiguring) onCancel() else onFinish(settings)
                },
                onGoToStep = { targetStep -> step = targetStep.coerceIn(1, 3) },
                onNext = { step = 1 },
            )
        } else {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(
                            onClick = {
                                haptics.click()
                                if (isReconfiguring && lastWizardStep == initialStep) {
                                    onCancel()
                                } else {
                                    step = (lastWizardStep - 1).coerceAtLeast(0)
                                }
                            },
                            shapes = ButtonDefaults.shapes(),
                        ) {
                            Text(if (isReconfiguring && lastWizardStep == initialStep) "Cancel" else "Back")
                        }

                        Button(
                            onClick = {
                                if (lastWizardStep < 3) {
                                    haptics.click()
                                    step = lastWizardStep + 1
                                } else {
                                    haptics.confirm()
                                    onFinish(settings)
                                }
                            },
                            shapes = ButtonDefaults.shapesFor(ButtonDefaults.MediumContainerHeight),
                            contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight),
                            modifier = Modifier.heightIn(min = ButtonDefaults.MediumContainerHeight),
                        ) {
                            Text(
                                text = if (lastWizardStep == 3) "Done" else "Next",
                                style = MaterialTheme.typography.titleMediumEmphasized,
                            )
                        }
                    }
                },
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp),
                ) {
                    Spacer(Modifier.height(12.dp))
                    PixelProgressPills(currentStep = lastWizardStep, totalSteps = 4)
                    Spacer(Modifier.height(18.dp))

                    AnimatedContent(
                        targetState = lastWizardStep,
                        transitionSpec = {
                            val forward = targetState >= initialState
                            val slideIn = slideInHorizontally(
                                animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
                            ) { width -> if (forward) width / 3 else -width / 3 }
                            val slideOut = slideOutHorizontally(
                                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                            ) { width -> if (forward) -width / 3 else width / 3 }
                            (slideIn + fadeIn()) togetherWith (slideOut + fadeOut())
                        },
                        label = "pixel_setup_step",
                        modifier = Modifier.weight(1f),
                    ) { activeStep ->
                        when (activeStep) {
                            1 -> PixelColorsStep(
                                settings = settings,
                                haptics = haptics,
                                onUpdateSettings = onUpdateSettings,
                            )
                            2 -> PixelCustomizeStep(
                                settings = settings,
                                haptics = haptics,
                                onUpdateSettings = onUpdateSettings,
                            )
                            else -> PixelReadyStep(
                                settings = settings,
                                haptics = haptics,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PixelProgressPills(
    currentStep: Int,
    totalSteps: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 0 until totalSteps) {
            val active = i == currentStep
            val completed = i < currentStep
            val pillWidth by animateDpAsState(
                targetValue = if (active) 32.dp else 10.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "pill_w_$i",
            )
            val pillColor by animateColorAsState(
                targetValue = when {
                    active -> MaterialTheme.colorScheme.primary
                    completed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                    else -> MaterialTheme.colorScheme.surfaceContainerHighest
                },
                label = "pill_c_$i",
            )
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(pillWidth)
                    .clip(CircleShape)
                    .background(pillColor),
            )
        }
    }
}

@Composable
private fun resolveIsDarkTheme(themeMode: AppThemeMode): Boolean = when (themeMode) {
    AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    AppThemeMode.LIGHT -> false
    AppThemeMode.DARK -> true
}

@Composable
private fun PixelWelcomeStep(
    settings: AppSettings,
    isReconfiguring: Boolean,
    haptics: PixelHaptics,
    onSkipOrCancel: () -> Unit,
    onGoToStep: (Int) -> Unit,
    onNext: () -> Unit,
) {
    val isDark = resolveIsDarkTheme(settings.themeMode)
    val scheme = MaterialTheme.colorScheme
    val backdropColor = when {
        isDark && settings.amoledBlack -> Color.Black
        isDark -> lerp(Color(0xFF0A0B12), scheme.surfaceContainerLowest, 0.45f)
        else -> scheme.surfaceContainerLowest
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PixelBouncingShapesStage(
            heroShape = settings.heroShape,
            isDark = isDark,
            amoledBlack = settings.amoledBlack,
            expressiveMotion = settings.expressiveMotion,
            haptics = haptics,
            bannerMode = false,
            autoHaptics = true,
            modifier = Modifier.fillMaxSize(),
        )

        Surface(
            onClick = {
                haptics.click()
                onSkipOrCancel()
            },
            shape = CircleShape,
            color = if (isDark) lerp(backdropColor, scheme.surfaceContainerHigh, 0.35f) else scheme.surfaceContainerHigh,
            contentColor = scheme.onSurface,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 20.dp, top = 14.dp)
                .size(44.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = if (isReconfiguring) "Cancel" else "Skip",
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = "Hi there",
                style = MaterialTheme.typography.displayMediumEmphasized,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) lerp(scheme.primary, Color.White, 0.25f) else scheme.onPrimaryContainer,
            )

            Spacer(Modifier.height(22.dp))

            val chipColor = if (isDark) {
                lerp(Color.Black, scheme.surfaceContainerLowest, 0.5f)
            } else {
                scheme.surface
            }
            val chipContentColor = if (isDark) {
                lerp(scheme.onSurface, scheme.primary, 0.15f)
            } else {
                scheme.onSurface
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                WelcomePillChip(
                    icon = Icons.Rounded.Palette,
                    label = "${settings.colorPalette.label} · ${settings.themeMode.label}",
                    containerColor = chipColor,
                    contentColor = chipContentColor,
                    onClick = {
                        haptics.click()
                        onGoToStep(1)
                    },
                )
                WelcomePillChip(
                    icon = Icons.Rounded.Tune,
                    label = "Customize",
                    containerColor = chipColor,
                    contentColor = chipContentColor,
                    onClick = {
                        haptics.click()
                        onGoToStep(2)
                    },
                )
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    haptics.click()
                    onNext()
                },
                shapes = ButtonDefaults.shapes(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = scheme.primary,
                    contentColor = scheme.onPrimary,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(78.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = "Get started",
                    modifier = Modifier.size(30.dp),
                )
            }
        }
    }
}

@Composable
private fun WelcomePillChip(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmallEmphasized,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PixelGlifHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(36.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLargeEmphasized,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun PixelColorsStep(
    settings: AppSettings,
    haptics: PixelHaptics,
    onUpdateSettings: (AppSettings) -> Unit,
) {
    val context = LocalContext.current
    val darkTheme = resolveIsDarkTheme(settings.themeMode)
    val dynamicScheme = remember(darkTheme) {
        resolveDynamicColorScheme(context, darkTheme)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        PixelGlifHeader(
            icon = Icons.Rounded.Palette,
            title = "Wallpaper & style",
        )

        PixelBouncingShapesHero(
            heroShape = settings.heroShape,
            haptics = haptics,
            compact = true,
            isDark = darkTheme,
            amoledBlack = settings.amoledBlack,
            expressiveMotion = settings.expressiveMotion,
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Color palette",
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    color = MaterialTheme.colorScheme.primary,
                )

                val rows = AppColorPalette.entries.chunked(4)
                for (row in rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        for (palette in row) {
                            val selected = settings.colorPalette == palette
                            val (cTop, cLeft, cRight) = palettePreviewColors(palette, darkTheme, dynamicScheme)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable {
                                        haptics.tick()
                                        onUpdateSettings(settings.copy(colorPalette = palette))
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                PixelColorSwatch(
                                    topColor = cTop,
                                    bottomLeftColor = cLeft,
                                    bottomRightColor = cRight,
                                    selected = selected,
                                )
                                Text(
                                    text = palette.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        repeat(4 - row.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleSmallEmphasized,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            PixelOptionPillRow(
                options = AppThemeMode.entries,
                selected = settings.themeMode,
                label = { it.label },
                onSelect = {
                    haptics.tick()
                    onUpdateSettings(settings.copy(themeMode = it))
                },
            )
        }

        SegmentedListItem(
            checked = settings.amoledBlack,
            onCheckedChange = {
                haptics.tick()
                onUpdateSettings(settings.copy(amoledBlack = it))
            },
            shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
            colors = ListItemDefaults.segmentedColors(),
            trailingContent = {
                Switch(
                    checked = settings.amoledBlack,
                    onCheckedChange = null,
                )
            },
        ) {
            Text("Pitch black dark mode", style = MaterialTheme.typography.titleSmallEmphasized)
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun PixelColorSwatch(
    topColor: androidx.compose.ui.graphics.Color,
    bottomLeftColor: androidx.compose.ui.graphics.Color,
    bottomRightColor: androidx.compose.ui.graphics.Color,
    selected: Boolean,
) {
    val ringScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.9f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "swatch_scale",
    )
    Box(
        modifier = Modifier
            .size(56.dp)
            .graphicsLayer {
                scaleX = ringScale
                scaleY = ringScale
            }
            .then(
                if (selected) {
                    Modifier.border(2.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                } else {
                    Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                },
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
        ) {
            val w = size.width
            val h = size.height
            drawRect(color = topColor, topLeft = Offset.Zero, size = Size(w, h / 2f))
            drawRect(color = bottomLeftColor, topLeft = Offset(0f, h / 2f), size = Size(w / 2f, h / 2f))
            drawRect(color = bottomRightColor, topLeft = Offset(w / 2f, h / 2f), size = Size(w / 2f, h / 2f))
        }
        if (selected) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun PixelCustomizeStep(
    settings: AppSettings,
    haptics: PixelHaptics,
    onUpdateSettings: (AppSettings) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        PixelGlifHeader(
            icon = Icons.Rounded.Tune,
            title = "Make it yours",
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Badge shape",
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    color = MaterialTheme.colorScheme.primary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AppHeroShape.entries.forEach { shapeOption ->
                        val selected = settings.heroShape == shapeOption
                        val scale by animateFloatAsState(
                            targetValue = if (selected) 1.08f else 0.94f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "shape_${shapeOption.name}",
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(MaterialTheme.shapes.medium)
                                .clickable {
                                    haptics.bounce()
                                    onUpdateSettings(settings.copy(heroShape = shapeOption))
                                }
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Surface(
                                shape = heroPolygonFor(shapeOption).toShape(),
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                },
                                contentColor = if (selected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    },
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (selected) {
                                        Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                            Text(
                                text = shapeOption.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
            SegmentedListItem(
                checked = settings.hapticsEnabled,
                onCheckedChange = { newValue ->
                    onUpdateSettings(settings.copy(hapticsEnabled = newValue))
                    if (newValue) haptics.confirm()
                },
                shapes = ListItemDefaults.segmentedShapes(index = 0, count = 2),
                colors = ListItemDefaults.segmentedColors(),
                leadingContent = {
                    Icon(Icons.Rounded.Vibration, contentDescription = null, modifier = Modifier.size(22.dp))
                },
                trailingContent = {
                    Switch(checked = settings.hapticsEnabled, onCheckedChange = null)
                },
            ) {
                Text("Haptics", style = MaterialTheme.typography.titleSmallEmphasized)
            }

            SegmentedListItem(
                checked = settings.expressiveMotion,
                onCheckedChange = { newValue ->
                    haptics.tick()
                    onUpdateSettings(settings.copy(expressiveMotion = newValue))
                },
                shapes = ListItemDefaults.segmentedShapes(index = 1, count = 2),
                colors = ListItemDefaults.segmentedColors(),
                leadingContent = {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(22.dp))
                },
                trailingContent = {
                    Switch(checked = settings.expressiveMotion, onCheckedChange = null)
                },
            ) {
                Text("Expressive spring motion", style = MaterialTheme.typography.titleSmallEmphasized)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Default features",
                style = MaterialTheme.typography.titleSmallEmphasized,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            PixelOptionPillRow(
                options = AppDefaultPreset.entries,
                selected = settings.defaultPreset,
                label = { it.label },
                onSelect = {
                    haptics.tick()
                    onUpdateSettings(settings.copy(defaultPreset = it))
                },
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun PixelReadyStep(
    settings: AppSettings,
    haptics: PixelHaptics,
) {
    val darkTheme = resolveIsDarkTheme(settings.themeMode)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        PixelGlifHeader(
            icon = Icons.Rounded.CheckCircle,
            title = "You're all set",
        )

        PixelBouncingShapesHero(
            heroShape = settings.heroShape,
            haptics = haptics,
            compact = false,
            isDark = darkTheme,
            amoledBlack = settings.amoledBlack,
            expressiveMotion = settings.expressiveMotion,
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SetupSummaryRow("Palette", settings.colorPalette.label)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                SetupSummaryRow(
                    "Theme",
                    if (settings.amoledBlack && settings.themeMode == AppThemeMode.DARK) {
                        "${settings.themeMode.label} · Black"
                    } else {
                        settings.themeMode.label
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                SetupSummaryRow("Badge shape", settings.heroShape.label)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                SetupSummaryRow("Default preset", settings.defaultPreset.label)
            }
        }
    }
}

@Composable
private fun SetupSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmallEmphasized,
        )
    }
}

@Composable
private fun <T> PixelOptionPillRow(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            if (isSelected) {
                Button(
                    onClick = { onSelect(option) },
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(label(option), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            } else {
                FilledTonalButton(
                    onClick = { onSelect(option) },
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(label(option), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
