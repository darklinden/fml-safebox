package com.darklinden.fml

import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.view.WindowManager
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.darklinden.fml.data.DataSynchronize
import com.darklinden.fml.data.FileListAdapter
import com.darklinden.fml.data.MessageDigestUtils
import com.darklinden.fml.data.SecFileItem
import kotlinx.android.synthetic.main.activity_item_list.*
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.floor
import kotlin.system.exitProcess


class ItemListActivity : AppCompatActivity() {

    private var selected_uuid: String? = null

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

        item_list.adapter = FileListAdapter(this)
        pull_refresh_layout.setOnRefreshListener {
            refreshData()
        }

        pull_refresh_layout.isRefreshing = true

        Timer(false).schedule(500) {
            refreshData()
        }

        btn_add.setOnClickListener {
            if (view_detail.visibility == View.GONE) {
                selected_uuid = null
                et_title.text.clear()
                et_info.text.clear()
                et_random.text.clear()
                tip_random.visibility = View.GONE
                view_detail.visibility = View.VISIBLE
            }
        }

        btn_random.setOnClickListener {

            val a = "abcdefghjkmnpqrstwxyz"
            val n = "23456789"
            val s = "`~!@#$%^&*-=_+"
            val arrs = arrayListOf<String>(a, n, s)

            val len = 8
            var x = ""
            while (x.length < len) {
                val ai = floor(Math.random() * arrs.size).toInt()
                val st = arrs[ai]
                val si = floor(Math.random() * st.length).toInt()
                x += st[si]
            }

            et_random.text.clear()
            et_random.text.append(x)
            tip_random.visibility = View.VISIBLE
        }

        btn_use_random.setOnClickListener {
            et_info.text.clear()
            et_info.text.append(et_random.text)
            tip_random.visibility = View.GONE
        }

        btn_save.setOnClickListener {
            val title = et_title.text.toString()
            val info = et_info.text.toString()

            if (title.isEmpty() || info.isEmpty()) {
                Toast.makeText(this, R.string.toast_title_info_required, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            var item: SecFileItem? = null
            if (selected_uuid != null && !selected_uuid!!.isEmpty()) {
                item = SecFileItem.fromFile(selected_uuid)
            }

            if (item == null) {
                item = SecFileItem()
            }

            item.title = title
            item.info = info
            if (item.uuid == null) {
                item.uuid = MessageDigestUtils.md5(item.title!!)
            }

            item.saveData()
            selected_uuid = null
            view_detail.visibility = View.GONE
            refreshData()
        }

        btn_delete.setOnClickListener {
            if (selected_uuid != null) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.alert_title)
                builder.setMessage(R.string.alert_confirm_delete)
                builder.setCancelable(true)
                builder.setPositiveButton(
                    R.string.alert_btn_ok
                ) { dialog, which ->
                    SecFileItem.fromFile(selected_uuid)?.deleteData()

                    showLoading(true)
                    DataSynchronize.rm(
                        selected_uuid!!, (object : DataSynchronize.Callback {
                            override fun onFinished() {
                                this@ItemListActivity.runOnUiThread {
                                    showLoading(false)
                                }
                            }
                        })
                    )

                    selected_uuid = null
                    view_detail.visibility = View.GONE
                    refreshData()
                    dialog.dismiss()
                }
                builder.setNegativeButton(
                    R.string.alert_btn_cancel
                ) { dialog, which ->
                    dialog.dismiss()
                }

                builder.create().show()
            }
        }

        btn_settings.setOnClickListener {

            showLoading(true)

            DataSynchronize.sync(object : DataSynchronize.Callback {
                override fun onFinished() {
                    this@ItemListActivity.runOnUiThread {
                        showLoading(false)
                    }
                }
            })
        }
    }


    private var dialog: AlertDialog? = null
    fun showLoading(show: Boolean) {
        if (!show) {
            dialog?.dismiss()
            dialog = null
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setView(R.layout.progress_dialog)
            dialog = builder.create()
            dialog?.setCancelable(false)
            dialog?.show()
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

    override fun onBackPressed() {
        if (view_detail.visibility != View.GONE) {
            view_detail.visibility = View.GONE
        } else {
            super.onBackPressed()
        }
    }

    fun showItemDetail(item: SecFileItem) {
        if (view_detail.visibility == View.GONE) {
            val f = SecFileItem.fromFile(item.uuid)
            if (f != null) {
                selected_uuid = item.uuid

                et_title.text.clear()
                et_title.text.append(f.title)

                et_info.text.clear()
                et_info.text.append(f.info)

                et_random.text.clear()
                tip_random.visibility = View.GONE
                view_detail.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, R.string.toast_open_detail_failed, Toast.LENGTH_LONG).show()
            }
        }
    }
}
