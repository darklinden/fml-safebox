package com.darklinden.fml

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.content_set_quick_password.*


class LockCheckAct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_check)

        lock_view.setOnPatternDetectedListener {
            Application.app.patten = lock_view.patternString

            val intent = Intent(this, ItemListActivity::class.java)
            this.startActivity(intent)

            this.finish()
        }
    }
}