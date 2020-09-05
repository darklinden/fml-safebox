package com.darklinden.fml.data

import android.content.Context
import android.util.Log
import com.darklinden.fml.Application
import com.darklinden.fml.BuildConfig
import org.json.JSONObject
import java.lang.Exception
import java.util.*

/**
 * A dummy item representing a piece of content.
 */
data class SecFileItem(var uuid: String? = null) {

    companion object {
        private const val kTitle = "title"
        private const val kInfo = "info"

        fun fromFile(uuid: String?): SecFileItem? {
            var o: SecFileItem? = null
            try {
                val file = Application.app.openFileInput(uuid)
                val content = file.readBytes()
                file.close()

                if (content.isNotEmpty()) {
                    val s = AesEncryption.decrypt(content, Application.app.patten!!)
                    val d = JSONObject(s)

                    val title = d.optString(kTitle)
                    val info = d.optString(kInfo)

                    if (!title.isEmpty() && !info.isEmpty()) {
                        o = SecFileItem()
                        o.title = title
                        o.info = info
                    }

                } else {
                    Log.d("SecFileItem", "load data from file $uuid empty")
                    // if (BuildConfig.DEBUG) Application.app.deleteFile(uuid);
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return o
        }
    }

    var title: String? = null
    var info: String? = null

    init {
        if (uuid == null) {
            // new
            this.uuid = UUID.randomUUID().toString()
        }
    }

    fun saveData(): Boolean {
        var success: Boolean
        try {

            val d = mapOf(kTitle to title, kInfo to info)
            val o = JSONObject(d)

            val s = AesEncryption.encrypt(o.toString(), Application.app.patten!!)
            val file = Application.app.openFileOutput(uuid, Context.MODE_PRIVATE)
            file.write(s)
            file.close()

            success = true;
        } catch (e: Exception) {
            success = false;
            e.printStackTrace()
        }

        return success;
    }
}