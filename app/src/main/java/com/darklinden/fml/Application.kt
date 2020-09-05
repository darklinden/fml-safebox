package com.darklinden.fml

import android.util.Log
import com.darklinden.fml.data.AesEncryption


class Application : android.app.Application() {

    companion object {
        lateinit var app: Application
    }

    var patten: String? = null

    override fun onCreate() {
        super.onCreate()

        app = this;

        // test encrypt
        var count = 0;
        while (false) {
            val tLen = (Math.random() * 0xff + 0xff).toInt()
            val kLen = (Math.random() * 0xff + 10).toInt()
            var tb = byteArrayOf()
            var k = ""
            for (i in 1..tLen) {
                tb += (Math.random() * 0xff + 1).toByte()
            }
            val t = String(tb, Charsets.UTF_8)

            for (i in 1..kLen) {
                k += (Math.random() * 10).toInt()
            }

            val en = AesEncryption.encrypt(t, k)
            val y = AesEncryption.decrypt(en, k)

            assert(t == y)
            Log.d("test", "test passed " + (count++))
            if (count > 0xffff) count = 0
        }
    }
}