package com.darklinden.fml.data

import android.util.Base64
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.max
import kotlin.math.min

object AesEncryption {

    private const val cipher = "AES/CBC/PKCS7Padding"
    private const val keyFactory = "PBKDF2withHmacSHA1"
    private const val encrypt = "AES"
    private const val keySize = 256
    private const val keyLen = 32
    private const val ivLen = 16

    @Throws(Exception::class)
    fun encrypt(textToEncrypt: String, plainText: String): ByteArray {
        val keygen = getRaw(plainText)
        val keySpec = SecretKeySpec(keygen, 0, keyLen, encrypt)
        val cipher = Cipher.getInstance(cipher)
        cipher.init(
            Cipher.ENCRYPT_MODE,
            keySpec,
            IvParameterSpec(keygen, keyLen, ivLen)
        )
        return cipher.doFinal(textToEncrypt.toByteArray(Charsets.UTF_8))
    }

    @Throws(Exception::class)
    fun decrypt(byteToDecrypt: ByteArray, plainText: String): String {
        val keygen = getRaw(plainText)
        val keySpec = SecretKeySpec(keygen, 0, keyLen, encrypt)
        val cipher = Cipher.getInstance(cipher)
        cipher.init(
            Cipher.DECRYPT_MODE,
            keySpec,
            IvParameterSpec(keygen, keyLen, ivLen)
        )
        val decrypted = cipher.doFinal(byteToDecrypt)
        return String(decrypted, Charsets.UTF_8)
    }

    private fun getRaw(plainText: String): ByteArray {
        try {
            val factory = SecretKeyFactory.getInstance(keyFactory)

            val desSize = keySize

            // TODO: gen key&salt from pwd, you can change you own here
            var k2u = plainText
            var p = 0
            while (k2u.length < desSize + plainText.length) {
                val pos = k2u[p % k2u.length].toInt() % k2u.length
                var len = k2u[p % k2u.length].toInt() % 10
                if (len == 0) len = 10
                if (pos + len > k2u.length) {
                    k2u += k2u.substring(pos)
                } else {
                    k2u += k2u.substring(pos, pos + len)
                }
                p += len
            }

            k2u = k2u.substring(plainText.length)

            val end = k2u[k2u.length - 1].toInt()
            var it = 0
            for (i in 0..min(plainText.length - 1, end)) {
                it += k2u[i].toInt()
            }

            val spec =
                PBEKeySpec(k2u.toCharArray(), plainText.toByteArray(), it, desSize)
            var buff = factory.generateSecret(spec).encoded

            // gen iv
            val pos = max(k2u[0].toInt(), k2u[k2u.length - 1].toInt())
            val ivp = k2u[pos % k2u.length].toInt()
            for (i in 1..ivLen) {
                if (ivp > k2u.length / 2) {
                    buff += k2u[ivp - i].toByte()

                } else {
                    buff += k2u[ivp + i].toByte()
                }
            }

            return buff;
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ByteArray(0)
    }
}

