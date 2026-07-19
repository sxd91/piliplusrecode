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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val PreferencesNode = "com/sxd/reliqliquid/session"
private const val CookieKey = "cookies"
private const val AccountsKey = "accounts"
private const val CurrentAccountKey = "current_account"
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

    actual fun loadAccounts(): List<StoredAccount> {
        val encoded = preferences.get(AccountsKey, null) ?: return emptyList()
        return runCatching { Json.decodeFromString<List<StoredAccount>>(decrypt(encoded)) }
            .getOrDefault(emptyList())
    }

    actual fun saveAccounts(accounts: List<StoredAccount>) {
        preferences.put(AccountsKey, encrypt(Json.encodeToString(accounts)))
    }

    actual fun currentAccountId(): String? = preferences.get(CurrentAccountKey, null)

    actual fun setCurrentAccountId(id: String?) {
        if (id == null) preferences.remove(CurrentAccountKey) else preferences.put(CurrentAccountKey, id)
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
        preferences.remove(CurrentAccountKey)
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

