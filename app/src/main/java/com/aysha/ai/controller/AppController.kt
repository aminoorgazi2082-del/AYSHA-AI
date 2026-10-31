package com.aysha.ai.controller
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
class AppController(private val c: Context) {
    fun youtubeSearch(q: String) = c.startActivity(Intent(Intent.ACTION_VIEW,
        Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(q)}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    fun browser() = c.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    fun camera() = c.startActivity(Intent("android.media.action.IMAGE_CAPTURE").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    fun settings() = c.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    fun dial(number: String) = c.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(number)}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
