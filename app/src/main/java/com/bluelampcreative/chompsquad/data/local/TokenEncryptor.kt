package com.bluelampcreative.chompsquad.data.local

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import org.koin.core.annotation.Singleton

private const val KEYSTORE_ALIAS = "chompsquad_token_key"
private const val AES_GCM_NOPADDING = "AES/GCM/NoPadding"
private const val AES_KEY_SIZE_BITS = 256
private const val GCM_IV_LENGTH = 12
private const val GCM_TAG_LENGTH_BITS = 128

/** Encrypts and decrypts token strings using an AES-256-GCM key backed by the Android Keystore. */
interface TokenEncryptor {
  fun encrypt(value: String): String

  /**
   * Returns the decrypted token, or null if decryption fails (e.g. key rotated, data corrupted, or
   * legacy plaintext value stored before encryption was introduced).
   */
  fun decrypt(value: String): String?
}

@Singleton(binds = [TokenEncryptor::class])
class KeystoreTokenEncryptor : TokenEncryptor {

  private fun secretKey(): SecretKey {
    val keystore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    keystore.getKey(KEYSTORE_ALIAS, null)?.let {
      return it as SecretKey
    }
    return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        .apply {
          init(
              KeyGenParameterSpec.Builder(
                      KEYSTORE_ALIAS,
                      KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                  )
                  .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                  .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                  .setKeySize(AES_KEY_SIZE_BITS)
                  .build()
          )
        }
        .generateKey()
  }

  override fun encrypt(value: String): String {
    val cipher = Cipher.getInstance(AES_GCM_NOPADDING)
    cipher.init(Cipher.ENCRYPT_MODE, secretKey())
    val iv = cipher.iv // fresh random IV for every encryption
    val ciphertext = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
    return Base64.encodeToString(iv + ciphertext, Base64.NO_WRAP)
  }

  override fun decrypt(value: String): String? =
      runCatching {
            val combined = Base64.decode(value, Base64.NO_WRAP)
            val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
            val ciphertext = combined.copyOfRange(GCM_IV_LENGTH, combined.size)
            val cipher = Cipher.getInstance(AES_GCM_NOPADDING)
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
            String(cipher.doFinal(ciphertext), Charsets.UTF_8)
          }
          .getOrNull()
}
