package com.ap.app.lock.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

object Encryption {
    private const val KEYSTORE_ALIAS = "AppLockKeyAlias"
    private const val CIPHER_ALGORITHM = "AES/CBC/PKCS7Padding"
    private const val KEY_ALGORITHM = "AES"

    init {
        ensureKeyExists()
    }

    private fun ensureKeyExists() {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
                generateKey()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun generateKey() {
        try {
            val keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM, "AndroidKeyStore")
            val spec = KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build()
            keyGenerator.init(spec)
            keyGenerator.generateKey()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun encrypt(plainText: String): String? {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val secretKey = keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
            val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val iv = cipher.iv
            val combined = iv + encryptedBytes
            Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun decrypt(encryptedText: String): String? {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val secretKey = keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
            val decodedBytes = Base64.decode(encryptedText, Base64.DEFAULT)
            val iv = decodedBytes.sliceArray(0 until 16)
            val encryptedData = decodedBytes.sliceArray(16 until decodedBytes.size)
            val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
            val decryptedBytes = cipher.doFinal(encryptedData)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}