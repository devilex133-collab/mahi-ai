package com.mahi.ai.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Stores the API key encrypted with an Android Keystore AES key. The key never leaves this phone. */
class SecureStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("mahi_secure", Context.MODE_PRIVATE)

    private fun secretKey(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        gen.init(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return gen.generateKey()
    }

    fun setApiKey(value: String) {
        if (value.isBlank()) { prefs.edit().remove(KEY_API).apply(); return }
        val c = Cipher.getInstance(TRANSFORM)
        c.init(Cipher.ENCRYPT_MODE, secretKey())
        val data = c.doFinal(value.trim().toByteArray(Charsets.UTF_8))
        val blob = Base64.encodeToString(c.iv, Base64.NO_WRAP) + ":" + Base64.encodeToString(data, Base64.NO_WRAP)
        prefs.edit().putString(KEY_API, blob).apply()
    }

    fun getApiKey(): String? {
        val blob = prefs.getString(KEY_API, null) ?: return null
        return try {
            val (iv, data) = blob.split(":")
            val c = Cipher.getInstance(TRANSFORM)
            c.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP)))
            String(c.doFinal(Base64.decode(data, Base64.NO_WRAP)), Charsets.UTF_8)
        } catch (e: Exception) { null }
    }

    fun hasApiKey() = getApiKey() != null
    fun getModel(): String = prefs.getString(KEY_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
    fun setModel(v: String) { prefs.edit().putString(KEY_MODEL, v.trim().ifBlank { DEFAULT_MODEL }).apply() }

    companion object {
        const val DEFAULT_MODEL = "gemini-2.5-flash"
        private const val ALIAS = "mahi_api_key"
        private const val KEY_API = "api_key_enc"
        private const val KEY_MODEL = "model"
        private const val TRANSFORM = "AES/GCM/NoPadding"
    }
}
