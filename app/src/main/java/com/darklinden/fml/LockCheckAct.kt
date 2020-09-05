package com.darklinden.fml

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_lock_check.*


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