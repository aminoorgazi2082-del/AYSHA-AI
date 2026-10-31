package com.aysha.ai.memory
import android.content.Context
class MemoryManager(context: Context) {
    private val p = context.getSharedPreferences("aysha_memory", Context.MODE_PRIVATE)
    fun list(): List<String> = p.getStringSet("items", emptySet())?.toList()?.sorted() ?: emptyList()
    fun add(value: String) {
        if (value.isBlank()) return
        val set = (p.getStringSet("items", emptySet()) ?: emptySet()).toMutableSet()
        set.add(value.trim()); p.edit().putStringSet("items", set).apply()
    }
    fun delete(value: String) {
        val set = (p.getStringSet("items", emptySet()) ?: emptySet()).toMutableSet()
        set.remove(value); p.edit().putStringSet("items", set).apply()
    }
    fun clear() { p.edit().remove("items").apply() }
}
