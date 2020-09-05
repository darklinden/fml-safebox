package com.darklinden.fml

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import com.darklinden.fml.data.FileListAdapter
import com.darklinden.fml.data.SecFileItem
import kotlinx.android.synthetic.main.activity_item_list.*
import java.util.*
import kotlin.concurrent.schedule
import kotlin.system.exitProcess


class ItemListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_list)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp = this.window.attributes
            lp.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            this.window.attributes = lp
            val decorView = this.window.decorView
            var systemUiVisibility = decorView.systemUiVisibility
            val flags = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
            systemUiVisibility = systemUiVisibility or flags
            this.window.decorView.systemUiVisibility = systemUiVisibility
        }

        search_view.setOnClickListener { search_view.isIconified = false }

        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                refreshData(newText)
                return true
            }
        })

        search_view.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                search_view.isIconified = true
                refreshData()
            }
        }

        item_list.adapter = FileListAdapter()
        pull_refresh_layout.setOnRefreshListener {
            refreshData()
        }

        pull_refresh_layout.isRefreshing = true

        Timer(false).schedule(500) {
            refreshData()
        }

        btn_add.setOnClickListener {

            val item = SecFileItem()
            item.title = "test" + (Math.random() * 0xff).toInt()
            item.info = (Math.random() * 0xff).toInt().toString()
            item.saveData()
        }
    }

    private fun refreshData(query: String? = null) {
        this.runOnUiThread {
            if (query != null) {
                (item_list.adapter as FileListAdapter).filterFile(query)
            } else {
                (item_list.adapter as FileListAdapter).reloadFileList()
            }
            pull_refresh_layout.isRefreshing = false
        }
    }

    override fun onPause() {
        super.onPause()
        killMe()
    }

    override fun onStop() {
        super.onStop()
        killMe()
    }

    fun killMe() {
        android.os.Process.killProcess(android.os.Process.myPid());
        exitProcess(0);
    }
}
