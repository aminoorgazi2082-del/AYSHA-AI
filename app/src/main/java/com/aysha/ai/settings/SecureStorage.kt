package com.aysha.ai.settings

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecureStorage(context: Context) {
    private val prefs = context.getSharedPreferences("aysha_secure", Context.MODE_PRIVATE)
    private val alias = "aysha_gemini_key"

    private fun getKey(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(alias, null) as? SecretKey)?.let { return it }
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        gen.init(KeyGenParameterSpec.Builder(alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true).build())
        return gen.generateKey()
    }

    fun save(value: String) {
        if (value.isBlank()) { prefs.edit().remove("key").apply(); return }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getKey())
        val encrypted = cipher.doFinal(value.toByteArray())
        val json = JSONObject().put("iv", Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .put("data", Base64.encodeToString(encrypted, Base64.NO_WRAP))
        prefs.edit().putString("key", json.toString()).apply()
    }

    fun read(): String {
        return try {
            val packed = prefs.getString("key", null) ?: return ""
            val json = JSONObject(packed)
            val iv = Base64.decode(json.getString("iv"), Base64.NO_WRAP)
            val data = Base64.decode(json.getString("data"), Base64.NO_WRAP)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, getKey(), GCMParameterSpec(128, iv))
            String(cipher.doFinal(data))
        } catch (_: Exception) { "" }
    }
}
