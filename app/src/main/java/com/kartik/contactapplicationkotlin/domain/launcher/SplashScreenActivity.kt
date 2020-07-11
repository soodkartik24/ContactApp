package com.kartik.contactapplicationkotlin.domain.launcher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.kartik.contactapplicationkotlin.R
import com.kartik.contactapplicationkotlin.domain.contactsList.ContactsListActivity

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Handler(Looper.getMainLooper()).postDelayed({
            ContactsListActivity.startActivity(this@SplashScreenActivity)
            finish()
        }, 300)
    }
}