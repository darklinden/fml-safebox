package com.darklinden.fml.data

import android.util.Log
import com.darklinden.fml.Application
import java.io.FileInputStream
import java.lang.Exception

/**
 * A dummy item representing a piece of content.
 */
data class SecFileItem constructor(val path: String) {

    var content: ByteArray? = null

    init {

        try {
            val x = Application.app.openFileInput(path)
            content = x.readBytes()
        } catch (e: Exception) {
            content = null
            e.printStackTrace()
        }

        Log.d("", "")
    }

    override fun toString(): String {
        return ""
    }

}