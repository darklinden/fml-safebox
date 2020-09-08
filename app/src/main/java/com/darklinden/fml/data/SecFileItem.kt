package com.darklinden.fml.data

import android.content.Context
import android.util.Log
import com.darklinden.fml.Application
import org.json.JSONObject

data class SecFileItem(
    var uuid: String? = null,
    var title: String? = null,
    var info: String? = null
) {

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
                        o.uuid = uuid
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

    fun deleteData(): Boolean {
        var success = false
        try {

            if (uuid != null)
                success = Application.app.deleteFile(uuid)

        } catch (e: Exception) {
            success = false;
            e.printStackTrace()
        }

        return success
    }
}