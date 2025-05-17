package com.example.petto.ui.profiles

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.petto.R

class HelpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }
    }
}
