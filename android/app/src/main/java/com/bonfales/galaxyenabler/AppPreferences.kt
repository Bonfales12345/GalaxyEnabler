package com.bonfales.galaxyenabler

import android.content.Context

enum class AppColorPalette(val label: String) {
    DYNAMIC("Dynamic"),
    PIXEL_BLUE("Pixel Blue"),
    SAGE("Sage"),
    CORAL("Coral"),
    IRIS("Iris"),
    AMBER("Amber"),
    MONOCHROME("Mono"),
}

enum class AppThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark"),
}

enum class AppHeroShape(val label: String) {
    COOKIE("Cookie"),
    CLOVER("Clover"),
    BURST("Burst"),
    FLOWER("Flower"),
    GEM("Gem"),
}

enum class AppDefaultPreset(val label: String) {
    RECOMMENDED("Recommended"),
    MINIMAL("Minimal"),
    ALL("All"),
}

data class AppSettings(
    val setupCompleted: Boolean = false,
    val colorPalette: AppColorPalette = AppColorPalette.DYNAMIC,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val amoledBlack: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val expressiveMotion: Boolean = true,
    val heroShape: AppHeroShape = AppHeroShape.COOKIE,
    val defaultPreset: AppDefaultPreset = AppDefaultPreset.RECOMMENDED,
)

object AppPrefsStore {
    private const val PREFS_NAME = "galaxyenabler_prefs"
    private const val KEY_SETUP_COMPLETED = "setup_completed"
    private const val KEY_COLOR_PALETTE = "color_palette"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_AMOLED_BLACK = "amoled_black"
    private const val KEY_HAPTICS_ENABLED = "haptics_enabled"
    private const val KEY_EXPRESSIVE_MOTION = "expressive_motion"
    private const val KEY_HERO_SHAPE = "hero_shape"
    private const val KEY_DEFAULT_PRESET = "default_preset"

    fun load(context: Context): AppSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return AppSettings(
            setupCompleted = prefs.getBoolean(KEY_SETUP_COMPLETED, false),
            colorPalette = enumByName(prefs.getString(KEY_COLOR_PALETTE, null), AppColorPalette.DYNAMIC),
            themeMode = enumByName(prefs.getString(KEY_THEME_MODE, null), AppThemeMode.SYSTEM),
            amoledBlack = prefs.getBoolean(KEY_AMOLED_BLACK, false),
            hapticsEnabled = prefs.getBoolean(KEY_HAPTICS_ENABLED, true),
            expressiveMotion = prefs.getBoolean(KEY_EXPRESSIVE_MOTION, true),
            heroShape = enumByName(prefs.getString(KEY_HERO_SHAPE, null), AppHeroShape.COOKIE),
            defaultPreset = enumByName(prefs.getString(KEY_DEFAULT_PRESET, null), AppDefaultPreset.RECOMMENDED),
        )
    }

    fun save(context: Context, settings: AppSettings) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SETUP_COMPLETED, settings.setupCompleted)
            .putString(KEY_COLOR_PALETTE, settings.colorPalette.name)
            .putString(KEY_THEME_MODE, settings.themeMode.name)
            .putBoolean(KEY_AMOLED_BLACK, settings.amoledBlack)
            .putBoolean(KEY_HAPTICS_ENABLED, settings.hapticsEnabled)
            .putBoolean(KEY_EXPRESSIVE_MOTION, settings.expressiveMotion)
            .putString(KEY_HERO_SHAPE, settings.heroShape.name)
            .putString(KEY_DEFAULT_PRESET, settings.defaultPreset.name)
            .apply()
    }

    private fun <T : Enum<T>> enumByName(raw: String?, fallback: T): T {
        if (raw.isNullOrBlank()) return fallback
        return fallback.declaringJavaClass.enumConstants?.firstOrNull { it.name == raw } ?: fallback
    }
}
