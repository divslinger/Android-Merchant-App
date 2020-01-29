package com.bitcoin.merchant.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.view.Window
import android.view.WindowManager

class SplashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        this.setContentView(R.layout.activity_splash)
        object : Thread() {
            override fun run() {
                Looper.prepare()
                try {
                    sleep(300)
                } catch (ie: InterruptedException) {
                }
                val mainActivityIntent = Intent(this@SplashActivity, MainActivity::class.java)
                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(mainActivityIntent)
                Looper.loop()
            }
        }.start()
    }
}