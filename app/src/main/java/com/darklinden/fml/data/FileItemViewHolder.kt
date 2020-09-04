package com.darklinden.fml.data

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class FileItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    var item: SecFileItem? = null
        set(value) {
            Log.d("", "")
            field = value
        }
}