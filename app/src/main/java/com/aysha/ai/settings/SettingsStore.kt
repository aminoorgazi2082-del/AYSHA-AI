package com.aysha.ai.settings
import android.content.Context
class SettingsStore(context: Context) {
    private val p = context.getSharedPreferences("aysha_settings", Context.MODE_PRIVATE)
    var model: String
        get() = p.getString("model", "gemini-3.8-flash") ?: "gemini-3.8-flash"
        set(v) { p.edit().putString("model", v).apply() }
    var voice: Boolean
        get() = p.getBoolean("voice", true)
        set(v) { p.edit().putBoolean("voice", v).apply() }
}
