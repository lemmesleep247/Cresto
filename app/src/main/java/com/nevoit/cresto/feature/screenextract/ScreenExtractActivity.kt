package com.nevoit.cresto.feature.screenextract

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

@Suppress("DEPRECATION")
class ScreenExtractActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, ScreenExtractService::class.java))
        finish()
        overridePendingTransition(0, 0)
    }
}
