package com.piliplus.recodeing.core.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.util.prefs.Preferences
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import java.util.Base64
import javax.crypto.spec.SecretKeySpec

private const val PreferencesNode = "com/sxd/liquidreode/session"
private const val CookieKey = "cookies"
private const val SecretKeyPreference = "secret_key"
private const val Transformation = "AES/GCM/NoPadding"

actual class SessionStore {
    actual fun loadCookies(): List<SessionCookie> {
        val encoded = preferences.get(CookieKey, null) ?: return emptyList()
        return runCatching {
            decrypt(encoded).split('\n').mapNotNull { line ->
                val separator = line.indexOf('=')
                if (separator <= 0) null else SessionCookie(line.substring(0, separator), line.substring(separator + 1))
            }
        }.getOrDefault(emptyList())
    }

    actual fun saveCookies(cookies: List<SessionCookie>) {
        val value = cookies.joinToString("\n") { "${it.name}=${it.value}" }
        preferences.put(CookieKey, encrypt(value))
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(Transformation)
        cipher.init(Cipher.ENCRYPT_MODE, preferencesKey())
        return Base64.getEncoder().encodeToString(cipher.iv + cipher.doFinal(value.encodeToByteArray()))
    }

    private fun decrypt(value: String): String {
        val payload = Base64.getDecoder().decode(value)
        val cipher = Cipher.getInstance(Transformation)
        cipher.init(Cipher.DECRYPT_MODE, preferencesKey(), GCMParameterSpec(128, payload.copyOfRange(0, 12)))
        return cipher.doFinal(payload.copyOfRange(12, payload.size)).decodeToString()
    }

    actual fun clear() {
        preferences.remove(CookieKey)
    }

    private val preferences get() = Preferences.userRoot().node(PreferencesNode)

    private fun preferencesKey(): SecretKey {
        val encoded = preferences.get(SecretKeyPreference, null)
        if (encoded != null) {
            return SecretKeySpec(Base64.getDecoder().decode(encoded), "AES")
        }
        return KeyGenerator.getInstance("AES").apply { init(256) }.generateKey().also { generated ->
            preferences.put(
                SecretKeyPreference,
                Base64.getEncoder().encodeToString(generated.encoded),
            )
        }
    }
}

@Composable
actual fun rememberSessionStore(): SessionStore = remember { SessionStore() }

