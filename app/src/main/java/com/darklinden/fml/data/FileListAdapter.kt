package com.darklinden.fml.data

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.darklinden.fml.Application
import com.darklinden.fml.R
import java.util.ArrayList
import kotlinx.android.synthetic.main.item_list_content.*

class FileListAdapter : RecyclerView.Adapter<FileListAdapter.FileItemViewHolder>() {

    inner class FileItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var item: SecFileItem? = null
            set(value) {
                Log.d("", "")
                field = value

                if (value != null) {
                    this.itemView.findViewById<TextView>(R.id.id_text).text = value.title
                }
            }
    }

    val values: MutableList<SecFileItem> = ArrayList()
    var filter: String = ""
    val filtedValues: MutableList<SecFileItem> = ArrayList()

    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { _ ->
            //            val item = v.tag as DummyContent.SecFileItem
//            val intent = Intent(
//                v.context,
//                ItemDetailActivity::class.java
//            ).apply {
//                putExtra(ItemDetailFragment.ARG_ITEM_ID, item.id)
//            }
//            v.context.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_content, parent, false)
        return FileItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileItemViewHolder, position: Int) {

        val item = if (filter.isNotEmpty()) {
            filtedValues[position]
        } else {
            values[position]
        }

        holder.item = item

        with(holder.itemView) {
            tag = item
            setOnClickListener(onClickListener)
        }
    }

    override fun getItemCount(): Int {
        return if (filter.isNotEmpty()) {
            filtedValues.size
        } else {
            values.size
        }
    }

    fun filterFile(query: String) {
        filter = query
        filtedValues.clear()
        for (o in values) {
            if (o.title?.indexOf(query) != -1) {
                filtedValues.add(o)
            }
        }
        this.notifyDataSetChanged()
    }

    fun reloadFileList() {
        filter = ""
        filtedValues.clear()
        values.clear()

        val fileList = Application.app.fileList()

        for (f in fileList) {
            SecFileItem.fromFile(f)?.let { values.add(it) }
        }
        this.notifyDataSetChanged()
    }
}