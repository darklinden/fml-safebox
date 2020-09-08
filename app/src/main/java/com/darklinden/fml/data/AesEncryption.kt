package com.darklinden.fml.data

import com.darklinden.fml.config.Config
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object AesEncryption {

    @Throws(Exception::class)
    fun encrypt(textToEncrypt: String, plainText: String): ByteArray {
        val keygen = getRaw(plainText)
        val keySpec = SecretKeySpec(keygen, 0, Config.keyLen, Config.encrypt)
        val cipher = Cipher.getInstance(Config.cipher)
        cipher.init(
            Cipher.ENCRYPT_MODE,
            keySpec,
            IvParameterSpec(keygen, Config.keyLen, Config.ivLen)
        )
        return cipher.doFinal(textToEncrypt.toByteArray(Charsets.UTF_8))
    }

    @Throws(Exception::class)
    fun decrypt(byteToDecrypt: ByteArray, plainText: String): String {
        val keygen = getRaw(plainText)
        val keySpec = SecretKeySpec(keygen, 0, Config.keyLen, Config.encrypt)
        val cipher = Cipher.getInstance(Config.cipher)
        cipher.init(
            Cipher.DECRYPT_MODE,
            keySpec,
            IvParameterSpec(keygen, Config.keyLen, Config.ivLen)
        )
        val decrypted = cipher.doFinal(byteToDecrypt)
        return String(decrypted, Charsets.UTF_8)
    }

    private fun getRaw(plainText: String): ByteArray {
        try {
            val factory = SecretKeyFactory.getInstance(Config.keyFactory)

            // TODO: gen key&salt from pwd, you can change you own here
            val tri = Config.keyToUse(plainText)
            val k2u = tri.first
            val salt = tri.second
            val it = tri.third

            val spec =
                PBEKeySpec(k2u.toCharArray(), salt.toByteArray(), it, Config.keySize)
            var buff = factory.generateSecret(spec).encoded

            // TODO: gen iv
            buff += Config.genIv(tri, buff)

            return buff;
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ByteArray(0)
    }
}

