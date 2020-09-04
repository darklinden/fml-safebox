package com.darklinden.fml


class Application : android.app.Application() {

    companion object {
        lateinit var app: Application
    }

    var patten: String? = null

    override fun onCreate() {
        super.onCreate()

        app = this;
    }
}