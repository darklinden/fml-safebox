package com.darklinden.fml.config

object Config {

    const val endpoint = "http://<endpoint>.aliyuncs.com"
    const val accessKeyId = "<accessKeyId>"
    const val accessKeySecret = "<accessKeySecret>"
    const val bucketName = "<bucketName>"

    const val cipher = "AES/CBC/PKCS7Padding"
    const val keyFactory = "PBKDF2withHmacSHA1"
    const val encrypt = "AES"
    const val keySize = 256
    const val keyLen = 32
    const val ivLen = 16

    // change to your own calc here
    fun keyToUse(plainText: String): Triple<String, String, Int> {
        return Triple(plainText, plainText, 1024)
    }

    // change to your own calc here
    fun genIv(tri: Triple<String, String, Int>, buff: ByteArray): ByteArray {
        val k2u = tri.first
        var ret = byteArrayOf()
        for (i in 0 until ivLen) ret += k2u[i].toByte()
        return ret
    }
}