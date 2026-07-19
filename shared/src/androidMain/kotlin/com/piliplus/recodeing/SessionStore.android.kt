package com.piliplus.recodeing.core.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val PreferencesName = "liquidreode_session"
private const val CookieKey = "cookies"
private const val KeyStoreType = "AndroidKeyStore"
private const val KeyAlias = "liquidreode_session_key"
private const val Transformation = "AES/GCM/NoPadding"

actual class SessionStore internal constructor(
    private val context: Context,
) {
    actual fun loadCookies(): List<SessionCookie> {
        val encoded = preferences.getString(CookieKey, null) ?: return emptyList()
        return runCatching {
            decrypt(encoded).split('\n').mapNotNull { line ->
                val separator = line.indexOf('=')
                if (separator <= 0) null else SessionCookie(line.substring(0, separator), line.substring(separator + 1))
            }
        }.getOrDefault(emptyList())
    }

    actual fun saveCookies(cookies: List<SessionCookie>) {
        val value = cookies.joinToString("\n") { "${it.name}=${it.value}" }
        preferences.edit().putString(CookieKey, encrypt(value)).apply()
    }

    actual fun clear() {
        preferences.edit().remove(CookieKey).apply()
    }

    private val preferences get() = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(Transformation)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val payload = cipher.iv + cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    private fun decrypt(value: String): String {
        val payload = Base64.decode(value, Base64.NO_WRAP)
        val cipher = Cipher.getInstance(Transformation)
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, payload.copyOfRange(0, 12)))
        return cipher.doFinal(payload.copyOfRange(12, payload.size)).toString(StandardCharsets.UTF_8)
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance(KeyStoreType).apply { load(null) }
        val existing = store.getKey(KeyAlias, null) as? SecretKey
        if (existing != null) return existing
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KeyStoreType).apply {
            init(
                KeyGenParameterSpec.Builder(
                    KeyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build(),
            )
        }.generateKey()
    }
}

@Composable
actual fun rememberSessionStore(): SessionStore {
    val context = LocalContext.current
    return remember(context) { SessionStore(context.applicationContext) }
}
